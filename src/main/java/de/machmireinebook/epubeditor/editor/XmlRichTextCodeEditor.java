package de.machmireinebook.epubeditor.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import org.apache.log4j.Logger;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.languagetool.rules.RuleMatch;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.xhtml.XmlUtils;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

/**
 * @author Michail Jungierek, CGI
 */
@Named("xmlRichTextCodeEditor")
@XmlCodeEditor
public class XmlRichTextCodeEditor extends AbstractRichTextCodeEditor {

    private static final Logger logger = Logger.getLogger(XmlRichTextCodeEditor.class);

    private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENTOPEN>(<\\h*)(\\w+:?\\w*)([^<>]*)(\\h*/?>))" +
            "|(?<ELEMENTCLOSE>(</?\\h*)(\\w+:?\\w*)([^<>]*)(\\h*>))" +
            "|(?<ENTITY>(&(.*?);))" +
            "|(?<COMMENT><!--[^<>]+-->)");
    private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");
    private static final int GROUP_OPEN_BRACKET = 2;
    private static final int GROUP_ELEMENT_NAME = 3;
    private static final int GROUP_ATTRIBUTES_SECTION = 4;
    private static final int GROUP_CLOSE_BRACKET = 5;
    private static final int GROUP_ATTRIBUTE_NAME = 1;
    private static final int GROUP_EQUAL_SYMBOL = 2;
    private static final int GROUP_ATTRIBUTE_VALUE = 3;


    public XmlRichTextCodeEditor() {
        super();

        try {
            String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/xml.css").toExternalForm();
            addStyleSheet(stylesheet);
        } catch (Exception e) {
            logger.error("error while loading xhtml.css", e);
        }
        setWrapText(true);

        //setup special keys
        CodeArea codeArea = getCodeArea();
        Nodes.addInputMap(codeArea, InputMap.consume(keyPressed(KeyCode.PERIOD, KeyCombination.CONTROL_DOWN), this::completeTag));
        Nodes.addInputMap(codeArea, InputMap.consume(keyPressed(KeyCode.SPACE, KeyCombination.CONTROL_DOWN), this::removeTags));
    }

    @PostConstruct
    public void init() {
        //no spellcheck
    }


        @Override
    public MediaType getMediaType() {
        return MediaType.XML;
    }

    @Override
    protected StyleSpans<Collection<String>> computeHighlighting(String text)
    {
        Matcher matcher = XML_TAG.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            if(matcher.group("COMMENT") != null) {
                spansBuilder.add(Collections.singleton("comment"), matcher.end() - matcher.start());
            }
            else if(matcher.group("ENTITY") != null) {
                spansBuilder.add(Collections.singleton("entity"), matcher.end() - matcher.start());
            }
            else
            {
                if(matcher.group("ELEMENTOPEN") != null) {
                    String attributesText = matcher.group(GROUP_ATTRIBUTES_SECTION);
                    spansBuilder.add(Collections.singleton("tag-open-open"), matcher.end(GROUP_OPEN_BRACKET) - matcher.start(GROUP_OPEN_BRACKET));
                    spansBuilder.add(Collections.singleton("opentag"), matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET));
                    if(!attributesText.isEmpty()) {
                        lastKwEnd = 0;
                        Matcher amatcher = ATTRIBUTES.matcher(attributesText);
                        while(amatcher.find()) {
                            spansBuilder.add(Collections.emptyList(), amatcher.start() - lastKwEnd);
                            spansBuilder.add(Collections.singleton("attribute"), amatcher.end(GROUP_ATTRIBUTE_NAME) - amatcher.start(GROUP_ATTRIBUTE_NAME));
                            spansBuilder.add(Collections.singleton("tagmark-equal"), amatcher.end(GROUP_EQUAL_SYMBOL) - amatcher.end(GROUP_ATTRIBUTE_NAME));
                            spansBuilder.add(Collections.singleton("attribute-value"), amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
                            lastKwEnd = amatcher.end();
                        }

                        if(attributesText.length() > lastKwEnd)
                        {
                            spansBuilder.add(Collections.emptyList(), attributesText.length() - lastKwEnd);
                        }
                    }
                    lastKwEnd = matcher.end(GROUP_ATTRIBUTES_SECTION);
                    spansBuilder.add(Collections.singleton("tag-open-close"), matcher.end(GROUP_CLOSE_BRACKET) - lastKwEnd);
                }
                else if(matcher.group("ELEMENTCLOSE") != null) {
                    spansBuilder.add(Collections.singleton("tag-close-open"), matcher.end(7) - matcher.start(7));
                    spansBuilder.add(Collections.singleton("closetag"), matcher.end(8) - matcher.end(7));
                    spansBuilder.add(Collections.singleton("tag-close-close"), matcher.end(10) - matcher.start(10));
                }
            }
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void removeTags(KeyEvent event) {
        logger.info("remove tags from selection");
        int selectionStartIndex = getSelectedRange().getStart();
        String selectedText = getSelection();
        String replacement = XmlUtils.removeTags(selectedText);
        replaceSelection(replacement);
        select(selectionStartIndex, selectionStartIndex + replacement.length());
    }

    private void completeTag(KeyEvent event) {
        logger.info("insert closing tag for last opened tag");
        String text = getCodeArea().subDocument(0, getAbsoluteCursorPosition()).getText();
        Matcher matcher = XML_TAG.matcher(text);
        Stack<String> openTagsStack = new Stack<>();
        while (matcher.find()) {
            if(matcher.group("ELEMENTOPEN") != null) {
                String elementOpen = matcher.group("ELEMENTOPEN");
                openTagsStack.push(elementOpen);
            } else if(matcher.group("ELEMENTCLOSE") != null) {
                openTagsStack.pop();
            }
        }
        if (!openTagsStack.empty()) {
            insertAt(getAbsoluteCursorPosition(), openTagsStack.pop());
        }
    }


    public List<RuleMatch> spellCheck() {
        return Collections.emptyList();
    }

    @Override
    public void applySpellCheckResults(List<RuleMatch> matches) {

    }
}
