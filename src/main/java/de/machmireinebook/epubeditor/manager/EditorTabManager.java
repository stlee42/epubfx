package de.machmireinebook.epubeditor.manager;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.fxmisc.richtext.CodeArea;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.clips.Clip;
import de.machmireinebook.epubeditor.clips.ClipManager;
import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.editor.CssRichTextCodeEditor;
import de.machmireinebook.epubeditor.editor.EditorPosition;
import de.machmireinebook.epubeditor.editor.XMLTagPair;
import de.machmireinebook.epubeditor.editor.XhtmlCodeEditor;
import de.machmireinebook.epubeditor.editor.XhtmlRichTextCodeEditor;
import de.machmireinebook.epubeditor.editor.XmlCodeEditor;
import de.machmireinebook.epubeditor.editor.XmlRichTextCodeEditor;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.bookprocessor.HtmlCleanerBookProcessor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.epub2.PackageDocumentReader;
import de.machmireinebook.epubeditor.epublib.epub3.Epub3PackageDocumentReader;
import de.machmireinebook.epubeditor.epublib.resource.CSSResource;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.ResourceDataException;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.resource.XMLResource;
import de.machmireinebook.epubeditor.gui.ExceptionDialog;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 20:29
 */
@Singleton
public class EditorTabManager {
    private static final Logger logger = Logger.getLogger(EditorTabManager.class);

    private TabPane tabPane;
    private ObjectProperty<CodeEditor> currentEditor = new SimpleObjectProperty<>();
    //getrennte Verwaltung der current resource für html und css, da der Previewer auf der html property lauscht und
    // wenn ein css bearbeitet wird, das letzte html-doument weiterhin im previewer angezeigt werden soll
    private ReadOnlyObjectWrapper<Resource> currentSearchableResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<XHTMLResource> currentXHTMLResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<CSSResource> currentCssResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<Resource> currentXMLResource = new ReadOnlyObjectWrapper<>();
    private BooleanProperty needsRefresh = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty currentEditorIsXHTML = new SimpleBooleanProperty();
    private SimpleBooleanProperty canUndo = new SimpleBooleanProperty();
    private SimpleBooleanProperty canRedo = new SimpleBooleanProperty();
    private StringProperty cursorPosLabelProperty = new SimpleStringProperty();
    private final ReadOnlyIntegerWrapper currentLineProperty = new ReadOnlyIntegerWrapper(this, "currentLine");

    private Book book;
    private ContextMenu contextMenuXHTML;
    private ContextMenu contextMenuXML;
    private ContextMenu contextMenuCSS;

    @Inject
    private ClipManager clipManager;
    @Inject
    private BookBrowserManager bookBrowserManager;
    @Inject
    private PreferencesManager preferencesManager;
    @Inject
    @XhtmlCodeEditor
    private Provider<XhtmlRichTextCodeEditor> xhtmlEditorProvider;
    @Inject
    @XmlCodeEditor
    private Provider<XmlRichTextCodeEditor> xmlEditorProvider;
    @Inject
    private Provider<CssRichTextCodeEditor> cssEditorProvider;

    private boolean openingEditorTab = false;
    private boolean refreshAllInProgress = false;
    private boolean refreshAll = false;


    public class ImageViewerPane extends ScrollPane implements Initializable {
        @FXML
        private ImageView imageView;
        @FXML
        private Label imagePropertiesLabel;
        @FXML
        private VBox vBox;
        private ImageResource imageResource;

        public ImageViewerPane() {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/image_view.fxml"), null, new JavaFXBuilderFactory(),
                    type -> BeanFactory.getInstance().getBean(type));
            loader.setRoot(this);
            loader.setController(this);

