package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 27.07.2014
 * Time: 21:36
 */
public class EditorRange
{
    private EditorPosition from;
    private EditorPosition to;
    private String selection;

    public EditorRange(EditorPosition from, EditorPosition to)
    {
        this.from = from;
        this.to = to;
    }

    public EditorRange(EditorPosition from, EditorPosition to, String selection)
    {
        this.from = from;
        this.to = to;
        this.selection = selection;
    }

    public EditorPosition getFrom()
    {
        return from;
    }

    public void setFrom(EditorPosition from)
    {
        this.from = from;
    }

    public EditorPosition getTo()
    {
        return to;
    }

    public void setTo(EditorPosition to)
    {
        this.to = to;
    }

    public String getSelection()
    {
        return selection;
    }

    public void setSelection(String selection)
    {
        this.selection = selection;
    }
}
