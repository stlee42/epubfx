package de.machmireinebook.epubeditor.htmlcleaner.conditional;


import de.machmireinebook.epubeditor.htmlcleaner.TagNode;

/**
 * Checks if node has specified name.
 */
public class TagNodeNameCondition implements ITagNodeCondition {
    private String name;

    public TagNodeNameCondition(String name) {
        this.name = name;
    }

    public boolean satisfy(TagNode tagNode) {
        return tagNode != null && tagNode.getName().equalsIgnoreCase(this.name);
    }
}