package de.machmireinebook.epubeditor.epublib.bookprocessor;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;

/**
 * In the future this will split up too large html documents into smaller ones.
 * 
 * @author paul
 *
 */
public class HtmlSplitterBookProcessor implements BookProcessor {

	@Override
	public Book processBook(Book book) {
		return book;
	}

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }
}
