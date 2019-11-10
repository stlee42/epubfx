package de.machmireinebook.epubeditor.epublib.epub2;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Guide;
import de.machmireinebook.epubeditor.epublib.domain.epub2.GuideReference;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.OPFAttribute;
import de.machmireinebook.epubeditor.epublib.domain.OPFTag;
import de.machmireinebook.epubeditor.epublib.domain.OPFValue;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.Resources;
import de.machmireinebook.epubeditor.epublib.domain.Spine;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

import static de.machmireinebook.epubeditor.epublib.Constants.CHARACTER_ENCODING;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_OPF;

/**
 * Reads the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *
 * @author paul
 */
public class PackageDocumentReader
{

    private static final Logger log = Logger.getLogger(PackageDocumentReader.class);
    private static final String[] POSSIBLE_NCX_ITEM_IDS = new String[]{"toc", "ncx"};


    public static void read(Resource packageResource, Document packageDocument, Book book, Resources resources)
    {
        Element root = packageDocument.getRootElement();

        String packageHref = packageResource.getHref();
        resources = fixHrefs(packageHref, resources);
        readGuide(root, book, resources);

        // Books sometimes use non-identifier ids. We map these here to legal ones
        Map<String, String> idMapping = new HashMap<>();

        Resources bookResources = readManifest(root, resources, idMapping);
        book.setResources(bookResources);
        readCover(root, book);
        book.setMetadata(new PackageDocumentMetadataReader().readMetadata(root));
        book.setSpine(readSpine(root, book, idMapping));
    }

    public static void read(Resource packageResource, Book book)
            throws IOException, JDOMException
    {
        Document packageDocument = ResourceUtil.getAsDocument(packageResource);
        Element root = packageDocument.getRootElement();
        EpubVersion version = EpubVersion.getByString(root.getAttributeValue("version"));
        book.setVersion(version);

        readGuide(root, book, book.getResources());

        // Books sometimes use non-identifier ids. We map these here to legal ones
        Map<String, String> idMapping = new HashMap<>();

        Resources resources = readManifest(root, book.getResources(), idMapping);
        book.setResources(resources);
        readCover(root, book);
        book.setMetadata(new PackageDocumentMetadataReader().readMetadata(root));
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
            log.error("Package document does not contain element " + OPFTag.manifest);
            return result;
        }
        List<Element> itemElements = manifestElement.getChildren(OPFTag.item.getName(), NAMESPACE_OPF);
        for (Element itemElement : itemElements)
        {
            String id = itemElement.getAttributeValue(OPFAttribute.id.getName());
            String href = itemElement.getAttributeValue(OPFAttribute.href.getName());
            try
            {
                href = URLDecoder.decode(href, CHARACTER_ENCODING);
            }
            catch (UnsupportedEncodingException e)
            {
                log.error(e.getMessage());
            }
            String mediaTypeName = itemElement.getAttributeValue(OPFAttribute.media_type.getName());
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
            String properties = itemElement.getAttributeValue(OPFAttribute.properties.getName());
            resource.setProperties(properties);

            result.put(resource);
            idMapping.put(id, resource.getId());
        }
        return result;
    }


    /**
     * Reads the book's guide.
     * Here some more attempts are made at finding the cover page.
     */
    private static void readGuide(Element root, Book book, Resources resources)
    {
        Element guideElement = root.getChild(OPFTag.guide.getName(), NAMESPACE_OPF);
        if (guideElement == null)
        {
            return;
        }
        Guide guide = book.getGuide();
        List<Element> guideReferences = guideElement.getChildren(OPFTag.reference.getName(), NAMESPACE_OPF);
        for (Element guideReference : guideReferences)
        {
            String resourceHref = guideReference.getAttributeValue(OPFAttribute.href.getName());
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
            String type = guideReference.getAttributeValue(OPFAttribute.type.getName());
            if (StringUtils.isBlank(type))
            {
                log.error("Guide is referencing resource with href " + resourceHref + " which is missing the 'type' attribute");
                continue;
            }
            String title = guideReference.getAttributeValue(OPFAttribute.title.getName());
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
     * @return the document's spine, containing all sections in reading order.
     */
    private static Spine readSpine(Element root, Book book, Map<String, String> idMapping)
    {
        Resources resources = book.getResources();
        Element spineElement =  root.getChild(OPFTag.spine.getName(), NAMESPACE_OPF);
        if (spineElement == null)
        {
            log.error("Element " + OPFTag.spine + " not found in package document, generating one automatically");
            return generateSpineFromResources(resources);
        }
        Spine result = new Spine();
        result.setTocResource(findTableOfContentsResource(spineElement, resources));
        List<Element> spineItems = spineElement.getChildren(OPFTag.itemref.getName(), NAMESPACE_OPF);
        List<SpineReference> spineReferences = new ArrayList<>(spineItems.size());
        for (Element spineItem : spineItems)
        {
            String itemref = spineItem.getAttributeValue(OPFAttribute.idref.getName());
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
     * @return the Resource containing the table of contents
     */
    private static Resource findTableOfContentsResource(Element spineElement, Resources resources)
    {
        String tocResourceId = spineElement.getAttributeValue(OPFAttribute.toc.getName());
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
     * @return all resources that have something to do with the coverpage and the cover image.
     */
    // package
    private static Set<String> findCoverHrefs(Element root)
    {

        Set<String> result = new HashSet<>();

        // try and find a meta tag with name = 'cover' and a non-blank id
        String coverResourceId = null;

        Element metadataElement =  root.getChild(OPFTag.metadata.getName(), NAMESPACE_OPF);
        if (metadataElement != null)
        {
            List<Element> metaElements = metadataElement.getChildren(OPFTag.meta.getName(), NAMESPACE_OPF);
            for (Element metaElement : metaElements)
            {
                if (OPFValue.meta_cover.getName().equals(metaElement.getAttributeValue(OPFAttribute.name_attribute.getName())))
                {
                    coverResourceId = metaElement.getAttributeValue(OPFAttribute.content.getName());
                }
            }
        }

        if (StringUtils.isNotEmpty(coverResourceId))
        {
            Element manifestElement = root.getChild(OPFTag.manifest.getName(), NAMESPACE_OPF);
            String coverHref = null;
            if (manifestElement != null)
            {
                List<Element> itemElements = manifestElement.getChildren(OPFTag.item.getName(), NAMESPACE_OPF);
                for (Element itemElement : itemElements)
                {
                    if (coverResourceId.equals(itemElement.getAttributeValue(OPFAttribute.id.getName())))
                    {
                        coverHref = itemElement.getAttributeValue(OPFAttribute.href.getName());
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
        Element guideElement = root.getChild(OPFTag.guide.getName());
        if (guideElement != null)
        {
            List<Element> refElements = guideElement.getChildren(OPFTag.reference.getName(), NAMESPACE_OPF);
            for (Element refElement : refElements)
            {
                if (OPFValue.reference_cover.getName().equals(refElement.getAttributeValue(OPFAttribute.type.getName())))
                {
                    coverHref = refElement.getAttributeValue(OPFAttribute.href.getName());
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
     * Finds the cover resource in the packageDocument and add it to the book if found.
     * Keeps the cover resource in the resources map
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
}
