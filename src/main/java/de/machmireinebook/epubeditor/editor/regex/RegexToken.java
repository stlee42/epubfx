package de.machmireinebook.epubeditor.editor.regex;

/**
 * User: Michail Jungierek
 * Date: 06.07.2019
 * Time: 02:51
 */
public class RegexToken {
    private String code;
    protected int start;
    protected int end;
    protected String text;

    public RegexToken(String code, int start, int end, String text) {
        this.code = code;
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }

    public String getCode() {
        return code;
    }
}