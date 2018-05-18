package de.machmireinebook.epubeditor.epublib.epub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Guide;
import de.machmireinebook.epubeditor.epublib.domain.GuideReference;
import de.machmireinebook.epubeditor.epublib.domain.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Resources;
import de.machmireinebook.epubeditor.epublib.domain.Spine;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.domain.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.epub3.Epub3NavigationDocumentReader;
import de.machmireinebook.epubeditor.epublib.epub3.PackageDocumentEpub3MetadataReader;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import static de.machmireinebook.epubeditor.epublib.Constants.CHARACTER_ENCODING;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_OPF;

/**
 * Reads the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *
 * @author paul
 */
public class PackageDocumentReader extends PackageDocumentBase
{

    private static final Logger log = Logger.getLogger(PackageDocumentReader.class);
    private static final String[] POSSIBLE_NCX_ITEM_IDS = new String[]{"toc", "ncx"};


    public static void read(Resource packageResource, Book book, Resources resources)
            throws IOException, JDOMException
    {
        Document packageDocument = ResourceUtil.getAsDocument(packageResource);
        Element root = packageDocument.getRootElement();
        EpubVersion version = EpubVersion.getByString(root.getAttributeValue("version"));
        book.setVersion(version);

        String packageHref = packageResource.getHref();
        resources = fixHrefs(packageHref, resources);
        if (book.isEpub3()) //bei epub 3 ist der guide nicht mehr vorhanden, etwas ähnliches findet sich mit den landmarks im navigation document
        {
            XHTMLResource navResource = Epub3NavigationDocumentReader.read(root, resources);
            book.setEpub3NavResource(navResource);
            Epub3NavigationDocumentReader.readLandmarks(navResource, book, resources);
        }
        else
        {
            readGuide(root, book, resources);
        }

        // Books sometimes use non-identifier ids. We map these here to legal ones
        Map<String, String> idMapping = new HashMap<>();

        Resources bookResources = readManifest(root, book, resources, idMapping);
        book.setResources(bookResources);
        if (!book.isEpub3())
        {
            readCover(root, book);
            book.setMetadata(PackageDocumentMetadataReader.readMetadata(root));
        }
        else
        {
            readCoverImage(root, book);
            book.setMetadata(PackageDocumentEpub3MetadataReader.readMetadata(root));
        }
        book.setSpine(readSpine(root, book, idMapping));
    }

    public static void read(Resource packageResource, Book book)
            throws IOException, JDOMException
    {
        Document packageDocument = ResourceUtil.getAsDocument(packageResource);
        Element root = packageDocument.getRootElement();
        EpubVersion version = EpubVersion.getByString(root.getAttributeValue("version"));
        book.setVersion(version);

        if (book.isEpub3()) //bei epub 3 ist der guide nicht mehr vorhanden, etwas ähnliches findet sich mit den landmarks im navigation document
        {
            XHTMLResource resource = Epub3NavigationDocumentReader.read(root, book.getResources());
            book.setEpub3NavResource(resource);
            Epub3NavigationDocumentReader.readLandmarks(resource, book, book.getResources());
        }
        else
        {
            readGuide(root, book, book.getResources());
        }

        // Books sometimes use non-identifier ids. We map these here to legal ones
        Map<String, String> idMapping = new HashMap<>();

        Resources resources = readManifest(root, book, book.getResources(), idMapping);
        book.setResources(resources);
        if (!book.isEpub3())
        {
            readCover(root, book);
            book.setMetadata(PackageDocumentMetadataReader.readMetadata(root));
        }
        else
        {
            readCoverImage(root, book);
            book.setMetadata(PackageDocumentEpub3MetadataReader.readMetadata(root));
        }
        book.setSpine(readSpine(root, book, idMapping));
    }

//	private static Resource readCoverImage(Element metadataElement, Resources resources) {
//		String coverResourceId = DOMUtil.getFindAttributeValue(metadataElement.getOwnerDocument(), NAMESPACE_OPF, OPFTags.meta, OPFAttributes.name, OPFValues.meta_cover, OPFAttributes.content);
//		if (StringUtil.isBlank(coverResourceId)) {
//			return null;
//		}
//		Resource coverResource = resources.getByIdOrHref(coverResourceId);
//		return coverResource;
//	}


