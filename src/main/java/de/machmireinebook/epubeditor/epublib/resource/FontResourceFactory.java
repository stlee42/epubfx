package de.machmireinebook.epubeditor.epublib.resource;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 22:36
 */
public class FontResourceFactory implements ResourceFactory
{
    private static final Logger logger = Logger.getLogger(FontResourceFactory.class);

    private static final FontResourceFactory instance = new FontResourceFactory();

    public static FontResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public Resource createResource()
    {
        return new FontResource();
    }

    @Override
    public Resource createResource(String href)
    {
        return new FontResource(href);
    }

    @Override
    public Resource createResource(byte[] data, String href)
    {
        return new FontResource(data, href);
    }

    @Override
    public Resource createResource(String id, byte[] data, String href)
    {
        Resource res = new FontResource(id, data, href, null);
        return res;
    }

    @Override
    public Resource createResource(String id, byte[] data, String href, MediaType mediaType)
    {
        Resource res = new FontResource(id, data, href, mediaType);
        return res;
    }

    @Override
    public Resource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new FontResource(data, href, mediaType);
    }
}
