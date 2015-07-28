package de.machmireinebook.epubeditor.htmlcleaner.conditional;


import de.machmireinebook.epubeditor.htmlcleaner.TagNode;

/**
 * Used as base for different node checkers.
 */
public interface ITagNodeCondition {
    public boolean satisfy(TagNode tagNode);
}