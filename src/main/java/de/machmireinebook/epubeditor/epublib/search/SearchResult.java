package de.machmireinebook.epubeditor.epublib.search;

import de.machmireinebook.epubeditor.epublib.domain.Resource;

public class SearchResult {
	private int pagePos = -1;
	private String searchTerm;
	private Resource resource;
	public SearchResult(int pagePos, String searchTerm, Resource resource) {
		super();
		this.pagePos = pagePos;
		this.searchTerm = searchTerm;
		this.resource = resource;
	}
	public int getPagePos() {
		return pagePos;
	}
	public String getSearchTerm() {
		return searchTerm;
	}
	public Resource getResource() {
		return resource;
	}
}