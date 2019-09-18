package de.machmireinebook.epubeditor.epublib.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 19:11
 */
public class XMLResource extends Resource<Document> implements TextResource
{
    private static final Logger logger = Logger.getLogger(XMLResource.class);
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
        try {
            return  new SAXBuilder().build(getInputStream());
        } catch (IOException | JDOMException e) {
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

    @Override
    public String asString() {
        try {
            return new String(getData(), getInputEncoding());
        }
        catch (UnsupportedEncodingException e) {
            //should not happens
            return null;
        }
    }
}
