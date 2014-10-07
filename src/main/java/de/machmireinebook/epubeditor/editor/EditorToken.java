package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 25.07.2014
 * Time: 21:21
 */
public class EditorToken
{
    private int start;
    private int end;
    private String content;
    private String type;

    public EditorToken()
    {
    }

    public EditorToken(int start, int end, String content, String type)
    {
        this.start = start;
        this.end = end;
        this.content = content;
        this.type = type;
    }

    public int getStart()
    {
        return start;
    }

    public int getEnd()
    {
        return end;
    }

    public String getContent()
    {
        return content;
    }

    public String getType()
    {
        return type;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public void setEnd(int end)
    {
        this.end = end;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
