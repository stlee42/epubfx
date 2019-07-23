package de.machmireinebook.epubeditor.editor;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.geometry.Point2D;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import org.controlsfx.control.PopOver;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.Nodes;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.util.IteratorIterable;
import org.languagetool.JLanguageTool;
import org.languagetool.ResultCache;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.CategoryIds;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.RuleMatch;
import org.reactfx.EventStreams;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.manager.ElementPosition;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

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
    private static final Pattern INDENT = Pattern.compile("style\\s*=\\s*\"(.*)margin-left:([-.0-9]*)([^;]*)(;?)(.*)\\s*\"", Pattern.DOTALL);

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
    private static final String SPELLCHECK_INDIVIDUAL_CLASS_PREFIX = "spell-check-match-";

    private JLanguageTool langTool;
    private ResultCache cache;
    private Map<String, RuleMatch> matchesToText = new HashMap<>();
    private PopOver popOver = new PopOver();
    private StyleClassedTextArea popOverTextArea = new StyleClassedTextArea();
    private Point2D popOverOpeningPosition;

    @FunctionalInterface
    public interface TagInspector
    {
        boolean isTagFound(String tagName);
    }

    public static class HtmlLayoutTagInspector implements TagInspector
    {
        private List<String> htmlLayoutTags = Arrays.asList("body", "h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "center", "table",
                "th", "td", "tr", "tbody", "thead", "tfoot", "figure", "figcaption", "caption", "dt", "dd", "q", "blockquote",
                "section", "nav", "aside", "address", "main", "code", "map", "svg", "object", "video", "audio",
                "ul", "ol", "li", "pre", "img", "a", "hr", "cite");

        public boolean isTagFound(String tagName)
        {
            return htmlLayoutTags.contains(tagName);
        }
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

        //configure popover for spellcheck result messsages
        codeArea.setMouseOverTextDelay(Duration.ofMillis(500));
        popOver.setContentNode(popOverTextArea);
        popOver.setTitle("Spell Check Result");
        popOver.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        popOverTextArea.setUseInitialStyleForInsertion(true);
        popOverTextArea.setEditable(false);
        popOverTextArea.getStylesheets().add(getClass().getResource("/editor-css/spellcheck-popover.css").toExternalForm());

        logger.info("creating spellcheck cache");
        cache = new ResultCache(10000, 1, TimeUnit.HOURS);
        logger.info("spellcheck cache created, creating langTool");
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
        logger.info("langTool created");

        EventStreams.eventsOf(codeArea, MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN)
                .successionEnds(Duration.ofMillis(500))
                .subscribe(event -> {
                    if (popOver.isShowing()) { //already visible do nothing
                        logger.info("already visible do nothing");
                        return;
                    }
                    int index = event.getCharacterIndex();
                    popOverTextArea.clear();
                    StyleSpans<Collection<String>> currentStyles = getCodeArea().getStyleSpans(index, index);
                    for (StyleSpan<Collection<String>> currentStyle : currentStyles) {
                        List<String> currentStyleNames = new ArrayList<>(currentStyle.getStyle());
                        for (String currentStyleName : currentStyleNames) {
                            if (currentStyleName.contains(SPELLCHECK_INDIVIDUAL_CLASS_PREFIX)) {
                                logger.info("mouse is over text with spellcheck match " + currentStyleName);
                                RuleMatch match = matchesToText.get(currentStyleName);
                                logger.info("match " + match);

                                String message = match.getMessage();
                                popOverTextArea.appendText(message);

                                List<String> examples = match.getRule().getIncorrectExamples()
                                        .stream()
                                        .map(IncorrectExample::toString)
                                        .collect(Collectors.toList());
                                popOverTextArea.appendText(StringUtils.join(examples, "\n"));

                                List<String> suggestions = match.getSuggestedReplacements();
                                popOverTextArea.appendText(StringUtils.join(suggestions, "\n"));
                                //dont use any x and y values of popover directly (like anchorY or anchorY), because its includes any
                                // unknown offsets, bounds and so on
                                popOverOpeningPosition = event.getScreenPosition();
                                popOver.show(codeArea, event.getScreenPosition().getX(), event.getScreenPosition().getY());
                            }
                        }
                    }
                });
        EventStreams.eventsOf(codeArea, MouseEvent.MOUSE_MOVED)
                .successionEnds(Duration.ofMillis(500))
                .subscribe(event -> {
                                if (popOver.isShowing()) { //do only anything if it's showing
                                    double popOverX = popOverOpeningPosition.getX();
                                    double popOverY = popOverOpeningPosition.getY();
                                    if (Math.abs(popOverX - event.getX()) > 20 || Math.abs(popOverY - event.getY()) > 20) {
                                        popOver.hide();
                                        popOverTextArea.clear();
                                    }
                                }
                            });
    }

    public void surroundParagraphWithTag(String tagName) {
        Optional<XMLTagPair> optional = findSurroundingTags(new XhtmlRichTextCodeEditor.BlockTagInspector());
        optional.ifPresent(pair -> {
            logger.info("found xml block tag " + pair.getTagName());
            // replcae the closing tag first, otherwise the end index of paragraph is changed
            replaceRange(pair.getCloseTagRange(), tagName);
            replaceRange(pair.getOpenTagRange(), tagName);
        });
    }

    private void removeTags(KeyEvent event) {
        logger.info("remove tags from selection");
        int selectionStartIndex = getSelectedRange().getStart();
        String selectedText = getSelection();
        //regex ungreedy and single line, that only tags matches and line breaks are included in . token
        String replacement = selectedText.replaceAll("(?s)<(.*?)>", "");
        replacement = replacement.replaceAll("(?s)</(.*?)>", "");
        replacement = replacement.replaceAll("(?s)<(.*?)/>", "");
        replaceSelection(replacement);
        select(selectionStartIndex, selectionStartIndex + replacement.length());
    }

    private void completeTag(KeyEvent event) {
        logger.info("insert closing tag for last opened tag");
        String text = getCodeArea().subDocument(0, getAbsoluteCursorPosition()).getText();
        Matcher matcher = XML_TAG.matcher(text);
        while (matcher.find()) {
            if(matcher.group("COMMENT") != null) {

            } else if(matcher.group("ELEMENTOPEN") != null) {

            } else if(matcher.group("ELEMENTCLOSE") != null) {

            }
        }
    }

    public void increaseIndent() {
        Optional<XMLTagPair> optional = findSurroundingTags(new XhtmlRichTextCodeEditor.BlockTagInspector());
        optional.ifPresent(pair -> {
            logger.info("found xml block tag " + pair.getTagName());
            String tagAtttributes = getRange(new IndexRange(pair.getOpenTagRange().getEnd(), pair.getTagAttributesEnd()));

            Matcher regexMatcher = INDENT.matcher(tagAtttributes);
            if (regexMatcher.find()) {
                String currentIndentStr = regexMatcher.group(2);
                int currentIndent = NumberUtils.toInt(currentIndentStr, 0);
                String currentUnit = regexMatcher.group(3);
                switch (currentUnit) {
                    case "%":
                    case "rem":
                    case "em":
                        currentIndent++;
                        break;
                    case "px":
                        currentIndent = currentIndent + 10;
                        break;
                }
                insertStyle("margin-left", currentIndent + currentUnit);
            }
            else {
                insertStyle("margin-left", "5%");
            }
        });
    }

    public void decreaseIndent() {
        Optional<XMLTagPair> optional = findSurroundingTags(new XhtmlRichTextCodeEditor.BlockTagInspector());
        optional.ifPresent(pair -> {
            logger.info("found xml block tag " + pair.getTagName());
            String tagAtttributes = getRange(pair.getOpenTagRange().getEnd(), pair.getTagAttributesEnd());

            Matcher regexMatcher = INDENT.matcher(tagAtttributes);
            if (regexMatcher.find()) {
                String currentIndentStr = regexMatcher.group(2);
                int currentIndent = NumberUtils.toInt(currentIndentStr, 0);
                String currentUnit = regexMatcher.group(3);
                switch (currentUnit) {
                    case "%":
                    case "rem":
                    case "em":
                        currentIndent--;
                        break;
                    case "px":
                        currentIndent = currentIndent - 10;
                        break;
                }
                insertStyle("margin-left", currentIndent + currentUnit);
            }
            else {
                insertStyle("margin-left", "-5%");
            }
        });
    }

    public void insertStyle(String styleName, String value) {
        Optional<XMLTagPair> optional = findSurroundingTags(new XhtmlRichTextCodeEditor.BlockTagInspector());
        optional.ifPresent(pair -> {
            logger.info("found xml block tag " + pair.getTagName());
            String tagAtttributes = getRange(new IndexRange(pair.getOpenTagRange().getEnd(), pair.getTagAttributesEnd()));
            if (tagAtttributes.contains("style=")) //wenn bereits styles vorhanden, dann diese modifizieren
            {
                if (tagAtttributes.contains(styleName)) //replace old value of style with new one
                {
                    tagAtttributes = tagAtttributes.replaceAll("style\\s*=\\s*\"(.*)" + styleName + ":([^;]*)(;?)(.*)\\s*\"",
                            "style=\"$1" + styleName + ":" + value + "$3$4\"");
                }
                else //otherwise append style
                {
                    tagAtttributes = tagAtttributes.replaceAll("style\\s*=\\s*\"(.*)\"",
                            "style=\"$1;" + styleName + ":" + value + "\"");
                }
                replaceRange(new IndexRange(pair.getOpenTagRange().getEnd(), pair.getTagAttributesEnd()), tagAtttributes);
            }
            else {
                int pos = pair.getOpenTagRange().getEnd();
                insertAt(pos, " style=\"" + styleName + ":" + value + "\"");
            }
        });
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
        logger.info("starting spellcheck");
        List<RuleMatch> matches = Collections.emptyList();
        if (mediaType == MediaType.XHTML) {
            String text = getCodeArea().getText();
            AnnotatedText annotatedText = makeAnnotatedText(text);

            try {
                matches = langTool.check(annotatedText);
            }
            catch (IOException e) {
                logger.error("can't spell check text", e);
            }
        }
        logger.info("spellcheck finished");
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
        logger.info("applying spellcheck result");
        matchesToText.clear();
        int id = 1;
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
                    String individualCssClass = SPELLCHECK_INDIVIDUAL_CLASS_PREFIX + id++;
                    matchesToText.put(individualCssClass, match);
                    Collections.addAll(currentStyleNames, cssClass, individualCssClass);
                    getCodeArea().setStyle(currentPosition, currentPosition + currentStyle.getLength(), currentStyleNames);
                    currentPosition = currentPosition + currentStyle.getLength();
                }
            }
        }
    }

    public boolean isInsertablePosition() {
        Optional<XMLTagPair> optional = findSurroundingTags(tagName -> "head".equals(tagName) || "body".equals(tagName)
                || "html".equals(tagName));
        return !(optional.isEmpty() || "head".equals(optional.get().getTagName())
                || "html".equals(optional.get().getTagName())
                || StringUtils.isEmpty(optional.get().getTagName()));
    }


    public void scrollTo(Deque<ElementPosition> nodeChain) {
        String code = getCode();
        LocatedJDOMFactory factory = new LocatedJDOMFactory();
        try {
            Document document = XHTMLUtils.parseXHTMLDocument(code, factory);
            Element currentElement = document.getRootElement();
            ElementPosition currentElementPosition = nodeChain.pop();
            while (currentElementPosition != null) {
                IteratorIterable<Element> children;
                if (StringUtils.isNotEmpty(currentElementPosition.getNamespaceUri())) {
                    List<Namespace> namespaces = currentElement.getNamespacesInScope();
                    Namespace currentNamespace = null;
                    for (Namespace namespace : namespaces) {
                        if (namespace.getURI().equals(currentElementPosition.getNamespaceUri())) {
                            currentNamespace = namespace;
                            break;
                        }
                    }
                    Filter<Element> filter = Filters.element(currentElementPosition.getNodeName(), currentNamespace);
                    children = currentElement.getDescendants(filter);
                }
                else {
                    Filter<org.jdom2.Element> filter = Filters.element(currentElementPosition.getNodeName());
                    children = currentElement.getDescendants(filter);
                }

                int currentNumber = 0;
                for (org.jdom2.Element child : children) {
                    if (currentNumber == currentElementPosition.getPosition()) {
                        currentElement = child;
                        break;
                    }
                    currentNumber++;
                }

                try {
                    currentElementPosition = nodeChain.pop();
                }
                catch (NoSuchElementException e) {
                    logger.info("no more element in node chain");
                    currentElementPosition = null;
                }
            }

            LocatedElement locatedElement = (LocatedElement) currentElement;
            EditorPosition pos = new EditorPosition(locatedElement.getLine(), locatedElement.getColumn());
            logger.info("pos for scrolling to is " + pos.toJson());
            scrollTo(pos);
        }
        catch (IOException | JDOMException e) {
            logger.error("", e);
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
        int openTagParagraphIndex = -1;
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
                            openTagParagraphIndex = paragraphIndex;
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
                pair = new XMLTagPair(new IndexRange(openTagNameStartPosition, openTagNameEndPosition), new IndexRange(closeTagNameStartPosition, closeTagNameEndPosition), openTagEndPosition, openTagParagraphIndex);
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

    public List<Content> getHeadContent() throws IOException, JDOMException {
        Document doc = XHTMLUtils.parseXHTMLDocument(getCode());
        Element root = doc.getRootElement();
        List<Content> contentList = new ArrayList<>();
        if (root != null) {
            Element headElement = root.getChild("head", Constants.NAMESPACE_XHTML);
            if (headElement != null) {
                List<Content> contents = headElement.getContent();
                contentList.addAll(contents);
            }
        }
        //erst ausserhalb der Schleife detachen
        for (Content content : contentList) {
            content.detach();
        }
        return contentList;
    }
}
