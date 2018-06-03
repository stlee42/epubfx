package de.machmireinebook.epubeditor.epublib.epub3;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Resources;
import de.machmireinebook.epubeditor.epublib.domain.TableOfContents;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.domain.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.domain.epub3.EpubType;
import de.machmireinebook.epubeditor.epublib.domain.epub3.LandmarkReference;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;

import static de.machmireinebook.epubeditor.epublib.Constants.*;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 17:11
 */
public class Epub3NavigationDocumentReader extends PackageDocumentBase
{
    private static final Logger logger = Logger.getLogger(Epub3NavigationDocumentReader.class);

    public static XHTMLResource read(Element packageRootElement, Resources resources)
    {
        Element manifestElement = packageRootElement.getChild(PackageDocumentBase.OPFTags.manifest, NAMESPACE_OPF);
        List<Element> manifestElements = manifestElement.getChildren("item", NAMESPACE_OPF);
        XHTMLResource resource = null;

        for (Element element : manifestElements)
        {
            if (EPUB3_NAV_DOCUMENT_TAG_VALUE.equals(element.getAttributeValue("properties")))
            {
                String href = element.getAttributeValue("href");
                String id = element.getAttributeValue("id");
                String mediaTypeName = element.getAttributeValue(OPFAttributes.media_type);

                try
                {
                    href = URLDecoder.decode(href, Constants.CHARACTER_ENCODING);
                }
                catch (UnsupportedEncodingException e)
                {
                    //never happens
                }
                resource = (XHTMLResource) resources.getByHref(href); //drin lassen damit die im Manifest dann noch gefunden werden,
                // da es ja auch als ein normales HTML-Doc verwendet werden kann
                if (resource == null)
                {
                    logger.error("resource with href '" + href + "' not found");
                    continue;
                }
                resource.setId(id);
                MediaType mediaType = MediaType.getByName(mediaTypeName);
                if (mediaType != null)
                {
                    resource.setMediaType(mediaType);
                }
            }
        }
        return resource;
    }

    public static void readNavElements(XHTMLResource navResource, Book book, Resources resources)
    {
        Document doc = navResource.asNativeFormat();
        Element root = doc.getRootElement();
        Element bodyElement = root.getChild("body", NAMESPACE_XHTML);
        if (bodyElement != null)
        {
            List<Element> navElements = bodyElement.getChildren("nav", NAMESPACE_XHTML);
            for (Element navElement : navElements)
            {
                String type = navElement.getAttributeValue("type", NAMESPACE_EPUB);
                if (EpubType.toc.getSepcificationName().equals(type))
                {
                    readToc(book, navElement, resources);
                }
                else if (EpubType.landmarks.getSepcificationName().equals(type))
                {
                    readLandmarks(book, navElement, resources);
                }
            }
        }
    }

    private static void readLandmarks(Book book, Element navElement, Resources resources)
    {
        Element olElement = navElement.getChild("ol", NAMESPACE_XHTML);
        if (olElement != null)
        {
            List<Element> liElements = olElement.getChildren("li", NAMESPACE_XHTML);
            for (Element liElement : liElements)
            {
                Element anchorElement = liElement.getChild("a", NAMESPACE_XHTML);
                if (anchorElement != null)
                {
                    String href = anchorElement.getAttributeValue("href");
                    Resource resource = resources.getByResolvedHref(book.getEpub3NavResource(), href);
                    if (resource == null)
                    {
                        logger.error("Landmark is referencing resource with href " + href + " which could not be found");
                        continue;
                    }

                    String type = anchorElement.getAttributeValue("type", NAMESPACE_EPUB);
                    LandmarkReference.Semantics semantic = LandmarkReference.Semantics.getByName(type);
                    if (LandmarkReference.Semantics.COVER.equals(semantic))
                    {
                        if (resource.getMediaType() == MediaType.XHTML)
                        {
                            book.setCoverPage(resource);
                        }
                    }
                    LandmarkReference reference = new LandmarkReference(resource, semantic, anchorElement.getTextNormalize());
                    book.getLandmarks().addReference(reference);
                }
            }
        }
    }

    private static void readToc(Book book, Element navElement, Resources resources)
    {
        TableOfContents tableOfContents = new TableOfContents();
        Element h1Element = navElement.getChild("h1", NAMESPACE_XHTML);
        if (h1Element != null)
        {
            tableOfContents.setTocTitle(h1Element.getText());
        }
        Element olElement = navElement.getChild("ol", NAMESPACE_XHTML);
        if (olElement != null)
        {
            tableOfContents.setTocReferences(readNavReferences(olElement.getChildren("li", NAMESPACE_XHTML), book, resources));
        }
        book.setTableOfContents(tableOfContents);
    }

    private static List<TocEntry<? extends TocEntry>> readNavReferences(List<Element> liElements, Book book, Resources resources)
    {
        if (liElements == null)
        {
            return new ArrayList<>();
        }
        List<TocEntry<?>> result = new ArrayList<>(liElements.size());
        for (Element liElement : liElements)
        {
            Optional<TocEntry<? extends TocEntry>> tocReferenceOptional = readTOCReference(liElement, book, resources);
            tocReferenceOptional.ifPresent(result::add);
        }
        return result;
    }

    private static Optional<TocEntry<? extends TocEntry>> readTOCReference(Element liElement, Book book, Resources resources)
    {
        Element anchorElement = liElement.getChild("a", NAMESPACE_XHTML);
        String label = "";
        String href = "";
        if (anchorElement != null)
        {
            label = anchorElement.getValue();
            href = anchorElement.getAttributeValue("href");
            try
            {
                href = URLDecoder.decode(href, Constants.CHARACTER_ENCODING);
            }
            catch (UnsupportedEncodingException e)
            {
                //never happens
            }
        }
        String fragmentId = StringUtils.substringAfter(href, Constants.FRAGMENT_SEPARATOR_CHAR);
        Resource resource = resources.getByResolvedHref(book.getEpub3NavResource(), href);
        if (resource == null)
        {
            logger.error("Resource with href " + href + " in nav document not found");
            return Optional.empty();
        }
        TocEntry<? extends TocEntry> result = new TocEntry<>(label, resource, fragmentId);
        result.setReference(resource.getHref());
        Element olElement = liElement.getChild("ol", NAMESPACE_XHTML);
        if (olElement != null)
        {
            result.setChildren(readNavReferences(olElement.getChildren("li", NAMESPACE_XHTML), book, resources));
        }
        return Optional.of(result);
    }

    private static String readNavReference(Element liElement)
    {
        Element anchorElement = liElement.getChild("a", NAMESPACE_XHTML);
        String result = anchorElement.getAttributeValue("href");
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
}
