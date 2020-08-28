package de.machmireinebook.epubeditor.epublib.epub3;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreAttributes;
import de.machmireinebook.epubeditor.epublib.domain.epub3.ManifestItemPropertiesValue;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.OPFAttribute;
import de.machmireinebook.epubeditor.epublib.domain.OPFTag;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.Resources;
import de.machmireinebook.epubeditor.epublib.toc.TableOfContents;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.domain.epub3.EpubType;
import de.machmireinebook.epubeditor.epublib.domain.epub3.LandmarkReference;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Landmarks;

import static de.machmireinebook.epubeditor.epublib.Constants.*;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 17:11
 */
public class Epub3NavigationDocumentReader
{
    private static final Logger logger = Logger.getLogger(Epub3NavigationDocumentReader.class);

    private Book book;
    private XHTMLResource navResource;
    private Resources resources;

    public Epub3NavigationDocumentReader(Book book, Resources resources) {
        this.book = book;
        this.resources = resources;
    }

    public Optional<XHTMLResource> read(Element packageRootElement)
    {
        Element manifestElement = packageRootElement.getChild(OPFTag.manifest.name(), NAMESPACE_OPF);
        List<Element> manifestElements = manifestElement.getChildren("item", NAMESPACE_OPF);
        XHTMLResource resource = null;

        for (Element element : manifestElements) {
            String propertiesValues = element.getAttributeValue("properties");
            if (StringUtils.isNotEmpty(propertiesValues)) {
                List<ManifestItemPropertiesValue> values = ManifestItemPropertiesValue.extractFromAttributeValue(propertiesValues);
                if (values.contains(ManifestItemPropertiesValue.nav))
                {
                    String href = element.getAttributeValue("href");
                    String id = element.getAttributeValue("id");
                    String mediaTypeName = element.getAttributeValue(OPFAttribute.media_type.getName());

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
                    resource.getProperties().clear();
                    resource.getProperties().addAll(values);
                    MediaType mediaType = MediaType.getByName(mediaTypeName);
                    if (mediaType != null)
                    {
                        resource.setMediaType(mediaType);
                    }
                }
            }
        }
        return Optional.ofNullable(resource);
    }

    public void readNavElements(XHTMLResource navResource)
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
                if (EpubType.toc.getSpecificationName().equals(type))
                {
                    readToc(navElement);
                }
                else if (EpubType.landmarks.getSpecificationName().equals(type))
                {
                    readLandmarks(navElement);
                }
            }
        }
    }

    private void readLandmarks(Element navElement)
    {
        Landmarks landmarks = book.getLandmarks();
        String title = navElement.getChildText("h1");
        landmarks.setTitle(title);
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
                    LandmarkReference.Semantic semantic = LandmarkReference.Semantic.getByName(type);
                    if (LandmarkReference.Semantic.COVER.equals(semantic))
                    {
                        if (resource.getMediaType() == MediaType.XHTML)
                        {
                            book.setCoverPage(resource);
                        }
                    }
                    LandmarkReference reference = new LandmarkReference(resource, semantic, anchorElement.getTextNormalize());
                    landmarks.addReference(reference);
                }
            }
        }
    }

    private void readToc(Element navElement)
    {
        TableOfContents tableOfContents = new TableOfContents();
        Element h1Element = navElement.getChild("h1", NAMESPACE_XHTML);
        tableOfContents.setId(navElement.getAttributeValue(DublinCoreAttributes.id.name()));
        if (h1Element != null)
        {
            tableOfContents.setTocTitle(h1Element.getText());
        }
        Element olElement = navElement.getChild("ol", NAMESPACE_XHTML);
        if (olElement != null)
        {
            tableOfContents.setTocReferences(readNavReferences(olElement.getChildren("li", NAMESPACE_XHTML)));
        }
        book.setTableOfContents(tableOfContents);
    }

    private List<TocEntry> readNavReferences(List<Element> liElements)
    {
        if (liElements == null)
        {
            return new ArrayList<>();
        }
        List<TocEntry> result = new ArrayList<>(liElements.size());
        for (Element liElement : liElements)
        {
            Optional<TocEntry> tocReferenceOptional = readTOCReference(liElement);
            tocReferenceOptional.ifPresent(result::add);
        }
        return result;
    }

    private Optional<TocEntry> readTOCReference(Element liElement)
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
        TocEntry result = new TocEntry(label, resource, fragmentId);
        result.setReference(resource.getHref());
        Element olElement = liElement.getChild("ol", NAMESPACE_XHTML);
        if (olElement != null)
        {
            result.setChildren(readNavReferences(olElement.getChildren("li", NAMESPACE_XHTML)));
        }
        return Optional.of(result);
    }
}
