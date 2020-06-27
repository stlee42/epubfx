package de.machmireinebook.epubeditor.epublib.resource;

import org.apache.log4j.Logger;

import org.jdom2.Document;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 21:28
 */
public class XMLResourceFactory implements ResourceFactory<XMLResource, Document>
{
    private static final Logger logger = Logger.getLogger(XMLResourceFactory.class);

    private static final XMLResourceFactory instance = new XMLResourceFactory();

    public static XMLResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public XMLResource createResource()
    {
        return new XMLResource();
    }

    @Override
    public XMLResource createResource(String href)
    {
        return new XMLResource(href);
    }

    @Override
    public XMLResource createResource(byte[] data, String href)
    {
        return new XMLResource(data, href);
    }

    @Override
    public XMLResource createResource(String id, byte[] data, String href)
    {
        XMLResource res = new XMLResource(id, data, href);
        return res;
    }

    @Override
    public XMLResource createResource(String id, byte[] data, String href, MediaType mediaType)
    {
        XMLResource res = new XMLResource(id, data, href);
        res.setMediaType(mediaType);
        return res;
    }

    @Override
    public XMLResource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new XMLResource(data, href, mediaType);
    }
}
