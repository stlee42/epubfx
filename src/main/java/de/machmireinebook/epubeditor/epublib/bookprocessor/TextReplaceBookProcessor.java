package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Cleans up regular html into xhtml.
 * Uses HtmlCleaner to do this.
 * 
 * @author paul
 *
 */
public class TextReplaceBookProcessor extends HtmlBookProcessor implements BookProcessor {

	@SuppressWarnings("unused")
	private final static Logger log = Logger.getLogger(TextReplaceBookProcessor.class);
	
	public TextReplaceBookProcessor() {
	}

	public byte[] processHtml(Resource resource, Book book, String outputEncoding) throws IOException {
		Reader reader = resource.asReader();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(out, Constants.CHARACTER_ENCODING);
		for(String line: IOUtils.readLines(reader)) {
			writer.write(processLine(line));
			writer.flush();
		}
		return out.toByteArray();
	}

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }

    private String processLine(String line) {
		return line.replace("&apos;", "'");
	}
}
