package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.machmireinebook.commons.htmlcleaner.HtmlCleaner;
import de.machmireinebook.commons.htmlcleaner.JDomSerializer;
import de.machmireinebook.commons.htmlcleaner.TagNode;
import de.machmireinebook.commons.jdom2.XHTMLOutputProcessor;
import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;

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
            Document jdomDocument = new JDomSerializer(cleaner.getProperties(), false).createJDom(rootNode);
            Element root = jdomDocument.getRootElement();
            root.setNamespace(Constants.NAMESPACE_XHTML);
            root.addNamespaceDeclaration(Constants.NAMESPACE_XHTML);
            IteratorIterable<Element> elements = root.getDescendants(Filters.element());
            for (Element element : elements)
            {
                if (element.getNamespace() == null)
                {
                    element.setNamespace(Constants.NAMESPACE_XHTML);
                }
            }
            jdomDocument.setDocType(Constants.DOCTYPE_XHTML.clone());

            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            outputter.output(jdomDocument, bos);
            bytes = bos.toByteArray();
        }
        catch (IllegalAddException e)
        {
            logger.error("", e);
        }
        return bytes;
	}

    @Override
    public Resource processResource(Resource resource)
    {
        try
        {
            HtmlCleaner cleaner = XHTMLUtils.createHtmlCleaner();
            TagNode rootNode = cleaner.clean(resource.getInputStream());
            Document jdomDocument = new JDomSerializer(cleaner.getProperties(), false).createJDom(rootNode);
            Element root = jdomDocument.getRootElement();
            root.setNamespace(Constants.NAMESPACE_XHTML);
            root.addNamespaceDeclaration(Constants.NAMESPACE_XHTML);
            IteratorIterable<Element> elements = root.getDescendants(Filters.element());
            for (Element element : elements)
            {
                if (element.getNamespace() == null)
                {
                    element.setNamespace(Constants.NAMESPACE_XHTML);
                }
            }
            jdomDocument.setDocType(Constants.DOCTYPE_XHTML.clone());

            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor());
            String content = outputter.outputString(jdomDocument);
            resource.setData(content.getBytes("UTF-8"));
            resource.setInputEncoding("UTF-8");
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return resource;
    }


}
