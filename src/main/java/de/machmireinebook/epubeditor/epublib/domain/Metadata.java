package de.machmireinebook.epubeditor.epublib.domain;

import java.util.List;

import de.machmireinebook.epubeditor.epublib.domain.epub2.Identifier;

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

    List<Identifier> getIdentifiers();
}
