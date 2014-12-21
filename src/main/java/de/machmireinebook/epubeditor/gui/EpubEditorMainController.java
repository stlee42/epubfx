package de.machmireinebook.epubeditor.gui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Named;

import de.machmireinebook.commons.cdi.BeanFactory;
import de.machmireinebook.commons.javafx.FXUtils;
import de.machmireinebook.commons.javafx.control.searchable.TreeViewSearchable;
import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TOCReference;
import de.machmireinebook.epubeditor.epublib.epub.EpubReader;
import de.machmireinebook.epubeditor.epublib.epub.EpubWriter;
import de.machmireinebook.epubeditor.httpserver.EpubHttpHandler;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.manager.HTMLEditorManager;
import de.machmireinebook.epubeditor.manager.ImageViewerManager;
import de.machmireinebook.epubeditor.manager.PreviewManager;
import de.machmireinebook.epubeditor.manager.TOCViewManager;

import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
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
import javafx.util.Duration;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.Dialogs;

/**
 * User: mjungierek
 * Date: 07.02.14
 * Time: 19:09
 */
@Named
public class EpubEditorMainController implements Initializable
{
    public static final Logger logger = Logger.getLogger(EpubEditorMainController.class);
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
    private Button searchReplaceButton;
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
    private MenuItem addCoverMenuItem;
    @FXML
    private MenuItem editMetadataMenuItem;
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

    private ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>();
    private Stage stage;
    private static EpubEditorMainController instance;

    @Inject
    private BookBrowserManager bookBrowserManager;
    @Inject
    private HTMLEditorManager editorManager;
    @Inject
    private ImageViewerManager imageViewerManager;
    @Inject
    private PreviewManager previewManager;
    @Inject
    private TOCViewManager tocViewManager;


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        TreeViewSearchable<Resource> searchable = new TreeViewSearchable<>(epubStructureTreeView);
        searchable.setRecursive(true);

        bookBrowserManager.setTreeView(epubStructureTreeView);
        bookBrowserManager.setEditorManager(editorManager);
        bookBrowserManager.setImageViewerManager(imageViewerManager);

        epubFilesTabPane.getTabs().clear();
        epubFilesTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        editorManager.setTabPane(epubFilesTabPane);
        editorManager.setBookBrowserManager(bookBrowserManager);
        imageViewerManager.setTabPane(epubFilesTabPane);

        previewManager.setWebview(previewWebview);
        previewManager.setEditorManager(editorManager);

        tocViewManager.setTreeView(tocTreeView);
        tocViewManager.setEditorManager(editorManager);

        currentBookProperty.addListener(new ChangeListener<Book>()
        {
            @Override
            public void changed(ObservableValue<? extends Book> observable, Book oldValue, Book newValue)
            {
                epubFilesTabPane.getTabs().clear();

                bookBrowserManager.setBook(newValue);
                tocViewManager.setBook(newValue);
                editorManager.reset();
                editorManager.setBook(newValue);
                imageViewerManager.reset();
                imageViewerManager.setBook(newValue);
                previewManager.reset();
            }
        });

        addCoverMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        editMetadataMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        saveMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        savAsMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        saveCopyMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        printPreviewMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        printMenuItem.disableProperty().bind(Bindings.isNull(currentBookProperty));
        addMenu.disableProperty().bind(Bindings.isNull(currentBookProperty));

        BooleanBinding isNoXhtmlEditorBinding = Bindings.isNull(currentBookProperty).or(Bindings.not(editorManager.currentEditorIsXHTMLProperty())
                .or(Bindings.isEmpty(epubFilesTabPane.getTabs())));
        BooleanBinding bookIsChangedBinding;
        if (currentBookProperty.get() == null)
        {
            bookIsChangedBinding = Bindings.isNull(currentBookProperty);
        }
        else
        {
            bookIsChangedBinding = Bindings.isNull(currentBookProperty).or(currentBookProperty.get().bookIsChangedProperty());
        }

        h1Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h2Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h3Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h4Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h5Button.disableProperty().bind(isNoXhtmlEditorBinding);
        h6Button.disableProperty().bind(isNoXhtmlEditorBinding);
        paragraphButton.disableProperty().bind(isNoXhtmlEditorBinding);

        boldButton.setGraphic(FXUtils.getIcon("/icons/font_style_bold.png", 18));
        boldButton.disableProperty().bind(isNoXhtmlEditorBinding);

        kursivButton.setGraphic(FXUtils.getIcon("/icons/font_style_italics.png", 18));
        kursivButton.disableProperty().bind(isNoXhtmlEditorBinding);

        orderedListButton.setGraphic(FXUtils.getIcon("/icons/list_style_numbered.png", 18));
        orderedListButton.disableProperty().bind(isNoXhtmlEditorBinding);

        unorderedListButton.setGraphic(FXUtils.getIcon("/icons/list_style_bullets.png", 18));
        unorderedListButton.disableProperty().bind(isNoXhtmlEditorBinding);

        underlineButton.setGraphic(FXUtils.getIcon("/icons/font_style_underline.png", 18));
        underlineButton.disableProperty().bind(isNoXhtmlEditorBinding);

