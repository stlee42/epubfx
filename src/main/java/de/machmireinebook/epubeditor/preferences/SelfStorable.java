package de.machmireinebook.epubeditor.preferences;

/**
 * User: Michail Jungierek
 * Date: 11.07.2019
 * Time: 23:52
 */
public interface SelfStorable {
    String getStorageContent();
    void readFromStorage(String storageContent);
    SelfStorable getNewInstance();
}
