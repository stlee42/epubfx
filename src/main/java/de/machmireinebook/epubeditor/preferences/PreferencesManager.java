package de.machmireinebook.epubeditor.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Singleton;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.log4j.Logger;

import org.jdom2.Element;
import org.languagetool.Language;
import org.languagetool.Languages;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;

/**
 * Created by Michail Jungierek
 */
@Singleton
public class PreferencesManager
{
    private static final Logger logger = Logger.getLogger(PreferencesManager.class);

    private EpubFxPreferencesStorageHandler storageHandler;
    private EpubFxPreferences preferencesFx;

    private ObjectProperty<StartupType> startupType = new SimpleObjectProperty<>(StartupType.MINIMAL_EBOOK);
    private SingleSelectionField<StartupType> startupTypeControl = Field.ofSingleSelectionType(Arrays.asList(StartupType.values()), 0).render(
            new RadioButtonControl<>());

    private StringProperty headlineToc = new SimpleStringProperty("Contents");
    private StringProperty landmarksToc = new SimpleStringProperty("Landmarks");

    private ObservableList<String> languageItems = FXCollections.observableArrayList(Collections.singletonList(
            "English"));
    private ObjectProperty<String> languageSelection = new SimpleObjectProperty<>("English");

    // spellcheckProperty
    private final BooleanProperty spellcheck = new SimpleBooleanProperty(this, "spellcheck", true);

    private List<Language> languages = Languages.get();
    private ObservableList<Language> languageSpellItems = FXCollections.observableArrayList(languages);
    private ObjectProperty<Language> languageSpellSelection = new SimpleObjectProperty<>(Languages.getLanguageForLocale(Locale.ENGLISH));

    private ObservableList<String> quotationMarkItems = FXCollections.observableArrayList(Arrays.asList(
            QuotationMark.ENGLISH.getDescription(), QuotationMark.GERMAN.getDescription(), QuotationMark.GERMAN_GUILLEMETS.getDescription(),
            QuotationMark.FRENCH.getDescription())
    );
    private ObjectProperty<String> quotationMarkSelection = new SimpleObjectProperty<>(QuotationMark.GERMAN.getDescription());

    private DoubleProperty version = new SimpleDoubleProperty(2.0);
    private SingleSelectionField<Double> versionControl = Field.ofSingleSelectionType(Arrays.asList(2.0, 3.2), 0).render(
            new RadioButtonControl<>());

    private ObjectProperty<ReferenceType> referenceType = new SettingEnumObjectProperty<>(ReferenceType.FOOTNOTE, ReferenceType.class);
    private SingleSelectionField<ReferenceType> referenceTypeControl = Field.ofSingleSelectionType(Arrays.asList(ReferenceType.values()), 0).render(
            new RadioButtonControl<>());

    private ObjectProperty<TocPosition> tocPosition = new SettingEnumObjectProperty<>(TocPosition.AFTER_COVER, TocPosition.class);
    private SingleSelectionField<TocPosition> positionTocControl = Field.ofSingleSelectionType(Arrays.asList(TocPosition.values()), 0).render(
            new RadioButtonControl<>());

    private BooleanProperty generateNCX = new SimpleBooleanProperty(this, "generateNcx", true);
    private BooleanProperty generateHtmlToc = new SimpleBooleanProperty(this, "generateHtmlToc",true);

    public void init(Element preferencesRootElement)
    {
        storageHandler = new EpubFxPreferencesStorageHandler(preferencesRootElement);
        Setting<SingleSelectionField<Language>, ObjectProperty<Language>> spellCheckingLanguageSetting = Setting.of("Language for Spell Checking", languageSpellItems, languageSpellSelection);

        preferencesFx = EpubFxPreferences.of(storageHandler,
            Category.of("Application",
                    Group.of("Startup",
                            Setting.of("Open application with ", startupTypeControl, startupType),
                            Setting.of("Version of new ebook", versionControl, version)
                    )
            ),
            Category.of("Book",
                    Group.of("EPUB 2",
                            Setting.of("Generate HTML ToC automatically ", generateHtmlToc)
                    ),
                    Group.of("EPUB 3",
                            Setting.of("Generate NCX automatically ", generateNCX)
                    ),
                    Group.of("Structure",
                            Setting.of("Type of References", referenceTypeControl, referenceType),
                            Setting.of("Position of generated Toc", positionTocControl, tocPosition)
                    )
            ),
            Category.of("Language specific Settings",
                    Group.of("UI",
                        Setting.of("UI Language", languageItems, languageSelection)
                    ),
                    Group.of("Content",
                        Setting.of("Spell Check", spellcheck),
                        spellCheckingLanguageSetting,
                        Setting.of("Type of Quotation Marks", quotationMarkItems, quotationMarkSelection),
                        Setting.of("Headline of Table of Contents", headlineToc),
                        Setting.of("Headline of Landmarks", landmarksToc)
                    )
            ));
    }

    public void showPreferencesDialog()
    {
        preferencesFx.show(true);
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

    public Language getLanguageSpellSelection()
    {
        return languageSpellSelection.get();
    }

    public ObjectProperty<Language> languageSpellSelectionProperty()
    {
        return languageSpellSelection;
    }

    public void setLanguageSpellSelection(Language languageSpellSelection)
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

    public ReferenceType getReferenceType()
    {
        return referenceType.get();
    }

    public ObjectProperty<ReferenceType> referenceTypeProperty()
    {
        return referenceType;
    }

    public boolean isGenerateNCX()
    {
        return generateNCX.get();
    }

    public BooleanProperty generateNCXProperty()
    {
        return generateNCX;
    }

    public TocPosition getTocPosition()
    {
        return tocPosition.get();
    }

    public ObjectProperty<TocPosition> tocPositionProperty()
    {
        return tocPosition;
    }

    public boolean isGenerateHtmlToc()
    {
        return generateHtmlToc.get();
    }

    public BooleanProperty generateHtmlTocProperty()
    {
        return generateHtmlToc;
    }

    public String getLandmarksToc()
    {
        return landmarksToc.get();
    }

    public StringProperty landmarksTocProperty()
    {
        return landmarksToc;
    }

    public Optional<Element> getPreferencesElement()
    {
        if (storageHandler != null) {
            return Optional.of(storageHandler.getPreferencesElement());
        } else {
            return Optional.empty();
        }
    }

    public final BooleanProperty spellcheckProperty() {
        return spellcheck;
    }
    public final boolean isSpellcheck() {
        return spellcheck.get();
    }
    public final void setSpellcheck(boolean value) {
        spellcheck.set(value);
    }

}
