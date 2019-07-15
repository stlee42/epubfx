package de.machmireinebook.epubeditor.epublib.domain;

import java.io.IOException;

import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.jdom2.LocatedIdentifiableJDOMFactory;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 18:50
 */
public class XHTMLResource extends Resource<Document>
{
    private static final Logger logger = Logger.getLogger(XHTMLResource.class);

    private byte[] webViewPreparedData;
    
    public XHTMLResource(String href)
    {
        super(href);
    }

    public XHTMLResource(String id, byte[] data, String href)
    {
        super(id, data, href, MediaType.XHTML);
    }

    public XHTMLResource(byte[] data, String href)
    {
        super(data, href);
    }

    public XHTMLResource()
    {
    }

    public XHTMLResource(Document document, String href)
    {
        super(XHTMLUtils.outputXHTMLDocument(document), href, MediaType.XHTML);
    }

    public XHTMLResource(byte[] data, String href, MediaType mediaType)
    {
        super(data, href, mediaType);
    }

    @Override
    public Document asNativeFormat()
    {
        try
        {
            return XHTMLUtils.parseXHTMLDocument(getData(), getInputEncoding());
        }
        catch (IOException | JDOMException e)
        {
            logger.error(e);
            throw new ResourceDataException(e);
        }
    }

    public boolean isValidXML()
    {
        boolean result = false;
        try
        {
            Document doc = asNativeFormat();
            if (doc != null)
            {
                result = true;
            }
        }
        catch (ResourceDataException e)
        {
            //ignoring, something is wrong with the xml
        }
        return result;
    }

    @Override
    public void setMediaType(MediaType mediaType)
    {
        super.setMediaType(mediaType);
    }

    public void prepareWebViewDocument() {
        byte[] code = getData();
        if (code == null || code.length == 0) {
            return;
        }
        LocatedIdentifiableJDOMFactory factory = new LocatedIdentifiableJDOMFactory();
        try {
            Document document = XHTMLUtils.parseXHTMLDocument(code, factory);
            webViewPreparedData  = XHTMLUtils.outputXHTMLDocument(document, true);
        }
        catch (IOException | JDOMException e) {
            logger.error("error while creating prepared document for webview, in most cases the xhtml is not valid, use the original data for webviewer");
            webViewPreparedData = code;
        }
    }

    public byte[] getWebViewPreparedData() {
        return webViewPreparedData;
    }
}
