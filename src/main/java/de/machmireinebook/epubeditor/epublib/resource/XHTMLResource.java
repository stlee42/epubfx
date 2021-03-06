package de.machmireinebook.epubeditor.epublib.resource;

import java.io.IOException;

import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.jdom2.LocatedIdentifiableJDOMFactory;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 18:50
 */
public class XHTMLResource extends XMLResource
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

    public void prepareWebViewDocument(EpubVersion version) {
        byte[] code = getData();
        if (code == null || code.length == 0) {
            return;
        }
        LocatedIdentifiableJDOMFactory factory = new LocatedIdentifiableJDOMFactory();
        try {
            Document document = XHTMLUtils.parseXHTMLDocument(code, factory);
            //inject special css only for preview
            Element root = document.getRootElement();
            Element head = root.getChild("head", Constants.NAMESPACE_XHTML);
            if (head != null) {
                //    <link href="../Styles/fonts.css" type="text/css" rel="stylesheet" />
                Element element = new Element("link");
                element.setAttribute("href", "http://localhost:8777/editor-css/preview.css");
                element.setAttribute("type", "text/css");
                element.setAttribute("rel", "stylesheet");
                head.addContent(element);
            }
            webViewPreparedData  = XHTMLUtils.outputXHTMLDocument(document, true, version);
        }
        catch (IOException | JDOMException e) {
            logger.error("error while creating prepared document for webview, in most cases the xhtml is not valid, use the original data for webviewer");
            webViewPreparedData = code;
        }
    }

    public byte[] getWebViewPreparedData() {
        return webViewPreparedData;
    }

    public void setHtmlTitle(String title, EpubVersion version) {
        Document document = asNativeFormat();
        Element htmlElement = document.getRootElement();
        if (htmlElement != null) {
            Element headElement = htmlElement.getChild("head", Constants.NAMESPACE_XHTML);
            if (headElement != null) {
                Element titleElement = headElement.getChild("title", Constants.NAMESPACE_XHTML);
                if (titleElement != null) {
                    titleElement.setText(title);
                }
            }
        }
        setData(XHTMLUtils.outputXHTMLDocument(document, true, version));
    }
}
