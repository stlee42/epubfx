package de.machmireinebook.epubeditor.epublib.epub3;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Resources;
import de.machmireinebook.epubeditor.epublib.domain.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.domain.epub3.LandmarkReference;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;

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

    public static void readLandmarks(XHTMLResource navResource, Book book, Resources resources)
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
                if (PackageDocumentBase.Epub3NavTypes.toc.equals(type))
                {
                    readToc(book, navElement, resources);
                }
                else if (PackageDocumentBase.Epub3NavTypes.landmarks.equals(type))
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


    }

}