            try {
                loader.load();
            }
            catch (IOException e) {
                ExceptionDialog.showAndWait(e, null, "Show Image", "Error while opening image.");
                logger.error("", e);
            }
        }

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            vBox.minWidthProperty().bind(this.widthProperty());
            vBox.minHeightProperty().bind(this.heightProperty());
        }

        public Label getImagePropertiesLabel() {
            return imagePropertiesLabel;
        }

        public void setImagePropertiesLabel(Label imagePropertiesLabel) {
            this.imagePropertiesLabel = imagePropertiesLabel;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public void setImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        public ImageResource getImageResource() {
            return imageResource;
        }

        public void setImageResource(ImageResource imageResource) {
            this.imageResource = imageResource;
        }
    }

    @PostConstruct
    public void init() {
        currentEditor.addListener((observable, oldValue, newValue) -> {
            canUndo.unbind();
            canRedo.unbind();
            if (newValue != null) {
                currentEditorIsXHTML.setValue(currentEditor.getValue().getMediaType().equals(MediaType.XHTML));
                canUndo.bind(currentEditor.getValue().canUndoProperty());
                canRedo.bind(currentEditor.getValue().canRedoProperty());
            }
            else {
                currentEditorIsXHTML.setValue(false);
                canUndo.setValue(false);
                canRedo.setValue(false);
            }
        });

        MenuItem separatorItem = new SeparatorMenuItem();
        //Html menu
        contextMenuXHTML = new ContextMenu();
        contextMenuXHTML.getStyleClass().add("context-menu");
        contextMenuXHTML.setAutoFix(true);
        contextMenuXHTML.setAutoHide(true);

        Menu clipsItem = new Menu("Text Snippets");
        clipManager.clipsRootProperty().addListener(event -> {
            logger.info("whole clips tree changed, (re)build the clip menu");
            clipsItem.getItems().clear();
            writeClipMenuItemChildren(clipManager.getClipsRoot(), clipsItem);
        });
        clipManager.setOnClipsTreeChanged(c -> {
            clipsItem.getItems().clear();
            writeClipMenuItemChildren(clipManager.getClipsRoot(), clipsItem);
        });
        contextMenuXHTML.getItems().add(clipsItem);
        contextMenuXHTML.getItems().add(separatorItem);

        MenuItem itemRepairHTML = new MenuItem("Repair and format HTML");
        itemRepairHTML.setOnAction(e -> {
            repairHTML();
        });
        contextMenuXHTML.getItems().add(itemRepairHTML);

        MenuItem itemUnescapeHTML = new MenuItem("Unescape HTML");
        itemUnescapeHTML.setOnAction(e -> {
            unescapeHTML();
        });
        contextMenuXHTML.getItems().add(itemUnescapeHTML);

        contextMenuXHTML.getItems().add(separatorItem);

        MenuItem openInExternalBrowserItem = new MenuItem("Open in external Browser");
        openInExternalBrowserItem.setOnAction(e -> {
            openInExternalBrowser(currentEditor);
        });
        contextMenuXHTML.getItems().add(openInExternalBrowserItem);

        //XML menu
        contextMenuXML = new ContextMenu();
        contextMenuXML.getStyleClass().add("context-menu");
        contextMenuXML.setAutoFix(true);
        contextMenuXML.setAutoHide(true);

        MenuItem generateUuidMenuItem = new MenuItem("Generate new UUID");
        generateUuidMenuItem.setOnAction(e -> {
            book.getMetadata().generateNewUuid();
            bookBrowserManager.refreshOpf();
        });
        contextMenuXML.getItems().add(generateUuidMenuItem);
        currentXMLResource.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && currentXMLResource.get().mediaTypeProperty().getValue().equals(MediaType.OPF)) {
                generateUuidMenuItem.visibleProperty().setValue(true);
            }
            else {
                generateUuidMenuItem.visibleProperty().setValue(false);
            }
        });

        MenuItem separatorItem2 = new SeparatorMenuItem();
        contextMenuXML.getItems().add(separatorItem2);
        separatorItem2.visibleProperty().bind(generateUuidMenuItem.visibleProperty());

        MenuItem itemRepairXML = new MenuItem("Repair and format XML");
        itemRepairXML.setOnAction(e -> {
            repairXML();
        });
        contextMenuXML.getItems().add(itemRepairXML);

        //css menu
        contextMenuCSS = new ContextMenu();
        contextMenuCSS.getStyleClass().add("context-menu");
        contextMenuCSS.setAutoFix(true);
        contextMenuCSS.setAutoHide(true);
        MenuItem formatCSSOneLineItem = new MenuItem("Format styles in one line");
        formatCSSOneLineItem.setOnAction(e -> beautifyCSS("one_line"));
        contextMenuCSS.getItems().add(formatCSSOneLineItem);

        MenuItem formatCSSMultipleLinesItem = new MenuItem("Format styles in multiple lines");
        formatCSSMultipleLinesItem.setOnAction(e -> beautifyCSS("multiple_lines"));
        contextMenuCSS.getItems().add(formatCSSMultipleLinesItem);
    }

    @PreDestroy
    public void shutdown() {
        logger.info("pre destroy");
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            if (tab.getContent() instanceof CodeEditor) {
                ((CodeEditor)tab.getContent()).shutdown();
            }
        }
    }

    private void writeClipMenuItemChildren(TreeItem<Clip> parentTreeItem, Menu parentMenu) {
        List<TreeItem<Clip>> children = parentTreeItem.getChildren();
        for (TreeItem<Clip> child : children) {
            if (child.getValue().isGroup()) {
                Menu menu = new Menu(child.getValue().getName());
                parentMenu.getItems().add(menu);
                writeClipMenuItemChildren(child, menu);
            }
            else {
                MenuItem menuItem = new MenuItem(child.getValue().getName());
                parentMenu.getItems().add(menuItem);
                menuItem.setOnAction(event -> {
                    insertClip(child.getValue());
                });
            }
        }
    }

    private void insertClip(Clip clip) {
        CodeEditor editor = currentEditor.getValue();
        String selection = editor.getSelection();
        String insertedClip = selection.replaceAll("(?s)^(.*)$", clip.getContent());
        editor.replaceSelection(insertedClip);
        book.setBookIsChanged(true);
        editor.requestFocus();
    }

    private void beautifyCSS(String type) {


    }

    private void openInExternalBrowser(ObjectProperty<CodeEditor> currentEditor) {


    }

    private void repairHTML() {
        logger.info("beautifying html");
        CodeEditor editor = currentEditor.getValue();
        Integer currentCursorPosition = editor.getAbsoluteCursorPosition();
        String code = editor.getCode();
        if (currentEditorIsXHTML.get()) {
            Resource resource = currentXHTMLResource.get();
            code = repairXHTML(code);
            resource.setData(code.getBytes(StandardCharsets.UTF_8));
        }
        editor.setCode(code);
        editor.setAbsoluteCursorPosition(currentCursorPosition);
        editor.scrollTo(currentCursorPosition);
        book.setBookIsChanged(true);
    }

    private void repairXML() {
    }

    private void unescapeHTML() {
        logger.info("unescape html");
        CodeEditor editor = currentEditor.getValue();
        Integer currentCursorPosition = editor.getAbsoluteCursorPosition();
        String code = editor.getCode();
        if (currentEditorIsXHTML.get()) {
            Resource resource = currentXHTMLResource.get();
            code = XHTMLUtils.unescapedHtmlWithXmlExceptions(code);
            resource.setData(code.getBytes(StandardCharsets.UTF_8));
        }
        refreshPreview();
        editor.setCode(code);
        editor.setAbsoluteCursorPosition(currentCursorPosition);
        editor.scrollTo(currentCursorPosition);
        book.setBookIsChanged(true);
        editor.requestFocus();
    }

    public void openImageFile(Resource resource) {
        Tab tab = new Tab();
        tab.setClosable(true);
        if (resource == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(tabPane.getScene().getWindow());
            alert.setTitle("Image not found");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("The image does not exist and cannot be opened");
            alert.showAndWait();

            return;
        }
        tab.setText(resource.getFileName());

        ImageResource imageResource = (ImageResource) resource;
        ImageViewerPane pane = new ImageViewerPane();
        pane.setImageResource(imageResource);

        ImageView imageView = pane.getImageView();
        imageView.setImage(imageResource.asNativeFormat());
        imageView.setFitHeight(-1);
        imageView.setFitWidth(-1);

        Label imagePropertiesLabel = pane.getImagePropertiesLabel();
        imagePropertiesLabel.setText(imageResource.getImageDescription());

        tab.setContent(pane);
        tab.setUserData(resource);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }


    private boolean isTabAlreadyOpen(Resource resource) {
        boolean found = false;
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            if (tab.getUserData().equals(resource)) {
                tabPane.getSelectionModel().select(tab);
                found = true;
            }
        }
        return found;
    }

    public void openFileInEditor(Resource resource) throws IllegalArgumentException {
        openFileInEditor(resource, resource.getMediaType());
    }

    public void openFileInEditor(Resource resource, MediaType mediaType) throws IllegalArgumentException {
        if (!isTabAlreadyOpen(resource)) {
            Tab tab = new Tab();
            tab.setClosable(true);
            if (resource == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(tabPane.getScene().getWindow());
                alert.setTitle("File not found");
                alert.getDialogPane().setHeader(null);
                alert.getDialogPane().setHeaderText(null);
                alert.setContentText("The file does not exist and cannot be opened");
                alert.showAndWait();

                return;
            }
            tab.setText(resource.getFileName());
            resource.hrefProperty().addListener((observable, oldValue, newValue) -> {
                tab.setText(resource.getFileName());
            });

            CodeEditor editor;
            if (mediaType.equals(MediaType.CSS)) {
                editor = cssEditorProvider.get();
                editor.setContextMenu(contextMenuCSS);
            }
            else if (mediaType.equals(MediaType.XHTML)) {
                editor = xhtmlEditorProvider.get();
                editor.setContextMenu(contextMenuXHTML);
                ((XHTMLResource)resource).prepareWebViewDocument(book.getVersion());
            }
            else if (mediaType.equals(MediaType.XML) || mediaType.equals(MediaType.OPF)) {
                editor = xmlEditorProvider.get();
                editor.setContextMenu(contextMenuXML);
            }
            else {
                throw new IllegalArgumentException("no editor for mediatype " + mediaType.getName());
            }
            tab.setOnCloseRequest(event -> editor.shutdown());

            tab.setContent((Node) editor);
            tab.setUserData(resource);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

            final String code = StringUtils.toEncodedString(resource.getData(), Charsets.toCharset(resource.getInputEncoding()));
            editor.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equals(Worker.State.SUCCEEDED)) {
                    openingEditorTab = true;
                    editor.setCode(code);
                    editor.clearUndoHistory();
                    editor.setCodeEditorSize(((AnchorPane) editor).getWidth() - 20, ((AnchorPane) editor).getHeight() - 20);
                    ((AnchorPane) editor).widthProperty().addListener((observable15, oldValue14, newValue14) -> editor.setCodeEditorSize(newValue14.doubleValue() - 20, ((AnchorPane) editor).getHeight() - 20));
                    ((AnchorPane) editor).heightProperty().addListener((observable12, oldValue1, newValue1) -> editor.setCodeEditorSize(((AnchorPane) editor).getWidth() - 20, newValue1.doubleValue() - 20));
                    editor.setCodeEditorSize(((AnchorPane) editor).getWidth() - 20, ((AnchorPane) editor).getHeight() - 20);
                    ((AnchorPane) editor).widthProperty().addListener((observable13, oldValue12, newValue12) -> editor.setCodeEditorSize(newValue12.doubleValue() - 20, ((AnchorPane) editor).getHeight() - 20));
                    ((AnchorPane) editor).heightProperty().addListener((observable14, oldValue13, newValue13) -> editor.setCodeEditorSize(((AnchorPane) editor).getWidth() - 20, newValue13.doubleValue() - 20));
                    editor.scrollTo(EditorPosition.START);
                    editor.setAbsoluteCursorPosition(0);
                    editor.requestFocus();
                    openingEditorTab = false;
                }
            });

            editor.cursorPositionProperty().addListener((observable, oldValue, newValue) -> {
                EditorPosition cursorPosition = editor.getCursorPosition();
                String textIformation = editor.getTextInformation();
                cursorPosLabelProperty.set("Absolute: " + newValue + ", Relative: " + (cursorPosition.getLine() + 1) + ":" + (cursorPosition.getColumn() + 1)
                        + " | Text Information: " + StringUtils.defaultString(textIformation, ""));
            });

            CodeArea codeArea = editor.getCodeArea();
            codeArea.multiPlainChanges()
                    .successionEnds(java.time.Duration.ofMillis(500))
                    .subscribe(plainTextChanges -> {
                        logger.info("subscribing eventstream");
                        //the openingEditorTab and refreshAllInProgress shows that a code change is in progress, dont reset it here,
                        // the other two that code changes are done, reset that the next changes are executed
                        if (openingEditorTab || refreshAllInProgress || editor.isChangingCode()  || refreshAll) {
                            editor.resetChangingCode();
                            refreshAll = false;
                            return;
                        }
                        CodeEditor codeEditor = currentEditor.getValue();
                        if (codeEditor.getMediaType().equals(MediaType.XHTML)) {
                            currentXHTMLResource.get().setData(codeEditor.getCode().getBytes(StandardCharsets.UTF_8));
                            currentXHTMLResource.get().prepareWebViewDocument(book.getVersion());
                        }
                        else if (codeEditor.getMediaType().equals(MediaType.CSS)) {
                            currentCssResource.get().setData(codeEditor.getCode().getBytes(StandardCharsets.UTF_8));
                        }
                        else if (codeEditor.getMediaType().equals(MediaType.XML)) {
                            try {
                                currentXMLResource.get().setData(codeEditor.getCode().getBytes(StandardCharsets.UTF_8));
                                if (((XMLResource) resource).isValidXML() && MediaType.OPF.equals(resource.getMediaType())) {
                                    if (book.isEpub3()) {
                                        Epub3PackageDocumentReader.read(resource, book);
                                    }
                                    else {
                                        PackageDocumentReader.read(resource, book);
                                    }
                                }
                            }
                            catch (JDOMException | IOException e) {
                                logger.error("", e);
                            }
                        }
                        book.setBookIsChanged(true);
                    });

            codeArea.multiPlainChanges()
                    .successionEnds(java.time.Duration.ofMillis(1000))
                    .subscribe(plainTextChanges -> {
                        logger.info("scheduled refresh task, one second after last change");
                        Platform.runLater(() -> {
                            needsRefresh.setValue(true);
                            needsRefresh.setValue(false);
                        });
                    });
            //snychronise caret position with web view
            if (mediaType.equals(MediaType.XHTML)) {
                codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
                    logger.debug("caret position " + newValue);
                    Optional<XMLTagPair> pairOptional = ((XhtmlRichTextCodeEditor)editor).findSurroundingTags(new XhtmlRichTextCodeEditor.HtmlLayoutTagInspector());
                    pairOptional.ifPresent(xmlTagPair -> currentLineProperty.set(xmlTagPair.getTagParagraphIndex() + 1));
                });
            }
        }
    }

    public Resource getCurrentSearchableResource() {
        return currentSearchableResource.get();
    }

    public ReadOnlyObjectProperty<Resource> currentSearchableResourceProperty() {
        return currentSearchableResource.getReadOnlyProperty();
    }

    public Resource getCurrentXHTMLResource() {
        return currentXHTMLResource.get();
    }

    public ReadOnlyObjectProperty<XHTMLResource> currentXHTMLResourceProperty() {
        return currentXHTMLResource.getReadOnlyProperty();
    }

    public Resource getCurrentCssResource() {
        return currentCssResource.get();
    }

    public ReadOnlyObjectProperty<CSSResource> currentCssResourceProperty() {
        return currentCssResource.getReadOnlyProperty();
    }

    public Resource getCurrentXMLResource() {
        return currentXMLResource.get();
    }

    public ReadOnlyObjectProperty<Resource> currentXMLResourceProperty() {
        return currentXMLResource.getReadOnlyProperty();
    }

    public BooleanProperty needsRefreshProperty() {
        return needsRefresh;
    }

    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Resource resource;
            if (newValue != null && newValue.getContent() instanceof CodeEditor) {
                CodeEditor selectedEditor = (CodeEditor) newValue.getContent();
                resource = (Resource) newValue.getUserData();
                currentSearchableResource.set(resource);
                currentEditor.setValue(selectedEditor);

                if (selectedEditor.getMediaType().equals(MediaType.XHTML) && resource instanceof XHTMLResource) {
                    currentXHTMLResource.set((XHTMLResource) resource);
                    currentLineProperty.set(selectedEditor.getCodeArea().getCurrentParagraph());
                }
                else if (selectedEditor.getMediaType().equals(MediaType.CSS) && resource instanceof CSSResource) {
                    currentCssResource.set((CSSResource) resource);
                }
                else if (selectedEditor.getMediaType().equals(MediaType.XML)) {
                    currentXMLResource.set(resource);
                }
            }
        });
    }

    public void scrollTo(EditorPosition position) {
        CodeEditor codeEditor = currentEditor.getValue();
        codeEditor.scrollTo(position);
        codeEditor.requestFocus();
    }

    public void insertAtCursorPosition(String text) {
        if (isInsertablePosition()) {
            CodeEditor editor = getCurrentEditor();
            Integer cursorPosition = editor.getAbsoluteCursorPosition();
            editor.insertAt(cursorPosition, text);
            refreshPreview();
        }
    }

    /**
     * Inserts the given text at caret position and moves the caret to a new position,
     * the new position is given as difference from the current position
     *
     * @param text text to insert
     * @param moveCaretIndex difference of the current position to that the caret should be moved
     */
    public void insertAtCursorPosition(String text, int moveCaretIndex) {
        if (isInsertablePosition() && currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            CodeEditor editor = getCurrentEditor();
            Integer cursorPosition = editor.getAbsoluteCursorPosition();
            editor.insertAt(cursorPosition, text);
            editor.setAbsoluteCursorPosition(cursorPosition + moveCaretIndex);
            refreshPreview();
            editor.requestFocus();
        }
    }

    public void surroundParagraphWithTag(String tagName) {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            xhtmlCodeEditor.surroundParagraphWithTag(tagName);
            refreshPreview();
            xhtmlCodeEditor.requestFocus();
        }
    }

    public void insertStyle(String styleName, String value) {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            xhtmlCodeEditor.insertStyle(styleName, value);
            refreshPreview();
            xhtmlCodeEditor.requestFocus();
        }
    }

    public void surroundSelectionWithTag(String tagName) {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            String selection = currentEditor.get().getSelection();
            currentEditor.get().replaceSelection("<" + tagName + ">" + selection + "</" + tagName + ">");
            refreshPreview();
            currentEditor.get().requestFocus();
        }
    }

    public void surroundSelection(String start, String end) {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            String selection = currentEditor.get().getSelection();
            currentEditor.get().replaceSelection(start + selection + end);
            refreshPreview();
            currentEditor.get().requestFocus();
        }
    }

    public void increaseIndent() {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            xhtmlCodeEditor.increaseIndent();
        }
        currentEditor.get().requestFocus();
    }

    public void decreaseIndent() {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            xhtmlCodeEditor.decreaseIndent();
        }
        currentEditor.get().requestFocus();
    }

    public boolean splitXHTMLFile() {
        boolean result = false;
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            Optional<XMLTagPair> optional = xhtmlCodeEditor.findSurroundingTags(tagName -> "head".equals(tagName) || "body".equals(tagName) || "html".equals(tagName));
            if (optional.isPresent()) {
                XMLTagPair pair = optional.get();
                if ("head".equals(pair.getTagName()) || "html".equals(pair.getTagName()) || StringUtils.isEmpty(pair.getTagName())) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.initOwner(tabPane.getScene().getWindow());
                    alert.setTitle("Split not possible");
                    alert.getDialogPane().setHeader(null);
                    alert.getDialogPane().setHeaderText(null);
                    alert.setContentText("Can't split file at current cursor position. Split is available only within xhtml body.");
                    alert.showAndWait();

                    return false;
                }
                logger.debug("umgebendes pair " + pair.getTagName());
                //wir sind innerhalb des Body
                int index = xhtmlCodeEditor.getAbsoluteCursorPosition();
                try {
                    String originalCode = xhtmlCodeEditor.getCode();
                    List<Content> originalHeadContent = xhtmlCodeEditor.getHeadContent();

                    byte[] frontPart = originalCode.substring(0, index).getBytes(StandardCharsets.UTF_8);
                    Resource oldResource = currentXHTMLResource.getValue();
                    oldResource.setData(frontPart);
                    HtmlCleanerBookProcessor processor = new HtmlCleanerBookProcessor();
                    processor.processResource(oldResource, book);
                    xhtmlCodeEditor.setCode(new String(oldResource.getData(), StandardCharsets.UTF_8));

                    byte[] backPart = originalCode.substring(index, originalCode.length() - 1).getBytes(StandardCharsets.UTF_8);
                    String fileName = book.getNextStandardFileName(MediaType.XHTML);
                    Resource resource = MediaType.XHTML.getResourceFactory().createResource("Text/" + fileName);
                    byte[] backPartXHTML = XHTMLUtils.repairWithHead(backPart, originalHeadContent);
                    resource.setData(backPartXHTML);

                    int spineIndex = book.getSpine().getResourceIndex(oldResource);
                    book.addSpineResource(resource, spineIndex + 1);
                    openFileInEditor(resource, MediaType.XHTML);

                    bookBrowserManager.refreshBookBrowser();
                    currentXHTMLResource.set((XHTMLResource) resource);
                    needsRefresh.setValue(true);
                    needsRefresh.setValue(false);
                }
                catch (IOException | JDOMException | ResourceDataException e) {
                    logger.error("", e);
                    ExceptionDialog.showAndWait(e, null, "Split not possible", "Can't split file because unknown error.");
                }
                result = true;
            }
        }
        return result;
    }

    public void toUpperCase() {
        CodeEditor codeEditor = currentEditor.get();
        String selectedText = codeEditor.getSelection();
        Locale spellcheckLocale = preferencesManager.getLanguageSpellSelection().getLanguage().getLocaleWithCountryAndVariant();
        String uppercaseText = StringUtils.upperCase(selectedText, spellcheckLocale);
        codeEditor.replaceSelection(uppercaseText);
        currentEditor.get().requestFocus();
    }

    public void toLowerCase() {
        CodeEditor codeEditor = currentEditor.get();
        String selectedText = codeEditor.getSelection();
        Locale spellcheckLocale = preferencesManager.getLanguageSpellSelection().getLanguage().getLocaleWithCountryAndVariant();
        String lowercaseText = StringUtils.lowerCase(selectedText, spellcheckLocale);
        codeEditor.replaceSelection(lowercaseText);
        currentEditor.get().requestFocus();
    }

    public void refreshPreview() {
        if (currentEditorIsXHTML.get()) {
            CodeEditor xhtmlCodeEditor = currentEditor.getValue();
            if (xhtmlCodeEditor != null && currentXHTMLResource.get() != null) {
                currentXHTMLResource.get().setData(xhtmlCodeEditor.getCode().getBytes(StandardCharsets.UTF_8));
            }
        }
        needsRefresh.setValue(true);
        needsRefresh.setValue(false);
    }

    public void reset() {
    }

    public ObservableBooleanValue currentEditorIsXHTMLProperty() {
        return currentEditorIsXHTML;
    }

    public boolean currentEditorIsXHTML() {
        return currentEditorIsXHTML.get();
    }

    public boolean getCanRedo() {
        return canRedo.get();
    }

    public SimpleBooleanProperty canRedoProperty() {
        return canRedo;
    }

    public boolean getCanUndo() {
        return canUndo.get();
    }

    public SimpleBooleanProperty canUndoProperty() {
        return canUndo;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public CodeEditor getCurrentEditor() {
        return currentEditor.get();
    }

    public ObjectProperty<CodeEditor> currentEditorProperty() {
        return currentEditor;
    }

    public String formatAsXHTML(String xhtml) throws IOException, JDOMException {
        Document document = XHTMLUtils.parseXHTMLDocument(xhtml);
        return XHTMLUtils.outputXHTMLDocumentAsString(document, true, EpubVersion.VERSION_2);
    }

    public String repairXHTML(String xhtml) {
        return XHTMLUtils.repair(xhtml, book.getVersion());
    }

    public void refreshAll() {
        //refresh all is in progress, avoid firing listener
        refreshAllInProgress = true;
        //refresh was executed, and after execution avoid firing listener, will be reseted by listener
        refreshAll = true;
        CodeEditor previousCodeEditior = currentEditor.get();
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            Resource resource = (Resource) tab.getUserData();
            if (tab.getContent() instanceof CodeEditor) {
                CodeEditor editor = (CodeEditor) tab.getContent();
                currentEditor.setValue(editor);
                editor.setCode(new String(resource.getData(), StandardCharsets.UTF_8));
                editor.scrollTo(0);
            }
        }
        currentEditor.set(previousCodeEditior);
        refreshAllInProgress = false;
    }

    public void refreshEditorCode(Resource resourceToUpdate) {
        openingEditorTab = true;
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            Resource resource = (Resource) tab.getUserData();
            if (resourceToUpdate.equals(resource)) {
                CodeEditor editor = (CodeEditor) tab.getContent();
                editor.setCode(new String(resourceToUpdate.getData(), StandardCharsets.UTF_8));
                editor.setAbsoluteCursorPosition(0);
            }
        }
        openingEditorTab = false;
    }

    public void refreshImageViewer(Resource resourceToUpdate) {
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            Resource resource = (Resource) tab.getUserData();
            if (resourceToUpdate.equals(resource)) {
                ImageResource imageResource = (ImageResource) resourceToUpdate;
                logger.info("refreshing image resource");
                ImageViewerPane imageViewerPane = (ImageViewerPane) tab.getContent();
                ImageView imageView = imageViewerPane.getImageView();
                imageView.setImage(imageResource.asNativeFormat());
                imageView.setFitHeight(-1);
                imageView.setFitWidth(-1);

                Label imagePropertiesLabel = imageViewerPane.getImagePropertiesLabel();
                imagePropertiesLabel.setText(imageResource.getImageDescription());
            }
        }
    }

    public boolean isInsertablePosition() {
        boolean result = false;
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            result = xhtmlCodeEditor.isInsertablePosition();
        }
        return result;
    }

    public void scrollTo(Deque<ElementPosition> nodeChain) {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML) && nodeChain.size() > 0) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            xhtmlCodeEditor.scrollTo(nodeChain);
        }
        currentEditor.get().requestFocus();
    }

    public ObservableValue<? extends String> cursorPosLabelProperty() {
        return cursorPosLabelProperty;
    }

    public final ReadOnlyIntegerProperty currentLineProperty() {
        return currentLineProperty.getReadOnlyProperty();
    }
    public final int getCurrentLine() {
        return currentLineProperty.get();
    }
}
