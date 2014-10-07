package de.machmireinebook.epubeditor.epublib.domain;

import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 22:36
 */
public class ImageResourceFactory implements ResourceFactory
{
    public static final Logger logger = Logger.getLogger(ImageResourceFactory.class);

    private static final ImageResourceFactory instance = new ImageResourceFactory();

    public static ImageResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public Resource createResource()
    {
        return new ImageResource();
    }

    @Override
    public Resource createResource(String href)
    {
        return new ImageResource(href);
    }

    @Override
    public Resource createResource(byte[] data, String href)
    {
        return new ImageResource(data, href);
    }

    @Override
    public Resource createResource(String id, byte[] data, String href)
    {
        Resource res = new ImageResource(id, data, href, null);
        return res;
    }

    @Override
    public Resource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new ImageResource(data, href, mediaType);
    }
}
