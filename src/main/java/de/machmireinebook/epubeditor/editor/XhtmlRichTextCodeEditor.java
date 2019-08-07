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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import org.controlsfx.control.PopOver;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
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
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.RuleMatch;
import org.reactfx.EventStreams;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.manager.ElementPosition;
import de.machmireinebook.epubeditor.manager.SpellcheckManager;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * Created by Michail Jungierek
 */
@Named("xhtmlRichTextCodeEditor")
@XhtmlCodeEditor
public class XhtmlRichTextCodeEditor extends XmlRichTextCodeEditor
{
    private static final Logger logger = Logger.getLogger(XhtmlRichTextCodeEditor.class);

    private static final Pattern INDENT = Pattern.compile("style\\s*=\\s*\"(.*)margin-left:([-.0-9]*)([^;]*)(;?)(.*)\\s*\"", Pattern.DOTALL);
    private static final String SPELLCHECK_CLASS_NAME = "spell-check-error";
    private static final String SPELLCHECK_HINT_CLASS_NAME = "spell-check-hint";
    private static final String SPELLCHECK_OTHER_CLASS_NAME = "spell-check-other";
    private static final String SPELLCHECK_INDIVIDUAL_CLASS_PREFIX = "spell-check-match-";

    private static final int POPUP_TEXTFLOW_PADDING = 10;
    private static final int POPUP_WRAPPING_WIDTH = 500;
    private static final Font POPUP_NORMAL = Font.font("Source Code Pro", FontWeight.NORMAL, 12);
    private static final Font POPUP_BOLD = Font.font("Source Code Pro", FontWeight.BOLD, 12);

    private static final Pattern MESSAGE_SUGGESTION_PATTERN = Pattern.compile("(.*)<suggestion>(.*)</suggestion>(.*)", Pattern.DOTALL);
    private static final Color COLOR_SUGGESTION = Color.web("#5871d4");

    private Map<String, RuleMatch> matchesToText = new HashMap<>();
    private PopOver popOver = new PopOver();
    private Point2D popOverOpeningPosition;

    @Inject
    private SpellcheckManager spellcheckManager;

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

    public XhtmlRichTextCodeEditor() {
        super();

        try {
            //the css for xml is already added by parent class
            String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/xhtml.css").toExternalForm();
            addStyleSheet(stylesheet);
        } catch (Exception e) {
            logger.error("error while loading xhtml.css", e);
        }
        setWrapText(true);
    }

