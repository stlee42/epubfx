package de.machmireinebook.epubeditor.epublib.resource;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 22:36
 */
public class ImageResourceFactory implements ResourceFactory<ImageResource>
{
    private static final Logger logger = Logger.getLogger(ImageResourceFactory.class);

    private static final ImageResourceFactory instance = new ImageResourceFactory();

    public static ImageResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public ImageResource createResource()
    {
        return new ImageResource();
    }

    @Override
    public ImageResource createResource(String href)
    {
        return new ImageResource(href);
    }

    @Override
    public ImageResource createResource(byte[] data, String href)
    {
        return new ImageResource(data, href);
    }

    @Override
    public ImageResource createResource(String id, byte[] data, String href)
    {
        ImageResource res = new ImageResource(id, data, href, null);
        return res;
    }

    @Override
    public ImageResource createResource(String id, byte[] data, String href, MediaType mediaType)
    {
        ImageResource res = new ImageResource(id, data, href, mediaType);
        return res;
    }

    @Override
    public ImageResource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new ImageResource(data, href, mediaType);
    }
}
