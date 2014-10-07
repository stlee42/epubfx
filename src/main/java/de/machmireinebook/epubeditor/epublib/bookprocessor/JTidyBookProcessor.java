package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.tidy.Tidy;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 00:26
 */
public class JTidyBookProcessor extends HtmlBookProcessor
{
    public static final Logger logger = Logger.getLogger(JTidyBookProcessor.class);
    private Tidy tidy;

    public JTidyBookProcessor()
    {
        tidy = new Tidy();
//        tidy.setConfigurationFromFile(JTidyBookProcessor.class.getResource("/jtidy.properties").getFile());
        tidy.setSpaces(2);
        tidy.setIndentContent(true);
        tidy.setSmartIndent(true);
        tidy.setXHTML(true);
        tidy.setQuoteMarks(false);
        tidy.setQuoteAmpersand(true);
        tidy.setDropEmptyParas(false);
        tidy.setTidyMark(false);
        tidy.setJoinClasses(true);
        tidy.setJoinStyles(true);
        tidy.setWraplen(0);
        tidy.setDropProprietaryAttributes(true);
        tidy.setEscapeCdata(true);
        Properties props = new Properties();
        props.put("new-blocklevel-tags", "svg image  altGlyph altGlyphDef altGlyphItem animate animateColor animateMotion animateTransform circle clipPath color-profile cursor defs desc ellipse feBlend feColorMatrix feComponentTransfer feComposite feConvolveMatrix feDiffuseLighting feDisplacementMap feDistantLight feFlood feFuncA feFuncB feFuncG feFuncR feGaussianBlur feImage feMerge feMergeNode feMorphology feOffset fePointLight feSpecularLighting feSpotLight feTile feTurbulence filter font font-face font-face-format font-face-name font-face-src font-face-uri foreignObject g glyph glyphRef hkern image line linearGradient marker mask metadata missing-glyph mpath path pattern polygon polyline radialGradient rect script set stop style svg switch symbol text textPath title tref tspan use view vkern");
        tidy.getConfiguration().addProps(props);
    }

    @Override
    protected byte[] processHtml(Resource resource, Book book, String encoding) throws IOException
    {
        logger.info("processing " + resource + " with tidy");
        tidy.setInputEncoding(encoding);
        tidy.setOutputEncoding("UTF-8");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        tidy.parse(resource.getInputStream(), bos);
        String doc = bos.toString("UTF-8");
        //fehlerhaftes xhtml für epub wieder reparieren
        doc = StringUtils.replace(doc, "/*<![CDATA[*/", "");
        doc = StringUtils.replace(doc, "/*]]>*/", "");
        doc = StringUtils.replace(doc, "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
        return doc.getBytes("UTF-8");
    }

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }

}
