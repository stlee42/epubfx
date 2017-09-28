package de.machmireinebook.epubeditor.epublib.domain;

import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 21:28
 */
public class XMLResourceFactory implements ResourceFactory
{
    private static final Logger logger = Logger.getLogger(XMLResourceFactory.class);

    private static final XMLResourceFactory instance = new XMLResourceFactory();

    public static XMLResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public Resource createResource()
    {
        return new XMLResource();
    }

    @Override
    public Resource createResource(String href)
    {
        return new XMLResource(href);
    }

    @Override
    public Resource createResource(byte[] data, String href)
    {
        return new XMLResource(data, href);
    }

    @Override
    public Resource createResource(String id, byte[] data, String href)
    {
        Resource res = new XMLResource(id, data, href);
        return res;
    }

    @Override
    public Resource createResource(String id, byte[] data, String href, MediaType mediaType)
    {
        Resource res = new XMLResource(id, data, href);
        res.setMediaType(mediaType);
        return res;
    }

    @Override
    public Resource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new XMLResource(data, href, mediaType);
    }
}
