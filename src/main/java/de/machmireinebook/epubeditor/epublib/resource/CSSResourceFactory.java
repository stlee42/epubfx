package de.machmireinebook.epubeditor.epublib.resource;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 21:49
 */
public class CSSResourceFactory implements ResourceFactory
{
    private static final Logger logger = Logger.getLogger(CSSResourceFactory.class);

    private static final CSSResourceFactory instance = new CSSResourceFactory();

    public static CSSResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public Resource createResource()
    {
        return new CSSResource();
    }

    @Override
    public Resource createResource(String href)
    {
        return new CSSResource(href);
    }

    @Override
    public Resource createResource(byte[] data, String href)
    {
        return new CSSResource(data, href);
    }

    @Override
    public Resource createResource(String id, byte[] data, String href)
    {
        Resource res = new CSSResource(id, data, href);
        return res;
    }

    @Override
    public Resource createResource(String id, byte[] data, String href, MediaType mediaType)
    {
        Resource res = new CSSResource(id, data, href);
        res.setMediaType(mediaType);
        return res;
    }

    @Override
    public Resource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new CSSResource(data, href, mediaType);
    }
}