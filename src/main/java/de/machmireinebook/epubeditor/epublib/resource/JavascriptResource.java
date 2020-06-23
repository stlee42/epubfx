package de.machmireinebook.epubeditor.epublib.resource;

import java.io.UnsupportedEncodingException;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 19:12
 */
public class JavascriptResource extends Resource<String> implements TextResource
{
    public JavascriptResource()
    {
    }

    public JavascriptResource(String href)
    {
        super(href);
    }

    public JavascriptResource(byte[] data, String href)
    {
        super(data, href);
    }

    public JavascriptResource(String id, byte[] data, String href)
    {
        super(id, data, href, MediaType.JAVASCRIPT);
    }

    public JavascriptResource(byte[] data, String href, MediaType mediaType)
    {
        super(data, href, mediaType);
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
