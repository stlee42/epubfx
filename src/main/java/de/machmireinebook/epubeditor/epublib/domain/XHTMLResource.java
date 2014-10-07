package de.machmireinebook.epubeditor.epublib.domain;

import java.io.IOException;

import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import org.jdom2.Document;
import org.jdom2.JDOMException;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 18:50
 */
public class XHTMLResource extends Resource<Document>
{
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

    public XHTMLResource(byte[] data, String href, MediaType mediaType)
    {
        super(data, href, mediaType);
    }

    @Override
    public Document getAsNativeFormat()  throws IOException, JDOMException
    {
        return XHTMLUtils.parseXHTMLDocument(getData(), getInputEncoding());
    }

    public boolean isValidXML()
    {
        boolean result = false;
        try
        {
            Document doc = getAsNativeFormat();
            if (doc != null)
            {
                result = true;
            }
        }
        catch (IOException | JDOMException e)
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
