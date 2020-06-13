package de.machmireinebook.epubeditor.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import org.apache.log4j.Logger;

import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.languagetool.rules.RuleMatch;

import de.machmireinebook.epubeditor.editor.regex.CssRegexLexer;
import de.machmireinebook.epubeditor.editor.regex.RegexToken;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 24.12.2014
 * Time: 01:11
 */
public class CssRichTextCodeEditor extends AbstractRichTextCodeEditor
{
    private static final Logger logger = Logger.getLogger(CssRichTextCodeEditor.class);
    private final CssRegexLexer cssRegexLexer = new CssRegexLexer();
    private static final Background DEFAULT_BACKGROUND =
            new Background(new BackgroundFill(Color.web("#ddd"), null, null));
    private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 0.0);

    public CssRichTextCodeEditor()
    {
        super();

        String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/css.css").toExternalForm();
        addStyleSheet(stylesheet);
        setWrapText(true);
    }

    protected void createParagraphGraphicFactory() {
        IntFunction<String> format = (digits -> " %" + digits + "d ");
        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea, format);
        IntFunction<Node> colorFactory = ColorFactory.get(codeArea);
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox(
                    numberFactory.apply(line),
                    colorFactory.apply(line));
            hbox.setBackground(DEFAULT_BACKGROUND);
            hbox.setPadding(DEFAULT_INSETS);

            hbox.setAlignment(Pos.CENTER_LEFT);
            return hbox;
        };
        codeArea.setParagraphGraphicFactory(graphicFactory);
    }


    @Override
    public MediaType getMediaType()
    {
        return MediaType.CSS;
    }

    @Override
    public List<RuleMatch> spellCheck() {
        return Collections.emptyList();
    }

    @Override
    public void applySpellCheckResults(List<RuleMatch> matches) {
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
