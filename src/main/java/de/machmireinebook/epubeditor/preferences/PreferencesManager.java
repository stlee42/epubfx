package de.machmireinebook.epubeditor.preferences;

import java.util.Arrays;

import javax.inject.Singleton;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Setting;

import de.machmireinebook.epubeditor.EpubEditorStarter;

/**
 * Created by Michail Jungierek
 */
@Singleton
public class PreferencesManager
{
    private StringProperty headlineToc = new SimpleStringProperty("Contents");
    private IntegerProperty integerProperty = new SimpleIntegerProperty(1);

    private ObservableList<String> languageItems = FXCollections.observableArrayList(Arrays.asList(
            "English", "Deutsch", "Francais", "Italiano")
    );
    private ObjectProperty<String> languageSelection = new SimpleObjectProperty<>("Deutsch");

    private ObservableList<String> quotationMarkItems = FXCollections.observableArrayList(Arrays.asList(
            "“ ” (English)", "„“ (Deutsch)", "»« (Deutsch)", "«» (Français)")
    );
    private ObjectProperty<String> quotationMarkSelection = new SimpleObjectProperty<>("„“ (Deutsch)");

    public void showPreferencesDialog()
    {

        PreferencesFx preferencesFx = PreferencesFx.of(EpubEditorStarter.class,
                Category.of("Language specific Settings",
                        Setting.of("Language", languageItems, languageSelection),
                        Setting.of("Type of Quotation Marks", quotationMarkItems, quotationMarkSelection),
                        Setting.of("Headline of Table of Contents", headlineToc)
                ),
                Category.of("Category title 2",
                        Setting.of("Setting title 3", integerProperty),
                        Setting.of("Setting title 4", integerProperty)
                ));
        preferencesFx.show();
    }

    public String getHeadlineToc()
    {
        return headlineToc.get();
    }

    public StringProperty headlineTocProperty()
    {
        return headlineToc;
    }

    public void setHeadlineToc(String headlineToc)
    {
        this.headlineToc.set(headlineToc);
    }

    public String getLanguageSelection()
    {
        return languageSelection.get();
    }

    public ObjectProperty<String> languageSelectionProperty()
    {
        return languageSelection;
    }

    public void setLanguageSelection(String languageSelection)
    {
        this.languageSelection.set(languageSelection);
    }

    public String getQuotationMarkSelection()
    {
        return quotationMarkSelection.get();
    }

    public ObjectProperty<String> quotationMarkSelectionProperty()
    {
        return quotationMarkSelection;
    }

    public void setQuotationMarkSelection(String quotationMarkSelection)
    {
        this.quotationMarkSelection.set(quotationMarkSelection);
    }
}
