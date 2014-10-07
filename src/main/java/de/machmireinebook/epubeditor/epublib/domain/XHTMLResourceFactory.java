package de.machmireinebook.epubeditor.epublib.domain;

import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 21:19
 */
public class XHTMLResourceFactory implements ResourceFactory
{
    public static final Logger logger = Logger.getLogger(XHTMLResourceFactory.class);

    private static final XHTMLResourceFactory instance = new XHTMLResourceFactory();

    public static XHTMLResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public Resource createResource()
    {
        Resource res = new XHTMLResource();
        res.setMediaType(MediaType.XHTML);
        return res;
    }

    @Override
    public Resource createResource(String href)
    {
        Resource res = new XHTMLResource(href);
        res.setMediaType(MediaType.XHTML);
        return res;
    }

    @Override
    public Resource createResource(byte[] data, String href)
    {
        Resource res = new XHTMLResource(data, href);
        res.setMediaType(MediaType.XHTML);
        return res;
    }

    @Override
    public Resource createResource(String id, byte[] data, String href)
    {
        Resource res = new XHTMLResource(id, data, href);
        res.setMediaType(MediaType.XHTML);
        return res;
    }

    @Override
    public Resource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new XHTMLResource(data, href, mediaType);
    }
}
