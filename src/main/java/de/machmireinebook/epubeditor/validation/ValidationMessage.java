package de.machmireinebook.epubeditor.validation;

/**
 * User: Michail Jungierek
 * Date: 16.07.2019
 * Time: 21:00
 */
public class ValidationMessage
{
    private String type;
    private String resource;
    private Integer line;
    private Integer column;
    private String message;

    public ValidationMessage(String type, String resource, Integer line, Integer column, String message)
    {
        this.type = type;
        this.resource = resource;
        this.line = line;
        this.column = column;
        this.message = message;
    }

    public String getType()
    {
        return type;
    }

    public String getResource()
    {
        return resource;
    }

    public int getLine()
    {
        return line;
    }

    public int getColumn()
    {
        return column;
    }

    public String getMessage()
    {
        return message;
    }
}

