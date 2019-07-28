package de.machmireinebook.epubeditor.epublib.epub3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.OPFAttribute;
import de.machmireinebook.epubeditor.epublib.domain.OPFTag;
import de.machmireinebook.epubeditor.epublib.domain.OPFValue;
import de.machmireinebook.epubeditor.epublib.domain.Spine;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.domain.epub3.LandmarkReference;
import de.machmireinebook.epubeditor.epublib.domain.epub3.ManifestItemAttribute;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Prefix;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.XMLResource;

import static de.machmireinebook.epubeditor.epublib.Constants.BOOK_ID_ID;
import static de.machmireinebook.epubeditor.epublib.Constants.DEFAULT_NCX_HREF;
import static de.machmireinebook.epubeditor.epublib.Constants.DEFAULT_NCX_ID;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_OPF;


/**
 * Writes the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *
 * @author paul
 */
public class Epub3PackageDocumentWriter
{

    private static final Logger logger = Logger.getLogger(Epub3PackageDocumentWriter.class);

    public static byte[] createOPFContent(Book book) throws IllegalArgumentException, IllegalStateException
    {
        Document opfDocument = write(book);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLOutputter outputter = new XMLOutputter();
        Format xmlFormat = Format.getPrettyFormat();
        outputter.setFormat(xmlFormat);
        try
        {
            outputter.output(opfDocument, baos);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return baos.toByteArray();
    }


    public static XMLResource createOPFResource(Book book) throws IllegalArgumentException, IllegalStateException
    {
        Document opfDocument = write(book);

        XMLOutputter outputter = new XMLOutputter();
        Format xmlFormat = Format.getPrettyFormat();
        outputter.setFormat(xmlFormat);
        String text = outputter.outputString(opfDocument);

        XMLResource resource = null;
        try
        {
            resource = new XMLResource("opf", text.getBytes(Constants.CHARACTER_ENCODING), "OEBPS/content.opf", MediaType.OPF);
        }
        catch (UnsupportedEncodingException e)
        {
            //should never happens
        }
        return resource;
    }



    public static Document write(Book book)
    {
        Document opfDocument = new Document();
        //<package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookId" version="2.0">
        Element root = new Element("package", NAMESPACE_OPF);
        root.setAttribute(OPFAttribute.version.getName(), String.valueOf(book.getVersion().asString()));
        root.setAttribute(OPFAttribute.uniqueIdentifier.getName(), BOOK_ID_ID);
        //write by default all common not predefined prefixes, later it could be dependend by values
        root.setAttribute("prefix", Prefix.allAsAttributeValue());
        opfDocument.setRootElement(root);

        new PackageDocumentEpub3MetadataWriter(book, root).writeMetaData();

        writeManifest(book, root);
        writeSpine(book, root);
        writeGuide(book, root);
        return opfDocument;
    }


    /**
     * Writes the package's spine.
     */
    private static void writeSpine(Book book, Element root)
    {
        Element spineElement = new Element(OPFTag.spine.getName(), NAMESPACE_OPF);
        //set toc ncx as attribute for compatibility with epub 2
        spineElement.setAttribute(OPFAttribute.toc.getName(), book.getNcxResource().getId());
        root.addContent(spineElement);

        if (book.getCoverPage() != null // there is a cover page
                && book.getSpine().findFirstResourceById(book.getCoverPage().getId()) < 0)
        {   // cover page is not already in the spine
            // write the cover html file
            Element itemRefElement = new Element(OPFTag.itemref.getName(), NAMESPACE_OPF);
            itemRefElement.setAttribute(OPFAttribute.idref.getName(), book.getCoverPage().getId());
            itemRefElement.setAttribute(OPFAttribute.linear.getName(), OPFValue.no.getName());
            spineElement.addContent(itemRefElement);
        }
        writeSpineItems(book.getSpine(), spineElement);
    }


    private static void writeManifest(Book book, Element root)
    {
        Element manifestElement = new Element(OPFTag.manifest.getName(), NAMESPACE_OPF);
        root.addContent(manifestElement);

        Element ncxItemElement = new Element(OPFTag.item.getName(), NAMESPACE_OPF.getURI());
        ncxItemElement.setAttribute(OPFAttribute.id.getName(), DEFAULT_NCX_ID);
        ncxItemElement.setAttribute(OPFAttribute.href.getName(), DEFAULT_NCX_HREF);
        ncxItemElement.setAttribute(OPFAttribute.media_type.getName(), MediaType.NCX.getName());
        manifestElement.addContent(ncxItemElement);

        List<Resource> allResources = getAllResourcesSortById(book);
        for(Resource resource: allResources)
        {
			writeItem(book, resource, manifestElement);
		}
    }

    private static List<Resource> getAllResourcesSortById(Book book)
    {
        List<Resource> allResources = new ArrayList<>(book.getResources().getAll());
        allResources.sort((resource1, resource2) -> resource1.getId().compareToIgnoreCase(resource2.getId()));
        return allResources;
    }

    /**
     * Writes a resources as an item element
     */
    private static void writeItem(Book book, Resource resource, Element manifestElement)
    {
        if (resource == null || (resource.getMediaType() == MediaType.NCX
                        && book.getSpine().getTocResource() != null))
        {
            return;
        }
        if (StringUtils.isBlank(resource.getId()))
        {
            logger.error("resource id must not be empty (href: " + resource.getHref() + ", mediatype:" + resource.getMediaType() + ")");
            return;
        }
        if (StringUtils.isBlank(resource.getHref()))
        {
            logger.error("resource href must not be empty (id: " + resource.getId() + ", mediatype:" + resource.getMediaType() + ")");
            return;
        }
        if (resource.getMediaType() == null)
        {
            logger.error("resource mediatype must not be empty (id: " + resource.getId() + ", href:" + resource.getHref() + ")");
            return;
        }
        //opf.fallback.attr? & opf.media-overlay.attr?
        Element manifestItemElement = new Element(OPFTag.item.getName(), NAMESPACE_OPF.getURI());
        manifestItemElement.setAttribute(ManifestItemAttribute.id.getName(), resource.getId());
        manifestItemElement.setAttribute(ManifestItemAttribute.href.getName(), resource.getHref());
        manifestItemElement.setAttribute(ManifestItemAttribute.media_type.getName(), resource.getMediaType().getName());
        if (StringUtils.isNotEmpty(resource.getProperties())) {
            manifestItemElement.setAttribute(ManifestItemAttribute.properties.getName(), resource.getProperties());
        }
        if (StringUtils.isNotEmpty(resource.getFallback())) {
            manifestItemElement.setAttribute(ManifestItemAttribute.fallback.getName(), resource.getFallback());
        }
        if (StringUtils.isNotEmpty(resource.getMediaOverlay())) {
            manifestItemElement.setAttribute(ManifestItemAttribute.media_overlay.getName(), resource.getMediaOverlay());
        }
        manifestElement.addContent(manifestItemElement);
    }

    /**
     * List all spine references
     */
    private static void writeSpineItems(Spine spine, Element spineElement)
    {

		for(SpineReference spineReference: spine.getSpineReferences())
        {
            Element itemRefElement = new Element(OPFTag.itemref.getName(), NAMESPACE_OPF.getURI());
            itemRefElement.setAttribute(OPFAttribute.idref.getName(), spineReference.getResourceId());
            spineElement.addContent(itemRefElement);
			if (!spineReference.isLinear())
            {
                itemRefElement.setAttribute(OPFAttribute.linear.getName(), OPFValue.no.getName());
			}
		}
    }

    private static void writeGuide(Book book, Element root)
    {
        Element guideElement = new Element(OPFTag.guide.getName(), NAMESPACE_OPF);
        root.addContent(guideElement);
        ensureCoverPageGuideReferenceWritten(book, guideElement);
        ensureNavGuideReferenceWritten(book, guideElement);
        for (LandmarkReference reference : book.getLandmarks().getReferences())
        {
            writeGuideReference(reference, guideElement);
        }
    }

    private static void ensureCoverPageGuideReferenceWritten(Book book, Element guideElement)
    {
        if (!(book.getLandmarks().getLandmarkReferencesByType(LandmarkReference.Semantic.COVER).isEmpty())){
            return;
        }
        Resource coverPage = book.getCoverPage();
        if (coverPage != null) {
            writeGuideReference(new LandmarkReference(coverPage, LandmarkReference.Semantic.COVER, LandmarkReference.Semantic.COVER.getName()), guideElement);
        }
    }

    private static void ensureNavGuideReferenceWritten(Book book, Element guideElement)
    {
        if (!(book.getLandmarks().getLandmarkReferencesByType(LandmarkReference.Semantic.TOC).isEmpty())){
            return;
        }
        Resource navPage = book.getEpub3NavResource();
        if (navPage != null) {
            writeGuideReference(new LandmarkReference(navPage, LandmarkReference.Semantic.TOC, LandmarkReference.Semantic.TOC.getName()), guideElement);
        }
    }

    private static void writeGuideReference(LandmarkReference reference, Element guideElement)
    {
        if (reference == null)
        {
            return;
        }
        Element referenceElement = new Element(OPFTag.reference.getName(), NAMESPACE_OPF.getURI());
        referenceElement.setAttribute(OPFAttribute.type.getName(), reference.getType().getName());
        referenceElement.setAttribute(OPFAttribute.href.getName(), reference.getCompleteHref());

        if (StringUtils.isNotBlank(reference.getTitle()))
        {
            referenceElement.setAttribute(OPFAttribute.title.getName(), reference.getTitle());
        }
        guideElement.addContent(referenceElement);
    }
}
