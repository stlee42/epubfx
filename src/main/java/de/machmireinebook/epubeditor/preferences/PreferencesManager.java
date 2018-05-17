package de.machmireinebook.epubeditor.preferences;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Singleton;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
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
    private PreferencesFx preferencesFx;

    private StringProperty headlineToc = new SimpleStringProperty("Contents");
    private IntegerProperty integerProperty = new SimpleIntegerProperty(1);

    private ObservableList<String> languageItems = FXCollections.observableArrayList(Collections.singletonList(
            "English"));
    private ObjectProperty<String> languageSelection = new SimpleObjectProperty<>("English");

    private ObservableList<String> languageSpellItems = FXCollections.observableArrayList(Arrays.asList(
            "English", "Deutsch", "Francais", "Italiano"));
    private ObjectProperty<String> languageSpellSelection = new SimpleObjectProperty<>("English");

    private ObservableList<String> quotationMarkItems = FXCollections.observableArrayList(Arrays.asList(
            "“ ” (English)", "„“ (Deutsch)", "»« (Deutsch)", "«» (Français)")
    );
    private ObjectProperty<String> quotationMarkSelection = new SimpleObjectProperty<>("„“ (Deutsch)");

    private DoubleProperty version = new SimpleDoubleProperty(2.0);
    private SingleSelectionField versionControl = Field.ofSingleSelectionType(Arrays.asList(2.0, 3.0, 3.1), 0).render(
            new RadioButtonControl<>());

    public PreferencesManager()
    {
        preferencesFx = PreferencesFx.of(EpubEditorStarter.class,
                Category.of("Book",
                        Setting.of("Version of new ebooks", versionControl, version)
                ),
                Category.of("Language specific Settings",
                        Setting.of("UI Language", languageItems, languageSelection),
                        Setting.of("Language for Spellchecking", languageSpellItems, languageSpellSelection),
                        Setting.of("Type of Quotation Marks", quotationMarkItems, quotationMarkSelection),
                        Setting.of("Headline of Table of Contents", headlineToc)
                ),
                Category.of("Category title 2",
                        Setting.of("Setting title 3", integerProperty),
                        Setting.of("Setting title 4", integerProperty)
                ));
    }

    public void showPreferencesDialog()
    {
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

    public String getLanguageSpellSelection()
    {
        return languageSpellSelection.get();
    }

    public ObjectProperty<String> languageSpellSelectionProperty()
    {
        return languageSpellSelection;
    }

    public void setLanguageSpellSelection(String languageSpellSelection)
    {
        this.languageSpellSelection.set(languageSpellSelection);
    }

    public double getVersion()
    {
        return version.get();
    }

    public DoubleProperty versionProperty()
    {
        return version;
    }
}
