package de.machmireinebook.epubeditor.epublib.domain;

import java.util.List;

/**
 * User: Michail Jungierek
 * Date: 07.05.2018
 * Time: 23:58
 */
public interface EpubMetadata
{
    String getFirstTitle();
    void generateNewUuid();
    String getLanguage();

    List<EpubIdentifier> getIdentifiers();
}
