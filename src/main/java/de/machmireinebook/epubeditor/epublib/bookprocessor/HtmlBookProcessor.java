package de.machmireinebook.epubeditor.epublib.bookprocessor;


import java.io.IOException;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.epub2.BookProcessor;

/**
 * Helper class for BookProcessors that only manipulate html type resources.
 * 
 * @author paul
 *
 */
public abstract class HtmlBookProcessor implements BookProcessor {

	private final static Logger log = Logger.getLogger(HtmlBookProcessor.class);

	public HtmlBookProcessor() {
	}

	@Override
	public Book processBook(Book book) {
		for(Resource resource: book.getResources().getAll()) {
			try {
				cleanupResource(resource, book);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		return book;
	}

	private void cleanupResource(Resource resource, Book book) throws IOException {
		if(resource.getMediaType() == MediaType.XHTML) {
			byte[] cleanedHtml = processHtml(resource, book, Constants.CHARACTER_ENCODING);
			resource.setData(cleanedHtml);
			resource.setInputEncoding(Constants.CHARACTER_ENCODING);
		}
	}

	protected abstract byte[] processHtml(Resource resource, Book book, String encoding) throws IOException;
}
