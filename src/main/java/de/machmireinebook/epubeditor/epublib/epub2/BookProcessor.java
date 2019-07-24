package de.machmireinebook.epubeditor.epublib.epub2;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.resource.Resource;

/**
 * Post-processes a book.
 * 
 * Can be used to clean up a book after reading or before writing.
 * 
 * @author paul
 *
 */
public interface BookProcessor {
	Book processBook(Book book);
    Resource processResource(Resource resource, Book book);
}
