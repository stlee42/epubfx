package de.machmireinebook.epubeditor.epublib.epub3;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.NavNotFoundException;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.epub3.ManifestItemAttribute;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.ManifestItemProperties;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.OPFAttribute;
import de.machmireinebook.epubeditor.epublib.domain.OPFTag;
import de.machmireinebook.epubeditor.epublib.domain.OPFValue;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.Resources;
import de.machmireinebook.epubeditor.epublib.domain.Spine;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Metadata;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

import static de.machmireinebook.epubeditor.epublib.Constants.CHARACTER_ENCODING;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_OPF;

/**
 * Reads the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *
 * @author paul
 */
public class Epub3PackageDocumentReader
{
    private static final Logger logger = Logger.getLogger(Epub3PackageDocumentReader.class);


    public static void read(Resource packageResource, Document packageDocument, Book book, Resources resources) {
        Element root = packageDocument.getRootElement();

        String packageHref = packageResource.getHref();
        resources = fixHrefs(packageHref, resources);

        //bei epub 3 ist der guide nicht mehr vorhanden, etwas ähnliches findet sich mit den landmarks im navigation document
        XHTMLResource navResource = Epub3NavigationDocumentReader.read(root, resources);
        if (navResource == null) {
            logger.error("no nav in epub");
            throw new NavNotFoundException("epub contains no nav resource");
        }
        book.setEpub3NavResource(navResource);
        Epub3NavigationDocumentReader.readNavElements(navResource, book, resources);

        // Books sometimes use non-identifier ids. We map these here to legal ones
        Map<String, String> idMapping = new HashMap<>();

        Resources bookResources = readManifest(root, resources, idMapping);
        book.setResources(bookResources);
        readCoverImage(root, book);
        PackageDocumentEpub3MetadataReader reader = new PackageDocumentEpub3MetadataReader();
        Metadata metadata = reader.readMetadata(root);
        book.setMetadata(metadata);
        book.setSpine(readSpine(root, book, idMapping));
    }

    public static void read(Resource packageResource, Book book)
            throws IOException, JDOMException
    {
        Document packageDocument = ResourceUtil.getAsDocument(packageResource);
        Element root = packageDocument.getRootElement();
        EpubVersion version = EpubVersion.getByString(root.getAttributeValue("version"));
        book.setVersion(version);

        //bei epub 3 ist der guide nicht mehr vorhanden, etwas ähnliches findet sich mit den landmarks im navigation document
        XHTMLResource resource = Epub3NavigationDocumentReader.read(root, book.getResources());
        book.setEpub3NavResource(resource);
        Epub3NavigationDocumentReader.readNavElements(resource, book, book.getResources());

        // Books sometimes use non-identifier ids. We map these here to legal ones
        Map<String, String> idMapping = new HashMap<>();

        Resources resources = readManifest(root, book.getResources(), idMapping);
        book.setResources(resources);
        readCoverImage(root, book);
        PackageDocumentEpub3MetadataReader reader = new PackageDocumentEpub3MetadataReader();
        Metadata metadata = reader.readMetadata(root);
        book.setMetadata(metadata);
        book.setSpine(readSpine(root, book, idMapping));
    }

    /**
     * Reads the manifest containing the resource ids, hrefs and mediatypes.

     * @return a Map with resources, with their id's as key.
     */
    private static Resources readManifest(Element root, Resources resources, Map<String, String> idMapping)
    {
        Element manifestElement = root.getChild(OPFTag.manifest.getName(), NAMESPACE_OPF);
        Resources result = new Resources();
        if (manifestElement == null)
        {
            logger.error("Package document does not contain element " + OPFTag.manifest);
            return result;
        }
        List<Element> itemElements = manifestElement.getChildren(OPFTag.item.getName(), NAMESPACE_OPF);
        for (Element itemElement : itemElements)
        {
            String id = itemElement.getAttributeValue(ManifestItemAttribute.id.getName());
            String href = itemElement.getAttributeValue(ManifestItemAttribute.href.getName());
            try
            {
                href = URLDecoder.decode(href, CHARACTER_ENCODING);
            }
            catch (UnsupportedEncodingException e)
            {
                logger.error(e.getMessage());
            }
            String mediaTypeName = itemElement.getAttributeValue(ManifestItemAttribute.media_type.getName());
            Resource resource = resources.remove(href);
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
            String properties = itemElement.getAttributeValue(ManifestItemAttribute.properties.getName());
            resource.setProperties(properties);
            String fallback = itemElement.getAttributeValue(ManifestItemAttribute.fallback.getName());
            resource.setFallback(fallback);
            String mediaOverlay = itemElement.getAttributeValue(ManifestItemAttribute.media_overlay.getName());
            resource.setMediaOverlay(mediaOverlay);

            result.add(resource);
            if (resource.getMediaType() == MediaType.CSS)
            {
                result.getCssResources().add(resource);
            }
            else if (resource.getMediaType().isFont())
            {
                result.getFontResources().add(resource);
            }
            else if (resource.getMediaType().isImage())
            {
                result.getImageResources().add(resource);
            }
            idMapping.put(id, resource.getId());
        }
        return result;
    }

