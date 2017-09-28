package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TOCReference;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class SectionTitleBookProcessor implements BookProcessor
{
    private static final Logger logger = Logger.getLogger(SectionTitleBookProcessor.class);
	@Override
	public Book processBook(Book book) {
		XPath xpath = createXPathExpression();
		processSections(book.getTableOfContents().getTocReferences(), book, xpath);
		return book;
	}

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }

    private void processSections(List<TOCReference> tocReferences, Book book, XPath xpath) {
		for(TOCReference tocReference: tocReferences) {
			if(! StringUtils.isBlank(tocReference.getTitle())) {
				continue;
			}
			try {
				String title = getTitle(tocReference, book, xpath);
				tocReference.setTitle(title);
			} catch (XPathExpressionException | IOException e)
            {
				logger.error("", e);
			}
        }
	}
	
	
	private String getTitle(TOCReference tocReference, Book book, XPath xpath) throws IOException, XPathExpressionException {
		Resource resource = tocReference.getResource();
		if(resource == null) {
			return null;
		}
		InputSource inputSource = new InputSource(resource.getInputStream());
		String title = xpath.evaluate("/html/head/title", inputSource);
		return title;
	}
	
	
	private XPath createXPathExpression() {
		return XPathFactory.newInstance().newXPath();
	}
}
