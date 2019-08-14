package de.machmireinebook.epubeditor.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Named;

import org.apache.commons.io.IOUtils;

import org.jdom2.Document;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * Created by Michail Jungierek
 */
@Named
public class TemplateManager
{
    public Document getNavTemplate() throws IOException, JDOMException
    {
        String template = IOUtils.toString(getClass().getResourceAsStream("/epub/nav.xhtml"), StandardCharsets.UTF_8);
        return XHTMLUtils.parseXHTMLDocument(template);
    }
}
