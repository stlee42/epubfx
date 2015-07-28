package de.machmireinebook.epubeditor.htmlcleaner.conditional;


import de.machmireinebook.epubeditor.htmlcleaner.TagNode;

/**
 * Checks if node contains specified attribute.
 */
public class TagNodeAttExistsCondition implements ITagNodeCondition {
    private String attName;

    public TagNodeAttExistsCondition(String attName) {
        this.attName = attName;
    }

    public boolean satisfy(TagNode tagNode) {
        return tagNode != null && tagNode.getAttributes().containsKey(attName.toLowerCase());
    }
}