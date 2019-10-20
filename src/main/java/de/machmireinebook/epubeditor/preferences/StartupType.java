package de.machmireinebook.epubeditor.preferences;

import java.util.Arrays;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

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

    public static ListProperty<StartupType> asListProperty() {
        return new SimpleListProperty<>(FXCollections.observableArrayList(Arrays.asList(StartupType.values())));
    }
}
