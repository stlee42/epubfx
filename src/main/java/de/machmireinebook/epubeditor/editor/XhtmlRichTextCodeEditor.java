package de.machmireinebook.epubeditor.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.model.StyledSegment;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * Created by Michail Jungierek, Acando GmbH on 07.05.2018
 */
public class XhtmlRichTextCodeEditor extends AbstractRichTextCodeEditor
{
    private static final Logger logger = Logger.getLogger(XHTMLCodeEditor.class);

    private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENTOPEN>(<\\h*)(\\w+)([^<>]*)(\\h*/?>))|(?<ELEMENTCLOSE>(</?\\h*)(\\w+)([^<>]*)(\\h*>))"
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
        boolean isTagFound(EditorToken token);
    }

    public static class BlockTagInspector implements TagInspector
    {
        public boolean isTagFound(EditorToken token)
        {
            String type = token.getType();
            if ("tag".equals(type))
            {
                String content = token.getContent();
                if ("h1".equals(content) || "h2".equals(content) || "h3".equals(content) || "h4".equals(content)
                        || "h5".equals(content) || "h6".equals(content) || "p".equals(content) || "div".equals(content))
                {
                    return true;
                }
            }
            return false;
        }
    }

    public XhtmlRichTextCodeEditor(MediaType mediaType)
    {
        super();
        this.mediaType = mediaType;
        String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/xhtml.css").toExternalForm();
        setStyleSheet(stylesheet);
        setWrapText(true);
    }

    @Override
    protected StyleSpans<? extends Collection<String>> computeHighlighting(String text)
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
                    spansBuilder.add(Collections.singleton("anytag"), matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET));
                    if(!attributesText.isEmpty()) {
                        lastKwEnd = 0;
                        Matcher amatcher = ATTRIBUTES.matcher(attributesText);
                        while(amatcher.find()) {
                            spansBuilder.add(Collections.emptyList(), amatcher.start() - lastKwEnd);
                            spansBuilder.add(Collections.singleton("attribute"), amatcher.end(GROUP_ATTRIBUTE_NAME) - amatcher.start(GROUP_ATTRIBUTE_NAME));
                            spansBuilder.add(Collections.singleton("tagmark-equal"), amatcher.end(GROUP_EQUAL_SYMBOL) - amatcher.end(GROUP_ATTRIBUTE_NAME));
                            spansBuilder.add(Collections.singleton("avalue"), amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
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
                    spansBuilder.add(Collections.singleton("anytag"), matcher.end(8) - matcher.end(7));
                    spansBuilder.add(Collections.singleton("tag-close-close"), matcher.end(10) - matcher.start(10));
                }
            }
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
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

    public XMLTagPair findSurroundingTags(TagInspector inspector)
    {
        XMLTagPair pair;
        Paragraph<Collection<String>, String, Collection<String>> paragraph = getCurrentParagraph();

        for (StyledSegment<String, Collection<String>> segment : paragraph.getStyledSegments())
        {
            Collection<String> styles = segment.getStyle();
            for (String style : styles)
            {
                if ("tag-open-open".equals(style))
                {
                    logger.info("found open tag");
                }
                else if ("tag-close-open".equals(style))
                {
                    logger.info("found closing tag");
                }
            }
        }

       /* pair = new XMLTagPair(openTagBegin, openTagEnd, closeTagBegin, closeTagEnd, lastBracketBegin);
        pair.setTagName(tagName);
        return pair;*/

        return null;
    }

    private boolean isTokenClosingTag(int line, EditorToken token)
    {
        /*
        EditorPosition pos = new EditorPosition(line, token.getStart() - 1);
        EditorToken tokenBefore = getTokenAt(pos);
        return "</".equals(tokenBefore.getContent()) && "tag bracket".equals(tokenBefore.getType());
        */
        return false;
    }
}
