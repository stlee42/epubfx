package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.io.IOException;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.jsoup.Jsoup;
import de.machmireinebook.epubeditor.jsoup.nodes.Document;
import de.machmireinebook.epubeditor.jsoup.nodes.Entities;
import de.machmireinebook.epubeditor.jsoup.parser.Parser;

import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 00:26
 */
public class JSoupBookProcessor extends HtmlBookProcessor
{
    public static final Logger logger = Logger.getLogger(JSoupBookProcessor.class);



    @Override
    protected byte[] processHtml(Resource resource, Book book, String encoding) throws IOException
    {
        logger.info("processing " + resource + " with jsoup");
        Document doc = Jsoup.parse(resource.getInputStream(), "UTF-8", "", Parser.xmlParser());
        doc.outputSettings().indentAmount(2);
        doc.outputSettings().prettyPrint(true);
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        return doc.outerHtml().getBytes("UTF-8");
    }

    @Override
    public Resource processResource(Resource resource)
    {
        try
        {
            byte[] bytes = processHtml(resource, null, null);
            resource.setData(bytes);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return resource;
    }
}
