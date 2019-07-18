package de.machmireinebook.epubeditor.epublib.resource;

import java.io.Serializable;

public class ResourceReference<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2596967243557743048L;
	protected Resource<T> resource;

	public ResourceReference(Resource resource) {
		this.resource = resource;
	}


	public Resource<T> getResource() {
		return resource;
	}

	/**
	 * Besides setting the resource it also sets the fragmentId to null.
	 * 
	 * @param resource
	 */
	public void setResource(Resource<T> resource) {
		this.resource = resource;
	}


	/**
	 * The id of the reference referred to.
	 * 
	 * null of the reference is null or has a null id itself.
	 * 
	 * @return The id of the reference referred to.
	 */
	public String getResourceId() {
		if (resource != null) {
			return resource.getId();
		}
		return null;
	}
}
