package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import org.apache.commons.lang.StringUtils;

import de.machmireinebook.epubeditor.epublib.Constants;

/**
 * All the resources that make up the book.
 * XHTML files, images and epub xml documents must be here.
 * 
 * @author paul
 *
 */
public class Resources implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2450876953383871451L;
	private static final String IMAGE_PREFIX = "image_";
	private static final String ITEM_PREFIX = "item_";
	private int lastId = 1;
	
	private ObservableMap<String, Resource> resources = FXCollections.observableMap(new HashMap<>());
	private ObservableList<Resource> cssResources = FXCollections.observableList(new ArrayList<>());
    private ObservableList<Resource> fontResources = FXCollections.observableList(new ArrayList<>());
    private ObservableList<Resource> imageResources = FXCollections.observableList(new ArrayList<>());

	/**
	 * Adds a resource to the resources.
	 * 
	 * Fixes the resources id and href if necessary.
	 * 
	 * @param resource
	 * @return the newly added resource
	 */
	public Resource add(Resource resource) {
		fixResourceHref(resource);
		fixResourceId(resource);
		this.resources.put(resource.getHref(), resource);
		return resource;
	}

	/**
	 * Checks the id of the given resource and changes to a unique identifier if it isn't one already.
	 * 
	 * @param resource
	 */
	public void fixResourceId(Resource resource) {
		String  resourceId = resource.getId();
		
		// first try and create a unique id based on the resource's href
		if (StringUtils.isBlank(resource.getId())) {
			resourceId = StringUtils.substringBeforeLast(resource.getHref(), ".");
			resourceId = StringUtils.substringAfterLast(resourceId, "/");
		}
		
		resourceId = makeValidId(resourceId, resource);
		
		// check if the id is unique. if not: create one from scratch
		if (StringUtils.isBlank(resourceId) || containsId(resourceId)) {
			resourceId = createUniqueResourceId(resource);
		}
		resource.setId(resourceId);
	}

	/**
	 * Check if the id is a valid identifier. if not: prepend with valid identifier
	 * 
	 * @param resource
	 * @return a valid id
	 */
	private String makeValidId(String resourceId, Resource resource) {
		if (StringUtils.isNotBlank(resourceId) && ! Character.isJavaIdentifierStart(resourceId.charAt(0))) {
			resourceId = getResourceItemPrefix(resource) + resourceId;
		}
		return resourceId;
	}
	
	private String getResourceItemPrefix(Resource resource) {
		String result;
		if (resource.getMediaType() != null  && resource.getMediaType().isBitmapImage()) {
			result = IMAGE_PREFIX;
		} else {
			result = ITEM_PREFIX;
		}
		return result;
	}
	
	/**
	 * Creates a new resource id that is guaranteed to be unique for this set of Resources
	 * 
	 * @param resource
	 * @return a new resource id that is guaranteed to be unique for this set of Resources
	 */
	private String createUniqueResourceId(Resource resource) {
		int counter = lastId;
		if (counter == Integer.MAX_VALUE) {
			if (resources.size() == Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Resources contains " + Integer.MAX_VALUE + " elements: no new elements can be added");
			} else {
				counter = 1;
			}
		}
		String prefix = getResourceItemPrefix(resource);
		String result = prefix + counter;
		while (containsId(result)) {
			result = prefix + (++ counter);
		}
		lastId = counter;
		return result;
	}

	/**
	 * Whether the map of resources already contains a resource with the given id.
	 * 
	 * @param id
	 * @return Whether the map of resources already contains a resource with the given id.
	 */
	public boolean containsId(String id) {
		if (StringUtils.isBlank(id)) {
			return false;
		}
		for (Resource resource: resources.values()) {
			if (id.equals(resource.getId())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the resource with the given id.
	 * 
	 * @param id
	 * @return null if not found
	 */
	public Resource getById(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		for (Resource resource: resources.values()) {
			if (id.equals(resource.getId())) {
				return resource;
			}
		}
		return null;
	}
	
	/**
	 * Remove the resource with the given href.
	 * 
	 * @param href
	 * @return the removed resource, null if not found
	 */
	public Resource remove(String href) {
		return resources.remove(href);
	}

    public Resource remove(Resource resource)
    {
        return resources.remove(resource.getHref());
    }

	private void fixResourceHref(Resource resource) {
		if(StringUtils.isNotBlank(resource.getHref())
				&& ! resources.containsKey(resource.getHref())) {
			return;
		}
		if(StringUtils.isBlank(resource.getHref())) {
			if(resource.getMediaType() == null) {
				throw new IllegalArgumentException("Resource must have either a MediaType or a href");
			}
			int i = 1;
			String href = createHref(resource.getMediaType(), i);
			while(resources.containsKey(href)) {
				href = createHref(resource.getMediaType(), (++i));
			}
			resource.setHref(href);
		}
	}
	
	private String createHref(MediaType mediaType, int counter) {
		if(mediaType.isBitmapImage()) {
			return "image_" + counter + mediaType.getDefaultExtension();
		} else {
			return "item_" + counter + mediaType.getDefaultExtension();
		}
	}
	
	
	public boolean isEmpty() {
		return resources.isEmpty();
	}
	
	/**
	 * The number of resources
	 * @return The number of resources
	 */
	public int size() {
		return resources.size();
	}
	
	/**
	 * The resources that make up this book.
	 * Resources can be xhtml pages, images, xml documents, etc.
	 * 
	 * @return The resources that make up this book.
	 */
    public ObservableMap<String, Resource> getResourcesMap()
    {
        return resources;
    }

    public Collection<Resource> getAll() {
		return resources.values();
	}
	
	
	/**
	 * Whether there exists a resource with the given href
	 * @param href
	 * @return Whether there exists a resource with the given href
	 */
	public boolean containsByHref(String href) {
        return !StringUtils.isBlank(href) &&
                resources.containsKey(StringUtils.substringBefore(href, Constants.FRAGMENT_SEPARATOR_CHAR));
    }
	
	/**
	 * Sets the collection of Resources to the given collection of resources
	 * 
	 * @param resources
	 */
	public void set(Collection<Resource> resources) {
		this.resources.clear();
		addAll(resources);
	}
	
	/**
	 * Adds all resources from the given Collection of resources to the existing collection.
	 * 
	 * @param resources
	 */
	public void addAll(Collection<Resource> resources) {
		for(Resource resource: resources) {
			fixResourceHref(resource);
			this.resources.put(resource.getHref(), resource);
		}
	}

	/**
	 * Sets the collection of Resources to the given collection of resources
	 * 
	 * @param resources A map with as keys the resources href and as values the Resources
	 */
	public void set(Map<String, Resource> resources) {
		this.resources = FXCollections.observableMap(new HashMap<>(resources));
	}
	
	
	/**
	 * First tries to find a resource with as id the given idOrHref, if that 
	 * fails it tries to find one with the idOrHref as href.
	 * 
	 * @param idOrHref
	 * @return the found Resource
	 */
	public Resource getByIdOrHref(String idOrHref) {
		Resource resource = getById(idOrHref);
		if (resource == null) {
			resource = getByHref(idOrHref);
		}
		return resource;
	}
	
	
	/**
	 * Gets the resource with the given href.
	 * If the given href contains a fragmentId then that fragment id will be ignored.
	 * 
	 * @param href
	 * @return null if not found.
	 */
	public Resource getByHref(String href) {
		if (StringUtils.isBlank(href)) {
			return null;
		}
		href = StringUtils.substringBefore(href, Constants.FRAGMENT_SEPARATOR_CHAR);
        return resources.get(href);
	}

    /**
     * Gets the resource with the given href.
     * If the given href contains a fragmentId then that fragment id will be ignored.
     *
     * @param href
     * @return null if not found.
     */
    public Resource getByResolvedHref(Resource resource, String href) {
        if (StringUtils.isBlank(href)) {
            return null;
        }

        String resourceHref = "/" + StringUtils.substringBefore(resource.getHref(), Constants.FRAGMENT_SEPARATOR_CHAR);
        int lastIndex = resourceHref.lastIndexOf("/");
        resourceHref = resourceHref.substring(0, lastIndex + 1);
        String combinedHref = "";
        if (href.startsWith("/"))
        {
            combinedHref = href;
        }
        else if (StringUtils.isEmpty(href))
        {
            combinedHref = resourceHref;
        }
        else if (href.startsWith(".."))
        {
            combinedHref =  doNormalize(resourceHref + "/" + href);
        }
        else if (href.startsWith("."))
        {
            combinedHref =  doNormalize(resourceHref + "/" + href.substring(2));
        }
        else if (!href.startsWith("/"))
        {
            combinedHref = resourceHref + href;
        }

        combinedHref = StringUtils.substringBefore(combinedHref, Constants.FRAGMENT_SEPARATOR_CHAR);
        if (combinedHref.startsWith("/"))
        {
            combinedHref = combinedHref.substring(1);
        }
        return resources.get(combinedHref);
    }
	
	/**
	 * Gets the first resource (random order) with the given mediatype.
	 * 
	 * Useful for looking up the table of contents as it's supposed to be the only resource with NCX mediatype.
	 * 
	 * @param mediaType
	 * @return the first resource (random order) with the give mediatype.
	 */
	public Resource findFirstResourceByMediaType(MediaType mediaType) {
		return findFirstResourceByMediaType(resources.values(), mediaType);
	}
	
	/**
	 * Gets the first resource (random order) with the give mediatype.
	 * 
	 * Useful for looking up the table of contents as it's supposed to be the only resource with NCX mediatype.
	 * 
	 * @param mediaType
	 * @return the first resource (random order) with the give mediatype.
	 */
	public static Resource findFirstResourceByMediaType(Collection<Resource> resources, MediaType mediaType) {
		for (Resource resource: resources) {
			if (resource.getMediaType() == mediaType) {
				return resource;
			}
		}
		return null;
	}

	/**
	 * All resources that have the given MediaType.
	 * 
	 * @param mediaType
	 * @return All resources that have the given MediaType.
	 */
	public List<Resource> getResourcesByMediaType(MediaType mediaType) {
		List<Resource> result = new ArrayList<>();
		if (mediaType == null) {
			return result;
		}
		for (Resource resource: getAll()) {
			if (resource.getMediaType() == mediaType) {
				result.add(resource);
			}
		}
		return result;
	}

	/**
	 * All Resources that match any of the given list of MediaTypes
	 * 
	 * @param mediaTypes
	 * @return All Resources that match any of the given list of MediaTypes
	 */
	public List<Resource> getResourcesByMediaTypes(MediaType[] mediaTypes) {
		List<Resource> result = new ArrayList<>();
		if (mediaTypes == null) {
			return result;
		}
		
		// this is the fastest way of doing this according to 
		// http://stackoverflow.com/questions/1128723/in-java-how-can-i-test-if-an-array-contains-a-certain-value
		List<MediaType> mediaTypesList = Arrays.asList(mediaTypes);
		for (Resource resource: getAll()) {
			if (mediaTypesList.contains(resource.getMediaType())) {
				result.add(resource);
			}
		}
		return result;
	}


	/**
	 * All resource hrefs
	 *
	 * @return all resource hrefs
	 */
	public Collection<String> getAllHrefs() {
		return resources.keySet();
	}

    private static String doNormalize(String href) {

        char separator = '/';
        if (href == null) {
            return null;
        }
        int size = href.length();
        if (size == 0) {
            return href;
        }
        int prefix = 1;

        char[] array = new char[size + 2];  // +1 for possible extra slash, +2 for arraycopy
        href.getChars(0, href.length(), array, 0);


        // fix separators throughout
        for (int i = 0; i < array.length; i++) {
            if (array[i] == separator) {
                array[i] = separator;
            }
        }

        // add extra separator on the end to simplify code below
        boolean lastIsDirectory = true;
        if (array[size - 1] != separator) {
            array[size++] = separator;
            lastIsDirectory = false;
        }

        // adjoining slashes
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == separator && array[i - 1] == separator) {
                System.arraycopy(array, i, array, i - 1, size - i);
                size--;
                i--;
            }
        }

        // dot slash
        for (int i = prefix + 1; i < size; i++) {
            if (array[i] == separator && array[i - 1] == '.' &&
                    (i == prefix + 1 || array[i - 2] == separator)) {
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                System.arraycopy(array, i + 1, array, i - 1, size - i);
                size -=2;
                i--;
            }
        }

        // double dot slash
        outer:
        for (int i = prefix + 2; i < size; i++) {
            if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' &&
                    (i == prefix + 2 || array[i - 3] == separator)) {
                if (i == prefix + 2) {
                    return null;
                }
                if (i == size - 1) {
                    lastIsDirectory = true;
                }
                int j;
                for (j = i - 4 ; j >= prefix; j--) {
                    if (array[j] == separator) {
                        // remove b/../ from a/b/../c
                        System.arraycopy(array, i + 1, array, j + 1, size - i);
                        size -= i - j;
                        i = j + 1;
                        continue outer;
                    }
                }
                // remove a/../ from a/../c
                System.arraycopy(array, i + 1, array, prefix, size - i);
                size -= i + 1 - prefix;
                i = prefix + 1;
            }
        }

        if (size <= 0) {  // should never be less than 0
            return "";
        }
        if (size <= prefix) {  // should never be less than prefix
            return new String(array, 0, size);
        }
        if (lastIsDirectory) {
            return new String(array, 0, size);  // keep trailing separator
        }
        return new String(array, 0, size - 1);  // lose trailing separator
    }

    public ObservableList<Resource> getCssResources()
    {
        return cssResources;
    }

    public void setCssResources(ObservableList<Resource> cssResources)
    {
        this.cssResources = cssResources;
    }

    public ObservableList<Resource> getFontResources()
    {
        return fontResources;
    }

    public void setFontResources(ObservableList<Resource> fontResources)
    {
        this.fontResources = fontResources;
    }

    public ObservableList<Resource> getImageResources()
    {
        return imageResources;
    }

    public void setImageResources(ObservableList<Resource> imageResources)
    {
        this.imageResources = imageResources;
    }
}
