package de.machmireinebook.epubeditor.preferences;

/**
 * Created by Michail Jungierek, Acando GmbH on 23.05.2018
 */
public enum TocPosition
{
    AFTER_COVER("After Cover"),
    END_OF_BOOK("End of Book");

    private String description;
    TocPosition(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
