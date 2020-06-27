package de.machmireinebook.epubeditor.epublib.domain;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.ResourceFactory;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 22:40
 */
public class DefaultResourceFactory<S> implements ResourceFactory<Resource<S>, S>
{
    private static final Logger logger = Logger.getLogger(DefaultResourceFactory.class);

    private static final DefaultResourceFactory instance = new DefaultResourceFactory<>();

    public static DefaultResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public Resource<S> createResource()
    {
        return new Resource<>();
    }

    @Override
    public Resource<S> createResource(String href)
    {
        return new Resource<>(href);
    }

    @Override
    public Resource<S> createResource(byte[] data, String href)
    {
        return new Resource<>(data, href);
    }

    @Override
    public Resource<S> createResource(String id, byte[] data, String href)
    {
        Resource<S> res = new Resource<>(id, data, href, null);
        return res;
    }

    @Override
    public Resource<S> createResource(String id, byte[] data, String href, MediaType mediaType)
    {
        Resource<S> res = new Resource<>(id, data, href, mediaType);
        return res;
    }

    @Override
    public Resource<S> createResource(byte[] data, String href, MediaType mediaType)
    {
        return new Resource<>(data, href, mediaType);
    }
}
