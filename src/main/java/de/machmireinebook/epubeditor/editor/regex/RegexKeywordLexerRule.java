package de.machmireinebook.epubeditor.editor.regex;

/**
 * User: Michail Jungierek
 * Date: 06.07.2019
 * Time: 02:53
 */
public class RegexKeywordLexerRule extends RegexLexerRule
{
    public RegexKeywordLexerRule(String code, String... keywords) {
        super(code, "\\b(" + String.join("|", keywords) + ")\\b");

    }
}
