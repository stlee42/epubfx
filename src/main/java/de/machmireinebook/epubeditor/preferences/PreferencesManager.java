package de.machmireinebook.epubeditor.preferences;

import java.util.Arrays;
import java.util.Collections;

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

import de.machmireinebook.epubeditor.EpubEditorStarter;

import org.apache.log4j.Logger;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.preferencesfx.PreferencesFx;
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
    private PreferencesFx preferencesFx;
    private IntegerProperty integerProperty = new SimpleIntegerProperty(1);

    private StringProperty headlineToc = new SimpleStringProperty("Contents");
    private StringProperty landmarksToc = new SimpleStringProperty("Landmarks");

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
    private SingleSelectionField versionControl = Field.ofSingleSelectionType(Arrays.asList(2.0, 3.2), 0).render(
            new RadioButtonControl<>());

    private ObjectProperty<ReferenceType> referenceType = new SimpleObjectProperty<>(ReferenceType.FOOTNOTE);
    private SingleSelectionField<ReferenceType> referenceTypeControl = Field.ofSingleSelectionType(Arrays.asList(ReferenceType.values()), 0).render(
            new RadioButtonControl<>());

    private ObjectProperty<TocPosition> tocPosition = new SettingEnumObjectProperty<>(TocPosition.AFTER_COVER, TocPosition.class);
    private SingleSelectionField<TocPosition> positionTocControl = Field.ofSingleSelectionType(Arrays.asList(TocPosition.values()), 0).render(
            new RadioButtonControl<>());

    private BooleanProperty generateNCX = new SimpleBooleanProperty(true);
    private BooleanProperty generateHtmlToc = new SimpleBooleanProperty(true);

    public PreferencesManager()
    {
        preferencesFx = PreferencesFx.of(EpubEditorStarter.class,
                Category.of("Book",
                        Group.of("EPUB Version",
                                Setting.of("Version of ebook at Start", versionControl, version) //could be a chooser for template to open at start
                        ),
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
                            Setting.of("Language for Spellchecking", languageSpellItems, languageSpellSelection),
                            Setting.of("Type of Quotation Marks", quotationMarkItems, quotationMarkSelection),
                            Setting.of("Headline of Table of Contents", headlineToc),
                            Setting.of("Headline of Landmarks", landmarksToc)
                        )

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
}
