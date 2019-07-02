package de.machmireinebook.epubeditor.epublib.domain;

/**
 * A Book's identifier.
 * 
 * Defaults to a random UUID and scheme "UUID"
 * 
 * @author paul
 *
 */
public interface EpubIdentifier
{
    boolean isBookId();
    String getValue();
}
