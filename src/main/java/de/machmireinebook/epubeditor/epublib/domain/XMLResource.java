package de.machmireinebook.epubeditor.epublib.domain;

import java.io.IOException;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import org.jdom2.Document;
import org.jdom2.JDOMException;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 19:11
 */
public class XMLResource extends Resource<Document>
{
    public XMLResource()
    {
    }

    public XMLResource(String href)
    {
        super(href);
    }

    public XMLResource(byte[] data, String href)
    {
        super(data, href);
    }

    public XMLResource(String id, byte[] data, String href)
    {
        super(id, data, href, null);
    }

    public XMLResource(byte[] data, String href, MediaType mediaType)
    {
        super(data, href, mediaType);
    }

    public XMLResource(String id, byte[] data, String href, MediaType mediaType) {
        super(id, data, href, mediaType, Constants.CHARACTER_ENCODING);
    }


    @Override
    public Document asNativeFormat()
    {
        try
        {
            return XHTMLUtils.parseXHTMLDocument(getData(), getInputEncoding());
        }
        catch (IOException | JDOMException e)
        {
            logger.error("", e);
            throw new ResourceDataException(e);
        }
    }

    public boolean isValidXML()
    {
        boolean result = false;
        try
        {
            Document doc = asNativeFormat();
            if (doc != null)
            {
                result = true;
            }
        }
        catch (ResourceDataException e)
        {
            //ignoring, somethin is wrong with the xml
        }
        return result;
    }
}
