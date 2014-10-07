package de.machmireinebook.epubeditor.epublib.domain;

import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 21:39
 */
public class JavascriptResourceFactory implements ResourceFactory
{
    public static final Logger logger = Logger.getLogger(JavascriptResourceFactory.class);

    private static final JavascriptResourceFactory instance = new JavascriptResourceFactory();

    public static JavascriptResourceFactory getInstance()
    {
        return instance;
    }

    @Override
    public Resource createResource()
    {
        return new JavascriptResource();
    }

    @Override
    public Resource createResource(String href)
    {
        return new JavascriptResource(href);
    }

    @Override
    public Resource createResource(byte[] data, String href)
    {
        return new JavascriptResource(data, href);
    }

    @Override
    public Resource createResource(String id, byte[] data, String href)
    {
        Resource res = new JavascriptResource(id, data, href);
        return res;
    }


    @Override
    public Resource createResource(byte[] data, String href, MediaType mediaType)
    {
        return new JavascriptResource(data, href, mediaType);
    }
}

