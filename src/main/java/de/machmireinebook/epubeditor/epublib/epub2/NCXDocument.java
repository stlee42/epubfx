package de.machmireinebook.epubeditor.epublib.epub2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.EpubIdentifier;
import de.machmireinebook.epubeditor.epublib.domain.EpubMetadata;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.toc.TableOfContents;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.XMLResource;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

/**
 * Writes and reads the ncx document as defined by namespace http://www.daisy.org/z3986/2005/ncx/
 *
 * @author paul
 */
public class NCXDocument
{
    public static final Namespace NAMESPACE_NCX = Namespace.getNamespace("http://www.daisy.org/z3986/2005/ncx/");
    public static final String NCX_ITEM_ID = "ncx";
    public static final String DEFAULT_NCX_HREF = "toc.ncx";
    public static final String PREFIX_DTB = "dtb";

    private static final Logger log = Logger.getLogger(NCXDocument.class);

    public interface NCXTags
    {
        String ncx = "ncx";
        String meta = "meta";
        String navPoint = "navPoint";
        String navMap = "navMap";
        String navLabel = "navLabel";
        String content = "content";
        String text = "text";
        String docTitle = "docTitle";
        String docAuthor = "docAuthor";
        String head = "head";
    }

    public interface NCXAttributes
    {
        String src = "src";
        String name = "name";
        String content = "content";
        String id = "id";
        String playOrder = "playOrder";
        String clazz = "class";
        String version = "version";
    }

    private interface NCXAttributeValues
    {
        String chapter = "chapter";
        String version = "2005-1";
    }

