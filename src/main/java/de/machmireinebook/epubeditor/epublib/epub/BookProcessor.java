package de.machmireinebook.epubeditor.epublib.epub;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;

/**
 * Post-processes a book.
 * 
 * Can be used to clean up a book after reading or before writing.
 * 
 * @author paul
 *
 */
public interface BookProcessor {
	
	/**
	 * A BookProcessor that returns the input book unchanged.
	 */
	BookProcessor IDENTITY_BOOKPROCESSOR = new BookProcessor() {
		
		@Override
		public Book processBook(Book book)
        {
			return book;
		}

        @Override
        public Resource processResource(Resource resource)
        {
            return resource;
        }
    };
	
	Book processBook(Book book);
    Resource processResource(Resource resource);
}
