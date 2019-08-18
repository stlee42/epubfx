package de.machmireinebook.epubeditor.gui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.NavNotFoundException;
import de.machmireinebook.epubeditor.epublib.OpfNotReadableException;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.epub2.EpubReader;
import de.machmireinebook.epubeditor.epublib.epub2.EpubWriter;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.toc.TocGenerator;
import de.machmireinebook.epubeditor.javafx.StashableSplitPane;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.manager.EditorTabManager;
import de.machmireinebook.epubeditor.manager.PreviewManager;
import de.machmireinebook.epubeditor.manager.SearchManager;
import de.machmireinebook.epubeditor.manager.TOCViewManager;
import de.machmireinebook.epubeditor.preferences.PreferencesLanguageStorable;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;
import de.machmireinebook.epubeditor.preferences.QuotationMark;
import de.machmireinebook.epubeditor.preferences.ReaderDevice;
import de.machmireinebook.epubeditor.validation.ValidationManager;
import de.machmireinebook.epubeditor.validation.ValidationMessage;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import com.pixelduke.control.Ribbon;

/**
 * User: mjungierek
 * Date: 07.02.14
 * Time: 19:09
 */
@Singleton
public class MainController implements Initializable
{
    private static final Logger logger = Logger.getLogger(MainController.class);
    @FXML
    private ChoiceBox<ReaderDevice> deviceWidthComboBox;
    @FXML
    private Label previewWidthLabel;
    @FXML
    private Button nonBreakingSpaceButton;
    @FXML
    private Button hrButton;
    @FXML
    private Button ellipsisButton;
    @FXML
    private Button editTocButton;
    @FXML
    private Button generateUuidButton;
    @FXML
    private Button addOtherFilesButton;
    @FXML
    private Button addCSSFilesButton;
    @FXML
    private Button blockQuoteButton;
    @FXML
    private Button validateEpubButton;
    @FXML
    private TableView<ValidationMessage> validationResultsTableView;
    @FXML
    private Button singleQuotationMarksButton;
    @FXML
    private Ribbon ribbon;
    @FXML
    private SplitPane mainDivider;
    @FXML
    private StashableSplitPane centerDivider;
    @FXML
    private StashableSplitPane rightDivider;
    @FXML
    private AnchorPane previewAnchorPane;
    @FXML
    private ToggleButton showBookBrowserToggleButton;
    @FXML
    private ToggleButton showPreviewToggleButton;
    @FXML
    private ToggleButton showTocToggleButton;
    @FXML
    private ToggleButton showValidationResultsToggleButton;
    @FXML
    private Button insertTableButton;
    @FXML
    private StashableSplitPane leftDivider;
    @FXML
    private ToggleButton showClipsToggleButton;
    @FXML
    private Button zoomInButton;
    @FXML
    private Button zoom100Button;
    @FXML
    private Button zoomOutButton;
    @FXML
    private Button increaseIndentButton;
    @FXML
    private Button decreaseIndentButton;
    @FXML
    private Button createTocButton;
    @FXML
    private Button insertSpecialCharacterButton;
    @FXML
    private Button editMetadataButton;
    @FXML
    private Label cursorPosLabel;
    @FXML
    private Button saveAsButton;
    @FXML
    private Button insertImageButton;
    @FXML
    private Button splitButton;
    @FXML
    private Button addExistingFileButton;
    @FXML
    private Button h1Button;
    @FXML
    private Button h2Button;
    @FXML
    private Button h3Button;
    @FXML
    private Button h4Button;
    @FXML
    private Button h5Button;
    @FXML
    private Button h6Button;
    @FXML
    private Button paragraphButton;
    @FXML
    private Button quotationMarksButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button cutButton;
    @FXML
    private Button copyButton;
    @FXML
    private Button pasteButton;
    @FXML
    private ToggleButton searchReplaceButton;
    @FXML
    private Button newBookButton;
    @FXML
    private MenuButton openBookButton;
    @FXML
    private Button addFileButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button alignLeftButton;
    @FXML
    private Button centerButton;
    @FXML
    private Button rightAlignButton;
    @FXML
    private Button justifyButton;
    @FXML
    private Button orderedListButton;
    @FXML
    private Button unorderedListButton;
    @FXML
    private Button underlineButton;
    @FXML
    private Button strikeButton;
    @FXML
    private Button subscriptButton;
    @FXML
    private Button superscriptButton;
    @FXML
    private Button boldButton;
    @FXML
    private Button italicButton;
    @FXML
    private ListView<Resource> clipListView;
    @FXML
    private TreeView<TocEntry> tocTreeView;
    @FXML
    private WebView previewWebview;
    @FXML
    private TabPane epubFilesTabPane;
    @FXML
    private AnchorPane statusAnchorPane;
    @FXML
    private TreeView<Resource> epubStructureTreeView;
    @FXML
    private Button createHtmlTocButton;
    @FXML
    private Button createNcxButton;
    @FXML
    private Button addCoverButton;
    @FXML
    private Button insertLinkButton;
    @FXML
    private AnchorPane centerAnchorPane;
    @FXML
    private Button uppercaseButton;
    @FXML
    private Button lowercaseButton;
    @FXML
    private ComboBox<PreferencesLanguageStorable> languageSpellComboBox;

