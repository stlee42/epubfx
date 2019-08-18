package de.machmireinebook.epubeditor.preferences;

/**
 * User: Michail Jungierek
 * Date: 17.08.2019
 * Time: 11:36
 */
public class ReaderDevice {
    private String description;
    private int width;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return description;
    }
}
