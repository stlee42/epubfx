package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The table of contents of the book.
 * The TableOfContents is a tree structure at the root it is a list of TOCReferences, each if which may have as children another list of TOCReferences.
 * 
 * The table of contents is used by epub as a quick index to chapters and sections within chapters.
 * It may contain duplicate entries, may decide to point not to certain chapters, etc.
 * 
 * See the spine for the complete list of sections in the order in which they should be read.
 * 
 * @see nl.siegmann.epublib.domain.Spine
 * 
 * @author paul
 *
 */
public class TableOfContents implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3147391239966275152L;
	
	public static final String DEFAULT_PATH_SEPARATOR = "/";
	
	private List<TocEntry> tocReferences;

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
	
	/**
	 * Calls addTOCReferenceAtLocation after splitting the path using the DEFAULT_PATH_SEPARATOR.
	 * @return the new TocEntry
	 */
	public TocEntry addSection(Resource resource, String path) {
		return addSection(resource, path, DEFAULT_PATH_SEPARATOR);
	}
	
	/**
	 * Calls addTOCReferenceAtLocation after splitting the path using the given pathSeparator.
	 * 
	 * @param resource
	 * @param path
	 * @param pathSeparator
	 * @return the new TocEntry
	 */
	public TocEntry addSection(Resource resource, String path, String pathSeparator) {
		String[] pathElements = path.split(pathSeparator);
		return addSection(resource, pathElements);
	}
	
	/**
	 * Finds the first TocEntry in the given list that has the same title as the given Title.
	 * 
	 * @param title
	 * @param tocReferences
	 * @return null if not found.
	 */
	private static TocEntry findTocReferenceByTitle(String title, List<TocEntry> tocReferences) {
		for (TocEntry tocReference: tocReferences) {
			if (title.equals(tocReference.getTitle())) {
				return tocReference;
			}
		}
		return null;
	}

	/**
	 * Adds the given Resources to the TableOfContents at the location specified by the pathElements.
	 * 
	 * Example:
	 * Calling this method with a Resource and new String[] {"chapter1", "paragraph1"} will result in the following:
	 * <ul>
	 * <li>a TocEntry with the title "chapter1" at the root level.<br/>
	 * If this TocEntry did not yet exist it will have been created and does not point to any resource</li>
	 * <li>A TocEntry that has the title "paragraph1". This TocEntry will be the child of TocEntry "chapter1" and
	 * will point to the given Resource</li>
	 * </ul>
	 *    
	 * @param resource
	 * @param pathElements
	 * @return the new TocEntry
	 */
	public TocEntry addSection(Resource resource, String[] pathElements) {
		if (pathElements == null || pathElements.length == 0) {
			return null;
		}
		TocEntry result = null;
		List<TocEntry> currentTocReferences = this.tocReferences;
		for (int i = 0; i < pathElements.length; i++) {
			String currentTitle = pathElements[i];
			result = findTocReferenceByTitle(currentTitle, currentTocReferences);
			if (result == null) {
				result = new TocEntry(currentTitle, null);
				currentTocReferences.add(result);
			}
			currentTocReferences = result.getChildren();
		}
		result.setResource(resource);
		return result;
	}
		
	/**
	 * Adds the given Resources to the TableOfContents at the location specified by the pathElements.
	 * 
	 * Example:
	 * Calling this method with a Resource and new int[] {0, 0} will result in the following:
	 * <ul>
	 * <li>a TocEntry at the root level.<br/>
	 * If this TocEntry did not yet exist it will have been created with a title of "" and does not point to any resource</li>
	 * <li>A TocEntry that points to the given resource and is a child of the previously created TocEntry.<br/>
	 * If this TocEntry didn't exist yet it will be created and have a title of ""</li>
	 * </ul>
	 *    
	 * @param resource
	 * @param pathElements
	 * @return the new TocEntry
	 */
	public TocEntry addSection(Resource resource, int[] pathElements, String sectionTitlePrefix, String sectionNumberSeparator) {
		if (pathElements == null || pathElements.length == 0) {
			return null;
		}
		TocEntry result = null;
		List<TocEntry> currentTocReferences = this.tocReferences;
		for (int i = 0; i < pathElements.length; i++) {
			int currentIndex = pathElements[i];
			if (currentIndex > 0 && currentIndex < (currentTocReferences.size() - 1)) {
				result = currentTocReferences.get(currentIndex);
			} else {
				result = null;
			}
			if (result == null) {
				paddTOCReferences(currentTocReferences, pathElements, i, sectionTitlePrefix, sectionNumberSeparator);
				result = currentTocReferences.get(currentIndex);
			}
			currentTocReferences = result.getChildren();
		}
		result.setResource(resource);
		return result;
	}
	
	private void paddTOCReferences(List<TocEntry> currentTocReferences,
			int[] pathElements, int pathPos, String sectionPrefix, String sectionNumberSeparator) {
		for (int i = currentTocReferences.size(); i <= pathElements[pathPos]; i++) {
			String sectionTitle = createSectionTitle(pathElements, pathPos, i, sectionPrefix,
					sectionNumberSeparator);
			currentTocReferences.add(new TocEntry(sectionTitle, null));
		}
	}

	private String createSectionTitle(int[] pathElements, int pathPos, int lastPos,
			String sectionPrefix, String sectionNumberSeparator) {
		StringBuilder title = new StringBuilder(sectionPrefix);
		for (int i = 0; i < pathPos; i++) {
			if (i > 0) {
				title.append(sectionNumberSeparator);
			}
			title.append(pathElements[i] + 1);
		}
		if (pathPos > 0) {
			title.append(sectionNumberSeparator);
		}
		title.append(lastPos + 1);
		return title.toString();
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
	public List<Resource> getAllUniqueResources() {
		Set<String> uniqueHrefs = new HashSet<>();
		List<Resource> result = new ArrayList<>();
		getAllUniqueResources(uniqueHrefs, result, tocReferences);
		return result;
	}
	
	
	private static void getAllUniqueResources(Set<String> uniqueHrefs, List<Resource> result, List<TocEntry> tocReferences) {
		for (TocEntry tocReference: tocReferences) {
			Resource resource = tocReference.getResource();
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
	
	private static int getTotalSize(Collection<TocEntry> tocReferences) {
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
}
