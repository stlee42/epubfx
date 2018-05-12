package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 25.07.2014
 * Time: 21:57
 */
public class EditorPosition
{
    private int line;
    private int column;

    public static EditorPosition START = new EditorPosition(0, 0);

    public EditorPosition()
    {
    }

    public EditorPosition(int line, int column)
    {
        this.line = line;
        this.column = column;
    }

    public int getLine()
    {
        return line;
    }

    public void setLine(int line)
    {
        this.line = line;
    }

    public int getColumn()
    {
        return column;
    }

    public void setColumn(int column)
    {
        this.column = column;
    }

    public String toJson()
    {
        return "{line:" + line + ",ch:" + column + "}";
    }
}
