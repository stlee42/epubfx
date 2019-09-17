package de.machmireinebook.epubeditor.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

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
import javafx.beans.binding.Bindings;

import org.apache.log4j.Logger;

import org.jdom2.Element;
import org.languagetool.Language;
import org.languagetool.Languages;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.IntegerField;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleIntegerControl;
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
    private PreferencesFx preferencesFx;

    private ObjectProperty<StartupType> defaultStartupType = new SettingEnumObjectProperty<>(StartupType.MINIMAL_EBOOK, StartupType.class);
    private SingleSelectionField<StartupType> startupTypeControl = Field.ofSingleSelectionType(Arrays.asList(StartupType.values()), 0).render(new RadioButtonControl<>());

    private DoubleProperty version = new SimpleDoubleProperty(2.0);
    private SingleSelectionField<Double> versionControl = Field.ofSingleSelectionType(Arrays.asList(2.0, 3.2), 0).render(
            new RadioButtonControl<>());

    private ObjectProperty<File> fileTemplateProperty = new SimpleObjectProperty<>();
    private Setting fileTemplateSetting = Setting.of("Template", fileTemplateProperty, false);


    private StringProperty headlineToc = new SimpleStringProperty("Contents");
    private StringProperty headlineLandmarks = new SimpleStringProperty("Landmarks");

    private ObservableList<String> languageItems = FXCollections.observableArrayList(Collections.singletonList(
            "English"));
    private ObjectProperty<String> languageSelection = new SimpleObjectProperty<>("English");

    private final BooleanProperty spellcheck = new SimpleBooleanProperty(this, "spellcheck", true);
    private final BooleanProperty onlyDictionaryBasedSpellCheck = new SimpleBooleanProperty(this, "onlyDictionaryBasedSpellCheck", true);

    private List<Language> languages = Languages.get();
    private ObservableList<PreferencesLanguageStorable> languageSpellItems = FXCollections.observableArrayList(languages
            .stream()
            .map(PreferencesLanguageStorable::new)
            .collect(Collectors.toList()));
    private ObjectProperty<PreferencesLanguageStorable> languageSpellSelection = new SimpleObjectProperty<>(PreferencesLanguageStorable.of(Languages.getLanguageForLocale(Locale.GERMANY)));

    private ObservableList<String> quotationMarkItems = FXCollections.observableArrayList(Arrays.asList(
            QuotationMark.ENGLISH.getDescription(),
            QuotationMark.GERMAN.getDescription(),
            QuotationMark.GERMAN_GUILLEMETS.getDescription(),
            QuotationMark.FRENCH.getDescription())
    );
    private ObjectProperty<String> quotationMarkSelection = new SimpleObjectProperty<>(QuotationMark.GERMAN.getDescription());


    private ObjectProperty<ReferenceType> referenceType = new SettingEnumObjectProperty<>(ReferenceType.FOOTNOTE, ReferenceType.class);
    private SingleSelectionField<ReferenceType> referenceTypeControl = Field.ofSingleSelectionType(Arrays.asList(ReferenceType.values()), 0).render(
            new RadioButtonControl<>());

    private ObjectProperty<TocPosition> tocPosition = new SettingEnumObjectProperty<>(TocPosition.AFTER_COVER, TocPosition.class);
    private SingleSelectionField<TocPosition> positionTocControl = Field.ofSingleSelectionType(Arrays.asList(TocPosition.values()), 0).render(
            new RadioButtonControl<>());

    private BooleanProperty generateNCX = new SimpleBooleanProperty(this, "generateNcx", true);
    private BooleanProperty generateHtmlToc = new SimpleBooleanProperty(this, "generateHtmlToc",true);
    // useTabProperty
    private final BooleanProperty useTabProperty = new SimpleBooleanProperty(this, "useTab");
    private final IntegerProperty tabSizeProperty = new SimpleIntegerProperty(this, "tabSize", 4);
    private final IntegerField tabSizeControl = Field.ofIntegerType(tabSizeProperty).span(6).render(new SimpleIntegerControl());
    private final IntegerProperty fontSizeProperty = new SimpleIntegerProperty(this, "fontSize", 12);
    private final IntegerField fontSizeControl = Field.ofIntegerType(fontSizeProperty).render(new SimpleIntegerControl());

    public void init(Element preferencesRootElement)
    {
        storageHandler = new EpubFxPreferencesStorageHandler(preferencesRootElement);

        versionControl.editableProperty().bind(Bindings.equal(startupTypeControl.selectionProperty(), StartupType.MINIMAL_EBOOK));
        ((Field)fileTemplateSetting.getElement()).editableProperty().bind(Bindings.equal(startupTypeControl.selectionProperty(), StartupType.EBOOK_TEMPLATE));

        tabSizeControl.editableProperty().bind(useTabProperty.not());

        preferencesFx = PreferencesFx.of(storageHandler,
            Category.of("Application",
                    Group.of("Startup",
                            Setting.of("Open application with ", startupTypeControl, defaultStartupType),
                            Setting.of("Version of new ebook", versionControl, version),
                            fileTemplateSetting
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
            Category.of("Editor",
                    Group.of("Font",
                            Setting.of("Font Size", fontSizeControl, fontSizeProperty)
                    ),
                    Group.of("Tabs and Indents",
                            Setting.of("Use Tab Characters", useTabProperty),
                            Setting.of("Tab Size", tabSizeControl, tabSizeProperty)
                    )
                ),
            Category.of("Language specific Settings",
                    Group.of("UI",
                        Setting.of("UI Language", languageItems, languageSelection)
                    ),
                    Group.of("Spell Check",
                        Setting.of("Enable Spell Check", spellcheck),
                        Setting.of("Language for Spell Checking", languageSpellItems, languageSpellSelection),
                            Setting.of("Use only Dictionary base Spell Check", onlyDictionaryBasedSpellCheck)
                    ),
                    Group.of("Content",
                            Setting.of("Type of Quotation Marks", quotationMarkItems, quotationMarkSelection),
                            Setting.of("Headline of Table of Contents", headlineToc),
                            Setting.of("Headline of Landmarks", headlineLandmarks))
            )
        ).saveSettings(true);
    }

    public void showPreferencesDialog()
    {
        preferencesFx.show(true);
    }

    public Optional<Element> getPreferencesElement()
    {
        if (storageHandler != null) {
            preferencesFx.saveSettings();
            return Optional.of(storageHandler.getPreferencesElement());
        } else {
            return Optional.empty();
        }
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
    public void setQuotationMarkSelection(String quotationMarkSelection) {
        this.quotationMarkSelection.set(quotationMarkSelection);
    }

    public PreferencesLanguageStorable getLanguageSpellSelection()
    {
        return languageSpellSelection.get();
    }
    public ObjectProperty<PreferencesLanguageStorable> languageSpellSelectionProperty() {
        return languageSpellSelection;
    }
    public void setLanguageSpellSelection(PreferencesLanguageStorable languageSpellSelection) {
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

    public String getHeadlineLandmarks() {
        return headlineLandmarks.get();
    }
    public StringProperty headlineLandmarksProperty()
    {
        return headlineLandmarks;
    }

    public final BooleanProperty onlyDictionaryBasedSpellCheckProperty() {
        return onlyDictionaryBasedSpellCheck;
    }
    public final boolean isOnlyDictionaryBasedSpellCheck() {
        return onlyDictionaryBasedSpellCheck.get();
    }
    public final void setOnlyDictionaryBasedSpellCheck(boolean value) {
        onlyDictionaryBasedSpellCheck.set(value);
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

    public ObservableList<PreferencesLanguageStorable> getLanguageSpellItems() {
        return languageSpellItems;
    }

    public final BooleanProperty useTabProperty() {
        return useTabProperty;
    }
    public final boolean isUseTab() {
        return useTabProperty.get();
    }
    public final void setUseTab(boolean value) {
        useTabProperty.set(value);
    }

    public final IntegerProperty tabSizeProperty() {
        return tabSizeProperty;
    }
    public final int getTabSize() {
        return tabSizeProperty.get();
    }
    public final void setTabSize(int value) {
        tabSizeProperty.set(value);
    }

    public final IntegerProperty fontSizeProperty() {
        return fontSizeProperty;
    }
    public final int getFontSize() {
        return fontSizeProperty.get();
    }
    public final void setFontSize(int value) {
        fontSizeProperty.set(value);
    }

    public final ObjectProperty<StartupType> startupTypeProperty() {
        return startupTypeControl.selectionProperty();
    }
    public final StartupType getStartupType() {
        return startupTypeControl.selectionProperty().get();
    }
    public final void setStartupType(StartupType startupType) {
        this.startupTypeControl.selectionProperty().set(startupType);
    }
}
