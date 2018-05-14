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
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.EpubEditorStarter;
import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TOCReference;
import de.machmireinebook.epubeditor.epublib.epub.EpubReader;
import de.machmireinebook.epubeditor.epublib.epub.EpubWriter;
import de.machmireinebook.epubeditor.httpserver.EpubHttpHandler;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.manager.EditorTabManager;
import de.machmireinebook.epubeditor.manager.PreviewManager;
import de.machmireinebook.epubeditor.manager.SearchManager;
import de.machmireinebook.epubeditor.manager.TOCViewManager;

import org.apache.log4j.Logger;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import jidefx.scene.control.searchable.TreeViewSearchable;

/**
 * User: mjungierek
 * Date: 07.02.14
 * Time: 19:09
 */
@Singleton
public class EpubEditorMainController implements Initializable
{
    private static final Logger logger = Logger.getLogger(EpubEditorMainController.class);
    @FXML
    private Menu fileMenu;
    @FXML
    private SeparatorMenuItem recentFilesSeparatorMenuItem;
    @FXML
    private MenuItem insertImageMenuItem;
    @FXML
    private SplitPane mainDivider;
    @FXML
    private SplitPane centerDivider;
    @FXML
    private SplitPane rightDivider;
    @FXML
    private AnchorPane previewAnchorPane;
    @FXML
    private RadioMenuItem showBookBrowserMenuItem;
    @FXML
    private RadioMenuItem showPreviewMenuItem;
    @FXML
    private RadioMenuItem showTocMenuItem;
    @FXML
    private RadioMenuItem showValidationResultsMenuItem;
    @FXML
    private Button insertTableButton;
    @FXML
    private SplitPane leftDivider;
    @FXML
    private RadioMenuItem clipsMenuItem;
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
    private Button openBookButton;
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
    private Button kursivButton;
    @FXML
    private Menu addMenu;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem savAsMenuItem;
    @FXML
    private MenuItem saveCopyMenuItem;
    @FXML
    private MenuItem printPreviewMenuItem;
    @FXML
    private MenuItem printMenuItem;
    @FXML
    private ListView<Resource> clipListView;
    @FXML
    private ListView validationResultsListView;
    @FXML
    private TreeView<TOCReference> tocTreeView;
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

    private ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>();
    private List<MenuItem> recentFilesMenuItems = new ArrayList<>();
    private Stage stage;

    @Inject
    private BookBrowserManager bookBrowserManager;
    @Inject
    private EditorTabManager editorManager;
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

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        TreeViewSearchable<Resource> searchable = new TreeViewSearchable<>(epubStructureTreeView);
        searchable.setRecursive(true);

        bookBrowserManager.setTreeView(epubStructureTreeView);
        bookBrowserManager.setEditorManager(editorManager);

        epubFilesTabPane.getTabs().clear();
        epubFilesTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        editorManager.setTabPane(epubFilesTabPane);
        editorManager.setBookBrowserManager(bookBrowserManager);

        previewManager.setWebview(previewWebview);
        previewManager.setEditorManager(editorManager);

        tocViewManager.setTreeView(tocTreeView);
        tocViewManager.setEditorManager(editorManager);

