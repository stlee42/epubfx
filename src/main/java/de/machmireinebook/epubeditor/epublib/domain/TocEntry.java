package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An item in the Table of Contents.
 */
public class TocEntry extends TitledResourceReference implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5787958246077042456L;
	private List<TocEntry> children;
    private String reference;
	private String level;
	private static final Comparator<TocEntry> COMPARATOR_BY_TITLE_IGNORE_CASE = (tocReference1, tocReference2) -> String.CASE_INSENSITIVE_ORDER.compare(tocReference1.getTitle(), tocReference2.getTitle());
	
	public TocEntry() {
		this(null, null, null);
	}
	
	public TocEntry(String name, Resource resource) {
		this(name, resource, null);
	}
	
	public TocEntry(String name, Resource resource, String fragmentId) {
		this(name, resource, fragmentId, new ArrayList<>());
	}
	
	public TocEntry(String title, Resource resource, String fragmentId, List<TocEntry> children) {
		super(resource, title, fragmentId);
		this.children = children;
	}

	public static Comparator<TocEntry> getComparatorByTitleIgnoreCase() {
		return COMPARATOR_BY_TITLE_IGNORE_CASE;
	}
	
	public List<TocEntry> getChildren() {
		return children;
	}

	public TocEntry addChildSection(TocEntry childSection) {
		this.children.add(childSection);
		return childSection;
	}
	
	public void setChildren(List<TocEntry> children) {
		this.children = children;
	}

    public boolean hasChildren()
    {
        return !children.isEmpty();
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
}
