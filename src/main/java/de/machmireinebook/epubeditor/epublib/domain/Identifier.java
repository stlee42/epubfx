package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

/**
 * A Book's identifier.
 * 
 * Defaults to a random UUID and scheme "UUID"
 * 
 * @author paul
 *
 */
public class Identifier implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 955949951416391810L;

	public interface Scheme {
		String UUID = "UUID";
		String ISBN = "ISBN";
		String URL = "URL";
		String URI = "URI";
	}
	
	private boolean bookId = false;
	private String scheme;
	private String value;

	/**
	 * Creates an Identifier with as value a random UUID and scheme "UUID"
	 */
	public Identifier() {
		this(Scheme.UUID, UUID.randomUUID().toString());
	}
	
	
	public Identifier(String scheme, String value) {
		this.scheme = scheme;
		this.value = value;
	}

	public String getScheme() {
		return scheme;
	}
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}


	public void setBookId(boolean bookId) {
		this.bookId = bookId;
	}


	/**
	 * This bookId property allows the book creator to add multiple ids and tell the epubwriter which one to write out as the bookId.
	 *  
	 * The Dublin Core metadata spec allows multiple identifiers for a Book.
	 * The epub spec requires exactly one identifier to be marked as the book id.
	 * 
	 * @return whether this is the unique book id.
	 */
	public boolean isBookId() {
		return bookId;
	}

	public int hashCode() {
		return StringUtils.defaultString(scheme).hashCode() ^ StringUtils.defaultString(value).hashCode();
	}
	
	public boolean equals(Object otherIdentifier) {
        return otherIdentifier instanceof Identifier
                && StringUtils.equals(scheme, ((Identifier) otherIdentifier).scheme)
                && StringUtils.equals(value, ((Identifier) otherIdentifier).value);
    }
	
	public String toString() {
		if (StringUtils.isBlank(scheme)) {
			return value;
		}
		return scheme + ":" + value;
	}
}