    private ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>();
    private List<MenuItem> recentFilesMenuItems = new ArrayList<>();
    private Stage stage;

    @Inject
    private BookBrowserManager bookBrowserManager;
    @Inject
    private EditorTabManager editorTabManager;
    @Inject
    private PreviewManager previewManager;
    @Inject
    private TOCViewManager tocViewManager;
    @Inject
    private EpubEditorConfiguration configuration;
    @Inject
    private SearchManager searchManager;
    @Inject
    private SearchAnchorPane searchAnchorPane;
    @Inject
    private PreferencesManager preferencesManager;
    @Inject
    private TocGenerator tocGenerator;
    @Inject
    private ValidationManager validationManager;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        bookBrowserManager.setTreeView(epubStructureTreeView);
        bookBrowserManager.setEditorManager(editorTabManager);

        epubFilesTabPane.getTabs().clear();
        epubFilesTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        editorTabManager.setTabPane(epubFilesTabPane);

        previewManager.setWebview(previewWebview);

        tocViewManager.setTreeView(tocTreeView);
        tocViewManager.setEditorManager(editorTabManager);
        tocViewManager.bookProperty().bind(currentBookProperty);
        tocGenerator.bookProperty().bind(currentBookProperty);
        validationManager.bookProperty().bind(currentBookProperty);

        currentBookProperty.addListener((observable, oldValue, newValue) -> {
            epubFilesTabPane.getTabs().clear();

            bookBrowserManager.setBook(newValue);
            editorTabManager.reset();
            editorTabManager.setBook(newValue);
            previewManager.reset();
            saveButton.disableProperty().unbind();
            createHtmlTocButton.disableProperty().unbind();
            createNcxButton.disableProperty().unbind();
            validateEpubButton.disableProperty().unbind();

            saveButton.disableProperty().bind(newValue.bookIsChangedProperty().not());
            createHtmlTocButton.disableProperty().bind(Bindings.equal(currentBookProperty.get().versionProperty(), EpubVersion.VERSION_2).not());
            createNcxButton.disableProperty().bind(Bindings.equal(currentBookProperty.get().versionProperty(), EpubVersion.VERSION_2));
            String stageTitle;
            if (newValue.getVersion() != null) {
                stageTitle = (newValue.getPhysicalFileName() != null ? newValue.getPhysicalFileName().getFileName().toString() : "empty.epub")
                        + " - EPUB " + newValue.getVersion().asString() + " - SmoekerSchriever";
            }
            else
            {
                stageTitle = (newValue.getPhysicalFileName() != null ? newValue.getPhysicalFileName().getFileName().toString() : "empty.epub")
                        + " - SmoekerSchriever";
            }
            stage.setTitle(stageTitle);
            newValue.bookIsChangedProperty().addListener((observable1, oldValue1, newValue1) -> {
                String currentTitle;
                if (newValue1) {
                    currentTitle = "* " + stageTitle;
                } else {
                    currentTitle =  stageTitle;
                }
                stage.setTitle(currentTitle);
            });

            validateEpubButton.disableProperty().bind(Bindings.createBooleanBinding(() -> currentBookProperty.get().getPhysicalFileName() == null));
            validationResultsTableView.getItems().clear();
        });
        BooleanBinding isNoXhtmlEditorBinding = Bindings.isNull(currentBookProperty).or(Bindings.not(editorTabManager.currentEditorIsXHTMLProperty())
                .or(Bindings.isEmpty(epubFilesTabPane.getTabs())));
        BooleanBinding isNoEditorBinding = currentBookProperty.isNull()
                .or(Bindings.isEmpty(epubFilesTabPane.getTabs()))
                .or(editorTabManager.currentXHTMLResourceProperty().isNull())
                .and(editorTabManager.currentCssResourceProperty().isNull())
                .and(editorTabManager.currentXMLResourceProperty().isNull());

        addCoverButton.disableProperty().bind(currentBookProperty.isNull());
        editMetadataButton.disableProperty().bind(currentBookProperty.isNull());

        h1Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h2Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h3Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h4Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h5Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h6Button.disableProperty().bind(isNoXhtmlEditorBinding);
        paragraphButton.disableProperty().bind(isNoXhtmlEditorBinding);
        quotationMarksButton.disableProperty().bind(isNoXhtmlEditorBinding);
        singleQuotationMarksButton.disableProperty().bind(isNoXhtmlEditorBinding);
        blockQuoteButton.disableProperty().bind(isNoXhtmlEditorBinding);

