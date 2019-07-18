package de.machmireinebook.epubeditor.xhtml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.commons.text.translate.NumericEntityUnescaper;
import org.apache.log4j.Logger;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.EpubJDomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.JDOMException;
import org.jdom2.JDOMFactory;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;
import de.machmireinebook.epubeditor.jdom2.XHTMLOutputProcessor;

/**
 * User: mjungierek
 * Date: 02.08.2014
 * Time: 23:20
 */
public class XHTMLUtils
{
    private static final Logger logger = Logger.getLogger(XHTMLUtils.class);

    public static final Map<CharSequence, CharSequence> BASIC_ESCAPE;
    static {
        final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
        initialMap.put("\"", "&quot;"); // " - double-quote
        BASIC_ESCAPE = Collections.unmodifiableMap(initialMap);
    }
    public static final Map<CharSequence, CharSequence> BASIC_UNESCAPE;
    static {
        BASIC_UNESCAPE = Collections.unmodifiableMap(EntityArrays.invert(BASIC_ESCAPE));
    }


    public static Resource fromHtml(Resource res)
    {
        try
        {
            String xhtml = fromHtml(res.getData());
            res.setData(xhtml.getBytes(StandardCharsets.UTF_8));
            res.setInputEncoding(StandardCharsets.UTF_8.displayName());
        }
        catch (IllegalAddException e)
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
            TagNode rootNode = cleaner.clean(new String(originalHtml, StandardCharsets.UTF_8));
            Document jdomDocument = new EpubJDomSerializer(cleaner.getProperties(), false).createJDom(rootNode);

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
                                        rootNode = cleaner2.clean(new String(recodedHTML, StandardCharsets.UTF_8));
                                        jdomDocument = new EpubJDomSerializer(cleaner2.getProperties(), false).createJDom(rootNode);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            content = outputXHTMLDocumentAsString(jdomDocument);
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
        cleanerProperties.setTranslateSpecialEntities(true);
        cleanerProperties.setTransSpecialEntitiesToNCR(true);
        cleanerProperties.setIgnoreQuestAndExclam(true);
        cleanerProperties.setUseEmptyElementTags(false);
        cleanerProperties.setNamespacesAware(true);
        cleanerProperties.setUseCdataFor("script,");
        cleanerProperties.setInvalidXmlAttributeNamePrefix("epubfx-");
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

    public static Document parseXHTMLDocument(byte[] bytes, JDOMFactory factory) throws IOException, JDOMException {
        String xhtml = new String(bytes, StandardCharsets.UTF_8);
        return parseXHTMLDocument(xhtml, factory);
    }

    public static Document parseXHTMLDocument(String xhtml, JDOMFactory factory) throws IOException, JDOMException
    {
        //DTD ersetzen, da die originale nicht erreichbar bzw. nur sehr langsam ist,
        xhtml = xhtml.replace("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd", "http://localhost:8777/dtd/www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
        ByteArrayInputStream bais = new ByteArrayInputStream(xhtml.getBytes(StandardCharsets.UTF_8));
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

    public static String repair(String originalHtml)
    {
        String content = null;
        try
        {
            HtmlCleaner cleaner = createHtmlCleaner();

            TagNode rootNode = cleaner.clean(originalHtml);
            Document jdomDocument = new EpubJDomSerializer(cleaner.getProperties(), false).createJDom(rootNode);
            content = outputXHTMLDocumentAsString(jdomDocument);
        }
        catch (IllegalAddException e)
        {
            logger.error("", e);
        }
        return content;
    }

    public static String outputXHTMLDocumentAsString(Document document) {
        return new String(outputXHTMLDocument(document), StandardCharsets.UTF_8);
    }

    public static String outputXHTMLDocumentAsString(Document document, boolean escapeOutput) {
        return new String(outputXHTMLDocument(document, escapeOutput), StandardCharsets.UTF_8);
    }

    public static byte[] outputXHTMLDocument(Document document) {
        return outputXHTMLDocument(document, false);
    }

    public static byte[] outputXHTMLDocument(Document document, boolean escapeOutput)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            Element root = document.getRootElement();
            if (root != null) {
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
            }

            document.setDocType(Constants.DOCTYPE_XHTML.clone());

            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor(escapeOutput));
            outputter.escapeElementEntities("&");
            outputter.escapeAttributeEntities("&");
            outputter.output(document, baos);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return baos.toByteArray();
    }

    public static byte[] repairWithHead(byte[] data, List<Content> originalHeadContent)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            HtmlCleaner htmlCleaner = createHtmlCleaner();
            TagNode rootNode = htmlCleaner.clean(new ByteArrayInputStream(data));

            Document jdomDocument = new EpubJDomSerializer(htmlCleaner.getProperties(), false).createJDom(rootNode);
            Element root = jdomDocument.getRootElement();

            Element headElement = root.getChild("head");
            for (Content content : originalHeadContent)
            {
                headElement.addContent(content);
            }

            root.setNamespace(Constants.NAMESPACE_XHTML);
            root.addNamespaceDeclaration(Constants.NAMESPACE_XHTML);
            IteratorIterable<Element> elements = root.getDescendants(Filters.element());
            for (Element element : elements)
            {
                if (element.getNamespace() == null || element.getNamespace() == Namespace.NO_NAMESPACE) //kein oder der leere NS zum XHTML namespace machen
                {
                    element.setNamespace(Constants.NAMESPACE_XHTML);
                }
            }
            jdomDocument.setDocType(Constants.DOCTYPE_XHTML.clone());

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

    public static String unescapedHtmlWithXmlExceptions(String escapedText) {
        //leave nbsp untouched
        Map<CharSequence, CharSequence> withoutNbsp = new HashMap<>(EntityArrays.ISO8859_1_UNESCAPE);
        withoutNbsp.remove("&nbsp;");
        //some scripts for generating html from docx generate this (wrong typed) entity for „
        withoutNbsp.put("&dbquo;", "„");
        CharSequenceTranslator translator =
                new AggregateTranslator(
                        new LookupTranslator(BASIC_UNESCAPE),
                        new LookupTranslator(withoutNbsp),
                        new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE),
                        new NumericEntityUnescaper()
                );
        String unescapedXhtml = translator.translate(escapedText);
        return unescapedXhtml;
    }
}