        strikeButton.setGraphic(FXUtils.getIcon("/icons/font_style_strikethrough.png", 18));
        strikeButton.disableProperty().bind(isNoXhtmlEditorBinding);

        subscriptButton.setGraphic(FXUtils.getIcon("/icons/font_style_subscript.png", 18));
        subscriptButton.disableProperty().bind(isNoXhtmlEditorBinding);

        superscriptButton.setGraphic(FXUtils.getIcon("/icons/font_style_superscript.png", 18));
        superscriptButton.disableProperty().bind(isNoXhtmlEditorBinding);

        alignLeftButton.setGraphic(FXUtils.getIcon("/icons/text_align_left.png", 18));
        alignLeftButton.disableProperty().bind(isNoXhtmlEditorBinding);

        centerButton.setGraphic(FXUtils.getIcon("/icons/text_align_center.png", 18));
        centerButton.disableProperty().bind(isNoXhtmlEditorBinding);

        rightAlignButton.setGraphic(FXUtils.getIcon("/icons/text_align_right.png", 18));
        rightAlignButton.disableProperty().bind(isNoXhtmlEditorBinding);

        justifyButton.setGraphic(FXUtils.getIcon("/icons/text_align_justified.png", 18));
        justifyButton.disableProperty().bind(isNoXhtmlEditorBinding);

        newBookButton.setGraphic(FXUtils.getIcon("/icons/book2_new.png", 18));
        openBookButton.setGraphic(FXUtils.getIcon("/icons/book_open.png", 18));

        addFileButton.setGraphic(FXUtils.getIcon("/icons/document_empty_add.png", 18));
        addFileButton.disableProperty().bind(Bindings.isNull(currentBookProperty));

        addExistingFileButton.setGraphic(FXUtils.getIcon("/icons/document_text_add.png", 18));
        addExistingFileButton.disableProperty().bind(Bindings.isNull(currentBookProperty));

        saveButton.setGraphic(FXUtils.getIcon("/icons/floppy_disk.png", 18));
        saveButton.disableProperty().bind(bookIsChangedBinding);

        undoButton.setGraphic(FXUtils.getIcon("/icons/undo.png", 18));
        redoButton.setGraphic(FXUtils.getIcon("/icons/redo.png", 18));

        searchReplaceButton.disableProperty().bind(Bindings.isNull(currentBookProperty).or(Bindings.isEmpty(epubFilesTabPane.getTabs())));

        increaseIndentButton.disableProperty().bind(isNoXhtmlEditorBinding);
        decreaseIndentButton.disableProperty().bind(isNoXhtmlEditorBinding);
        saveAsButton.disableProperty().bind(Bindings.isNull(currentBookProperty));
        insertImageButton.disableProperty().bind(isNoXhtmlEditorBinding);
        splitButton.disableProperty().bind(isNoXhtmlEditorBinding);
        insertTableButton.disableProperty().bind(isNoXhtmlEditorBinding);
        insertSpecialCharacterButton.disableProperty().bind(isNoXhtmlEditorBinding);

        cursorPosLabel.textProperty().bind(editorManager.cursorPosLabelProperty());

