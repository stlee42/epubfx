package de.machmireinebook.epubeditor.editor;

import java.text.BreakIterator;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 24.12.2014
 * Time: 01:11
 */
public class CssRichTextCodeEditor extends AbstractRichTextCodeEditor
{
    private static final Logger logger = Logger.getLogger(CssRichTextCodeEditor.class);

    private static final List<String> SELECTOR_DELIMITER = Arrays.asList(".", ",");

    private static final List<String> CSS_PROPERTIES_VALUES = Arrays.asList("normal", "bold", "bolder", "italic",
            "black", "grey", "gray", "firebrick",
            "transparent",
            "cover", "no-repeat",
            "true", "false",
            "px", "em", "rem", "pt",
            "monospace", "sans-serif", "serif",
            "initial", "center", "solid", "auto", "right", "left", "justify", "inherit",
            "table-cell", "table-row", "table-row-group", "button",
            "middle", "bottom", "baseline", "separate",
            "content-box", "border-box",
            "collapse", "separate",
            "visible", "none", "block", "inline-block", "flex",
            "underline", "dotted",
            "help", "pointer",
            "uppercase", "lowercase",
            "break-word", "wrap",
            "scroll", "hidden",
            "relative", "absolute", "fixed");


    private static final List<String> CSS_PROPERTIES = Arrays.asList("align-content", "align-items", "align-self", "alignment-adjust",
            "alignment-baseline", "anchor-point", "animation", "animation-delay",
            "animation-direction", "animation-duration", "animation-fill-mode",
            "animation-iteration-count", "animation-name", "animation-play-state",
            "animation-timing-function", "appearance", "azimuth", "backface-visibility",
            "background", "background-attachment", "background-clip", "background-color",
            "background-image", "background-origin", "background-position",
            "background-repeat", "background-size", "baseline-shift", "binding",
            "bleed", "bookmark-label", "bookmark-level", "bookmark-state",
            "bookmark-target", "border", "border-bottom", "border-bottom-color",
            "border-bottom-left-radius", "border-bottom-right-radius",
            "border-bottom-style", "border-bottom-width", "border-collapse",
            "border-color", "border-image", "border-image-outset",
            "border-image-repeat", "border-image-slice", "border-image-source",
            "border-image-width", "border-left", "border-left-color",
            "border-left-style", "border-left-width", "border-radius", "border-right",
            "border-right-color", "border-right-style", "border-right-width",
            "border-spacing", "border-style", "border-top", "border-top-color",
            "border-top-left-radius", "border-top-right-radius", "border-top-style",
            "border-top-width", "border-width", "bottom", "box-decoration-break",
            "box-shadow", "box-sizing", "break-after", "break-before", "break-inside",
            "caption-side", "clear", "clip", "color", "color-profile", "column-count",
            "column-fill", "column-gap", "column-rule", "column-rule-color",
            "column-rule-style", "column-rule-width", "column-span", "column-width",
            "columns", "content", "counter-increment", "counter-reset", "crop", "cue",
            "cue-after", "cue-before", "cursor", "direction", "display",
            "dominant-baseline", "drop-initial-after-adjust",
            "drop-initial-after-align", "drop-initial-before-adjust",
            "drop-initial-before-align", "drop-initial-size", "drop-initial-value",
            "elevation", "empty-cells", "fit", "fit-position", "flex", "flex-basis",
            "flex-direction", "flex-flow", "flex-grow", "flex-shrink", "flex-wrap",
            "float", "float-offset", "flow-from", "flow-into", "font", "font-feature-settings",
            "font-family", "font-kerning", "font-language-override", "font-size", "font-size-adjust",
            "font-stretch", "font-style", "font-synthesis", "font-variant",
            "font-variant-alternates", "font-variant-caps", "font-variant-east-asian",
            "font-variant-ligatures", "font-variant-numeric", "font-variant-position",
            "font-weight", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow",
            "grid-auto-position", "grid-auto-rows", "grid-column", "grid-column-end",
            "grid-column-start", "grid-row", "grid-row-end", "grid-row-start",
            "grid-template", "grid-template-areas", "grid-template-columns",
            "grid-template-rows", "hanging-punctuation", "height", "hyphens",
            "icon", "image-orientation", "image-rendering", "image-resolution",
            "inline-box-align", "justify-content", "left", "letter-spacing",
            "line-break", "line-height", "line-stacking", "line-stacking-ruby",
            "line-stacking-shift", "line-stacking-strategy", "list-style",
            "list-style-image", "list-style-position", "list-style-type", "margin",
            "margin-bottom", "margin-left", "margin-right", "margin-top",
            "marker-offset", "marks", "marquee-direction", "marquee-loop",
            "marquee-play-count", "marquee-speed", "marquee-style", "max-height",
            "max-width", "min-height", "min-width", "move-to", "nav-down", "nav-index",
            "nav-left", "nav-right", "nav-up", "object-fit", "object-position",
            "opacity", "order", "orphans", "outline",
            "outline-color", "outline-offset", "outline-style", "outline-width",
            "overflow", "overflow-style", "overflow-wrap", "overflow-x", "overflow-y",
            "padding", "padding-bottom", "padding-left", "padding-right", "padding-top",
            "page", "page-break-after", "page-break-before", "page-break-inside",
            "page-policy", "pause", "pause-after", "pause-before", "perspective",
            "perspective-origin", "pitch", "pitch-range", "play-during", "position",
            "presentation-level", "punctuation-trim", "quotes", "region-break-after",
            "region-break-before", "region-break-inside", "region-fragment",
            "rendering-intent", "resize", "rest", "rest-after", "rest-before", "richness",
            "right", "rotation", "rotation-point", "ruby-align", "ruby-overhang",
            "ruby-position", "ruby-span", "shape-image-threshold", "shape-inside", "shape-margin",
            "shape-outside", "size", "speak", "speak-as", "speak-header",
            "speak-numeral", "speak-punctuation", "speech-rate", "stress", "string-set",
            "tab-size", "table-layout", "target", "target-name", "target-new",
            "target-position", "text-align", "text-align-last", "text-decoration",
            "text-decoration-color", "text-decoration-line", "text-decoration-skip",
            "text-decoration-style", "text-emphasis", "text-emphasis-color",
            "text-emphasis-position", "text-emphasis-style", "text-height",
            "text-indent", "text-justify", "text-outline", "text-overflow", "text-shadow",
            "text-size-adjust", "text-space-collapse", "text-transform", "text-underline-position",
            "text-wrap", "top", "transform", "transform-origin", "transform-style",
            "transition", "transition-delay", "transition-duration",
            "transition-property", "transition-timing-function", "unicode-bidi",
            "vertical-align", "visibility", "voice-balance", "voice-duration",
            "voice-family", "voice-pitch", "voice-range", "voice-rate", "voice-stress",
            "voice-volume", "volume", "white-space", "widows", "width", "word-break",
            "word-spacing", "word-wrap", "z-index",
            // SVG-specific
            "clip-path", "clip-rule", "mask", "enable-background", "filter", "flood-color",
            "flood-opacity", "lighting-color", "stop-color", "stop-opacity", "pointer-events",
            "color-interpolation", "color-interpolation-filters",
            "color-rendering", "fill", "fill-opacity", "fill-rule", "image-rendering",
            "marker", "marker-end", "marker-mid", "marker-start", "shape-rendering", "stroke",
            "stroke-dasharray", "stroke-dashoffset", "stroke-linecap", "stroke-linejoin",
            "stroke-miterlimit", "stroke-opacity", "stroke-width", "text-rendering",
            "baseline-shift", "dominant-baseline", "glyph-orientation-horizontal",
            "glyph-orientation-vertical", "text-anchor", "writing-mode");

