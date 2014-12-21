package de.machmireinebook.epubeditor.epublib.epub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Guide;
import de.machmireinebook.epubeditor.epublib.domain.GuideReference;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Spine;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


/**
 * Writes the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *
 * @author paul
 */
public class PackageDocumentWriter extends PackageDocumentBase
{

    private static final Logger logger = Logger.getLogger(PackageDocumentWriter.class);

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


    public static Resource createOPFResource(Book book) throws IllegalArgumentException, IllegalStateException
    {
        Document opfDocument = write(book);

        XMLOutputter outputter = new XMLOutputter();
        Format xmlFormat = Format.getPrettyFormat();
        outputter.setFormat(xmlFormat);
        String text = outputter.outputString(opfDocument);

        Resource resource = null;
        try
        {
            resource = new Resource("opf", text.getBytes(Constants.CHARACTER_ENCODING), "OEBPS/content.opf", MediaType.OPF);
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
        root.setAttribute(OPFAttributes.version, String.valueOf(book.getVersion()));
        root.setAttribute(OPFAttributes.uniqueIdentifier, BOOK_ID_ID);
        opfDocument.setRootElement(root);

        PackageDocumentMetadataWriter.writeMetaData(book, root);

        writeManifest(book, root);
        writeSpine(book, root);
        writeGuide(book, root);
        return opfDocument;
    }


    /**
     * Writes the package's spine.
     *
     * @param book
     * @param root
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    private static void writeSpine(Book book, Element root)
    {
        Element spineElement = new Element(OPFTags.spine, NAMESPACE_OPF);
        spineElement.setAttribute(OPFAttributes.toc, book.getSpine().getTocResource().getId());
        root.addContent(spineElement);

        if (book.getCoverPage() != null // there is a cover page
                && book.getSpine().findFirstResourceById(book.getCoverPage().getId()) < 0)
        { // cover page is not already in the spine
            // write the cover html file
            Element itemRefElement = new Element(OPFTags.itemref, NAMESPACE_OPF);
            itemRefElement.setAttribute(OPFAttributes.idref, book.getCoverPage().getId());
            itemRefElement.setAttribute(OPFAttributes.linear, "no");
            spineElement.addContent(itemRefElement);
        }
        writeSpineItems(book.getSpine(), spineElement);
    }


    private static void writeManifest(Book book, Element root)
    {
        Element manifestElement = new Element(OPFTags.manifest, NAMESPACE_OPF);
        root.addContent(manifestElement);

        Element ncxItemElement = new Element(OPFTags.item, NAMESPACE_OPF.getURI());
        ncxItemElement.setAttribute(OPFAttributes.id, EpubWriter.getNcxId());
        ncxItemElement.setAttribute(OPFAttributes.href, EpubWriter.getNcxHref());
        ncxItemElement.setAttribute(OPFAttributes.media_type, EpubWriter.getNcxMediaType());
        manifestElement.addContent(ncxItemElement);

		for(Resource resource: getAllResourcesSortById(book))
        {
			writeItem(book, resource, manifestElement);
		}
    }

    private static List<Resource> getAllResourcesSortById(Book book)
    {
        List<Resource> allResources = new ArrayList<>(book.getResources().getAll());
        Collections.sort(allResources, new Comparator<Resource>()
        {
            @Override
            public int compare(Resource resource1, Resource resource2)
            {
                return resource1.getId().compareToIgnoreCase(resource2.getId());
            }
        });
        return allResources;
    }

    /**
     * Writes a resources as an item element
     *
     * @param resource
     * @param serializer
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    private static void writeItem(Book book, Resource resource, Element manifestElement)
    {
        if (resource == null ||
                (resource.getMediaType() == MediaType.NCX
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

        Element ncxItemElement = new Element(OPFTags.item, NAMESPACE_OPF.getURI());
        ncxItemElement.setAttribute(OPFAttributes.id, resource.getId());
        ncxItemElement.setAttribute(OPFAttributes.href, resource.getHref());
        ncxItemElement.setAttribute(OPFAttributes.media_type, resource.getMediaType().getName());
        manifestElement.addContent(ncxItemElement);
    }

    /**
     * List all spine references
     *
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    private static void writeSpineItems(Spine spine, Element spineElement)
    {

		for(SpineReference spineReference: spine.getSpineReferences())
        {
            Element itemRefElement = new Element(OPFTags.itemref, NAMESPACE_OPF.getURI());
            itemRefElement.setAttribute(OPFAttributes.idref, spineReference.getResourceId());
            spineElement.addContent(itemRefElement);
			if (! spineReference.isLinear())
            {
                itemRefElement.setAttribute(OPFAttributes.linear, OPFValues.no);
			}
		}
    }

    private static void writeGuide(Book book, Element root)
    {
        Element guideElement = new Element(OPFTags.guide, NAMESPACE_OPF);
        root.addContent(guideElement);
        ensureCoverPageGuideReferenceWritten(book.getGuide(), guideElement);
        for (GuideReference reference : book.getGuide().getReferences())
        {
            writeGuideReference(reference, guideElement);
        }
    }

    private static void ensureCoverPageGuideReferenceWritten(Guide guide, Element guideElement)
    {
        if (!(guide.getGuideReferencesByType(GuideReference.Semantics.COVER).isEmpty()))
        {
            return;
        }
        Resource coverPage = guide.getCoverPage();
        if (coverPage != null)
        {
            writeGuideReference(new GuideReference(guide.getCoverPage(), GuideReference.Semantics.COVER, GuideReference.Semantics.COVER.getName()), guideElement);
        }
    }


    private static void writeGuideReference(GuideReference reference, Element guideElement)
    {
        if (reference == null)
        {
            return;
        }
        Element referenceElement = new Element(OPFTags.reference, NAMESPACE_OPF.getURI());
        referenceElement.setAttribute(OPFAttributes.type, reference.getType().getName());
        referenceElement.setAttribute(OPFAttributes.href, reference.getCompleteHref());

        if (StringUtils.isNotBlank(reference.getTitle()))
        {
            referenceElement.setAttribute(OPFAttributes.title, reference.getTitle());
        }
        guideElement.addContent(referenceElement);
    }
}