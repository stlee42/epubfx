package de.machmireinebook.epubeditor.epublib.epub;

import java.io.OutputStream;

import de.machmireinebook.epubeditor.epublib.domain.Resource;

public interface HtmlProcessor {
	
	void processHtmlResource(Resource resource, OutputStream out);
}
