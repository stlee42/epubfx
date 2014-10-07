package de.machmireinebook.epubeditor.epublib.bookprocessor;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;

/**
 * If the book has no identifier it adds a generated UUID as identifier.
 * 
 * @author paul
 *
 */
public class FixIdentifierBookProcessor implements BookProcessor {

	@Override
	public Book processBook(Book book) {
		if(book.getMetadata().getIdentifiers().isEmpty()) {
			book.getMetadata().addIdentifier(new Identifier());
		}
		return book;
	}

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }
}
