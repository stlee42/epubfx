package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.machmireinebook.epubeditor.epublib.resource.Resource;

/**
 * The spine sections are the sections of the book in the order in which the book should be read.
 * <p>
 * This contrasts with the Table of Contents sections which is an index into the Book's sections.
 *
 * @author paul
 */
public class Spine implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 3878483958947357246L;
    /**
     * not available in epub3
     */
    private Resource tocResource;
    private List<SpineReference> spineReferences;

    public Spine()
    {
        this(new ArrayList<>());
    }

    public Spine(List<SpineReference> spineReferences)
    {
        this.spineReferences = spineReferences;
    }

    public static List<SpineReference> createSpineReferences(Collection<Resource<?>> resources)
    {
        List<SpineReference> result = new ArrayList<>(resources.size());
        for (Resource<?> resource : resources)
        {
            result.add(new SpineReference(resource));
        }
        return result;
    }

    public List<SpineReference> getSpineReferences()
    {
        return spineReferences;
    }

    public void setSpineReferences(List<SpineReference> spineReferences)
    {
        this.spineReferences = spineReferences;
    }

    /**
     * Gets the resource at the given index.
     * Null if not found.
     *
     * @param index
     * @return the resource at the given index.
     */
    public Resource getResource(int index)
    {
        if (index < 0 || index >= spineReferences.size())
        {
            return null;
        }
        return spineReferences.get(index).getResource();
    }

    /**
     * Finds the first resource that has the given resourceId.
     * <p>
     * Null if not found.
     *
     * @param resourceId
     * @return the first resource that has the given resourceId.
     */
    public int findFirstResourceById(String resourceId)
    {
        if (StringUtils.isBlank(resourceId))
        {
            return -1;
        }

        for (int i = 0; i < spineReferences.size(); i++)
        {
            SpineReference spineReference = spineReferences.get(i);
            if (resourceId.equals(spineReference.getResourceId()))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds the given spineReference to the spine references and returns it.
     *
     * @param spineReference
     * @return the given spineReference
     */
    public SpineReference addSpineReference(SpineReference spineReference, Integer index)
    {
        if (spineReferences == null)
        {
            this.spineReferences = new ArrayList<>();
        }

        if (index == null)
        {
            spineReferences.add(spineReference);
        }
        else
        {
            spineReferences.add(index, spineReference);
        }
        return spineReference;
    }

    /**
     * Adds the given resource to the spine references and returns it.
     *
     * @return the given spineReference
     */
    public SpineReference addResource(Resource resource)
    {
        return addSpineReference(new SpineReference(resource), null);
    }

    public SpineReference addResource(Resource resource, int index)
    {
        return addSpineReference(new SpineReference(resource), index);
    }

    /**
     * The number of elements in the spine.
     *
     * @return The number of elements in the spine.
     */
    public int size()
    {
        return spineReferences.size();
    }

    /**
     * As per the epub file format the spine officially maintains a reference to the Table of Contents.
     * The epubwriter will look for it here first, followed by some clever tricks to find it elsewhere if not found.
     * Put it here to be sure of the expected behaviours.
     *
     * @param tocResource
     */
    public void setTocResource(Resource tocResource)
    {
        this.tocResource = tocResource;
    }

    /**
     * The resource containing the XML for the tableOfContents.
     * When saving an epub file this resource needs to be in this place.
     *
     * @return The resource containing the XML for the tableOfContents.
     */
    public Resource getTocResource()
    {
        return tocResource;
    }

    /**
     * The position within the spine of the given resource.
     *
     * @param currentResource
     * @return something &lt; 0 if not found.
     */
    public int getResourceIndex(Resource currentResource)
    {
        if (currentResource == null) {
            return -1;
        }
        return getResourceIndex(currentResource.getHref());
    }

    /**
     * The first position within the spine of a resource with the given href.
     *
     * @return something &lt; 0 if not found.
     */
    public int getResourceIndex(String resourceHref)
    {
        int result = -1;
        if (StringUtils.isBlank(resourceHref))
        {
            return result;
        }
        for (int i = 0; i < spineReferences.size(); i++)
        {
            if (resourceHref.equals(spineReferences.get(i).getResource().getHref()))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Whether the spine has any references
     *
     * @return Whether the spine has any references
     */
    public boolean isEmpty()
    {
        return spineReferences.isEmpty();
    }

    public void moveSpineReference(Resource resource, Resource beforeResource)
    {
        SpineReference reference = spineReferences.get(getResourceIndex(resource.getHref()));
        spineReferences.remove(reference);
        int index = getResourceIndex(beforeResource.getHref());
        spineReferences.add(index, reference);
    }
}
