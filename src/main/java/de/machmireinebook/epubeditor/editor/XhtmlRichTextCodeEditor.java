package de.machmireinebook.epubeditor.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import org.apache.log4j.Logger;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.Nodes;
import org.languagetool.JLanguageTool;
import org.languagetool.ResultCache;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.CategoryIds;
import org.languagetool.rules.RuleMatch;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

/**
 * Created by Michail Jungierek
 */
public class XhtmlRichTextCodeEditor extends AbstractRichTextCodeEditor
{
    private static final Logger logger = Logger.getLogger(XhtmlRichTextCodeEditor.class);

    private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENTOPEN>(<\\h*)(\\w+:?\\w*)([^<>]*)(\\h*/?>))|(?<ELEMENTCLOSE>(</?\\h*)(\\w+:?\\w*)([^<>]*)(\\h*>))"
            + "|(?<COMMENT><!--[^<>]+-->)");
    private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");

    private static final int GROUP_OPEN_BRACKET = 2;
    private static final int GROUP_ELEMENT_NAME = 3;
    private static final int GROUP_ATTRIBUTES_SECTION = 4;
    private static final int GROUP_CLOSE_BRACKET = 5;
    private static final int GROUP_ATTRIBUTE_NAME = 1;
    private static final int GROUP_EQUAL_SYMBOL = 2;
    private static final int GROUP_ATTRIBUTE_VALUE = 3;

    private static final String SPELLCHECK_CLASS_NAME = "spell-check-error";
    private static final String SPELLCHECK_HINT_CLASS_NAME = "spell-check-hint";
    private static final String SPELLCHECK_OTHER_CLASS_NAME = "spell-check-other";

    private JLanguageTool langTool;
    private ResultCache cache;

    @FunctionalInterface
    public interface TagInspector
    {
        boolean isTagFound(String tagName);
    }

    public static class BlockTagInspector implements TagInspector
    {
        private List<String> blockTags = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "center");
        
        public boolean isTagFound(String tagName)
        {
            return blockTags.contains(tagName);
        }
    }

    public XhtmlRichTextCodeEditor(MediaType mediaType)
    {
        super();
        this.mediaType = mediaType;
        try {
            String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/xhtml.css").toExternalForm();
            addStyleSheet(stylesheet);
        } catch (Exception e) {
            logger.error("error while loading xhtml.css", e);
        }
        setWrapText(true);

        //setup special keys
        CodeArea codeArea = getCodeArea();
        Nodes.addInputMap(codeArea, consume(keyPressed(KeyCode.PERIOD, KeyCombination.CONTROL_DOWN), this::completeTag));
        Nodes.addInputMap(codeArea, consume(keyPressed(KeyCode.SPACE, KeyCombination.CONTROL_DOWN), this::removeTags));
        /*codeArea.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.SPACE) {
                logger.debug("Ctrl-SPACE Pressed");
                removeTags();
                event.consume();
            }
        });*/

    }

    private void removeTags(KeyEvent event) {
        logger.info("remove tags from selection");
        int selectionStartIndex = getSelectedRange().getStart();
        String selectedText = getSelection();
        //regex ungreedy and sing line, that only tags matches and line breaks are included in . token
        String replacement = selectedText.replaceAll("(?s)<(.*?)>", "");
        replacement = replacement.replaceAll("(?s)</(.*?)>", "");
        replacement = replacement.replaceAll("(?s)<(.*?)/>", "");
        replaceSelection(replacement);
        select(selectionStartIndex, selectionStartIndex + replacement.length());
    }

    private void completeTag(KeyEvent event) {
        logger.info("insert closing tag for last opened tag");
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

    @Override
    public List<RuleMatch> spellCheck() {
        List<RuleMatch> matches = Collections.emptyList();
        if (mediaType == MediaType.XHTML) {
            String text = getCodeArea().getText();
            AnnotatedText annotatedText = makeAnnotatedText(text);
            if (cache == null) {
                cache = new ResultCache(10000, 1, TimeUnit.HOURS);
            }

            if (langTool == null) {
                langTool = new JLanguageTool(preferencesManager.getLanguageSpellSelection().getLanguage(), null, cache);
                langTool.disableCategory(CategoryIds.TYPOGRAPHY);
                langTool.disableCategory(CategoryIds.CONFUSED_WORDS);
                langTool.disableCategory(CategoryIds.REDUNDANCY);
                langTool.disableCategory(CategoryIds.STYLE);
                langTool.disableCategory(CategoryIds.GENDER_NEUTRALITY);
                langTool.disableCategory(CategoryIds.SEMANTICS);
                langTool.disableCategory(CategoryIds.COLLOQUIALISMS);
                langTool.disableCategory(CategoryIds.WIKIPEDIA);
                langTool.disableCategory(CategoryIds.BARBARISM);
                langTool.disableCategory(CategoryIds.MISC);
            }

            try {
                matches = langTool.check(annotatedText);
            }
            catch (IOException e) {
                logger.error("can't spell check text", e);
            }
        }
        return matches;
    }

    private AnnotatedText makeAnnotatedText(String xhtml) {
        AnnotatedTextBuilder builder = new AnnotatedTextBuilder();
        StringTokenizer tokenizer = new StringTokenizer(xhtml, "<>", true);
        boolean inMarkup = false;
        while (tokenizer.hasMoreTokens()) {
            String part = tokenizer.nextToken();
            if (part.startsWith("<")) {
                builder.addMarkup(part);
                inMarkup = true;
            } else if (part.startsWith(">")) {
                inMarkup = false;
                builder.addMarkup(part);
            } else {
                if (inMarkup) {
                    builder.addMarkup(part);
                } else {
                    builder.addText(part);
                }
            }
        }
        return builder.build();
    }
    @Override
    public void applySpellCheckResults(List<RuleMatch> matches) {
        for (RuleMatch match : matches) {
            int start = match.getFromPos();
            int end = match.getToPos();
            if (start > -1) {
                int currentPosition = start;
                end = Math.min(end, getCodeArea().getLength());
                StyleSpans<Collection<String>> currentStyles = getCodeArea().getStyleSpans(start, end);
                for (StyleSpan<Collection<String>> currentStyle : currentStyles) {
                    List<String> currentStyleNames = new ArrayList<>(currentStyle.getStyle());
                    String cssClass;
                    switch (match.getType().name()) {
                        case "Other"    : cssClass = SPELLCHECK_OTHER_CLASS_NAME;
                                          break;
                        case "Hint"     : cssClass = SPELLCHECK_HINT_CLASS_NAME;
                                          break;
                        default         : cssClass = SPELLCHECK_CLASS_NAME;
                    }
                    Collections.addAll(currentStyleNames, cssClass);
                    getCodeArea().setStyle(currentPosition, currentPosition + currentStyle.getLength(), currentStyleNames);
                    currentPosition = currentPosition + currentStyle.getLength();
                }
            }
        }
    }

    public Optional<XMLTagPair> findSurroundingTags(TagInspector inspector)
    {
        XMLTagPair pair = null;
        int paragraphIndex = getCurrentParagraphIndex();
        Paragraph<Collection<String>, String, Collection<String>> paragraph = getCurrentParagraph();
        boolean foundOpenOpen = false;
        boolean foundOpenClose = false;
        boolean foundCloseOpen = false;
        boolean foundCloseClose = false;

        int openTagEnd = -1;

        int openTagNameStartOffset;
        int openTagNameEndOffset;
        int closeTagNameStartOffset;
        int closeTagNameEndOffset;

        int openTagNameStartPosition = -1;
        int openTagNameEndPosition = -1;
        int openTagEndPosition = -1;
        int closeTagNameStartPosition = -1;
        int closeTagNameEndPosition = -1;

        String openTagName = "open";
        String closeTagName = "close";

        boolean bothTagsNotFound = true;
        boolean foundOpenTag = false;
        boolean foundCloseTag = false;

        while (bothTagsNotFound)
        {
            StyleSpans<Collection<String>> spans = paragraph.getStyleSpans();

            int offset = 0;
            for (StyleSpan<Collection<String>> span : spans)
            {
                Collection<String> styles = span.getStyle();
                for (String style : styles)
                {
                    if ("tag-open-open".equals(style))
                    {
                        logger.info("found open tag");
                        foundOpenOpen = true;
                    }
                    else if ("tag-open-close".equals(style))
                    {
                        logger.info("found closing tag");
                        openTagEnd = offset + span.getLength();
                        foundOpenClose = true;
                    }
                    else if ("tag-close-open".equals(style))
                    {
                        logger.info("found open tag");
                        foundCloseOpen = true;
                    }
                    else if ("tag-close-close".equals(style))
                    {
                        logger.info("found closing tag");
                        foundCloseClose = true;
                    }
                }
                offset += span.getLength();
            }
            //nochmals drüberiterieren um tagnamen zu finden
            offset = 0;
            for (StyleSpan<Collection<String>> span : spans)
            {
                Collection<String> styles = span.getStyle();
                for (String style : styles)
                {
                    if (foundOpenOpen && foundOpenClose &&  "opentag".equals(style) && !foundOpenTag)
                    {
                        openTagNameStartOffset = offset;
                        openTagNameEndOffset = offset + span.getLength();
                        openTagName = paragraph.substring(openTagNameStartOffset, openTagNameEndOffset);
                        if (inspector.isTagFound(openTagName))
                        {
                            foundOpenTag = true;
                            openTagNameStartPosition = getAbsolutePosition(paragraphIndex, openTagNameStartOffset);
                            openTagNameEndPosition = getAbsolutePosition(paragraphIndex, openTagNameEndOffset);
                            openTagEndPosition = getAbsolutePosition(paragraphIndex, openTagEnd);
                        }
                    }
                    if (foundCloseOpen && foundCloseClose &&  "closetag".equals(style) && !foundCloseTag)
                    {
                        closeTagNameStartOffset = offset;
                        closeTagNameEndOffset = offset + span.getLength();
                        closeTagName = paragraph.substring(closeTagNameStartOffset, closeTagNameEndOffset);
                        if (inspector.isTagFound(closeTagName))
                        {
                            foundCloseTag = true;
                            closeTagNameStartPosition = getAbsolutePosition(paragraphIndex, closeTagNameStartOffset);
                            closeTagNameEndPosition = getAbsolutePosition(paragraphIndex, closeTagNameEndOffset);
                        }
                    }
                }
                offset += span.getLength();
            }

            if (foundOpenTag && foundCloseTag && openTagName.equals(closeTagName))
            {
                bothTagsNotFound = false;
                pair = new XMLTagPair(new IndexRange(openTagNameStartPosition, openTagNameEndPosition), new IndexRange(closeTagNameStartPosition, closeTagNameEndPosition), openTagEndPosition);
                pair.setTagName(openTagName);
            }
            else if (foundOpenTag)
            {
                paragraphIndex++;
                if (paragraphIndex >= getNumberParagraphs()) //reaching the end
                {
                    break;
                }
                paragraph = getParagraph(paragraphIndex);
            }
            else if (foundCloseTag)
            {
                paragraphIndex--;
                if (paragraphIndex < 0)
                {
                    break;
                }
                paragraph = getParagraph(paragraphIndex);
            }
            else
            {
                //zuerst nach vorn
                paragraphIndex++;
                if (paragraphIndex >= getNumberParagraphs()) //reaching the end
                {
                    break;
                }
                paragraph = getParagraph(paragraphIndex);
            }
        }

        return Optional.ofNullable(pair);
    }
}
