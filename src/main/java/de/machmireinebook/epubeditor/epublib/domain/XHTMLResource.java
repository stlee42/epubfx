package de.machmireinebook.epubeditor.epublib.domain;

import java.io.IOException;

import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 18:50
 */
public class XHTMLResource extends Resource<Document>
{
    private static final Logger logger = Logger.getLogger(XHTMLResource.class);
    
    public XHTMLResource(String href)
    {
        super(href);
    }

    public XHTMLResource(String id, byte[] data, String href)
    {
        super(id, data, href, MediaType.XHTML);
    }

    public XHTMLResource(byte[] data, String href)
    {
        super(data, href);
    }

    public XHTMLResource()
    {
    }

    public XHTMLResource(Document document, String href)
    {
        super(XHTMLUtils.outputXHTMLDocument(document), href, MediaType.XHTML);
    }

    public XHTMLResource(byte[] data, String href, MediaType mediaType)
    {
        super(data, href, mediaType);
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
            logger.error(e);
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
            //ignoring, something is wrong with the xml
        }
        return result;
    }

    @Override
    public void setMediaType(MediaType mediaType)
    {
        super.setMediaType(mediaType);
    }
}