        currentBookProperty.addListener((observable, oldValue, newValue) -> {
            epubFilesTabPane.getTabs().clear();

            bookBrowserManager.setBook(newValue);
            tocViewManager.setBook(newValue);
            editorManager.reset();
            editorManager.setBook(newValue);
            previewManager.reset();
            saveButton.disableProperty().unbind();
            if (newValue != null)
            {
                saveButton.disableProperty().bind(newValue.bookIsChangedProperty().not());
                createHtmlTocButton.disableProperty().unbind();
                createHtmlTocButton.disableProperty().bind(Bindings.equal(currentBookProperty.get().versionProperty(), EpubVersion.VERSION_2).not());
                createNcxButton.disableProperty().unbind();
                createNcxButton.disableProperty().bind(Bindings.equal(currentBookProperty.get().versionProperty(), EpubVersion.VERSION_2));
            }

        });
        BooleanBinding isNoXhtmlEditorBinding = Bindings.isNull(currentBookProperty).or(Bindings.not(editorManager.currentEditorIsXHTMLProperty())
                .or(Bindings.isEmpty(epubFilesTabPane.getTabs())));
        BooleanBinding isNoEditorBinding = Bindings.isNull(currentBookProperty)
                .or(Bindings.isEmpty(epubFilesTabPane.getTabs()))
                .or(Bindings.isNull(editorManager.currentXHTMLResourceProperty())
                .and(Bindings.isNull(editorManager.currentCssResourceProperty()))
                .and(Bindings.isNull(editorManager.currentXMLResourceProperty())));

        addCoverButton.disableProperty().bind(Bindings.isNull(currentBookProperty));
        editMetadataButton.disableProperty().bind(Bindings.isNull(currentBookProperty));
        saveMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        savAsMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        saveCopyMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        printPreviewMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        printMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        addMenu.disableProperty().bind(Bindings.isNull(currentBookProperty));

        insertImageMenuItem.disableProperty().bind(isNoXhtmlEditorBinding);

        h1Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h2Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h3Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h4Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h5Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h6Button.disableProperty().bind(isNoXhtmlEditorBinding);
        paragraphButton.disableProperty().bind(isNoXhtmlEditorBinding);

        boldButton.disableProperty().bind(isNoXhtmlEditorBinding);
        kursivButton.disableProperty().bind(isNoXhtmlEditorBinding);
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
        addFileButton.disableProperty().bind(Bindings.isNull(currentBookProperty));
        addExistingFileButton.disableProperty().bind(Bindings.isNull(currentBookProperty));

        saveButton.setDisable(true);
        undoButton.disableProperty().bind(isNoXhtmlEditorBinding.or(Bindings.not(editorManager.canUndoProperty())));
        redoButton.disableProperty().bind(isNoXhtmlEditorBinding.or(Bindings.not(editorManager.canRedoProperty())));

        searchReplaceButton.disableProperty().bind(Bindings.isNull(currentBookProperty).or(Bindings.isEmpty(epubFilesTabPane.getTabs())));

        increaseIndentButton.disableProperty().bind(isNoXhtmlEditorBinding);
        decreaseIndentButton.disableProperty().bind(isNoXhtmlEditorBinding);
        saveAsButton.disableProperty().bind(Bindings.isNull(currentBookProperty));
        insertImageButton.disableProperty().bind(isNoXhtmlEditorBinding);
        splitButton.disableProperty().bind(isNoXhtmlEditorBinding);
        insertTableButton.disableProperty().bind(isNoXhtmlEditorBinding);
        insertSpecialCharacterButton.disableProperty().bind(isNoXhtmlEditorBinding);
        insertLinkButton.disableProperty().bind(isNoXhtmlEditorBinding);
        lowercaseButton.disableProperty().bind(isNoXhtmlEditorBinding);
        uppercaseButton.disableProperty().bind(isNoXhtmlEditorBinding);

        createHtmlTocButton.disableProperty().bind(Bindings.isNull(currentBookProperty));
        createNcxButton.disableProperty().bind(Bindings.isNull(currentBookProperty));

        cursorPosLabel.textProperty().bind(editorManager.cursorPosLabelProperty());

