package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

import org.htmlcleaner.EpubJDomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jdom2.Document;
import org.jdom2.IllegalAddException;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.epub2.BookProcessor;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * Cleans up regular html into xhtml. Uses HtmlCleaner to do this.
 * 
 * @author paul
 * 
 */
public class HtmlCleanerBookProcessor extends HtmlBookProcessor implements
		BookProcessor {

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(HtmlCleanerBookProcessor.class);


	public byte[] processHtml(Resource resource, Book book, String outputEncoding) throws IOException
    {
        logger.info("processing " + resource + " with htmlcleaner");
        byte[] bytes = null;
        try
        {
            HtmlCleaner cleaner = XHTMLUtils.createHtmlCleaner();

            TagNode rootNode = cleaner.clean(resource.getInputStream());
            Document jdomDocument = new EpubJDomSerializer(cleaner.getProperties(), false).createJDom(rootNode);
            return XHTMLUtils.outputXHTMLDocument(jdomDocument, book.getVersion());
        }
        catch (IllegalAddException e)
        {
            logger.error("", e);
        }
        return bytes;
	}

    @Override
    public Resource processResource(Resource resource, Book book)
    {
        try
        {
            HtmlCleaner cleaner = XHTMLUtils.createHtmlCleaner();
            TagNode rootNode = cleaner.clean(resource.getInputStream());
            Document jdomDocument = new EpubJDomSerializer(cleaner.getProperties(), false).createJDom(rootNode);
            String content = XHTMLUtils.outputXHTMLDocumentAsString(jdomDocument, book.getVersion());
            logger.debug("new content " + content);
            resource.setData(content.getBytes(StandardCharsets.UTF_8));
            resource.setInputEncoding("UTF-8");
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return resource;
    }


}
