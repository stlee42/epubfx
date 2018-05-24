package de.machmireinebook.epubeditor.epublib.domain;

/**
 * User: Michail Jungierek
 * Date: 07.05.2018
 * Time: 23:58
 */
public interface Metadata
{
    String getFirstTitle();
    void generateNewUuid();
    String getLanguage();
}
