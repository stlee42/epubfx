package de.machmireinebook.epubeditor.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.IndexRange;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

import org.apache.log4j.Logger;

import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

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

    private final MediaType mediaType;

    @FunctionalInterface
    public interface TagInspector
    {
        boolean isTagFound(String tagName);
    }

    public static class BlockTagInspector implements TagInspector
    {
        public boolean isTagFound(String tagName)
        {
            return "h1".equals(tagName) || "h2".equals(tagName) || "h3".equals(tagName) || "h4".equals(tagName)
                    || "h5".equals(tagName) || "h6".equals(tagName) || "p".equals(tagName) || "div".equals(tagName);
        }
    }

    public XhtmlRichTextCodeEditor(MediaType mediaType)
    {
        super();
        this.mediaType = mediaType;
        String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/xhtml.css").toExternalForm();
        addStyleSheet(stylesheet);
        setWrapText(true);
    }

    @Override
    protected void computeHighlighting(String text)
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
                            spansBuilder.add(Collections.singleton("spell-check-error"), amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
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
        getCodeArea().setStyleSpans(0, spansBuilder.create());
    }


    @Override
    public MediaType getMediaType()
    {
        return mediaType;
    }

    @Override
    public void spellCheck()
    {
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

        int openTagNameStartOffset = -1;
        int openTagNameEndOffset = -1;
        int closeTagNameStartOffset = -1;
        int closeTagNameEndOffset = -1;

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
