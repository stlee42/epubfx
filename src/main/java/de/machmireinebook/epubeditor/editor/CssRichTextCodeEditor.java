package de.machmireinebook.epubeditor.editor;

import java.util.Collection;
import java.util.Collections;

import de.machmireinebook.epubeditor.editor.regex.CssRegexLexer;
import de.machmireinebook.epubeditor.editor.regex.RegexToken;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;

import org.apache.log4j.Logger;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * User: mjungierek
 * Date: 24.12.2014
 * Time: 01:11
 */
public class CssRichTextCodeEditor extends AbstractRichTextCodeEditor
{
    private static final Logger logger = Logger.getLogger(CssRichTextCodeEditor.class);
    protected final CssRegexLexer cssRegexLexer = new CssRegexLexer();

    public CssRichTextCodeEditor()
    {
        super();
        String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/css.css").toExternalForm();
        addStyleSheet(stylesheet);
        setWrapText(true);
    }

    @Override
    public MediaType getMediaType()
    {
        return MediaType.CSS;
    }

    @Override
    public void spellCheck()
    {
    }

    protected StyleSpans<Collection<String>> computeHighlighting(String text)
    {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        cssRegexLexer.setContent(text);

        int lastKwEnd = 0;
        RegexToken token;

        while ((token = cssRegexLexer.nextToken()) != null) {
            spansBuilder.add(Collections.emptyList(), token.getStart() - lastKwEnd);
            spansBuilder.add(Collections.singleton(token.getCode().toLowerCase()), token.getEnd() - token.getStart());
            lastKwEnd = token.getEnd();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
