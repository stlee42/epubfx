package de.machmireinebook.epubeditor.preferences;

import java.util.Arrays;

import javax.inject.Singleton;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
    public void showPreferencesDialog()
    {
        StringProperty stringProperty = new SimpleStringProperty("String");
        BooleanProperty booleanProperty = new SimpleBooleanProperty(true);
        IntegerProperty integerProperty = new SimpleIntegerProperty(12);
        DoubleProperty doubleProperty = new SimpleDoubleProperty(6.5);

        ObservableList<String> languageItems = FXCollections.observableArrayList(Arrays.asList(
                "English", "Deutsch", "Francais", "Italiano")
        );
        ObjectProperty<String> languageSelection = new SimpleObjectProperty<>("Deutsch");

        ObservableList<String> quotationMarkItems = FXCollections.observableArrayList(Arrays.asList(
                "“ ” (English)", "„“ (Deutsch)", "»« (Deutsch)", "«» (Français)")
        );
        ObjectProperty<String> quotationMarkSelection = new SimpleObjectProperty<>("„“ (Deutsch)");

        PreferencesFx preferencesFx = PreferencesFx.of(EpubEditorStarter.class,
                Category.of("Language Settings",
                        Setting.of("Language", languageItems, languageSelection),
                        Setting.of("Type of Quotation Marks", quotationMarkItems, quotationMarkSelection)
                ),
                Category.of("Category title 2",
                        Setting.of("Setting title 3", integerProperty),
                        Setting.of("Setting title 4", integerProperty)
                ));
        preferencesFx.show();
    }
}
