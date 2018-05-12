package de.machmireinebook.epubeditor.epublib.domain;

import javafx.scene.text.Font;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 18:56
 */
public class FontResource extends Resource<Font>
{
    public FontResource(String id, byte[] data, String href, MediaType mediaType)
    {
        super(id, data, href, mediaType);
    }

    public FontResource()
    {
        this(null, new byte[]{}, null, MediaType.TTF);
    }

    public FontResource(String href)
    {
        this(null, new byte[]{}, href, MediaType.TTF);
    }

    public FontResource(byte[] data, String href)
    {
        this(null, data, href, MediaType.TTF);
    }

    public FontResource(byte[] data, String href, MediaType mediaType)
    {
        this(null, data, href, mediaType);
    }
}