        //Teile der Oberfläche an-/abschalten, per Binding an die Menüeinträge
        clipListView.visibleProperty().bindBidirectional(clipsMenuItem.selectedProperty());
        clipsMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!newValue)
                {
                    leftDivider.getItems().remove(clipListView);
                }
                else
                {
                    leftDivider.getItems().add(clipListView);
                    leftDivider.setDividerPosition(0, 0.7);
                }
            }
        });
        epubStructureTreeView.visibleProperty().bindBidirectional(showBookBrowserMenuItem.selectedProperty());
        showBookBrowserMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue)
                {
                    leftDivider.getItems().add(epubStructureTreeView);
                    leftDivider.setDividerPosition(0, 0.7);
                }
                else
                {
                    leftDivider.getItems().remove(epubStructureTreeView);
                }
            }
        });
        validationResultsListView.visibleProperty().bindBidirectional(showValidationResultsMenuItem.selectedProperty());
        showValidationResultsMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!newValue)
                {
                    centerDivider.getItems().remove(validationResultsListView);
                }
                else
                {
                    centerDivider.getItems().add(validationResultsListView);
                    centerDivider.setDividerPosition(0, 0.8);
                }
            }
        });
        previewAnchorPane.visibleProperty().bindBidirectional(showPreviewMenuItem.selectedProperty());
        showPreviewMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (newValue)
                {
                    rightDivider.getItems().add(previewAnchorPane);
                    rightDivider.setDividerPosition(0, 0.7);
                }
                else
                {
                    rightDivider.getItems().remove(previewAnchorPane);
                }
            }
        });
        tocTreeView.visibleProperty().bindBidirectional(showTocMenuItem.selectedProperty());
        showTocMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!newValue)
                {
                    rightDivider.getItems().remove(tocTreeView);
                }
                else
                {
                    rightDivider.getItems().add(tocTreeView);
                    rightDivider.setDividerPosition(0, 0.7);
                }
            }
        });

        instance = this;
        //erst jetzt configuration lesen, da alles gesetzt ist
        EpubEditorConfiguration.getInstance().readConfiguration();
    }

    public static EpubEditorMainController getInstance()
    {
        return instance;
    }


    public void setStage(Stage stage)
    {
        this.stage = stage;

        //tastencodes erst jetzt setzen, da scene in init noch nicht vorhanden
        setAccelerator(saveButton, KeyCode.S, KeyCombination.SHORTCUT_DOWN);
    }

    private void setAccelerator(Button button, KeyCode keyCode, KeyCombination.Modifier modifier)
    {
        Scene scene = stage.getScene();
        if (scene == null) {
            throw new IllegalArgumentException("setAccelerator must be called when a button is attached to a scene");
        }

        scene.getAccelerators().put(
                new KeyCodeCombination(keyCode, modifier),
                () -> fireButton(button)
        );
    }

    private void fireButton(final Button button) {
        button.arm();
        PauseTransition pt = new PauseTransition(Duration.millis(300));
        pt.setOnFinished(event -> {
            button.fire();
            button.disarm();
        });
        pt.play();
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
            }
            catch (IOException e)
            {
                logger.error("", e);
                Dialogs.create()
                        .owner(stage)
                        .title("E-Book öffnen")
                        .message("Kann E-Book-Datei " + file.getName()  + " nicht öffnen.")
                        .showException(e);
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
        chooser.setTitle("Einzufügende Dateien auswählen");
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
                        Dialogs.create()
                                .owner(stage)
                                .title("Datei hinzugefügen")
                                .message("Kann Datei " + file.getName()  + "  nicht hinzufügen.")
                                .showException(e);
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
            saveEpubAsAction(actionEvent);
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


    public void setEpubHttpHandler(EpubHttpHandler epubHttpHandler)
    {
        if (epubHttpHandler != null)
        {
            currentBookProperty.addListener((observable, oldValue, newValue) -> epubHttpHandler.setBook(currentBookProperty.get()));
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

            EditMetadataController controller = EditMetadataController.getInstance();
            controller.setBook(currentBookProperty.getValue());
            controller.setStage(editMetadataStage);

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
    }

    public void centerButtonAction(ActionEvent actionEvent)
    {
    }

    public void rightAlignButtonAction(ActionEvent actionEvent)
    {
    }

    public void justifyButtonAction(ActionEvent actionEvent)
    {
    }

    public void undoButtonAction(ActionEvent actionEvent)
    {
        

    }

    public void redoButtonAction(ActionEvent actionEvent)
    {
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

    public void searchReplaceButtonAction(ActionEvent actionEvent)
    {
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
            Dialogs.create()
                    .owner(stage)
                    .title("Einfügen nicht möglich")
                    .message("Kann Bild bzw. Mediendatei nicht an dieser Position einfügen. Dies ist nur innerhalb des XHTML-Bodys möglich.")
                    .showWarning();
        }
    }

    public void insertSpecialCharacterAction(ActionEvent actionEvent)
    {
    }

    public void createTocAction(ActionEvent actionEvent)
    {


    }

    public void increaseIndentButtonAction(ActionEvent actionEvent)
    {


    }

    public void decreaseIndentButtonAction(ActionEvent actionEvent)
    {


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

    public BookBrowserManager getBookBrowserManager()
    {
        return bookBrowserManager;
    }

    public HTMLEditorManager getEditorManager()
    {
        return editorManager;
    }

    public PreviewManager getPreviewManager()
    {
        return previewManager;
    }

    public TOCViewManager getTocViewManager()
    {
        return tocViewManager;
    }

    public void newMinimalEpubAction()
    {
        Book minimalBook = Book.createMinimalBook();
        currentBookProperty.set(minimalBook);
        currentBookProperty.get().setBookIsChanged(true);
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

    public void setShowBookBrowserMenuItem(RadioMenuItem showBookBrowserMenuItem)
    {
        this.showBookBrowserMenuItem = showBookBrowserMenuItem;
    }

    public RadioMenuItem getShowPreviewMenuItem()
    {
        return showPreviewMenuItem;
    }

    public void setShowPreviewMenuItem(RadioMenuItem showPreviewMenuItem)
    {
        this.showPreviewMenuItem = showPreviewMenuItem;
    }

    public RadioMenuItem getShowTocMenuItem()
    {
        return showTocMenuItem;
    }

    public void setShowTocMenuItem(RadioMenuItem showTocMenuItem)
    {
        this.showTocMenuItem = showTocMenuItem;
    }

    public RadioMenuItem getShowValidationResultsMenuItem()
    {
        return showValidationResultsMenuItem;
    }

    public void setShowValidationResultsMenuItem(RadioMenuItem showValidationResultsMenuItem)
    {
        this.showValidationResultsMenuItem = showValidationResultsMenuItem;
    }

    public RadioMenuItem getClipsMenuItem()
    {
        return clipsMenuItem;
    }

    public void setClipsMenuItem(RadioMenuItem clipsMenuItem)
    {
        this.clipsMenuItem = clipsMenuItem;
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
}
