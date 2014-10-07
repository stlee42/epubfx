package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 25.07.2014
 * Time: 22:00
 */
public class XMLTagPair
{
    private EditorPosition openTagBegin;
    private EditorPosition openTagEnd;
    private EditorPosition closeTagBegin;
    private EditorPosition closeTagEnd;
    private String tagName;

    public XMLTagPair()
    {
    }

    public XMLTagPair(EditorPosition openTagBegin, EditorPosition openTagEnd, EditorPosition closeTagBegin, EditorPosition closeTagEnd)
    {
        this.openTagBegin = openTagBegin;
        this.openTagEnd = openTagEnd;
        this.closeTagBegin = closeTagBegin;
        this.closeTagEnd = closeTagEnd;
    }

    public EditorPosition getOpenTagBegin()
    {
        return openTagBegin;
    }

    public void setOpenTagBegin(EditorPosition openTagBegin)
    {
        this.openTagBegin = openTagBegin;
    }

    public EditorPosition getOpenTagEnd()
    {
        return openTagEnd;
    }

    public void setOpenTagEnd(EditorPosition openTagEnd)
    {
        this.openTagEnd = openTagEnd;
    }

    public EditorPosition getCloseTagBegin()
    {
        return closeTagBegin;
    }

    public void setCloseTagBegin(EditorPosition closeTagBegin)
    {
        this.closeTagBegin = closeTagBegin;
    }

    public EditorPosition getCloseTagEnd()
    {
        return closeTagEnd;
    }

    public void setCloseTagEnd(EditorPosition closeTagEnd)
    {
        this.closeTagEnd = closeTagEnd;
    }

    public String getTagName()
    {
        return tagName;
    }

    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }
}
