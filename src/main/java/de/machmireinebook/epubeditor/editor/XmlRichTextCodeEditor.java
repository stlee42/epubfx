package de.machmireinebook.epubeditor.editor;

import javax.inject.Named;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * @author Michail Jungierek, CGI
 */
@Named("xmlRichTextCodeEditor")
@XmlCodeEditor
public class XmlRichTextCodeEditor extends XhtmlRichTextCodeEditor {

    public XmlRichTextCodeEditor() {
        super();
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.XML;
    }
}