    /**
     * Reads the manifest containing the resource ids, hrefs and mediatypes.
     *
     * @param packageDocument
     * @param packageHref
     * @param epubReader
     * @param book
     * @param resourcesByHref
     * @return a Map with resources, with their id's as key.
     */
    private static Resources readManifest(Element root, Book book, Resources resources, Map<String, String> idMapping)
    {
        Element manifestElement = root.getChild(OPFTags.manifest, NAMESPACE_OPF);
        Resources result = new Resources();
        if (manifestElement == null)
        {
            log.error("Package document does not contain element " + OPFTags.manifest);
            return result;
        }
        List<Element> itemElements = manifestElement.getChildren(OPFTags.item, NAMESPACE_OPF);
        for (Element itemElement : itemElements)
        {
            String id = itemElement.getAttributeValue(OPFAttributes.id);
            String href = itemElement.getAttributeValue(OPFAttributes.href);
            try
            {
                href = URLDecoder.decode(href, CHARACTER_ENCODING);
            }
            catch (UnsupportedEncodingException e)
            {
                log.error(e.getMessage());
            }
            String mediaTypeName = itemElement.getAttributeValue(OPFAttributes.media_type);
            Resource resource = resources.remove(href);
            if (resource == null)
            {
                log.error("resource with href '" + href + "' not found");
                continue;
            }
            resource.setId(id);
            MediaType mediaType = MediaType.getByName(mediaTypeName);
            if (mediaType != null)
            {
                resource.setMediaType(mediaType);
            }
            if (book.isEpub3())
            {

            }
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
     * Reads the book's guide.
     * Here some more attempts are made at finding the cover page.
     *
     * @param packageDocument
     * @param epubReader
     * @param book
     * @param resources
     */
    private static void readGuide(Element root, Book book, Resources resources)
    {
        Element guideElement = root.getChild(OPFTags.guide, NAMESPACE_OPF);
        if (guideElement == null)
        {
            return;
        }
        Guide guide = book.getGuide();
        List<Element> guideReferences = guideElement.getChildren(OPFTags.reference, NAMESPACE_OPF);
        for (Element guideReference : guideReferences)
        {
            String resourceHref = guideReference.getAttributeValue(OPFAttributes.href);
            if (StringUtils.isBlank(resourceHref))
            {
                continue;
            }
            Resource resource = resources.getByHref(StringUtils.substringBefore(resourceHref, Constants.FRAGMENT_SEPARATOR_CHAR));
            if (resource == null)
            {
                log.error("Guide is referencing resource with href " + resourceHref + " which could not be found");
                continue;
            }
            String type = guideReference.getAttributeValue(OPFAttributes.type);
            if (StringUtils.isBlank(type))
            {
                log.error("Guide is referencing resource with href " + resourceHref + " which is missing the 'type' attribute");
                continue;
            }
            String title = guideReference.getAttributeValue(OPFAttributes.title);
            GuideReference reference = new GuideReference(resource, type, title, StringUtils.substringAfter(resourceHref, Constants.FRAGMENT_SEPARATOR_CHAR));
            guide.addReference(reference);
        }
    }


    /**
     * Strips off the package prefixes up to the href of the packageHref.
     * <p>
     * Example:
     * If the packageHref is "OEBPS/content.opf" then a resource href like "OEBPS/foo/bar.html" will be turned into "foo/bar.html"
     *
     * @param packageHref
     * @param resourcesByHref
     * @return The stipped package href
     */
    private static Resources fixHrefs(String packageHref,
                                      Resources resourcesByHref)
    {
        int lastSlashPos = packageHref.lastIndexOf('/');
        if (lastSlashPos < 0)
        {
            return resourcesByHref;
        }
        String packageFolderName = packageHref.substring(0, lastSlashPos);
        //den komplizierten heckmeck machen, da Resources intern eine Map mit href als Key ist,
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
     * @param packageDocument
     * @param epubReader
     * @param book
     * @param resourcesById
     * @return the document's spine, containing all sections in reading order.
     */
    private static Spine readSpine(Element root, Book book, Map<String, String> idMapping)
    {
        Resources resources = book.getResources();
        Element spineElement =  root.getChild(OPFTags.spine, NAMESPACE_OPF);
        if (spineElement == null)
        {
            log.error("Element " + OPFTags.spine + " not found in package document, generating one automatically");
            return generateSpineFromResources(resources);
        }
        Spine result = new Spine();
        if (!book.isEpub3())
        {
            result.setTocResource(findTableOfContentsResource(spineElement, resources));
        }
        List<Element> spineItems = spineElement.getChildren(OPFTags.itemref, NAMESPACE_OPF);
        List<SpineReference> spineReferences = new ArrayList<>(spineItems.size());
        for (Element spineItem : spineItems)
        {
            String itemref = spineItem.getAttributeValue(OPFAttributes.idref);
            if (StringUtils.isBlank(itemref))
            {
                log.error("itemref with missing or empty idref"); // XXX
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
                log.error("resource with id \'" + id + "\' not found");
                continue;
            }

            SpineReference spineReference = new SpineReference(resource);
            if (OPFValues.no.equalsIgnoreCase(spineItem.getAttributeValue(OPFAttributes.linear)))
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
     * @param resources
     * @return a spine created out of all resources in the resources.
     */
    private static Spine generateSpineFromResources(Resources resources)
    {
        Spine result = new Spine();
        List<String> resourceHrefs = new ArrayList<>(resources.getAllHrefs());
        resourceHrefs.sort(String.CASE_INSENSITIVE_ORDER);
        for (String resourceHref : resourceHrefs)
        {
            Resource resource = resources.getByHref(resourceHref);
            if (resource.getMediaType() == MediaType.NCX)
            {
                result.setTocResource(resource);
            }
            else if (resource.getMediaType() == MediaType.XHTML)
            {
                result.addSpineReference(new SpineReference(resource), null);
            }
        }
        return result;
    }


    /**
     * The spine tag should contain a 'toc' attribute with as value the resource id of the table of contents resource.
     * <p>
     * Here we try several ways of finding this table of contents resource.
     * We try the given attribute value, some often-used ones and finally look through all resources for the first resource with the table of contents mimetype.
     *
     * @param spineElement
     * @param resourcesById
     * @return the Resource containing the table of contents
     */
    private static Resource findTableOfContentsResource(Element spineElement, Resources resources)
    {
        String tocResourceId = spineElement.getAttributeValue(OPFAttributes.toc);
        Resource tocResource = null;
        if (StringUtils.isNotBlank(tocResourceId))
        {
            tocResource = resources.getByIdOrHref(tocResourceId);
        }

        if (tocResource != null)
        {
            return tocResource;
        }

        for (String possibleNcxItemId : POSSIBLE_NCX_ITEM_IDS)
        {
            tocResource = resources.getByIdOrHref(possibleNcxItemId);
            if (tocResource != null)
            {
                return tocResource;
            }
            tocResource = resources.getByIdOrHref(possibleNcxItemId.toUpperCase());
            if (tocResource != null)
            {
                return tocResource;
            }
        }

        // get the first resource with the NCX mediatype
        tocResource = resources.findFirstResourceByMediaType(MediaType.NCX);

        if (tocResource == null)
        {
            log.error("Could not find table of contents resource. Tried resource with id '" + tocResourceId + "', " + Constants.DEFAULT_TOC_ID + ", " + Constants.DEFAULT_TOC_ID.toUpperCase() + " and any NCX resource.");
        }
        return tocResource;
    }

    /**
     * Find all resources that have something to do with the coverpage and the cover image.
     * Search the meta tags and the guide references
     *
     * @param packageDocument
     * @return all resources that have something to do with the coverpage and the cover image.
     */
    // package
    static Set<String> findCoverHrefs(Element root)
    {

        Set<String> result = new HashSet<>();

        // try and find a meta tag with name = 'cover' and a non-blank id
        String coverResourceId = null;

        Element metadataElement =  root.getChild(OPFTags.metadata, NAMESPACE_OPF);
        if (metadataElement != null)
        {
            List<Element> metaElements = metadataElement.getChildren(OPFTags.meta, NAMESPACE_OPF);
            for (Element metaElement : metaElements)
            {
                if (OPFValues.meta_cover.equals(metaElement.getAttributeValue(OPFAttributes.name)))
                {
                    coverResourceId = metaElement.getAttributeValue(OPFAttributes.content);
                }
            }
        }

        if (StringUtils.isNotEmpty(coverResourceId))
        {
            Element manifestElement = root.getChild(OPFTags.manifest, NAMESPACE_OPF);
            String coverHref = null;
            if (manifestElement != null)
            {
                List<Element> itemElements = manifestElement.getChildren(OPFTags.item, NAMESPACE_OPF);
                for (Element itemElement : itemElements)
                {
                    //noinspection ConstantConditions
                    if (coverResourceId.equals(itemElement.getAttributeValue(OPFAttributes.id)))
                    {
                        coverHref = itemElement.getAttributeValue(OPFAttributes.href);
                    }
                }
            }

            if (StringUtils.isNotEmpty(coverHref))
            {
                result.add(coverHref);
            }
            else
            {
                result.add(coverResourceId); // maybe there was a cover href put in the cover id attribute
            }
        }
        // try and find a reference tag with type is 'cover' and reference is not blank
        String coverHref = null;
        Element guideElement = root.getChild(OPFTags.guide);
        if (guideElement != null)
        {
            List<Element> refElements = guideElement.getChildren(OPFTags.reference, NAMESPACE_OPF);
            for (Element refElement : refElements)
            {
                //noinspection ConstantConditions
                if (OPFValues.reference_cover.equals(refElement.getAttributeValue(OPFAttributes.type)))
                {
                    coverHref = refElement.getAttributeValue(OPFAttributes.href);
                }
            }
        }

        if (StringUtils.isNotEmpty(coverHref))
        {
            result.add(coverHref);
        }
        return result;
    }

    /**
     * Finds the cover resource in the packageDocument and adds it to the book if found.
     * Keeps the cover resource in the resources map
     *
     * @param book
     */
    private static void readCover(Element root, Book book)
    {

        Collection<String> coverHrefs = findCoverHrefs(root);
        for (String coverHref : coverHrefs)
        {
            Resource resource = book.getResources().getByHref(coverHref);
            if (resource == null)
            {
                log.error("Cover resource " + coverHref + " not found");
                continue;
            }
            if (resource.getMediaType() == MediaType.XHTML)
            {
                book.setCoverPage(resource);
            }
            else if (resource.getMediaType().isBitmapImage())
            {
                book.setCoverImage((ImageResource)resource);
            }
        }
    }

    private static void readCoverImage(Element root, Book book)
    {
        Element manifestElement = root.getChild(OPFTags.manifest, NAMESPACE_OPF);
        if (manifestElement != null)
        {
            List<Element> itemElements = manifestElement.getChildren(OPFTags.item, NAMESPACE_OPF);
            for (Element itemElement : itemElements)
            {
                //noinspection ConstantConditions
                if (Epub3ManifestPropertiesValues.cover_image.equals(itemElement.getAttributeValue(OPFAttributes.properties)))
                {
                    String coverHref = itemElement.getAttributeValue(OPFAttributes.href);
                    Resource resource = book.getResources().getByHref(coverHref);
                    if (resource == null)
                    {
                        log.error("Cover image resource " + coverHref + " not found");
                        continue;
                    }
                    if (resource.getMediaType().isBitmapImage())
                    {
                        book.setCoverImage((ImageResource)resource);
                        break;
                    }
                    else
                    {
                        log.error("Cover image resource " + coverHref + " is not an image");
                    }
                }
            }
        }
    }
}
