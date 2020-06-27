package de.machmireinebook.epubeditor.epublib.resource;

import org.apache.log4j.Logger;

import org.jdom2.Document;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 21:19
 */
public class XHTMLResourceFactory implements ResourceFactory<XHTMLResource, Document>
{
    private static final Logger logger = Logger.getLogger(XHTMLResourceFactory.class);

    private static final XHTMLResourceFactory instance = new XHTMLResourceFactory();

    public static XHTMLResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public XHTMLResource createResource()
    {
        XHTMLResource res = new XHTMLResource();
        res.setMediaType(MediaType.XHTML);
        return res;
    }

    @Override
    public XHTMLResource createResource(String href)
    {
        XHTMLResource res = new XHTMLResource(href);
        res.setMediaType(MediaType.XHTML);
        return res;
    }

    @Override
    public XHTMLResource createResource(byte[] data, String href)
    {
        XHTMLResource res = new XHTMLResource(data, href);
        res.setMediaType(MediaType.XHTML);
        return res;
    }

    @Override
    public XHTMLResource createResource(String id, byte[] data, String href)
    {
        XHTMLResource res = new XHTMLResource(id, data, href);
        res.setMediaType(MediaType.XHTML);
        return res;
    }

    @Override
    public XHTMLResource createResource(String id, byte[] data, String href, MediaType mediaType)
    {
        XHTMLResource res = new XHTMLResource(id, data, href);
        res.setMediaType(mediaType);
        return res;
    }

    @Override
    public XHTMLResource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new XHTMLResource(data, href, mediaType);
    }
}
