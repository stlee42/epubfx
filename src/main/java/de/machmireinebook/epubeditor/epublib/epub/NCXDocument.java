package de.machmireinebook.epubeditor.epublib.epub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Author;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TOCReference;
import de.machmireinebook.epubeditor.epublib.domain.TableOfContents;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Writes the ncx document as defined by namespace http://www.daisy.org/z3986/2005/ncx/
 *
 * @author paul
 */
public class NCXDocument
{

    public static final Namespace NAMESPACE_NCX_WITH_PREFIX = Namespace.getNamespace("ncx", "http://www.daisy.org/z3986/2005/ncx/");
    public static final Namespace NAMESPACE_NCX = Namespace.getNamespace("http://www.daisy.org/z3986/2005/ncx/");
    public static final String NCX_ITEM_ID = "ncx";
    public static final String DEFAULT_NCX_HREF = "toc.ncx";
    public static final String PREFIX_DTB = "dtb";

    private static final Logger log = Logger.getLogger(NCXDocument.class);

    private interface NCXTags
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

    private interface NCXAttributes
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
        if (book.getSpine().getTocResource() == null)
        {
            log.error("Book does not contain a table of contents file");
            return null;
        }
        try
        {
            ncxResource = book.getSpine().getTocResource();
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

    private static List<TOCReference> readTOCReferences(List<Element> navpoints, Book book)
    {
        if (navpoints == null)
        {
            return new ArrayList<>();
        }
        List<TOCReference> result = new ArrayList<>(navpoints.size());
        for (Element navpoint : navpoints)
        {
            TOCReference tocReference = readTOCReference(navpoint, book);
            result.add(tocReference);
        }
        return result;
    }

    private static TOCReference readTOCReference(Element navpointElement, Book book)
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
        Resource resource = book.getResources().getByHref(href);
        if (resource == null)
        {
            log.error("Resource with href " + href + " in NCX document not found");
        }
        TOCReference result = new TOCReference(label, resource, fragmentId);
        result.setNcxReference(reference);
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
     * @param xmlSerializer the serializer used
     * @param book          the book to serialize
     * @throws javax.xml.stream.FactoryConfigurationError
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public static Document write(Book book) throws IllegalArgumentException, IllegalStateException, IOException
    {
        return write(book.getMetadata().getIdentifiers(), book.getTitle(), book.getMetadata().getAuthors(), book.getTableOfContents());
    }

    public static Resource createNCXResource(Book book) throws IllegalArgumentException, IllegalStateException, IOException
    {
        return createNCXResource(book.getMetadata().getIdentifiers(), book.getTitle(), book.getMetadata().getAuthors(), book.getTableOfContents());
    }

    public static Resource createNCXResource(List<Identifier> identifiers, String title, List<Author> authors, TableOfContents tableOfContents) throws IllegalArgumentException, IllegalStateException, IOException
    {
        Document ncxDocument = write(identifiers, title, authors, tableOfContents);

        XMLOutputter outputter = new XMLOutputter();
        Format xmlFormat = Format.getPrettyFormat();
        outputter.setFormat(xmlFormat);
        String text = outputter.outputString(ncxDocument);

        Resource resource = MediaType.NCX.getResourceFactory().createResource(NCX_ITEM_ID, text.getBytes(Constants.CHARACTER_ENCODING), DEFAULT_NCX_HREF, MediaType.NCX);
        return resource;
    }

    public static Document write(List<Identifier> identifiers, String title, List<Author> authors, TableOfContents tableOfContents) throws IllegalArgumentException, IllegalStateException, IOException
    {
        /*
        <?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE ncx PUBLIC "-//NISO//DTD ncx 2005-1//EN"
   "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd">
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
         */
        Document ncxDocument = new Document();
        DocType docType = new DocType("ncx", "-//NISO//DTD ncx 2005-1//EN", "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd");
        ncxDocument.setDocType(docType);

        Element root = new Element(NCXTags.ncx, NAMESPACE_NCX);
        root.setAttribute(NCXAttributes.version, NCXAttributeValues.version);
        ncxDocument.setRootElement(root);

//		serializer.writeNamespace("ncx", NAMESPACE_NCX);
//		serializer.attribute("xmlns", NAMESPACE_NCX);
        Element headElement = new Element(NCXTags.head, NAMESPACE_NCX);
        root.addContent(headElement);

        for (Identifier identifier : identifiers)
        {
            writeMetaElement(identifier.getScheme(), identifier.getValue(), headElement);
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


    private static void writeMetaElement(String dtbName, String content, Element headElement) throws IllegalArgumentException, IllegalStateException, IOException
    {
        Element metaElement = new Element(NCXTags.meta, NAMESPACE_NCX);
        metaElement.setAttribute(NCXAttributes.name, PREFIX_DTB + ":" + dtbName);
        metaElement.setAttribute(NCXAttributes.content, content);
        headElement.addContent(metaElement);
    }

    private static int writeNavPoints(List<TOCReference> tocReferences, int playOrder,
                                      Element navMapElement) throws IllegalArgumentException, IllegalStateException, IOException
    {
        for (TOCReference tocReference : tocReferences)
        {
            if (tocReference.getResource() == null)
            {
                playOrder = writeNavPoints(tocReference.getChildren(), playOrder, navMapElement);
                continue;
            }
            writeNavPointStart(tocReference, playOrder, navMapElement);
            playOrder++;
            if (!tocReference.getChildren().isEmpty())
            {
                playOrder = writeNavPoints(tocReference.getChildren(), playOrder, navMapElement);
            }
        }
        return playOrder;
    }


    private static void writeNavPointStart(TOCReference tocReference, int playOrder, Element navMapElement) throws IllegalArgumentException, IllegalStateException, IOException
    {
        Element navPointElement = new Element(NCXTags.navPoint, NAMESPACE_NCX);
        navPointElement.setAttribute(NCXAttributes.id, "navPoint-" + playOrder);
        navPointElement.setAttribute(NCXAttributes.playOrder, String.valueOf(playOrder));
        navPointElement.setAttribute(NCXAttributes.clazz, NCXAttributeValues.chapter);
        navMapElement.addContent(navPointElement);

        Element navLabelElement = new Element(NCXTags.navLabel, NAMESPACE_NCX);
        navPointElement.addContent(navLabelElement);
        Element textElement = new Element(NCXTags.text, NAMESPACE_NCX);
        navLabelElement.addContent(textElement);
        textElement.setText(tocReference.getTitle());

        Element contentElement = new Element(NCXTags.content, NAMESPACE_NCX);
        contentElement.setAttribute(NCXAttributes.src, tocReference.getCompleteHref());
        navPointElement.addContent(contentElement);
    }
}
