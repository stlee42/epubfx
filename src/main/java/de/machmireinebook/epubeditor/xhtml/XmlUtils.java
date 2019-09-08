package de.machmireinebook.epubeditor.xhtml;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;

/**
 * User: Michail Jungierek
 * Date: 05.09.2019
 * Time: 23:31
 */
public class XmlUtils {
    private static final Pattern OPEN_TAG_PATTERN = Pattern.compile("(?s)<(.*?)>");
    private static final Pattern CLOSE_TAG_PATTERN = Pattern.compile("(?s)</(.*?)>");
    private static final Pattern SINGLE_TAG_PATTERN = Pattern.compile("(?s)<(.*?)/>");

    public static String removeTags(String text) {
        //regex ungreedy (.*?) and single line (?s), that only tags matches and line breaks are included in . token
        String cleanedText = RegExUtils.removeAll(text, OPEN_TAG_PATTERN);
        cleanedText = RegExUtils.removeAll(cleanedText, CLOSE_TAG_PATTERN);
        cleanedText = RegExUtils.removeAll(cleanedText, SINGLE_TAG_PATTERN);
        return cleanedText;
    }
}
