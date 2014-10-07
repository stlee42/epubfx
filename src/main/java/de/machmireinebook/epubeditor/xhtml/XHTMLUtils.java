package de.machmireinebook.epubeditor.xhtml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import de.machmireinebook.commons.htmlcleaner.CleanerProperties;
import de.machmireinebook.commons.htmlcleaner.HtmlCleaner;
import de.machmireinebook.commons.htmlcleaner.JDomSerializer;
import de.machmireinebook.commons.htmlcleaner.TagNode;
import de.machmireinebook.commons.jdom2.XHTMLOutputProcessor;
import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

import org.apache.log4j.Logger;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.JDOMException;
import org.jdom2.JDOMFactory;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;

/**
 * User: mjungierek
 * Date: 02.08.2014
 * Time: 23:20
 */
public class XHTMLUtils
{
    public static final Logger logger = Logger.getLogger(XHTMLUtils.class);

    public static Resource fromHtml(Resource res)
    {
        try
        {
            String xhtml = fromHtml(res.getData());
            res.setData(xhtml.getBytes("UTF-8"));
            res.setInputEncoding("UTF-8");
        }
        catch (IOException | IllegalAddException e)
        {
            logger.error("", e);
        }

        return res;
    }

    public static String fromHtml(byte[] originalHtml)
    {
        String content = null;
        try
        {
            HtmlCleaner cleaner = createHtmlCleaner();

            //wir probieren es erstmal mit UTF-8
            TagNode rootNode = cleaner.clean(new String(originalHtml, "UTF-8"));
            Document jdomDocument = new JDomSerializer(new CleanerProperties(), true).createJDom(rootNode);

            // hat die Datei ein anderes Encoding im HTML-Header deklariert?
            Element root = jdomDocument.getRootElement();
            if (root != null)
            {
                Element headElement = root.getChild("head");
                if (headElement != null)
                {
                    List<Element> metaElements = headElement.getChildren("meta");
                    for (Element metaElement : metaElements)
                    {
                        if ("content-type".equals(metaElement.getAttributeValue("http-equiv")))
                        {
                            String charsetContent = metaElement.getAttributeValue("content");
                            String[] splitted = charsetContent.split(";");
                            if (splitted.length > 1)
                            {
                                charsetContent = splitted[1].trim();
                                String[] splitted2 = charsetContent.split("=");
                                if (splitted2.length > 1)
                                {
                                    charsetContent = splitted2[1].trim();  //endlich
                                    if (!"UTF-8".equalsIgnoreCase(charsetContent))
                                    {
                                        byte[] recodedHTML = ResourceUtil.recode(charsetContent, "UTF-8", originalHtml);
                                        HtmlCleaner cleaner2 = createHtmlCleaner();
                                        rootNode = cleaner2.clean(new String(recodedHTML, "UTF-8"));
                                        jdomDocument = new JDomSerializer(cleaner2.getProperties(), false).createJDom(rootNode);
                                        root = jdomDocument.getRootElement();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                root.setNamespace(Constants.NAMESPACE_XHTML);
                root.addNamespaceDeclaration(Constants.NAMESPACE_XHTML);
                IteratorIterable<Element> elements = root.getDescendants(Filters.element());
                for (Element element : elements)
                {
                    if (element.getNamespace() == null)
                    {
                        element.setNamespace(Constants.NAMESPACE_XHTML);
                    }
                }
                jdomDocument.setDocType(Constants.DOCTYPE_XHTML.clone());
            }

            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor());
            content = outputter.outputString(jdomDocument);
        }
        catch (IOException | IllegalAddException e)
        {
            logger.error("", e);
        }
        return content;
    }

    public static HtmlCleaner createHtmlCleaner()
    {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties cleanerProperties = cleaner.getProperties();
        cleanerProperties.setAdvancedXmlEscape(true);
        cleanerProperties.setOmitXmlDeclaration(true);
        cleanerProperties.setOmitDoctypeDeclaration(false);
        cleanerProperties.setRecognizeUnicodeChars(true);
        cleanerProperties.setTranslateSpecialEntities(false);
        cleanerProperties.setIgnoreQuestAndExclam(true);
        cleanerProperties.setUseEmptyElementTags(false);
        cleanerProperties.setNamespacesAware(true);
        cleanerProperties.setUseCdataForScriptAndStyle(false);
        return cleaner;
    }

    public static Document parseXHTMLDocument(byte[] bytes, String encoding) throws IOException, JDOMException
    {
        String xhtml = new String(bytes, encoding);
        return parseXHTMLDocument(xhtml);
    }

    public static Document parseXHTMLDocument(String xhtml) throws IOException, JDOMException
    {
        return parseXHTMLDocument(xhtml, null);
    }

    public static Document parseXHTMLDocument(String xhtml, JDOMFactory factory) throws IOException, JDOMException
    {
        //DTD ersetzen, da die originale nicht erreichbar bzw. nur sehr langsam ist,
        xhtml = xhtml.replace("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd", "http://localhost:8777/dtd/www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
        ByteArrayInputStream bais = new ByteArrayInputStream(xhtml.getBytes("UTF-8"));
        SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setExpandEntities(false);
        if (factory != null)
        {
            builder.setJDOMFactory(factory);
        }
        Document document = builder.build(bais);
        return document;
    }

    public static byte[] outputXHTMLDocument(Document document)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            document.setDocType(Constants.DOCTYPE_XHTML.clone());

            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor());
            outputter.output(document, baos);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return baos.toByteArray();
    }

    public static String repair(String originalHtml)
    {
        String content = null;
        try
        {
            HtmlCleaner cleaner = createHtmlCleaner();

            //wir probieren es erstmal mit UTF-8
            TagNode rootNode = cleaner.clean(originalHtml);
            Document jdomDocument = new JDomSerializer(cleaner.getProperties(), false).createJDom(rootNode);

            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor());
            content = outputter.outputString(jdomDocument);
        }
        catch (IllegalAddException e)
        {
            logger.error("", e);
        }
        return content;
    }

    public static byte[] repairWithHead(byte[] data, List<Content> originalHeadContent)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            HtmlCleaner htmlCleaner = createHtmlCleaner();
            TagNode rootNode = htmlCleaner.clean(new ByteArrayInputStream(data));

            Document jdomDocument = new JDomSerializer(htmlCleaner.getProperties(), false).createJDom(rootNode);
            Element root = jdomDocument.getRootElement();
            root.setNamespace(Constants.NAMESPACE_XHTML);
            root.addNamespaceDeclaration(Constants.NAMESPACE_XHTML);
            IteratorIterable<Element> elements = root.getDescendants(Filters.element());
            for (Element element : elements)
            {
                if (element.getNamespace() == null)
                {
                    element.setNamespace(Constants.NAMESPACE_XHTML);
                }
            }
            jdomDocument.setDocType(Constants.DOCTYPE_XHTML.clone());

            Element headElement = root.getChild("head", Constants.NAMESPACE_XHTML);
            for (Content content : originalHeadContent)
            {
                headElement.addContent(content);
            }

            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor());
            outputter.output(jdomDocument, baos);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return baos.toByteArray();
    }
}