    /**
     * Strips off the package prefixes up to the href of the packageHref.
     * <p>
     * Example:
     * If the packageHref is "OEBPS/content.opf" then a resource href like "OEBPS/foo/bar.html" will be turned into "foo/bar.html"
     *
     * @return The stipped package href
     */
    private static Resources fixHrefs(String packageHref, Resources resourcesByHref)
    {
        int lastSlashPos = packageHref.lastIndexOf('/');
        if (lastSlashPos < 0)
        {
            return resourcesByHref;
        }
        String packageFolderName = packageHref.substring(0, lastSlashPos);
        // den komplizierten heckmeck machen, da Resources intern eine Map mit href als Key ist,
        // aber genau der wird ja hier geändert
        List<String> oldHrefsToRemove = new ArrayList<>();
        List<Resource> newResources = new ArrayList<>();
        for (Resource resource : resourcesByHref.getAll())
        {
            if (StringUtils.isNotBlank(resource.getHref())
                    && resource.getHref().length() > lastSlashPos && resource.getHref().contains(packageFolderName))
            {
                oldHrefsToRemove.add(resource.getHref());
                resource.setHref(resource.getHref().substring(lastSlashPos + 1));
                newResources.add(resource);
            }
        }
        for (String oldHref : oldHrefsToRemove)
        {
            resourcesByHref.remove(oldHref);
        }
        resourcesByHref.addAll(newResources);
        return resourcesByHref;
    }

    /**
     * Reads the document's spine, containing all sections in reading order.
     *
     * @return the document's spine, containing all sections in reading order.
     */
    private static Spine readSpine(Element root, Book book, Map<String, String> idMapping)
    {
        Resources resources = book.getResources();
        Element spineElement =  root.getChild(OPFTag.spine.getName(), NAMESPACE_OPF);
        if (spineElement == null)
        {
            logger.error("Element " + OPFTag.spine + " not found in package document, generating one automatically");
            return generateSpineFromResources(book);
        }
        Spine result = new Spine();
        result.setTocResource(book.getEpub3NavResource());
        //for compatibility it's possible that a ncx toc is included in epub, read it into
        String epub2TocId = spineElement.getAttributeValue(OPFAttribute.toc.getName());
        if (StringUtils.isNotEmpty(epub2TocId)) {
            Resource ncxResource = resources.getByIdOrHref(epub2TocId);
            book.setNcxResource(ncxResource);
        }
        List<Element> spineItems = spineElement.getChildren(OPFTag.itemref.getName(), NAMESPACE_OPF);
        List<SpineReference> spineReferences = new ArrayList<>(spineItems.size());
        for (Element spineItem : spineItems)
        {
            String itemref = spineItem.getAttributeValue(OPFAttribute.idref.getName());
            if (StringUtils.isBlank(itemref))
            {
                logger.error("itemref with missing or empty idref"); // XXX
                continue;
            }
            String id = idMapping.get(itemref);
            if (id == null)
            {
                id = itemref;
            }
            Resource resource = resources.getByIdOrHref(id);
            if (resource == null)
            {
                logger.error("resource with id \'" + id + "\' not found");
                continue;
            }

            SpineReference spineReference = new SpineReference(resource);
            if (OPFValue.no.getName().equalsIgnoreCase(spineItem.getAttributeValue(OPFAttribute.linear.getName())))
            {
                spineReference.setLinear(false);
            }
            spineReferences.add(spineReference);
        }
        result.setSpineReferences(spineReferences);
        return result;
    }

    /**
     * Creates a spine out of all resources in the resources.
     * The generated spine consists of all XHTML pages in order of their href.
     *
     * @return a spine created out of all resources in the resources.
     */
    private static Spine generateSpineFromResources(Book book)
    {
        Resources resources = book.getResources();
        Spine result = new Spine();
        List<String> resourceHrefs = new ArrayList<>(resources.getAllHrefs());
        resourceHrefs.sort(String.CASE_INSENSITIVE_ORDER);
        for (String resourceHref : resourceHrefs)
        {
            Resource resource = resources.getByHref(resourceHref);
            if (resource.getMediaType() == MediaType.NCX)
            {
                book.setNcxResource(resource);
            }
            else if (resource.getMediaType() == MediaType.XHTML)
            {
                result.addSpineReference(new SpineReference(resource), null);
            }
        }
        return result;
    }

    private static void readCoverImage(Element root, Book book)
    {
        Element manifestElement = root.getChild(OPFTag.manifest.getName(), NAMESPACE_OPF);
        if (manifestElement != null)
        {
            List<Element> itemElements = manifestElement.getChildren(OPFTag.item.getName(), NAMESPACE_OPF);
            for (Element itemElement : itemElements)
            {
                if (ManifestItemProperties.cover_image.getName().equals(itemElement.getAttributeValue(OPFAttribute.properties.getName())))
                {
                    String coverHref = itemElement.getAttributeValue(OPFAttribute.href.getName());
                    Resource resource = book.getResources().getByHref(coverHref);
                    if (resource == null)
                    {
                        logger.error("Cover image resource " + coverHref + " not found");
                        continue;
                    }
                    if (resource.getMediaType().isBitmapImage())
                    {
                        book.setCoverImage((ImageResource)resource);
                        break;
                    }
                    else
                    {
                        logger.error("Cover image resource " + coverHref + " is not an image");
                    }
                }
            }
        }
    }
}