    @PostConstruct
    public void init() {
        CodeArea codeArea = getCodeArea();

        codeArea.multiPlainChanges()
                .filter(plainTextChanges -> preferencesManager.isSpellcheck())
                .successionEnds(Duration.ofMillis(10))
                .supplyTask(this::spellCheckAsync)
                .awaitLatest(codeArea.multiPlainChanges())
                .filterMap(tryTask -> {
                    if (tryTask.isSuccess()) {
                        return Optional.of(tryTask.get());
                    } else {
                        tryTask.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applySpellCheckResults);

        //configure popover for spellcheck result messsages
        codeArea.setMouseOverTextDelay(Duration.ofMillis(500));
        popOver.setTitle("Spell Check Result");

        EventStreams.eventsOf(codeArea, MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN)
                .filter(mouseEvent -> !popOver.isShowing())
                .successionEnds(Duration.ofMillis(500))
                .subscribe(event -> {
                    if (popOver.isShowing()) { //already visible do nothing
                        logger.info("already visible do nothing");
                        return;
                    }
                    int index = event.getCharacterIndex();
                    StyleSpans<Collection<String>> currentStyles = getCodeArea().getStyleSpans(index, index);
                    for (StyleSpan<Collection<String>> currentStyle : currentStyles) {
                        List<String> currentStyleNames = new ArrayList<>(currentStyle.getStyle());
                        for (String currentStyleName : currentStyleNames) {
                            if (currentStyleName.contains(SPELLCHECK_INDIVIDUAL_CLASS_PREFIX)) {

                                logger.info("mouse is over text with spellcheck match " + currentStyleName);
                                RuleMatch match = matchesToText.get(currentStyleName);
                                logger.info("match " + match);

                                String message = match.getMessage();
                                List<Text> messageTexts = getMessageTexts(message, match);

                                List<String> examples = match.getRule().getIncorrectExamples()
                                        .stream()
                                        .map(IncorrectExample::toString)
                                        .collect(Collectors.toList());
                                List<Text> exampleTexts = getExampleTexts(examples);

                                List<String> suggestions = match.getSuggestedReplacements();
                                List<Text> suggestionTexts = getSuggestionTexts(suggestions, match);

                                Separator separator = new Separator();
                                TextFlow textFlow = new TextFlow();
                                separator.prefWidthProperty().bind(Bindings.subtract(textFlow.widthProperty(), 2 * POPUP_TEXTFLOW_PADDING));
                                textFlow.setPadding(new Insets(POPUP_TEXTFLOW_PADDING));

                                List<Node> allChildren = new ArrayList<>();
                                allChildren.addAll(messageTexts);
                                allChildren.add(separator);
                                allChildren.addAll(exampleTexts);
                                allChildren.addAll(suggestionTexts);

                                popOverOpeningPosition = event.getScreenPosition();
                                textFlow.getChildren().clear();
                                textFlow.getChildren().addAll(allChildren);
                                popOver.setContentNode(textFlow);
                                // dont use any x and y values of popover directly (like anchorY or anchorY), because its includes any
                                // unknown offsets, bounds and so on
                                popOver.show(codeArea, event.getScreenPosition().getX(), event.getScreenPosition().getY());
                            }
                        }
                    }
                });
        EventStreams.eventsOf(codeArea, MouseEvent.MOUSE_MOVED)
                .filter(mouseEvent -> popOver.isShowing())
                .thenIgnoreFor(Duration.ofMillis(500))
                .subscribe(event -> {
                    if (popOver.isShowing()) { //do only anything if it's visible
                        double popOverX = popOverOpeningPosition.getX();
                        double popOverY = popOverOpeningPosition.getY();
                        if (Math.abs(popOverX - event.getX()) > 20 || Math.abs(popOverY - event.getY()) > 20) {
                            popOver.hide();
                        }
                    }
                });

    }

    private List<Text> getMessageTexts(String message, RuleMatch match) {
        List<Text> result = new ArrayList<>();
        if (StringUtils.isNotEmpty(message) && message.contains("<suggestion>")) {
            Matcher regexMatcher = MESSAGE_SUGGESTION_PATTERN.matcher(message);
            if (regexMatcher.find()) {
                String startGroup = regexMatcher.group(1);
                result.add(getNormalText(startGroup));
                if (regexMatcher.groupCount() > 1) {
                    String suggestionGroup = regexMatcher.group(2);
                    Text suggestionText = getColoredText(suggestionGroup, COLOR_SUGGESTION);
                    suggestionText.setCursor(Cursor.HAND);
                    suggestionText.setOnMouseClicked(event -> {
                        logger.info("clicked on suggestion: " + suggestionGroup);
                        getCodeArea().replaceText(match.getFromPos(), match.getToPos(), suggestionGroup);
                        popOver.hide();
                    });
                    result.add(suggestionText);
                }
                if (regexMatcher.groupCount() > 2) {
                    String endGroup = regexMatcher.group(3);
                    result.add(getNormalText(endGroup));
                }
            }
            result.add(getNormalText("\n\n"));
        } else {
            result.add(getNormalText(message + "\n\n"));
        }
        return result;
    }

    private List<Text> getExampleTexts(List<String> examples) {
        List<Text> result = new ArrayList<>();
        if (!examples.isEmpty()) {
            result.add(getBoldText("Examples\n"));
            for (String example : examples) {
                String[] splitted = StringUtils.splitByWholeSeparator(example, "<marker>");
                if (splitted.length == 1 && example.startsWith("<marker>")) {
                    String[] splitted2 = StringUtils.splitByWholeSeparator(splitted[0], "</marker>");
                    result.add(getColoredText(splitted2[0], Color.FIREBRICK));
                    result.add(getNormalText(splitted2[1]));
                } else if (splitted.length > 1) {
                    String[] splitted2 = StringUtils.splitByWholeSeparator(splitted[1], "</marker>");
                    result.add(getNormalText(splitted[0]));
                    result.add(getColoredText(splitted2[0], Color.FIREBRICK));
                    result.add(getNormalText(splitted2[1]));
                } else {
                    result.add(getNormalText(splitted[0]));
                }
                result.add(getNormalText("\n"));
            }
            result.add(getNormalText("\n"));
        }
        return result;
    }

    private List<Text> getSuggestionTexts(List<String> suggestions, RuleMatch match) {
        List<Text> result = new ArrayList<>();
        if (!suggestions.isEmpty()) {
            result.add(getBoldText("Suggestions\n"));
            for (String suggestion : suggestions) {
                Text text = getColoredText(suggestion + "\n", COLOR_SUGGESTION);
                text.setCursor(Cursor.HAND);
                text.setOnMouseClicked(event -> {
                    logger.info("clicked on suggestion: " + suggestion);
                    getCodeArea().replaceText(match.getFromPos(), match.getToPos(), suggestion);
                    popOver.hide();
                });
                result.add(text);
            }
        }
        return result;
    }

    private Text getNormalText(String textValue) {
        Text text = new Text();
        text.setWrappingWidth(POPUP_WRAPPING_WIDTH);
        text.setFont(POPUP_NORMAL);
        text.setText(textValue);
        return text;
    }

    private Text getBoldText(String textValue) {
        Text text = new Text();
        text.setWrappingWidth(POPUP_WRAPPING_WIDTH);
        text.setFont(POPUP_BOLD);
        text.setText(textValue);
        return text;
    }

    private Text getColoredText(String textValue, Color color) {
        Text text = new Text();
        text.setWrappingWidth(POPUP_WRAPPING_WIDTH);
        text.setFont(POPUP_NORMAL);
        text.setText(textValue);
        text.setFill(color);
        return text;
    }

    @Override
    public MediaType getMediaType()
    {
        return MediaType.XHTML;
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

    private Task<List<RuleMatch>> spellCheckAsync() {
        logger.info("creating spellcheck task");
        Task<List<RuleMatch>> task = new Task<>() {
            @Override
            protected List<RuleMatch> call() {
                return spellCheck();
            }
        };
        taskExecutor.execute(task);
        return task;
    }

    @Override
    public List<RuleMatch> spellCheck() {
        logger.info("starting spellcheck");
        List<RuleMatch> matches = Collections.emptyList();
        String text = getCodeArea().getText();
        AnnotatedText annotatedText = makeAnnotatedText(text);

        try {
            matches = spellcheckManager.check(annotatedText);
        }
        catch (IOException e) {
            logger.error("can't spell check text", e);
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
    public Optional<XMLTagPair> findSurroundingTags(TagInspector inspector) {
        return findSurroundingTags(inspector, false);
    }

    public Optional<XMLTagPair> findSurroundingTags(TagInspector inspector, boolean checkClosingTag)
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
                        foundOpenOpen = true;
                    }
                    else if ("tag-open-close".equals(style))
                    {
                        openTagEnd = offset + span.getLength();
                        foundOpenClose = true;
                    }
                    else if ("tag-close-open".equals(style))
                    {
                        foundCloseOpen = true;
                    }
                    else if ("tag-close-close".equals(style))
                    {
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

                        if (inspector.isTagFound(closeTagName) && (!checkClosingTag || openTagName.equals(closeTagName)))
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
