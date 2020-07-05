package de.machmireinebook.epubeditor.epublib.toc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.jdom2.Document;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.resource.Resource;

/**
 * The table of contents of the book.
 * The TableOfContents is a tree structure at the root it is a list of TOCReferences, each if which may have as children another list of TOCReferences.
 * 
 * The table of contents is used by epub as a quick index to chapters and sections within chapters.
 * It may contain duplicate entries, may decide to point not to certain chapters, etc.
 * 
 * For EPUB2: See the spine for the complete list of sections in the order in which they should be read.
 */
public class TableOfContents implements Serializable {

	private static final long serialVersionUID = -3147391239966275152L;
	
	private List<TocEntry> tocReferences;
	private String tocTitle;
	private String id;

	public TableOfContents() {
		this(new ArrayList<>());
	}
	
	public TableOfContents(List<TocEntry> tocReferences) {
		this.tocReferences = tocReferences;
	}

	public List<TocEntry> getTocReferences() {
		return tocReferences;
	}

	public void setTocReferences(List<TocEntry> tocReferences) {
		this.tocReferences = tocReferences;
	}
	
	public TocEntry addTOCReference(TocEntry tocReference) {
		if (tocReferences == null) {
			tocReferences = new ArrayList<>();
		}
		tocReferences.add(tocReference);
		return tocReference;
	}

	/**
	 * All unique references (unique by href) in the order in which they are referenced to in the table of contents.
	 * 
	 * @return All unique references (unique by href) in the order in which they are referenced to in the table of contents.
	 */
	public List<Resource<Document>> getAllUniqueResources() {
		Set<String> uniqueHrefs = new HashSet<>();
		List<Resource<Document>> result = new ArrayList<>();
		getAllUniqueResources(uniqueHrefs, result, tocReferences);
		return result;
	}
	
	
	private static void getAllUniqueResources(Set<String> uniqueHrefs, List<Resource<Document>> result, List<TocEntry> tocReferences) {
		for (TocEntry tocReference: tocReferences) {
			Resource<Document> resource = tocReference.getResource();
			if (resource != null && ! uniqueHrefs.contains(resource.getHref())) {
				uniqueHrefs.add(resource.getHref());
				result.add(resource);
			}
			getAllUniqueResources(uniqueHrefs, result, tocReference.getChildren());
		}
	}

	/**
	 * The total number of references in this table of contents.
	 * 
	 * @return The total number of references in this table of contents.
	 */
	public int size() {
		return getTotalSize(tocReferences);
	}
	
	private static int getTotalSize(List<TocEntry> tocReferences) {
		int result = tocReferences.size();
		for (TocEntry tocReference: tocReferences) {
			result += getTotalSize(tocReference.getChildren());
		}
		return result;
	}
	
	/**
	 * The maximum depth of the reference tree
	 * @return The maximum depth of the reference tree
	 */
	public int calculateDepth() {
		return calculateDepth(tocReferences, 0);
	}

	private int calculateDepth(List<TocEntry> tocReferences, int currentDepth) {
		int maxChildDepth = 0;
		for (TocEntry tocReference: tocReferences) {
			int childDepth = calculateDepth(tocReference.getChildren(), 1);
			if (childDepth > maxChildDepth) {
				maxChildDepth = childDepth;
			}
		}
		return currentDepth + maxChildDepth;
	}

    public String getTocTitle()
    {
        return tocTitle;
    }

    public void setTocTitle(String tocTitle)
    {
        this.tocTitle = tocTitle;
    }

	public String getId() {
		if (StringUtils.isNotEmpty(id)) {
			return id;
		} else {
			return Constants.DEFAULT_TOC_ID;
		}
	}

	public void setId(String id) {
		this.id = id;
	}
}
