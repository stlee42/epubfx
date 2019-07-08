package de.machmireinebook.epubeditor.preferences;

/**
 * @author Michail Jungierek
 */
public enum StartupType
{
    MINIMAL_EBOOK("Empty ebook"),
    EBOOK_TEMPLATE("New ebook from Template"),
    RECENT_EBOOK("Continue where you left off");

    private String description;
    StartupType(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }
}
