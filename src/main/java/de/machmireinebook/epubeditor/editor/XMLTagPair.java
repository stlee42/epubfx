package de.machmireinebook.epubeditor.editor;

import javafx.scene.control.IndexRange;

/**
 * User: mjungierek
 * Date: 25.07.2014
 * Time: 22:00
 */
public class XMLTagPair
{
    private IndexRange openTagRange;
    private IndexRange closeTagRange;
    private int tagAttributesEnd;
    private int tagParagraphIndex;
    private String tagName;

    public XMLTagPair(IndexRange openTagRange, IndexRange closeTagRange, int tagAttributesEnd, int tagParagraphIndex)
    {
        this.openTagRange = openTagRange;
        this.closeTagRange = closeTagRange;
        this.tagAttributesEnd = tagAttributesEnd;
        this.tagParagraphIndex = tagParagraphIndex;
    }

    public IndexRange getOpenTagRange()
    {
        return openTagRange;
    }

    public void setOpenTagRange(IndexRange openTagRange)
    {
        this.openTagRange = openTagRange;
    }

    public IndexRange getCloseTagRange()
    {
        return closeTagRange;
    }

    public void setCloseTagRange(IndexRange closeTagRange)
    {
        this.closeTagRange = closeTagRange;
    }

    public String getTagName()
    {
        return tagName;
    }

    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    public int getTagAttributesEnd()
    {
        return tagAttributesEnd;
    }

    public void setTagAttributesEnd(int tagAttributesEnd)
    {
        this.tagAttributesEnd = tagAttributesEnd;
    }

    public int getTagParagraphIndex() {
        return tagParagraphIndex;
    }
}
