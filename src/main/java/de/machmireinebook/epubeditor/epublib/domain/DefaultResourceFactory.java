package de.machmireinebook.epubeditor.epublib.domain;

import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 22:40
 */
public class DefaultResourceFactory  implements ResourceFactory
{
    public static final Logger logger = Logger.getLogger(DefaultResourceFactory.class);

    private static final DefaultResourceFactory instance = new DefaultResourceFactory();

    public static DefaultResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public Resource createResource()
    {
        return new Resource();
    }

    @Override
    public Resource createResource(String href)
    {
        return new Resource(href);
    }

    @Override
    public Resource createResource(byte[] data, String href)
    {
        return new Resource(data, href);
    }

    @Override
    public Resource createResource(String id, byte[] data, String href)
    {
        Resource res = new Resource(id, data, href, null);
        return res;
    }

    @Override
    public Resource createResource(String id, byte[] data, String href, MediaType mediaType)
    {
        Resource res = new Resource(id, data, href, mediaType);
        return res;
    }

    @Override
    public Resource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new Resource(data, href, mediaType);
    }
}