    public CssRichTextCodeEditor()
    {
        super();
        String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/css.css").toExternalForm();
        addStyleSheet(stylesheet);
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

    /*    protected StyleSpans<Collection<String>> computeHighlighting(String text) {
            Matcher matcher = PATTERN.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            while(matcher.find()) {
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                if(matcher.group("CLASSSELECTOR") != null) {
                    StyleSpan<Collection<String>> styleSpan = new StyleSpan<>(Collections.singletonList("class-selector"), matcher.end() - matcher.start());
                    spansBuilder.add(styleSpan);
                }
                else if(matcher.group("COMMENT") != null) {
                    spansBuilder.add(Collections.singletonList("comment"), matcher.end() - matcher.start());
                }
                lastKwEnd = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
            StyleSpans<Collection<String>> spans = spansBuilder.create();
            return spans;
        } */
    protected void computeHighlighting(String text)
    {
        getCodeArea().setStyleClass(0, text.length(), "css-default");
        BreakIterator wb = BreakIterator.getWordInstance();
        wb.setText(text);

        int lastIndex = wb.first();

        int stringStartIndex = 0;
        int singleQuoteStringStartIndex = 0;
        int commentStartIndex = 0;

        boolean insideBrackets = false;
        boolean closingBracket;

        boolean startComment = false;
        boolean startEndComment = false;
        boolean insideComment = false;
        boolean propertyValue = false;
        boolean string = false;
        while (lastIndex != BreakIterator.DONE)
        {
            int firstIndex = lastIndex;
            lastIndex = wb.next();
            closingBracket = false;

            if (lastIndex != BreakIterator.DONE)
            {
                if (!insideComment)
                {
                    if ('{' == text.charAt(firstIndex))
                    {
                        insideBrackets = true;
                    }
                    else if ('}' == text.charAt(firstIndex))
                    {
                        closingBracket = true;
                        insideBrackets = false;
                        propertyValue = false;
                    }
                    else if ('/' == text.charAt(firstIndex))
                    {
                        startComment = true;
                        commentStartIndex = firstIndex;
                    }
                    else if (':' == text.charAt(firstIndex) && insideBrackets)
                    {
                        propertyValue = true;
                        continue;
                    }
                    else if (';' == text.charAt(firstIndex) && insideBrackets)
                    {
                        propertyValue = false;
                    }
                    else if ('"' == text.charAt(firstIndex))
                    {
                        if (string) {
                            getCodeArea().setStyleClass(stringStartIndex, lastIndex, "css-textvalue");
                        } else {
                            stringStartIndex = firstIndex;
                        }
                        string = !string;
                    }
                    else if ('\'' == text.charAt(firstIndex))
                    {
                        if (string) {
                            getCodeArea().setStyleClass(singleQuoteStringStartIndex, lastIndex, "css-textvalue");
                        } else {
                            singleQuoteStringStartIndex = firstIndex;
                        }
                        string = !string;
                    }
                    else if (startComment && '*' == text.charAt(firstIndex))
                    {
                        insideComment = true;
                    }
                }
                else if ('*' == text.charAt(firstIndex))  //inside comments remenber every *, if the next char is a /
                {
                    startEndComment = true;
                }
                else if (startEndComment)  //the char before was a *
                {
                    startEndComment = false;
                    if ('/' == text.charAt(firstIndex)) //and this on is a /, end the comment
                    {
                        insideComment = false;
                        getCodeArea().setStyleClass(commentStartIndex, lastIndex, "css-comment");
                    }
                }

                String word = text.substring(firstIndex, lastIndex).toLowerCase();
                logger.info("current word " + word);
                if (insideBrackets)
                {
                    logger.info("inside brackets ");
                    if (CSS_PROPERTIES.contains(word))
                    {
                        getCodeArea().setStyleClass(firstIndex, lastIndex, "css-property");
                    }
                    else if (propertyValue)
                    {
                        if (string)
                        {
                            getCodeArea().setStyleClass(firstIndex, lastIndex, "css-textvalue");
                        }
                        else if (NumberUtils.isDigits(word)) {
                            getCodeArea().setStyleClass(firstIndex, lastIndex, "css-number");
                        }
                        else if (CSS_PROPERTIES_VALUES.contains(word))
                        {
                            getCodeArea().setStyleClass(firstIndex, lastIndex, "css-textvalue");
                        }
                        else
                        {
                            getCodeArea().setStyleClass(firstIndex, lastIndex, "css-property-value");
                        }
                    }
                }
                else if (!SELECTOR_DELIMITER.contains(word) && !closingBracket && StringUtils.isNotBlank(word))
                {
                    getCodeArea().setStyleClass(firstIndex, lastIndex, "css-selector");
                }
            }
        }

        //if we at the end and a block comment is not closed, all text after opening comment is a comment
        if (insideComment) {
            getCodeArea().setStyleClass(commentStartIndex, text.length(), "css-comment");
        }
    }

}
