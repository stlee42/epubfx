package de.machmireinebook.epubeditor.htmlcleaner.conditional;


import de.machmireinebook.epubeditor.htmlcleaner.TagNode;

/**
 * All nodes.
 */
public class TagAllCondition implements ITagNodeCondition {
    public boolean satisfy(TagNode tagNode) {
        return true;
    }
}