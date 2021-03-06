package de.machmireinebook.epubeditor.epublib.resource;

import java.io.Serializable;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.filesystem.EpubFileSystem;

public class TitledResourceReference<T> extends ResourceReference<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3918155020095190080L;
	private String fragmentId;
	private String title;

	public TitledResourceReference(Resource<T> resource) {
		this(resource, null);
	}

	public TitledResourceReference(Resource<T> resource, String title) {
		this(resource, title, null);
	}
	
	public TitledResourceReference(Resource<T> resource, String title, String fragmentId) {
		super(resource);
		this.title = title;
		this.fragmentId = fragmentId;
	}
	
	public String getFragmentId() {
		return fragmentId;
	}

	public void setFragmentId(String fragmentId) {
		this.fragmentId = fragmentId;
	}

	public boolean hasFragmentId()
	{
		return StringUtils.isNotEmpty(fragmentId);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	

	/**
	 * If the fragmentId is blank it returns the resource href, otherwise it returns the resource href + '#' + the fragmentId.
	 * 
	 * @return If the fragmentId is blank it returns the resource href, otherwise it returns the resource href + '#' + the fragmentId.
	 */
	public String getCompleteHref() {
		if (StringUtils.isBlank(fragmentId)) {
			return resource.getHref();
		} else {
			return resource.getHref() + Constants.FRAGMENT_SEPARATOR_CHAR + fragmentId;
		}
	}

	public Path getCompleteHrefAsPath() {
		String href;
		if (StringUtils.isBlank(fragmentId)) {
			href = resource.getHref();
		} else {
			href = resource.getHref() + Constants.FRAGMENT_SEPARATOR_CHAR + fragmentId;
		}
/*		int index = StringUtils.lastIndexOf(href, "/");
		if (index > -1)
		{
			href = href.substring(0, index);
		}*/
		return EpubFileSystem.INSTANCE.getPath("/" + href);

	}

	public void setResource(Resource<T> resource, String fragmentId) {
		super.setResource(resource);
		this.fragmentId = fragmentId;
	}

	/**
	 * Sets the resource to the given resource and sets the fragmentId to null.
	 * 
	 */
	public void setResource(Resource<T> resource) {
		setResource(resource, null);
	}

    public String toString()
    {
        return title;
    }
}