        //Teile der Oberfläche an-/abschalten, per Binding an die Menüeinträge
        clipListView.visibleProperty().bindBidirectional(clipsMenuItem.selectedProperty());
        clipsMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
            {
                leftDivider.getItems().remove(clipListView);
            }
            else
            {
                leftDivider.getItems().add(clipListView);
                leftDivider.setDividerPosition(0, 0.7);
            }
        });
        epubStructureTreeView.visibleProperty().bindBidirectional(showBookBrowserMenuItem.selectedProperty());
        showBookBrowserMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                leftDivider.getItems().add(epubStructureTreeView);
                leftDivider.setDividerPosition(0, 0.7);
            }
            else
            {
                leftDivider.getItems().remove(epubStructureTreeView);
            }
        });
        validationResultsListView.visibleProperty().bindBidirectional(showValidationResultsMenuItem.selectedProperty());
        showValidationResultsMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
            {
                centerDivider.getItems().remove(validationResultsListView);
            }
            else
            {
                centerDivider.getItems().add(validationResultsListView);
                centerDivider.setDividerPosition(0, 0.8);
            }
        });
        previewAnchorPane.visibleProperty().bindBidirectional(showPreviewMenuItem.selectedProperty());
        showPreviewMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                rightDivider.getItems().add(previewAnchorPane);
                rightDivider.setDividerPosition(0, 0.7);
            }
            else
            {
                rightDivider.getItems().remove(previewAnchorPane);
            }
        });
        tocTreeView.visibleProperty().bindBidirectional(showTocMenuItem.selectedProperty());
        showTocMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
            {
                rightDivider.getItems().remove(tocTreeView);
            }
            else
            {
                rightDivider.getItems().add(tocTreeView);
                rightDivider.setDividerPosition(0, 0.7);
            }
        });

        ObservableList<Path> recentFiles = configuration.getRecentFiles();
        createRecentFilesMenuItems(recentFiles);
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
    }

    private void createRecentFilesMenuItems(ObservableList<Path> recentFiles)
    {
        recentFilesSeparatorMenuItem.setVisible(recentFiles.size() > 0);
        for (MenuItem recentFilesMenuItem : recentFilesMenuItems)
        {
            fileMenu.getItems().remove(recentFilesMenuItem);
        }
        recentFilesMenuItems.clear();

        int index = fileMenu.getItems().indexOf(recentFilesSeparatorMenuItem);
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
                }
                catch (IOException e)
                {
                    logger.error("", e);
                    ExceptionDialog.showAndWait(e, stage, "Open ebook", "Can't open ebook file " + recentFile.toFile().getName());
                }
            });
            fileMenu.getItems().add(index, recentFileMenuItem);
            recentFilesMenuItems.add(recentFileMenuItem);
            index++;
            number++;
        }
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setTitle("Epub FX");

        stage.setOnCloseRequest(event -> {
            checkBeforeCloseBook();
        });

        stage.getScene().setOnKeyPressed(event -> {
            if ((event.isControlDown() || event.isShortcutDown()) && event.getCode().equals(KeyCode.F))
            {
                logger.debug("Ctrl-F Pressed");
                searchReplaceButton.setSelected(true);
                searchReplaceButtonAction();
            }
        });
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

    @SuppressWarnings("UnusedParameters")
    public void openEpubAction(ActionEvent actionEvent)
    {
        checkBeforeCloseBook();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("EPUB-Datei öffnen");
        fileChooser.getExtensionFilters().removeAll();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EPUB-Datei", "*.epub"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null)
        {
            stage.getScene().setCursor(Cursor.WAIT);
            EpubReader reader = new EpubReader();
            try
            {
                Book currentBook = reader.readEpub(file);
                currentBookProperty.set(currentBook);
                configuration.getRecentFiles().add(0, file.toPath());
            }
            catch (IOException e)
            {
                logger.error("", e);
                ExceptionDialog.showAndWait(e, stage, "E-Book öffnen", "Kann E-Book-Datei " + file.getName()  + " nicht öffnen.");
            }
            finally
            {
                stage.getScene().setCursor(Cursor.DEFAULT);
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void addExistingFilesAction(ActionEvent actionEvent)
    {
        addExistingFiles();
    }

    public void addExistingFiles(TreeItem item)
    {
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
                boolean addToSpine = false;
                if (MediaType.CSS.equals(mediaType))
                {
                    href = "Styles/" + file.getName();
                }
                else if (MediaType.XHTML.equals(mediaType) || MediaType.XML.equals(mediaType))
                {
                    href = "Text/" + file.getName();
                    addToSpine = true;
                }
                else if (mediaType.isBitmapImage())
                {
                    href = "Images/" + file.getName();
                }
                else if (MediaType.JAVASCRIPT.equals(mediaType))
                {
                    href = "Scripts/" + file.getName();
                }
                else if (mediaType.isFont())
                {
                    href = "Fonts/" + file.getName();
                }
                else
                {
                    href = "Misc/" + file.getName();
                }
                Book book = currentBookProperty.getValue();
                if (addToSpine)
                {
                    try
                    {
                        book.addSpineResourceFromFile(file, href, mediaType);
                    }
                    catch (IOException e)
                    {
                        logger.error("", e);
                        ExceptionDialog.showAndWait(e, stage, "Datei hinzugefügen", "Kann Datei " + file.getName()  + "  nicht hinzufügen." );
                    }
                }
                else
                {
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

        currentBookProperty.get().setBookIsChanged(true);
    }

    @SuppressWarnings("UnusedParameters")
    public void addEmptySVGFileAction(ActionEvent actionEvent)
    {

        currentBookProperty.get().setBookIsChanged(true);
    }

    @SuppressWarnings("UnusedParameters")
    public void saveEpubAction(ActionEvent actionEvent)
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
        currentBookProperty.get().setBookIsChanged(false);
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
        currentBookProperty.get().setBookIsChanged(false);
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
            alert.setContentText("Das E-Book wurde geändert. " +
                    "Sollen die Änderungen gespeichert werden?");
            alert.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> choosedButton = alert.showAndWait();
            if (choosedButton.isPresent() && choosedButton.get().equals(ButtonType.YES))
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
        }
    }

    public void setEpubHttpHandler(EpubHttpHandler epubHttpHandler)
    {
        if (epubHttpHandler != null)
        {
            epubHttpHandler.bookProperty().bind(currentBookProperty);
        }
    }

    public void addCoverAction(ActionEvent actionEvent)
    {
        Stage addCoverStage = new Stage(StageStyle.UTILITY);
        try
        {
            FXMLLoader loader = new FXMLLoader(EpubEditorMainController.class.getResource("/add_cover.fxml"), null, new JavaFXBuilderFactory(),
                    type -> BeanFactory.getInstance().getBean(type));
            Pane root = loader.load();

            Scene scene = new Scene(root);

            AddCoverController addCoverController = AddCoverController.getInstance();
            addCoverController.setBook(currentBookProperty.getValue());
            addCoverController.setStage(addCoverStage);
            addCoverController.setBookBrowserManager(bookBrowserManager);

            addCoverStage.setScene(scene);
            addCoverStage.initOwner(stage);
            addCoverStage.initModality(Modality.APPLICATION_MODAL);
        }
        catch (IOException e)
        {
            logger.error("cannot open edit window", e);
        }
        addCoverStage.show();
    }

    public void editMetadataAction(ActionEvent actionEvent)
    {
        Stage editMetadataStage = new Stage(StageStyle.UTILITY);
        try
        {
            FXMLLoader loader = new FXMLLoader(EpubEditorMainController.class.getResource("/metadata_editor.fxml"), null, new JavaFXBuilderFactory(),
                    type -> BeanFactory.getInstance().getBean(type));
            Pane root = loader.load();

            Scene scene = new Scene(root);

            if (currentBookProperty.getValue().isEpub3())
            {
                Epub3EditMetadataController controller = Epub3EditMetadataController.getInstance();
                controller.setBook(currentBookProperty.getValue());
                controller.setStage(editMetadataStage);
            }
            else
            {
                EditMetadataController controller = EditMetadataController.getInstance();
                controller.setBook(currentBookProperty.getValue());
                controller.setStage(editMetadataStage);
            }

            editMetadataStage.setScene(scene);
            editMetadataStage.initOwner(stage);
            editMetadataStage.initModality(Modality.APPLICATION_MODAL);
        }
        catch (IOException e)
        {
            logger.error("cannot open edit window", e);
        }
        editMetadataStage.show();
    }

    public void h1ButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundParagraphWithTag("h1");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h2ButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundParagraphWithTag("h2");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h3ButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundParagraphWithTag("h3");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h4ButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundParagraphWithTag("h4");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h5ButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundParagraphWithTag("h5");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void h6ButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundParagraphWithTag("h6");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void paragraphButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundParagraphWithTag("p");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void boldButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundSelectionWithTag("b");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void kursivButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundSelectionWithTag("i");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void orderedListButtonAction(ActionEvent actionEvent)
    {


    }

    public void unorderedListButtonAction(ActionEvent actionEvent)
    {
    }

    public void underlineButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundSelectionWithTag("u");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void strikeButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundSelectionWithTag("strike");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void subscriptButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundSelectionWithTag("sub");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void superscriptButtonAction(ActionEvent actionEvent)
    {
        editorManager.surroundSelectionWithTag("sup");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void alignLeftButtonAction(ActionEvent actionEvent)
    {
        editorManager.insertStyle("text-align", "left");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void centerButtonAction(ActionEvent actionEvent)
    {
        editorManager.insertStyle("text-align", "center");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void rightAlignButtonAction(ActionEvent actionEvent)
    {
        editorManager.insertStyle("text-align", "right");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void justifyButtonAction(ActionEvent actionEvent)
    {
        editorManager.insertStyle("text-align", "justify");
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void undoButtonAction(ActionEvent actionEvent)
    {
        CodeEditor currentEditor = editorManager.currentEditorProperty().get();
        if (currentEditor != null)
        {
            currentEditor.undo();
        }
    }

    public void redoButtonAction(ActionEvent actionEvent)
    {
        CodeEditor currentEditor = editorManager.currentEditorProperty().get();
        if (currentEditor != null)
        {
            currentEditor.redo();
        }
    }

    public void cutButtonAction(ActionEvent actionEvent)
    {
    }

    public void copyButtonAction(ActionEvent actionEvent)
    {
    }

    public void pasteButtonAction(ActionEvent actionEvent)
    {
    }

    public void searchReplaceButtonAction()
    {
        //set search string also if pane is already open
        String selection = editorManager.getCurrentEditor().getSelection();
        searchAnchorPane.setSearchString(selection);
    }

    public void splitButtonAction(ActionEvent actionEvent)
    {
        boolean success = editorManager.splitXHTMLFile();
        if (success)
        {
            currentBookProperty.get().setBookIsChanged(true);
        }
    }

    public void insertImageButtonAction(ActionEvent actionEvent)
    {
        if (editorManager.isInsertablePosition())
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

    public void clipEditorAction(ActionEvent actionEvent)
    {
        createAndOpenStandardController("/clip_editor.fxml", ClipEditorController.class);
    }


    public void insertSpecialCharacterAction(ActionEvent actionEvent)
    {
    }

    public void createTocAction(ActionEvent actionEvent)
    {


    }

    public void increaseIndentButtonAction(ActionEvent actionEvent)
    {
        editorManager.increaseIndent();

    }

    public void decreaseIndentButtonAction(ActionEvent actionEvent)
    {
        editorManager.decreaseIndent();
    }

    public void insertTableButtonAction(ActionEvent actionEvent)
    {


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

    public void previewZoomIn(ActionEvent actionEvent)
    {
        double oldZoom = previewWebview.getZoom();
        previewWebview.setZoom(oldZoom + 0.1);
    }

    public void preview100PercentZoom(ActionEvent actionEvent)
    {
        previewWebview.setZoom(1.0);
    }

    public void previewZoomOut(ActionEvent actionEvent)
    {
        double oldZoom = previewWebview.getZoom();
        previewWebview.setZoom(oldZoom - 0.1);
    }

    public RadioMenuItem getShowBookBrowserMenuItem()
    {
        return showBookBrowserMenuItem;
    }

    public RadioMenuItem getShowPreviewMenuItem()
    {
        return showPreviewMenuItem;
    }

    public RadioMenuItem getShowTocMenuItem()
    {
        return showTocMenuItem;
    }

    public RadioMenuItem getShowValidationResultsMenuItem()
    {
        return showValidationResultsMenuItem;
    }

    public RadioMenuItem getClipsMenuItem()
    {
        return clipsMenuItem;
    }

    public SplitPane getMainDivider()
    {
        return mainDivider;
    }

    public void setMainDivider(SplitPane mainDivider)
    {
        this.mainDivider = mainDivider;
    }

    public SplitPane getCenterDivider()
    {
        return centerDivider;
    }

    public void setCenterDivider(SplitPane centerDivider)
    {
        this.centerDivider = centerDivider;
    }

    public SplitPane getRightDivider()
    {
        return rightDivider;
    }

    public void setRightDivider(SplitPane rightDivider)
    {
        this.rightDivider = rightDivider;
    }

    public SplitPane getLeftDivider()
    {
        return leftDivider;
    }

    public void setLeftDivider(SplitPane leftDivider)
    {
        this.leftDivider = leftDivider;
    }

    public void generateUuidAction(ActionEvent actionEvent)
    {
        Book book = currentBookProperty.getValue();
        book.getMetadata().generateNewUuid();
        bookBrowserManager.refreshOpf();
        currentBookProperty.get().setBookIsChanged(true);
    }

    public void replaceAction(ActionEvent actionEvent)
    {
    }

    public void replaceAllAction(ActionEvent actionEvent)
    {
    }

    public void findReplaceAction(ActionEvent actionEvent)
    {
    }

    public void findBeforeAction(ActionEvent actionEvent)
    {
    }

    public void findNextAction(ActionEvent actionEvent)
    {
    }

    public void showAddMoreFilesAction(ActionEvent actionEvent)
    {

    }

    public void editTocAction(ActionEvent actionEvent)
    {

    }

    public void createNcxAction(ActionEvent actionEvent)
    {

    }

    public void createHtmlTocAction(ActionEvent actionEvent)
    {

    }

    public void insertLinkAction(ActionEvent actionEvent)
    {

    }

    public void uppercaseButtonAction(ActionEvent actionEvent)
    {

    }

    public void lowercaseButtonAction(ActionEvent actionEvent)
    {

    }

    public void quotationMarksButtonAction(ActionEvent actionEvent)
    {


    }

    public void settingsButtonAction(ActionEvent actionEvent)
    {
        StringProperty stringProperty = new SimpleStringProperty("String");
        BooleanProperty booleanProperty = new SimpleBooleanProperty(true);
        IntegerProperty integerProperty = new SimpleIntegerProperty(12);
        DoubleProperty doubleProperty = new SimpleDoubleProperty(6.5);

        PreferencesFx preferencesFx = PreferencesFx.of(EpubEditorStarter.class,
                Category.of("Category title 1",
                        Setting.of("Setting title 1", stringProperty), // creates a group automatically
                        Setting.of("Setting title 2", booleanProperty) // which contains both settings
                ),
                Category.of("Category title 2")
                        .subCategories( // adds a subcategory to "Category title 2"
                                Category.of("Category title 3",
                                        Group.of("Group title 1",
                                                Setting.of("Setting title 3", integerProperty)
                                        ),
                                        Group.of( // group without title
                                                Setting.of("Setting title 3", doubleProperty)
                                        )
                                )
                        )        );
        preferencesFx.show();
    }
}
