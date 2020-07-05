package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import org.jdom2.Document;

import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.TitledResourceReference;

/**
 * An item in the Table of Contents.
 *
 * S - type of childs
 */
public class TocEntry extends TitledResourceReference<Document> implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5787958246077042456L;
	private List<TocEntry> children;
    private String reference;
	private String level;

	public TocEntry() {
		this(null, null, null);
	}
	
	public TocEntry(String name, Resource<Document> resource) {
		this(name, resource, null);
	}
	
	public TocEntry(String name, Resource<Document> resource, String fragmentId) {
		this(name, resource, fragmentId, new ArrayList<>());
	}
	
	public TocEntry(String title, Resource<Document> resource, String fragmentId, List<TocEntry> children) {
		super(resource, title, fragmentId);
		this.children = children;
	}

	public boolean hasChildren() {
        return !children.isEmpty();
    }

	public List<TocEntry> getChildren() {
		return children;
	}

	public void setChildren(List<TocEntry> children) {
		this.children = children;
	}

	public void addChildSection(TocEntry childSection) {
		this.children.add(childSection);
	}

	public String getReference()
    {
        return reference;
    }

    public void setReference(String reference)
    {
        this.reference = reference;
    }

	public String getLevel()
	{
		return level;
	}

	public void setLevel(String level)
	{
		this.level = level;
	}

	@Override
	public TocEntry clone() {
		TocEntry newTocEntry = null;
		try {
			newTocEntry = (TocEntry) super.clone();
			newTocEntry.setChildren(ObjectUtils.clone(getChildren()));
		}
		catch (CloneNotSupportedException e) {
			//
		}
		return newTocEntry;
	}
}
