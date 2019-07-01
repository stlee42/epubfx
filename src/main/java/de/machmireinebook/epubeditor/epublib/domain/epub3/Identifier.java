package de.machmireinebook.epubeditor.epublib.domain.epub3;

import java.io.Serializable;
import java.util.UUID;

/**
 * A Book's identifier.
 * 
 * Defaults to a random UUID and scheme "UUID"
 * 
 * @author paul
 *
 */
public class Identifier extends DublinCoreMetadataElement implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 955949951416391810L;

	public enum Scheme {
		UUID("uuid", false),
		ISBN("isbn", false),
		URL("URL", true),
		URI("URI", true),
		DOI("doi", false);

		private String value;
		private boolean isObsolete;

		Scheme(String value, boolean isObsolete)
		{
			this.value = value;
			this.isObsolete = isObsolete;
		}

		public String getValue()
		{
			return value;
		}

		public boolean isObsolete()
		{
			return isObsolete;
		}
	}

	public Identifier(String id, String scheme, String value)
	{
		super(id, scheme, value, null);
	}

	/**
	 * Creates an Identifier with as value a random UUID and scheme "UUID"
	 */
	public Identifier() {
		super(null, Scheme.UUID.getValue(), UUID.randomUUID().toString(), null);
	}


	private boolean bookId = false;

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
}
