package de.machmireinebook.epubeditor.preferences;

/**
 * Created by Michail Jungierek, Acando GmbH on 23.05.2018
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
