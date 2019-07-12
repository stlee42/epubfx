package de.machmireinebook.epubeditor.preferences;

import java.util.Locale;

import org.languagetool.Language;
import org.languagetool.Languages;

/**
 * User: Michail Jungierek
 * Date: 11.07.2019
 * Time: 23:53
 */
public class PreferencesLanguageStorable implements SelfStorable {
    private Language language;

    public PreferencesLanguageStorable() {
    }

    public PreferencesLanguageStorable(Language language) {
        this.language = language;
    }

    public static PreferencesLanguageStorable of(Language language) {
        return new PreferencesLanguageStorable(language);
    }

    @Override
    public String getStorageContent() {
        return language.getLocaleWithCountryAndVariant().toLanguageTag();
    }

    @Override
    public void readFromStorage(String storageContent) {
        language = Languages.getLanguageForLocale(Locale.forLanguageTag(storageContent));
    }

    @Override
    public SelfStorable getNewInstance() {
        return new PreferencesLanguageStorable();
    }

    public Language getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return language.toString();
    }

/*    @Override
    public boolean equals(Object obj) {
        if (language != null) {
            return language.equals(obj);
        } else {
            return super.equals(obj);
        }
    }*/
}