        boldButton.disableProperty().bind(isNoXhtmlEditorBinding);
        italicButton.disableProperty().bind(isNoXhtmlEditorBinding);
        orderedListButton.disableProperty().bind(isNoXhtmlEditorBinding);
        unorderedListButton.disableProperty().bind(isNoXhtmlEditorBinding);
        underlineButton.disableProperty().bind(isNoXhtmlEditorBinding);
        strikeButton.disableProperty().bind(isNoXhtmlEditorBinding);
        subscriptButton.disableProperty().bind(isNoXhtmlEditorBinding);
        superscriptButton.disableProperty().bind(isNoXhtmlEditorBinding);
        alignLeftButton.disableProperty().bind(isNoXhtmlEditorBinding);
        centerButton.disableProperty().bind(isNoXhtmlEditorBinding);
        rightAlignButton.disableProperty().bind(isNoXhtmlEditorBinding);
        justifyButton.disableProperty().bind(isNoXhtmlEditorBinding);
        ellipsisButton.disableProperty().bind(isNoXhtmlEditorBinding);
        nonBreakingSpaceButton.disableProperty().bind(isNoXhtmlEditorBinding);
        hrButton.disableProperty().bind(isNoXhtmlEditorBinding);

        addFileButton.disableProperty().bind(currentBookProperty.isNull());
        addExistingFileButton.disableProperty().bind(currentBookProperty.isNull());
        addCSSFilesButton.disableProperty().bind(currentBookProperty.isNull());
        addOtherFilesButton.disableProperty().bind(currentBookProperty.isNull());
        generateUuidButton.disableProperty().bind(currentBookProperty.isNull());
        createTocButton.disableProperty().bind(currentBookProperty.isNull());
        editTocButton.disableProperty().bind(currentBookProperty.isNull());

        saveButton.setDisable(true);
        undoButton.disableProperty().bind(isNoEditorBinding.or(Bindings.not(editorTabManager.canUndoProperty())));
        redoButton.disableProperty().bind(isNoEditorBinding.or(Bindings.not(editorTabManager.canRedoProperty())));

        cutButton.disableProperty().bind(isNoEditorBinding);
        copyButton.disableProperty().bind(isNoEditorBinding);
        pasteButton.disableProperty().bind(isNoEditorBinding);

        searchReplaceButton.disableProperty().bind(isNoEditorBinding);

        increaseIndentButton.disableProperty().bind(isNoXhtmlEditorBinding);
        decreaseIndentButton.disableProperty().bind(isNoXhtmlEditorBinding);
        saveAsButton.disableProperty().bind(currentBookProperty.isNull());
        insertImageButton.disableProperty().bind(isNoXhtmlEditorBinding);
        splitButton.disableProperty().bind(isNoXhtmlEditorBinding);
        insertTableButton.disableProperty().bind(isNoXhtmlEditorBinding);
        insertSpecialCharacterButton.disableProperty().bind(isNoXhtmlEditorBinding);
        insertLinkButton.disableProperty().bind(isNoXhtmlEditorBinding);
        lowercaseButton.disableProperty().bind(isNoEditorBinding);
        uppercaseButton.disableProperty().bind(isNoEditorBinding);

        createHtmlTocButton.disableProperty().bind(currentBookProperty.isNull());
        createNcxButton.disableProperty().bind(currentBookProperty.isNull());

        cursorPosLabel.textProperty().bind(editorTabManager.cursorPosLabelProperty());
        previewWidthLabel.textProperty().bind(Bindings.createStringBinding(() -> "Preview width: " + previewWebview.widthProperty().getValue(), previewWebview.widthProperty()));