    public static Resource read(Book book)
    {
        Resource ncxResource = null;
        Resource tocResource = book.getSpine().getTocResource();
        if (tocResource == null || (book.isEpub3() && !tocResource.getMediaType().equals(MediaType.NCX)))
        {
            log.error("Book does not contain a table of contents file");
            return null;
        }
        try
        {
            ncxResource = tocResource;
            Document ncxDocument = ResourceUtil.getAsDocument(ncxResource);
            Element navMapElement = ncxDocument.getRootElement().getChild(NCXTags.navMap, NAMESPACE_NCX);
            TableOfContents tableOfContents = new TableOfContents(readTOCReferences(navMapElement.getChildren("navPoint", NAMESPACE_NCX), book));
            book.setTableOfContents(tableOfContents);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return ncxResource;
    }

    private static List<TocEntry> readTOCReferences(List<Element> navpoints, Book book)
    {
        if (navpoints == null)
        {
            return new ArrayList<>();
        }
        List<TocEntry> result = new ArrayList<>(navpoints.size());
        for (Element navpoint : navpoints)
        {
            TocEntry tocReference = readTOCReference(navpoint, book);
            result.add(tocReference);
        }
        return result;
    }

    private static TocEntry readTOCReference(Element navpointElement, Book book)
    {
        String label = readNavLabel(navpointElement);
        String tocResourceRoot = StringUtils.substringBeforeLast(book.getSpine().getTocResource().getHref(), "/");
        if (tocResourceRoot.length() == book.getSpine().getTocResource().getHref().length())
        {
            tocResourceRoot = "";
        }
        else
        {
            tocResourceRoot = tocResourceRoot + "/";
        }
        String reference = tocResourceRoot + readNavReference(navpointElement);
        String href = StringUtils.substringBefore(reference, Constants.FRAGMENT_SEPARATOR_CHAR);
        String fragmentId = StringUtils.substringAfter(reference, Constants.FRAGMENT_SEPARATOR_CHAR);
        Resource<Document> resource = book.getResources().getByHref(href);
        if (resource == null)
        {
            log.error("Resource with href " + href + " in NCX document not found");
        }
        TocEntry result = new TocEntry(label, resource, fragmentId);
        result.setReference(reference);
        result.setChildren(readTOCReferences(navpointElement.getChildren("navPoint", NAMESPACE_NCX), book));
        return result;
    }

    private static String readNavReference(Element navpointElement)
    {
        Element contentElement = navpointElement.getChild(NCXTags.content, NAMESPACE_NCX);
        String result = contentElement.getAttributeValue(NCXAttributes.src);
        try
        {
            result = URLDecoder.decode(result, Constants.CHARACTER_ENCODING);
        }
        catch (UnsupportedEncodingException e)
        {
            //never happens
        }
        return result;
    }

    private static String readNavLabel(Element navpointElement)
    {
        Element navLabel = navpointElement.getChild(NCXTags.navLabel, NAMESPACE_NCX);
        return navLabel.getChildText(NCXTags.text, NAMESPACE_NCX);
    }


    public static void write(Book book, ZipOutputStream resultStream) throws IOException
    {
        resultStream.putNextEntry(new ZipEntry(book.getSpine().getTocResource().getHref()));
        Document ncxDocument = write(book);

        XMLOutputter outputter = new XMLOutputter();
        Format xmlFormat = Format.getPrettyFormat();
        outputter.setFormat(xmlFormat);
        outputter.output(ncxDocument, resultStream);
    }


    /**
     * Generates a resource containing an xml document containing the table of contents of the book in ncx format.
     *
     * @param book          the book to serialize
     */
    public static Document write(Book book)
    {
       EpubMetadata metadata = book.getMetadata();
       return write(metadata.getIdentifiers(), book.getTitle(), book.getTableOfContents(), book.getVersion());
    }

    public static Resource<Document> createNCXResource(Book book)
    {
        EpubMetadata metadata = book.getMetadata();
        return createNCXResource(metadata.getIdentifiers(), book);
    }

    public static XMLResource createNCXResource(List<EpubIdentifier> identifiers, Book book)
    {
        Document ncxDocument = write(identifiers, book.getTitle(), book.getTableOfContents(), book.getVersion());

        XMLOutputter outputter = new XMLOutputter();
        Format xmlFormat = Format.getPrettyFormat();
        outputter.setFormat(xmlFormat);
        String text = outputter.outputString(ncxDocument);

        XMLResource resource = null;
        try
        {
            resource = (XMLResource) MediaType.NCX.getResourceFactory().createResource(NCX_ITEM_ID, text.getBytes(Constants.CHARACTER_ENCODING), DEFAULT_NCX_HREF, MediaType.NCX);
        }
        catch (UnsupportedEncodingException e)
        {
            //never happens
        }
        return resource;
    }

    public static Document write(List<EpubIdentifier> identifiers, String title, TableOfContents tableOfContents, EpubVersion epubVersion)
    {
        /*
        <?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE ncx PUBLIC "-//NISO//DTD ncx 2005-1//EN"
   "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd">
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
         */
        Document ncxDocument = new Document();
        DocType docType;
        if (epubVersion.isEpub3()) {
            docType = new DocType("ncx");
        } else {
            docType = new DocType("ncx", "-//NISO//DTD ncx 2005-1//EN", "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd");
        }
        ncxDocument.setDocType(docType);

        Element root = new Element(NCXTags.ncx, NAMESPACE_NCX);
        root.setAttribute(NCXAttributes.version, NCXAttributeValues.version);
        ncxDocument.setRootElement(root);

//		serializer.writeNamespace("ncx", NAMESPACE_NCX);
//		serializer.attribute("xmlns", NAMESPACE_NCX);
        Element headElement = new Element(NCXTags.head, NAMESPACE_NCX);
        root.addContent(headElement);

        for (EpubIdentifier identifier : identifiers)
        {
            if (identifier.isBookId())
            {
                writeMetaElement("uid", identifier.getValue(), headElement);
            }
        }

        writeMetaElement("generator", Constants.EPUBLIB_GENERATOR_NAME, headElement);
        writeMetaElement("depth", String.valueOf(tableOfContents.calculateDepth()), headElement);
        writeMetaElement("totalPageCount", "0", headElement);
        writeMetaElement("maxPageNumber", "0", headElement);

        Element docTitleElement = new Element(NCXTags.docTitle, NAMESPACE_NCX);
        root.addContent(docTitleElement);
        Element textElement = new Element(NCXTags.text, NAMESPACE_NCX);
        docTitleElement.addContent(textElement);
        // write the first title
        textElement.setText(StringUtils.defaultString(title));

        Element navMapElement = new Element(NCXTags.navMap, NAMESPACE_NCX);
        writeNavPoints(tableOfContents.getTocReferences(), 1, navMapElement);
        root.addContent(navMapElement);

        return ncxDocument;
    }


    private static void writeMetaElement(String dtbName, String content, Element headElement)
    {
        Element metaElement = new Element(NCXTags.meta, NAMESPACE_NCX);
        metaElement.setAttribute(NCXAttributes.name, PREFIX_DTB + ":" + dtbName);
        metaElement.setAttribute(NCXAttributes.content, content);
        headElement.addContent(metaElement);
    }

    private static int writeNavPoints(List<TocEntry> tocReferences, int playOrder,
                                      Element parentElement) throws IllegalArgumentException, IllegalStateException
    {
        for (TocEntry tocReference : tocReferences)
        {
            if (tocReference.getResource() == null)
            {
                playOrder = writeNavPoints(tocReference.getChildren(), playOrder, parentElement);
                continue;
            }
            Element currentElement = writeNavPointStart(tocReference, playOrder, parentElement);
            playOrder++;
            if (!tocReference.getChildren().isEmpty())
            {
                playOrder = writeNavPoints(tocReference.getChildren(), playOrder, currentElement);
            }
        }
        return playOrder;
    }

    private static Element writeNavPointStart(TocEntry tocReference, int playOrder, Element parentElement)
    {
        Element navPointElement = new Element(NCXTags.navPoint, NAMESPACE_NCX);
        navPointElement.setAttribute(NCXAttributes.id, "navPoint-" + playOrder);
        navPointElement.setAttribute(NCXAttributes.playOrder, String.valueOf(playOrder));
        navPointElement.setAttribute(NCXAttributes.clazz, NCXAttributeValues.chapter);
        parentElement.addContent(navPointElement);

        Element navLabelElement = new Element(NCXTags.navLabel, NAMESPACE_NCX);
        navPointElement.addContent(navLabelElement);
        Element textElement = new Element(NCXTags.text, NAMESPACE_NCX);
        navLabelElement.addContent(textElement);
        textElement.setText(tocReference.getTitle());

        Element contentElement = new Element(NCXTags.content, NAMESPACE_NCX);
        contentElement.setAttribute(NCXAttributes.src, tocReference.getCompleteHref());
        navPointElement.addContent(contentElement);

        return navPointElement;
    }
}
