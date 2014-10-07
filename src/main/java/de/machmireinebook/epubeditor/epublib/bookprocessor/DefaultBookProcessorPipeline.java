package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessorPipeline;

import org.apache.log4j.Logger;

/**
 * A book processor that combines several other bookprocessors
 * 
 * Fixes coverpage/coverimage.
 * Cleans up the XHTML.
 * 
 * @author paul.siegmann
 *
 */
public class DefaultBookProcessorPipeline extends BookProcessorPipeline {

	private Logger log = Logger.getLogger(DefaultBookProcessorPipeline.class);

	public DefaultBookProcessorPipeline() {
		super(createDefaultBookProcessors());
	}

	private static List<BookProcessor> createDefaultBookProcessors() {
		List<BookProcessor> result = new ArrayList<>();
		result.addAll(Arrays.asList(new SectionHrefSanityCheckBookProcessor(),
                new HtmlCleanerBookProcessor(),
                new CoverpageBookProcessor(),
                new FixIdentifierBookProcessor()));
		return result;
	}
}