        //Teile der Oberfläche an-/abschalten, per Binding an die Buttons im Ribbon
        clipListView.visibleProperty().bindBidirectional(showClipsToggleButton.selectedProperty());
        showClipsToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                if (!leftDivider.getItems().contains(clipListView))
                {
                    leftDivider.getItems().add(clipListView);
                    leftDivider.setDividerPosition(0, 0.7);
                }
            }
            leftDivider.setVisibility(1, newValue);
        });
        epubStructureTreeView.visibleProperty().bindBidirectional(showBookBrowserToggleButton.selectedProperty());
        showBookBrowserToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                if (!leftDivider.getItems().contains(epubStructureTreeView))
                {
                    leftDivider.getItems().add(epubStructureTreeView);
                    leftDivider.setDividerPosition(0, 0.7);
                }
            }
            leftDivider.setVisibility(0, newValue);
        });
        validationResultsTableView.visibleProperty().bindBidirectional(showValidationResultsToggleButton.selectedProperty());
        showValidationResultsToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                if (!centerDivider.getItems().contains(validationResultsTableView))
                {
                    centerDivider.getItems().add(validationResultsTableView);
                    centerDivider.setDividerPosition(0, 0.8);
                }
            }
            centerDivider.setVisibility(1, newValue);
        });

        validateEpubButton.disableProperty().bind(currentBookProperty.isNull());
        validationManager.setTableView(validationResultsTableView);

        previewAnchorPane.visibleProperty().bindBidirectional(showPreviewToggleButton.selectedProperty());
        showPreviewToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                if (!rightDivider.getItems().contains(previewAnchorPane))
                {
                    rightDivider.getItems().add(previewAnchorPane);
                    rightDivider.setDividerPosition(0, 0.7);
                }
            }
            rightDivider.setVisibility(0, newValue);
        });
        tocTreeView.visibleProperty().bindBidirectional(showTocToggleButton.selectedProperty());
        showTocToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                if (!rightDivider.getItems().contains(tocTreeView))
                {
                    rightDivider.getItems().add(tocTreeView);
                    rightDivider.setDividerPosition(0, 0.7);
                }
            }
            rightDivider.setVisibility(1, newValue);
        });

        ObservableList<Path> recentFiles = configuration.getRecentFiles();
        recentFiles.addListener((ListChangeListener<Path>) change -> {
            ObservableList<Path> currentRecentFiles = configuration.getRecentFiles();
            createRecentFilesMenuItems(currentRecentFiles);
        });

        centerAnchorPane.getChildren().add(searchAnchorPane);
        AnchorPane.setTopAnchor(searchAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(searchAnchorPane, 0.0);
        AnchorPane.setRightAnchor(searchAnchorPane, 0.0);
        searchAnchorPane.visibleProperty().bind(isNoEditorBinding.not().and(searchReplaceButton.selectedProperty()));
        searchAnchorPane.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                AnchorPane.setTopAnchor(epubFilesTabPane, 70.0);
            }
            else
            {
                AnchorPane.setTopAnchor(epubFilesTabPane, 0.0);
            }
        });
        searchAnchorPane.getCloseButton().setOnAction(event -> {
            searchReplaceButton.setSelected(false);
        });

        searchManager.currentBookProperty().bind(currentBookProperty);

        languageSpellComboBox.setItems(preferencesManager.getLanguageSpellItems());
        //not bind bidiretional, because selectionModel has only read only properties and preferences can not set values from preferences store if property is bind
        preferencesManager.languageSpellSelectionProperty().addListener((observable, oldValue, newValue) -> languageSpellComboBox.getSelectionModel().select(newValue));
        //initialize the value in combobox
        languageSpellComboBox.getSelectionModel().select(preferencesManager.languageSpellSelectionProperty().get());
        languageSpellComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> preferencesManager.languageSpellSelectionProperty().set(newValue));

        languageSpellComboBox.disableProperty().bind(preferencesManager.spellcheckProperty().not());

        deviceWidthComboBox.getSelectionModel().select(0);
        deviceWidthComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            previewManager.changePreviewWidth(newValue.getWidth());
        });
    }

    private void createRecentFilesMenuItems(ObservableList<Path> recentFiles)
    {
        openBookButton.getItems().clear();
        int number = 0;
        for (Path recentFile : recentFiles)
        {
            if (number > EpubEditorConfiguration.RECENT_FILE_NUMBER)
            {
                break;
            }
            MenuItem recentFileMenuItem = new MenuItem(recentFile.toString());
            recentFileMenuItem.setOnAction(event -> {
                EpubReader reader = new EpubReader();
                try
                {
                    File file = recentFile.toFile();
                    Book currentBook = reader.readEpub(file);
                    currentBookProperty.set(currentBook);
                    if (!currentBook.getSpine().isEmpty())
                    {
                        Resource firstResource = currentBook.getSpine().getResource(0);
                        editorTabManager.openFileInEditor(firstResource);
                    }
                }
                catch (IOException e)
                {
                    logger.error("", e);
                    ExceptionDialog.showAndWait(e, stage, "Open ebook", "Can't open ebook file " + recentFile.toFile().getName());
                }
            });
            openBookButton.getItems().add(recentFileMenuItem);
            recentFilesMenuItems.add(recentFileMenuItem);
            number++;
        }
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setTitle("SmoekerSchriever - EpubFX");

        stage.setOnCloseRequest(event -> {
            checkBeforeCloseBook();
            editorTabManager.shutdown();
        });

        stage.getScene().setOnKeyPressed(event -> {
            if ((event.isControlDown() || event.isShortcutDown()) && event.getCode().equals(KeyCode.F))
            {
                logger.debug("Ctrl-F Pressed");
                searchReplaceButton.setSelected(true);
                searchReplaceButtonAction();
            }
        });

        stage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN), this::openEpubAction);
        stage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), this::saveEpubAction);
    }

    public Book getCurrentBook()
    {
        return currentBookProperty.get();
    }

    public ObjectProperty<Book> currentBookProperty()
    {
        return currentBookProperty;
    }

    @SuppressWarnings("UnusedParameters")
    public void newEpubAction(ActionEvent actionEvent)
    {
        Stage windowStage = createStandardController("/new_ebook.fxml", NewEBookController.class);
        NewEBookController controller = NewEBookController.getInstance();

        //ausnahmsweise die currentBookProperty bidirectional binden, damit das Programm das neue Buch mitbekommt
        controller.currentBookProperty().unbind();
        controller.currentBookProperty().bindBidirectional(currentBookProperty);
        windowStage.show();
    }

    public void openEpubAction()
    {
        checkBeforeCloseBook();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open ebook file");
        fileChooser.getExtensionFilters().removeAll();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EPUB File", "*.epub"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null)
        {
            Platform.runLater(() -> {
                stage.getScene().setCursor(Cursor.WAIT);
                EpubReader reader = new EpubReader();
                try
                {
                    Book currentBook = reader.readEpub(file);
                    currentBookProperty.set(currentBook);
                    Platform.runLater(() ->
                        configuration.getRecentFiles().add(0, file.toPath())
                    );
                    if (!currentBook.getSpine().isEmpty())
                    {
                        Resource firstResource = currentBook.getSpine().getResource(0);
                        editorTabManager.openFileInEditor(firstResource);
                    }
                }
                catch (IOException | NavNotFoundException | OpfNotReadableException e)
                {
                    logger.error("", e);
                    ExceptionDialog.showAndWait(e, stage, "Open ebook", "Can't open ebook file: " + file.getName() + ", cause: ");
                }
                finally
                {
                    stage.getScene().setCursor(Cursor.DEFAULT);
                }
            });
        }
    }

    public void addExistingFilesAction()
    {
        addExistingFiles();
    }

    public void addExistingFiles(TreeItem item) {
        addExistingFiles();
    }

    public void addExistingFiles()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Files to Insert");
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files != null)
        {
            for (File file : files)
            {
                MediaType mediaType = MediaType.getByFileName(file.getName());
                String href;
                Book book = currentBookProperty.getValue();
                if (MediaType.CSS.equals(mediaType))
                {
                    href = "Styles/" + file.getName();
                    Resource resource = book.addResourceFromFile(file, href, mediaType);
                    book.getResources().getCssResources().add(resource);
                }
                else if (MediaType.XHTML.equals(mediaType) || MediaType.XML.equals(mediaType))
                {
                    href = "Text/" + file.getName();
                    book.addSpineResourceFromFile(file, href, mediaType);
                }
                else if (mediaType.isImage())
                {
                    href = "Images/" + file.getName();
                    Resource resource = book.addResourceFromFile(file, href, mediaType);
                    book.getResources().getImageResources().add(resource);
                }
                else if (MediaType.JAVASCRIPT.equals(mediaType))
                {
                    href = "Scripts/" + file.getName();
                    book.addResourceFromFile(file, href, mediaType);
                }
                else if (mediaType.isFont())
                {
                    href = "Fonts/" + file.getName();
                    Resource resource = book.addResourceFromFile(file, href, mediaType);
                    book.getResources().getFontResources().add(resource);
                }
                else
                {
                    href = "Misc/" + file.getName();
                    book.addResourceFromFile(file, href, mediaType);
                }
            }
            bookBrowserManager.refreshBookBrowser();
            currentBookProperty.get().setBookIsChanged(true);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void addEmptyHTMLFileAction(ActionEvent actionEvent)
    {
        bookBrowserManager.addEmptyXHTMLFile();
        currentBookProperty.get().setBookIsChanged(true);
    }

    @SuppressWarnings("UnusedParameters")
    public void addEmptyCSSFileAction(ActionEvent actionEvent)
    {
        bookBrowserManager.addEmptyCssFile();
        currentBookProperty.get().setBookIsChanged(true);
    }

    @SuppressWarnings("UnusedParameters")
    public void addEmptySVGFileAction(ActionEvent actionEvent)
    {

        currentBookProperty.get().setBookIsChanged(true);
    }

    public void saveEpubAction()
    {
        Book book = currentBookProperty.get();
        if (book.getPhysicalFileName() == null)
        {
            saveEpubAs();
        }
        else
        {
            saveEpub(book);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void saveEpubAsAction(ActionEvent actionEvent)
    {
        saveEpubAs();
    }

    public void saveEpubAs()
    {
        Book book = currentBookProperty.get();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("EPUB-Datei speichern");
        fileChooser.getExtensionFilters().removeAll();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EPUB-Datei", "*.epub"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null)
        {
            book.setPhysicalFileName(file.toPath());
            saveEpub(book);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void saveEpubCopyAction(ActionEvent actionEvent)
    {
        Book book = currentBookProperty.get();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("EPUB-Datei speichern");
        fileChooser.getExtensionFilters().removeAll();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EPUB-Datei", "*.epub"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null)
        {
            Path oldName = book.getPhysicalFileName();
            book.setPhysicalFileName(file.toPath());
            saveEpub(book);
            book.setPhysicalFileName(oldName);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void saveAsTemplateAction(ActionEvent actionEvent)
    {
    }

    public void saveEpub(Book book)
    {
        EpubWriter writer = new EpubWriter();
        try(OutputStream out = Files.newOutputStream(book.getPhysicalFileName()))
        {
            writer.write(book, out);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        editorTabManager.refreshEditorCode(book.getOpfResource());
        editorTabManager.refreshEditorCode(book.getNcxResource());
        currentBookProperty.get().setBookIsChanged(false);
    }



    public void printPreviewAction(ActionEvent actionEvent)
    {


    }

    public void printAction(ActionEvent actionEvent)
    {


    }

    public void exitAction(ActionEvent actionEvent)
    {
        stage.close();
    }

    public void checkBeforeCloseBook()
    {
        if (currentBookProperty.getValue() != null && currentBookProperty.getValue().getBookIsChanged())
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(stage);
            alert.setTitle("epub4mmee beenden");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("The ebook was changed, save the changes before closing?");
            alert.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> choosedButton = alert.showAndWait();
            choosedButton.ifPresent(buttonType -> {
                if (buttonType.equals(ButtonType.YES)) {
                    Book book = currentBookProperty.get();
                    if (book.getPhysicalFileName() == null)
                    {
                        saveEpubAs();
                    }
                    else
                    {
                        saveEpub(book);
                    }
                }
            });
        }
    }

    public void addCoverAction(ActionEvent actionEvent)
    {
        Stage addCoverStage = new Stage(StageStyle.UTILITY);
        try
        {
            FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/add_cover.fxml"), null, new JavaFXBuilderFactory(),
                    type -> BeanFactory.getInstance().getBean(type));
            Pane root = loader.load();

            Scene scene = new Scene(root);

            AddCoverController addCoverController = AddCoverController.getInstance();
            addCoverController.setBook(currentBookProperty.getValue());
            addCoverController.setStage(addCoverStage);

            addCoverStage.setScene(scene);
            addCoverStage.initOwner(stage);
            addCoverStage.initModality(Modality.APPLICATION_MODAL);
        }
        catch (IOException e)
        {
            logger.error("can't open edit window", e);
        }
        addCoverStage.show();
    }

    public void editMetadataAction(ActionEvent actionEvent)
    {
        Stage editMetadataStage = new Stage(StageStyle.UTILITY);
        try
        {

            Pane root;
            if (currentBookProperty.getValue().isEpub3())
            {
                FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/metadata_editor_epub3.fxml"), null, new JavaFXBuilderFactory(),
                        type -> BeanFactory.getInstance().getBean(type));
                root = loader.load();

                Epub3EditMetadataController controller = Epub3EditMetadataController.getInstance();
                controller.setBook(currentBookProperty.getValue());
                controller.setStage(editMetadataStage);
            }
            else
            {
                FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/metadata_editor.fxml"), null, new JavaFXBuilderFactory(),
                        type -> BeanFactory.getInstance().getBean(type));
                root = loader.load();

                EditMetadataController controller = EditMetadataController.getInstance();
                controller.setBook(currentBookProperty.getValue());
                controller.setStage(editMetadataStage);
            }
            Scene scene = new Scene(root);
            editMetadataStage.setScene(scene);
            editMetadataStage.initOwner(stage);
            editMetadataStage.initModality(Modality.APPLICATION_MODAL);
        }
        catch (IOException e)
        {
            logger.error("can't open edit window", e);
        }
        editMetadataStage.show();
    }

    public void h1ButtonAction(ActionEvent actionEvent)
    {
        editorTabManager.surroundParagraphWithTag("h1");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h2ButtonAction(ActionEvent actionEvent)
    {
        editorTabManager.surroundParagraphWithTag("h2");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h3ButtonAction(ActionEvent actionEvent)
    {
        editorTabManager.surroundParagraphWithTag("h3");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h4ButtonAction(ActionEvent actionEvent)
    {
        editorTabManager.surroundParagraphWithTag("h4");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h5ButtonAction(ActionEvent actionEvent)
    {
        editorTabManager.surroundParagraphWithTag("h5");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h6ButtonAction(ActionEvent actionEvent)
    {
        editorTabManager.surroundParagraphWithTag("h6");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void paragraphButtonAction()
    {
        editorTabManager.surroundParagraphWithTag("p");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void quotationMarksButtonAction()
    {
        String selectedQuotationMark = preferencesManager.getQuotationMarkSelection();
        logger.info("select quotation mark " + selectedQuotationMark);
        QuotationMark quotationMark = QuotationMark.findByDescription(selectedQuotationMark);
        editorTabManager.surroundSelection(quotationMark.getLeft(), quotationMark.getRight());
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void singleQuotationMarksButtonAction()
    {
        String selectedQuotationMark = preferencesManager.getQuotationMarkSelection();
        logger.info("select quotation mark " + selectedQuotationMark);
        QuotationMark quotationMark = QuotationMark.findByDescription(selectedQuotationMark);
        editorTabManager.surroundSelection(quotationMark.getSingleLeft(), quotationMark.getSingleRight());
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void blockQuoteButtonAction() {
        editorTabManager.surroundSelectionWithTag("blockquote");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void boldButtonAction()
    {
        editorTabManager.surroundSelectionWithTag("b");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void italicButtonAction()
    {
        editorTabManager.surroundSelectionWithTag("i");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void nonBreakingSpaceButtonAction() {
        editorTabManager.insertAtCursorPositionOrReplaceSelection("&#160;");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void hrButtonAction() {
        editorTabManager.insertAtCursorPositionOrReplaceSelection("<hr />");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void ellipsisButtonAction() {
        editorTabManager.insertAtCursorPositionOrReplaceSelection("…");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void orderedListButtonAction()
    {
        insertList("ol");
    }

    public void unorderedListButtonAction() {
        insertList("ul");
    }

    private void insertList(String tagName) {
        String tab;
        int tabOffset;
        if (preferencesManager.isUseTab()) {
            tab = "\t";
            tabOffset = 1;
        } else {
            tab = StringUtils.repeat(" ", preferencesManager.getTabSize());
            tabOffset = preferencesManager.getTabSize();
        }

        String olString = "<" + tagName + ">\n" + tab + "<li></li>\n</" + tagName + ">";
        editorTabManager.insertAtCursorPosition(olString, 9 + tabOffset);
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void underlineButtonAction()
    {
        editorTabManager.surroundSelectionWithTag("u");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void strikeButtonAction()
    {
        editorTabManager.surroundSelectionWithTag("s");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void subscriptButtonAction()
    {
        editorTabManager.surroundSelectionWithTag("sub");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void superscriptButtonAction()
    {
        editorTabManager.surroundSelectionWithTag("sup");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void alignLeftButtonAction()
    {
        editorTabManager.insertStyle("text-align", "left");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void centerButtonAction()
    {
        editorTabManager.insertStyle("text-align", "center");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void rightAlignButtonAction()
    {
        editorTabManager.insertStyle("text-align", "right");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void justifyButtonAction()
    {
        editorTabManager.insertStyle("text-align", "justify");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void undoButtonAction()
    {
        CodeEditor currentEditor = editorTabManager.currentEditorProperty().get();
        if (currentEditor != null)
        {
            currentEditor.undo();
        }
    }

    public void redoButtonAction()
    {
        CodeEditor currentEditor = editorTabManager.currentEditorProperty().get();
        if (currentEditor != null)
        {
            currentEditor.redo();
        }
    }

    public void cutButtonAction()
    {
        editorTabManager.cutSelection();
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void copyButtonAction()
    {
        editorTabManager.copySelection();
        //only copying text is not a change of the book
    }

    public void pasteButtonAction()
    {
        editorTabManager.pasteFromClipboard();
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void searchReplaceButtonAction()
    {
        //set search string also if pane is already open
        String selection = editorTabManager.getCurrentEditor().getSelection();
        searchAnchorPane.setSearchString(selection);
    }

    public void splitButtonAction()
    {
        boolean success = editorTabManager.splitXHTMLFile();
        if (success)
        {
            currentBookProperty.get().setBookIsChanged(true);
        }
    }

    public void insertImageButtonAction()
    {
        if (editorTabManager.isInsertablePosition())
        {
            createAndOpenStandardController("/insert_media.fxml", InsertMediaController.class);
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Einfügen nicht möglich");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("Kann Bild bzw. Mediendatei nicht an dieser Position einfügen. Dies ist nur innerhalb des XHTML-Bodys möglich.");
            alert.showAndWait();
        }
    }

    public void clipEditorAction()
    {
        createAndOpenStandardController("/clip_editor.fxml", ClipEditorController.class);
    }


    public void insertSpecialCharacterAction()
    {
    }

    public void createTocAction()
    {
        Stage stage = createStandardController("/create_toc.fxml", GenerateTocController.class);
        GenerateTocController controller = GenerateTocController.getInstance();
        controller.setEditMode(false);
        stage.show();
    }

    public void editTocAction()
    {
        Stage stage = createStandardController("/create_toc.fxml", GenerateTocController.class);
        GenerateTocController controller = GenerateTocController.getInstance();
        controller.setEditMode(true);
        stage.show();
    }


    public void increaseIndentButtonAction()
    {
        editorTabManager.increaseIndent();
    }

    public void decreaseIndentButtonAction()
    {
        editorTabManager.decreaseIndent();
    }

    public void insertTableButtonAction()
    {
        if (editorTabManager.isInsertablePosition())
        {
            createAndOpenStandardController("/insert-table.fxml", InsertTableController.class);
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Insert not possible");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("Can't insert table on this position. This is only available within the body of the document.");
            alert.showAndWait();
        }
    }

    private void createAndOpenStandardController(String fxmlFile, Class<? extends StandardController> controllerClass)
    {
        Stage windowStage = createStandardController(fxmlFile, controllerClass);
        windowStage.show();
    }

    public Stage createStandardController(String fxmlFile, Class<? extends StandardController> controllerClass)
    {
        Method staticMethod;
        StandardController controller;
        try
        {
            staticMethod = controllerClass.getMethod("getInstance");
            controller = (StandardController) staticMethod.invoke(controllerClass);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            logger.error("", e);
            return null;
        }

        Stage windowStage = null;
        if (controller == null)
        {
            try
            {
                windowStage = new Stage(StageStyle.UTILITY);

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile), null, new JavaFXBuilderFactory(),
                        type -> BeanFactory.getInstance().getBean(type));

                Pane root = loader.load();
                Scene scene = new Scene(root);
                windowStage.setScene(scene);
                windowStage.initOwner(stage);
                windowStage.initModality(Modality.APPLICATION_MODAL);

                controller = (StandardController) staticMethod.invoke(controllerClass);
                controller.currentBookProperty().bind(currentBookProperty);
                controller.setStage(windowStage);
            }
            catch (IOException | IllegalAccessException | InvocationTargetException e)
            {
                logger.error("", e);
            }
        }
        else
        {
            windowStage = controller.getStage();
        }
        return windowStage;
    }

    public void newMinimalEpubAction()
    {
        Book minimalBook = Book.createMinimalBook();
        currentBookProperty.set(minimalBook);
        currentBookProperty.get().setBookIsChanged(false);
    }

    public void previewZoomIn()
    {
        double oldZoom = previewWebview.getZoom();
        previewWebview.setZoom(oldZoom + 0.1);
    }

    public void preview100PercentZoom()
    {
        previewWebview.setZoom(1.0);
    }

    public void previewZoomOut()
    {
        double oldZoom = previewWebview.getZoom();
        previewWebview.setZoom(oldZoom - 0.1);
    }

    public ToggleButton getShowBookBrowserToggleButton()
    {
        return showBookBrowserToggleButton;
    }

    public ToggleButton getShowPreviewToggleButton()
    {
        return showPreviewToggleButton;
    }

    public ToggleButton getShowTocToggleButton()
    {
        return showTocToggleButton;
    }

    public ToggleButton getShowValidationResultsToggleButton()
    {
        return showValidationResultsToggleButton;
    }

    public ToggleButton getShowClipsToggleButton()
    {
        return showClipsToggleButton;
    }

    public SplitPane getMainDivider()
    {
        return mainDivider;
    }

    public StashableSplitPane getCenterDivider()
    {
        return centerDivider;
    }

    public StashableSplitPane getRightDivider()
    {
        return rightDivider;
    }

    public StashableSplitPane getLeftDivider()
    {
        return leftDivider;
    }

    public void generateUuidAction()
    {
        Book book = currentBookProperty.getValue();
        book.getMetadata().generateNewUuid();
        bookBrowserManager.refreshOpf();
        bookBrowserManager.refreshNcx();
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void createNcxAction()
    {
        TocGenerator.TocGeneratorResult result = tocGenerator.createNcxFromNav();
        Map<Resource, Document> allResourcesToRewrite = result.getResourcesToRewrite();

        for (Resource resource : allResourcesToRewrite.keySet())
        {
            resource.setData(XHTMLUtils.outputXHTMLDocument(allResourcesToRewrite.get(resource), getCurrentBook().getVersion()));
            editorTabManager.refreshEditorCode(resource);
        }

        bookBrowserManager.refreshBookBrowser();
        editorTabManager.refreshEditorCode(result.getTocResource());
        getCurrentBook().setBookIsChanged(true);
    }

    public void createHtmlTocAction()
    {

    }

    public void insertLinkAction()
    {
        if (editorTabManager.isInsertablePosition())
        {
            createAndOpenStandardController("/insert-link.fxml", InsertLinkController.class);
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Insert not possible");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("Can't insert link on this position. This is only available within the body of the document.");
            alert.showAndWait();
        }
    }

    public void uppercaseButtonAction() {
        editorTabManager.toUpperCase();
    }

    public void lowercaseButtonAction() {
        editorTabManager.toLowerCase();
    }

    public void settingsButtonAction()
    {
        preferencesManager.showPreferencesDialog();
    }

    public void validateEpubButton() {
        validationManager.startValidationEpub(currentBookProperty.get().getPhysicalFileName());
        getShowValidationResultsToggleButton().selectedProperty().set(true);
    }

    public void checkLinksButton() {
    }
}
