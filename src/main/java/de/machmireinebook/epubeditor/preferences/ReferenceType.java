package de.machmireinebook.epubeditor.preferences;

/**
 * Created by Michail Jungierek
 */
public enum ReferenceType
{
    FOOTNOTE("Footnote"),
    ENDNOTE("Endnote");

    private String description;
    ReferenceType(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }

}
