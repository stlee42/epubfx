package de.machmireinebook.epubeditor.clips;

/**
 * User: mjungierek
 * Date: 03.01.2015
 * Time: 00:37
 */
public class Clip
{
    private String name;
    private String content;
    private boolean isGroup;

    public Clip(String name, String content)
    {
        this.content = content;
        this.name = name;
        isGroup = false;
    }

    public Clip(String name, boolean isGroup)
    {
        this.isGroup = isGroup;
        this.name = name;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setGroup(boolean isGroup)
    {
        this.isGroup = isGroup;
    }

    public boolean isGroup()
    {
        return isGroup;
    }
}
