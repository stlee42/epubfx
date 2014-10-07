package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.util.Collection;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TOCReference;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;

public class FixMissingResourceBookProcessor implements BookProcessor {

	@Override
	public Book processBook(Book book) {
		return book;
	}

	private void fixMissingResources(Collection<TOCReference> tocReferences, Book book) {
		for (TOCReference tocReference:  tocReferences) {
			if (tocReference.getResource() == null) {
				
			}
		}
	}

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }

}
