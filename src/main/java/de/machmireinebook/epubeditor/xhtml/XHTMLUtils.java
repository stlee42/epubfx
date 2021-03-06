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
import org.apache.log4j.Logger;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.EpubJDomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
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

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
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
        // " - double-quote
        BASIC_ESCAPE = Map.of("\"", "&quot;");
    }
    public static final Map<CharSequence, CharSequence> BASIC_UNESCAPE;
    static {
        BASIC_UNESCAPE = Collections.unmodifiableMap(EntityArrays.invert(BASIC_ESCAPE));
    }


    public static Resource fromHtml(Resource res, EpubVersion epubVersion)
    {
        try
        {
            String xhtml = fromHtml(res.getData(), epubVersion);
            res.setData(xhtml.getBytes(StandardCharsets.UTF_8));
            res.setInputEncoding(StandardCharsets.UTF_8.displayName());
        }
        catch (IllegalAddException e)
        {
            logger.error("", e);
        }

        return res;
    }

    public static String fromHtml(byte[] originalHtml, EpubVersion epubVersion)
    {
        String content = null;
        try
        {
            HtmlCleaner cleaner = createHtmlCleaner(epubVersion);

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
                                        HtmlCleaner cleaner2 = createHtmlCleaner(epubVersion);
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
            content = outputXHTMLDocumentAsString(jdomDocument, epubVersion);
        }
        catch (IOException | IllegalAddException e)
        {
            logger.error("", e);
        }
        return content;
    }

    public static HtmlCleaner createHtmlCleaner(EpubVersion epubVersion)
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
        if (epubVersion.isEpub2()) {
            cleanerProperties.setHtmlVersion(4);
        } else {
            cleanerProperties.setHtmlVersion(5);
        }
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

    public static String repair(String originalHtml, EpubVersion epubVersion) {
        String content = null;
        try
        {
            HtmlCleaner cleaner = createHtmlCleaner(epubVersion);

            TagNode rootNode = cleaner.clean(originalHtml);
            Document jdomDocument = new EpubJDomSerializer(cleaner.getProperties(), false).createJDom(rootNode);
            content = outputXHTMLDocumentAsString(jdomDocument, epubVersion);
        }
        catch (IllegalAddException e)
        {
            logger.error("", e);
        }
        return content;
    }

    public static String outputXHTMLDocumentAsString(Document document, EpubVersion epubVersion) {
        return new String(outputXHTMLDocument(document, epubVersion), StandardCharsets.UTF_8);
    }

    public static String outputXHTMLDocumentAsString(Document document, boolean escapeOutput, EpubVersion epubVersion) {
        return new String(outputXHTMLDocument(document, escapeOutput, epubVersion), StandardCharsets.UTF_8);
    }

    public static byte[] outputXHTMLDocument(Document document, EpubVersion epubVersion) {
        return outputXHTMLDocument(document, false, epubVersion);
    }

    public static byte[] outputXHTMLDocument(Document document, boolean escapeOutput, EpubVersion epubVersion)
    {
        Element root = document.getRootElement();
        if (root != null) {
            root.setNamespace(Constants.NAMESPACE_XHTML);
            root.addNamespaceDeclaration(Constants.NAMESPACE_XHTML);
            if (epubVersion.isEpub3()) {
                root.addNamespaceDeclaration(Constants.NAMESPACE_EPUB);
            }
            IteratorIterable<Element> elements = root.getDescendants(Filters.element());
            for (Element element : elements)
            {
                if (element.getNamespace() == null)
                {
                    element.setNamespace(Constants.NAMESPACE_XHTML);
                }
            }
        }

        if (epubVersion.isEpub2()) {
            document.setDocType(Constants.DOCTYPE_XHTML.clone());
        } else {
            document.setDocType(Constants.DOCTYPE_HTML.clone());
        }

        ;
        byte[] bytes;
        try {
            ByteArrayOutputStream baos = outputXhtml(document, escapeOutput);
            bytes = baos.toByteArray();
            bytes = unescapedHtmlWithXmlAndNbspExceptions(bytes);
        }
        catch (IOException e) {
            logger.error("", e);
            throw new XhtmlOutputException(e.getMessage());
        }
        return bytes;
    }

    public static ByteArrayOutputStream outputXhtml(Document document, boolean escapeOutput) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            xmlFormat.setExpandEmptyElements(true);
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor(escapeOutput));
            outputter.escapeElementEntities("&");
            outputter.escapeAttributeEntities("&");
            outputter.output(document, baos);
            return baos;
        }
    }

    public static String unescapedHtmlWithXmlAndNbspExceptions(String escapedText) {
        //leave nbsp untouched
        Map<CharSequence, CharSequence> withoutNbsp = new HashMap<>(EntityArrays.ISO8859_1_UNESCAPE);
        withoutNbsp.remove("&nbsp;");
        //some scripts for generating html from docx generate this (wrong typed) entity for „ (german double quote bottom)
        //for convience replace it too
        withoutNbsp.put("&dbquo;", "„");
        CharSequenceTranslator translator =
                new AggregateTranslator(
                        new LookupTranslator(BASIC_UNESCAPE),
                        new LookupTranslator(withoutNbsp),
                        new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE),
                        new NumericEntityWithoutSpacesUnescaper()
                );
        String unescapedXhtml = translator.translate(escapedText);
        return unescapedXhtml;

    }

    public static byte[] unescapedHtmlWithXmlAndNbspExceptions(byte[] escapedText) {
        String unescapedXhtml = unescapedHtmlWithXmlAndNbspExceptions(new String(escapedText, StandardCharsets.UTF_8));
        if (unescapedXhtml != null) {
            return unescapedXhtml.getBytes(StandardCharsets.UTF_8);
        }
        return new byte[]{};
    }
}
