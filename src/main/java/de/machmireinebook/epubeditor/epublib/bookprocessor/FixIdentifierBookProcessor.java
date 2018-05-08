package de.machmireinebook.epubeditor.epublib.bookprocessor;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Epub2Metadata;
import de.machmireinebook.epubeditor.epublib.domain.Epub3Metadata;
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
		if (book.isEpub3())
		{
			Epub3Metadata metadata = (Epub3Metadata) book.getMetadata();
			if (metadata.getIdentifiers().isEmpty())
			{
				metadata.addIdentifier(new Identifier());
			}
		}
		else
		{
			Epub2Metadata metadata = (Epub2Metadata) book.getMetadata();
			if (metadata.getIdentifiers().isEmpty())
			{
				metadata.addIdentifier(new Identifier());
			}
		}
		return book;
	}

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }
}
