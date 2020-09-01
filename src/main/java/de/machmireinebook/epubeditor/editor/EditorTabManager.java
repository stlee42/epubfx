package de.machmireinebook.epubeditor.editor;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import javafx.application.Platform;
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
import javafx.scene.Cursor;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.wellbehaved.event.Nodes;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.reactfx.value.Var;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.clips.Clip;
import de.machmireinebook.epubeditor.clips.ClipManager;
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
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;
import de.machmireinebook.epubeditor.xhtml.XhtmlFileSplitter;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

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
    private ReadOnlyObjectWrapper<Resource<?>> currentSearchableResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<XHTMLResource> currentXHTMLResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<CSSResource> currentCssResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<Resource<?>> currentXMLResource = new ReadOnlyObjectWrapper<>();
    private Var<Boolean> needsRefresh = Var.newSimpleVar(false);
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
    @Inject
    private EpubEditorConfiguration configuration;

    private boolean openingEditorTab = false;
    private boolean suppressNextScheduledRefresh = false;
    private boolean refreshAllInProgress = false;
    private boolean refreshAll = false;

    public static class ImageViewerPane extends ScrollPane implements Initializable {
        @FXML
        private ImageView imageView;
        @FXML
        private Label imagePropertiesLabel;
        @FXML
        private VBox vBox;
        private ImageResource imageResource;

        ImageViewerPane() {
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

        MenuItem itemFormatHTML = new MenuItem("Format HTML");
        itemFormatHTML.setOnAction(e -> formatHtml());
        contextMenuXHTML.getItems().add(itemFormatHTML);

        MenuItem itemRepairHTML = new MenuItem("Repair and format HTML");
        itemRepairHTML.setOnAction(e -> repairHTML());
        contextMenuXHTML.getItems().add(itemRepairHTML);

        MenuItem itemUnescapeHTML = new MenuItem("Unescape HTML");
        itemUnescapeHTML.setOnAction(e -> unescapeHTML());
        contextMenuXHTML.getItems().add(itemUnescapeHTML);

        contextMenuXHTML.getItems().add(separatorItem);

        MenuItem openInExternalBrowserItem = new MenuItem("Open in external Browser");
        openInExternalBrowserItem.setOnAction(e -> openInExternalBrowser(currentEditor));
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
            generateUuidMenuItem.visibleProperty().setValue(newValue != null && currentXMLResource.get().getMediaType().equals(MediaType.OPF));
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

    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;

        tabPane.getTabs().clear();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Resource<?> resource;
            if (newValue != null && newValue.getContent() instanceof CodeEditor) {
                CodeEditor selectedEditor = (CodeEditor) newValue.getContent();
                resource = (Resource<?>) newValue.getUserData();
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
                selectedEditor.requestFocus();
            }
        });
        //key mapping for tab handling
        Nodes.addInputMap(tabPane, sequence(
                consume(keyPressed(KeyCode.RIGHT, KeyCombination.ALT_DOWN), this::altRightPressed),
                consume(keyPressed(KeyCode.LEFT, KeyCombination.ALT_DOWN), this::altLeftPressed)));
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

    private void altRightPressed(KeyEvent event) {
        tabPane.getSelectionModel().selectNext();
    }

    private void altLeftPressed(KeyEvent event) {
        tabPane.getSelectionModel().selectPrevious();
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

    private void formatHtml() {
        logger.info("beautifying html");
        CodeEditor editor = currentEditor.getValue();
        Integer currentCursorPosition = editor.getAbsoluteCursorPosition();
        String code = editor.getCode();
        if (currentEditorIsXHTML.get()) {
            Resource resource = currentXHTMLResource.get();
            code = formatAsXHTML(code);
            resource.setData(code.getBytes(StandardCharsets.UTF_8));
        }
        editor.setCode(code);
        editor.setAbsoluteCursorPosition(currentCursorPosition);
        editor.scrollTo(currentCursorPosition);
        book.setBookIsChanged(true);
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
            code = XHTMLUtils.unescapedHtmlWithXmlAndNbspExceptions(code);
            resource.setData(code.getBytes(StandardCharsets.UTF_8));
        }
        refreshPreview();
        editor.setCode(code);
        editor.setAbsoluteCursorPosition(currentCursorPosition);
        editor.scrollTo(currentCursorPosition);
        book.setBookIsChanged(true);
        editor.requestFocus();
    }

    public void openImageFile(ImageResource imageResource) {
        if (imageResource == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(tabPane.getScene().getWindow());
            alert.setTitle("Image not found");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("The image does not exist and cannot be opened");
            alert.showAndWait();

            return;
        }
        if (!isTabAlreadyOpen(imageResource)) {

            Tab tab = new Tab();
            tab.setClosable(true);
            tab.setText(imageResource.getFileName());
            tab.setContextMenu(createTabContextMenu(tab));

            ImageViewerPane pane = new ImageViewerPane();
            pane.setImageResource(imageResource);

            ImageView imageView = pane.getImageView();
            imageView.setImage(imageResource.asNativeFormat());
            imageView.setFitHeight(-1);
            imageView.setFitWidth(-1);

            Label imagePropertiesLabel = pane.getImagePropertiesLabel();
            imagePropertiesLabel.setText(imageResource.getImageDescription());

            tab.setContent(pane);
            tab.setUserData(imageResource);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        }
    }

    private ContextMenu createTabContextMenu(Tab tab) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("context-menu");
        contextMenu.setAutoFix(true);
        contextMenu.setAutoHide(true);

        MenuItem item = new MenuItem("Close");
        item.setOnAction(e -> tabPane.getTabs().remove(tab));
        contextMenu.getItems().add(item);

        MenuItem item2 = new MenuItem("Close All");
        item2.setOnAction(e -> tabPane.getTabs().removeAll(tabPane.getTabs()));
        contextMenu.getItems().add(item2);

        MenuItem item3 = new MenuItem("Close Others");
        item3.setOnAction(e -> {
            List<Tab> removed = tabPane.getTabs().stream()
                    .filter(tabToTest -> tabToTest != tab)
                    .collect(Collectors.toList());
            tabPane.getTabs().removeAll(removed);
        });
        contextMenu.getItems().add(item3);

        SeparatorMenuItem separatorItem = new SeparatorMenuItem();
        contextMenu.getItems().add(separatorItem);

        MenuItem item4 = new MenuItem("Show in Book browser");
        item4.setOnAction(e -> {
            bookBrowserManager.selectTreeItem((Resource) tab.getUserData());
        });
        contextMenu.getItems().add(item4);

        return contextMenu;
    }


    public boolean isTabAlreadyOpen(Resource<?> resource) {
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

    public void openFileInEditor(Resource<?> resource) throws IllegalArgumentException {
        openFileInEditor(resource, resource.getMediaType());
    }

    public void openFileInEditor(Resource<?> resource, MediaType mediaType) throws IllegalArgumentException {
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

        if (!isTabAlreadyOpen(resource)) {
            configuration.getMainWindow().getScene().setCursor(Cursor.WAIT);
            Tab tab = new Tab();
            tab.setClosable(true);
            tab.setText(resource.getFileName());
            resource.hrefProperty().addListener((observable, oldValue, newValue) -> {
                tab.setText(resource.getFileName());
            });
            tab.setContextMenu(createTabContextMenu(tab));

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
            else if (mediaType.equals(MediaType.XML) || mediaType.equals(MediaType.OPF) || mediaType.equals(MediaType.NCX)) {
                editor = xmlEditorProvider.get();
                editor.setContextMenu(contextMenuXML);
            }
            else {
                logger.warn("no editor for mediatype " + mediaType.getName() + ", ignoring the call to open this file");
                configuration.getMainWindow().getScene().setCursor(Cursor.DEFAULT);
                return;
            }
            tab.setOnCloseRequest(event -> editor.shutdown());

            tab.setContent((Node) editor);
            tab.setUserData(resource);

            final String code = StringUtils.toEncodedString(resource.getData(), Charsets.toCharset(resource.getInputEncoding()));
            editor.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equals(Worker.State.SUCCEEDED)) {
                    openingEditorTab = true;
                    editor.setCode(code);
                    editor.clearUndoHistory();
                    editor.scrollTo(EditorPosition.START);
                    editor.setAbsoluteCursorPosition(0);
                    editor.requestFocus();
                    openingEditorTab = false;
                    //openeing tab after code is set
                    tabPane.getTabs().add(tab);
                    tabPane.getSelectionModel().select(tab);
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
                        logger.info("eventstream arrived");
                        //the openingEditorTab and refreshAllInProgress shows that a code change is in progress, dont reset it here,
                        // the other two that code changes are done, reset that the next changes are executed
                        if (openingEditorTab || refreshAllInProgress || editor.isChangingCode()  || refreshAll) {
                            logger.info("ignoring event possible reasons, openingEditorTab: " + openingEditorTab
                                    + ", refreshAllInProgress: " + refreshAllInProgress + ",  editor.isChangingCode(): " +  editor.isChangingCode()
                                    + ", refreshAll: " + refreshAll);
                            editor.resetChangingCode();
                            refreshAll = false;
                            return;
                        }
                        CodeEditor codeEditor = currentEditor.getValue();
                        if (codeEditor.getMediaType().equals(MediaType.XHTML)) {
                            currentXHTMLResource.getValue().setData(codeEditor.getCode().getBytes(StandardCharsets.UTF_8));
                            currentXHTMLResource.getValue().prepareWebViewDocument(book.getVersion());
                        }
                        else if (codeEditor.getMediaType().equals(MediaType.CSS)) {
                            currentCssResource.get().setData(codeEditor.getCode().getBytes(StandardCharsets.UTF_8));
                        }
                        else if (codeEditor.getMediaType().equals(MediaType.XML)) {
                            try {
                                currentXMLResource.get().setData(codeEditor.getCode().getBytes(StandardCharsets.UTF_8));
                                if (((XMLResource) resource).isValidXML() && MediaType.OPF.equals(resource.getMediaType())) {
                                    if (book.isEpub3()) {
                                        Epub3PackageDocumentReader.read((XMLResource)resource, book);
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
                        if (suppressNextScheduledRefresh) {
                            suppressNextScheduledRefresh = false;
                            logger.info("suppressNextScheduledRefresh");
                            return;
                        }
                        logger.info("scheduled refresh task, one second after last change, resource: " + resource.getFileName());
                        Platform.runLater(() -> {
                            needsRefresh.setValue(true);
                        });
                    });
            //snychronise caret position with web view
            if (mediaType.equals(MediaType.XHTML)) {
                codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
                    logger.debug("caret position " + newValue);
                    Optional<XMLTagPair> pairOptional = ((XhtmlRichTextCodeEditor)editor).findSurroundingTags(new XhtmlRichTextCodeEditor.HtmlLayoutTagInspector(), true);
                    pairOptional.ifPresent(xmlTagPair -> currentLineProperty.set(xmlTagPair.getTagParagraphIndex() + 1));
                });
            }

            configuration.getMainWindow().getScene().setCursor(Cursor.DEFAULT);
        }
    }

    public void closeTab(Resource<?> resource) {
        List<Tab> tabs =  tabPane.getTabs();
        tabs.stream().filter(tab -> tab.getUserData() == resource)
                .findFirst()
                .ifPresent(tab -> tabPane.getTabs().remove(tab));
    }

    public Resource<?> getCurrentSearchableResource() {
        return currentSearchableResource.get();
    }

    public ReadOnlyObjectProperty<Resource<?>> currentSearchableResourceProperty() {
        return currentSearchableResource.getReadOnlyProperty();
    }

    public XHTMLResource getCurrentXHTMLResource() {
        return currentXHTMLResource.get();
    }

    public ReadOnlyObjectProperty<XHTMLResource> currentXHTMLResourceProperty() {
        return currentXHTMLResource.getReadOnlyProperty();
    }

    public Resource<?> getCurrentCssResource() {
        return currentCssResource.get();
    }

    public ReadOnlyObjectProperty<CSSResource> currentCssResourceProperty() {
        return currentCssResource.getReadOnlyProperty();
    }

    public Resource<?> getCurrentXMLResource() {
        return currentXMLResource.get();
    }

    public ReadOnlyObjectProperty<Resource<?>> currentXMLResourceProperty() {
        return currentXMLResource.getReadOnlyProperty();
    }

    public Var<Boolean> needsRefreshProperty() {
        return needsRefresh;
    }

    public void scrollTo(EditorPosition position) {
        CodeEditor codeEditor = currentEditor.getValue();
        codeEditor.scrollTo(position);
        codeEditor.requestFocus();
    }

    public void cutSelection() {
        CodeEditor editor = getCurrentEditor();
        editor.getCodeArea().cut();
    }

    public void copySelection() {
        CodeEditor editor = getCurrentEditor();
        editor.getCodeArea().copy();
    }

    public void pasteFromClipboard() {
        CodeEditor editor = getCurrentEditor();
        editor.getCodeArea().paste();
    }

    public void insertAtCursorPositionOrReplaceSelection(String text) {
        insertAtCursorPositionOrReplaceSelection(text, false);
    }

    public void insertAtCursorPositionOrReplaceSelection(String text, boolean ignoreCursorPosition) {
        if (isInsertablePosition() || ignoreCursorPosition) {
            CodeEditor editor = getCurrentEditor();
            if(StringUtils.isNotEmpty(editor.getSelection())) {
                editor.replaceSelection(text);
            } else {
                Integer cursorPosition = editor.getAbsoluteCursorPosition();
                editor.insertAt(cursorPosition, text);
            }
            refreshPreview();
            editor.requestFocus();
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
        CodeEditor codeEditor = currentEditor.getValue();
        if (isInsertablePosition() && codeEditor.getMediaType().equals(MediaType.XHTML)) {
            Integer cursorPosition = codeEditor.getAbsoluteCursorPosition();
            codeEditor.insertAt(cursorPosition, text);
            codeEditor.setAbsoluteCursorPosition(cursorPosition + moveCaretIndex);
            refreshPreview();
            codeEditor.requestFocus();
        }
    }

    public void surroundParagraphWithTag(String tagName) {
        CodeEditor codeEditor = currentEditor.getValue();
        if (codeEditor.getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) codeEditor;
            xhtmlCodeEditor.surroundParagraphWithTag(tagName);
            refreshPreview();
            xhtmlCodeEditor.requestFocus();
        }
    }

    public void insertStyle(String styleName, String value) {
        CodeEditor codeEditor = currentEditor.getValue();
        if (codeEditor.getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) codeEditor;
            xhtmlCodeEditor.insertStyle(styleName, value);
            refreshPreview();
            xhtmlCodeEditor.requestFocus();
        }
    }

    public void surroundSelectionWithTag(String tagName) {
        CodeEditor codeEditor = currentEditor.getValue();
        if (codeEditor.getMediaType().equals(MediaType.XHTML)) {
            String selection = codeEditor.getSelection();
            codeEditor.replaceSelection("<" + tagName + ">" + selection + "</" + tagName + ">");
            refreshPreview();
            codeEditor.requestFocus();
        }
    }

    public void surroundSelection(String start, String end) {
        CodeEditor codeEditor = currentEditor.getValue();
        if (codeEditor.getMediaType().equals(MediaType.XHTML)) {
            String selection = codeEditor.getSelection();
            codeEditor.replaceSelection(start + selection + end);
            refreshPreview();
            codeEditor.requestFocus();
        }
    }

    public void increaseIndent() {
        CodeEditor codeEditor = currentEditor.getValue();
        if (codeEditor.getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) codeEditor;
            xhtmlCodeEditor.increaseIndent();
        }
        codeEditor.requestFocus();
    }

    public void decreaseIndent() {
        CodeEditor codeEditor = currentEditor.getValue();
        if (codeEditor.getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) codeEditor;
            xhtmlCodeEditor.decreaseIndent();
        }
        codeEditor.requestFocus();
    }

    public boolean splitXHTMLFile() {
        boolean result = false;
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML)) {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            Optional<XMLTagPair> optional = xhtmlCodeEditor.findSurroundingTags(tagName -> "head".equals(tagName) || "html".equals(tagName) || "body".equals(tagName) || "section".equals(tagName));
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
                logger.debug("enclosing pair " + pair.getTagName());
                //inside body
                int index = xhtmlCodeEditor.getAbsoluteCursorPosition();
                try {
                    String originalCode = xhtmlCodeEditor.getCode();
                    List<Content> originalHeadContent = xhtmlCodeEditor.getHeadContent();

                    String frontPart = originalCode.substring(0, index);
                    XHTMLResource oldResource = currentXHTMLResource.getValue();
                    /*HtmlCleanerBookProcessor processor = new HtmlCleanerBookProcessor();
                    processor.processResource(oldResource, book);*/
                    XhtmlFileSplitter splitter = new XhtmlFileSplitter(book.getVersion());
                    String completedFrontPart = splitter.completeFrontPart(frontPart);

                    oldResource.setData(completedFrontPart.getBytes(StandardCharsets.UTF_8));
                    xhtmlCodeEditor.setCode(completedFrontPart);
                    oldResource.prepareWebViewDocument(book.getVersion());

                    String backPart = originalCode.substring(index);
                    String fileName = book.getNextStandardFileName(MediaType.XHTML);
                    XHTMLResource resource = (XHTMLResource) MediaType.XHTML.getResourceFactory().createResource("Text/" + fileName);
                    String backPartXHTML = splitter.completeBackPart(backPart, originalHeadContent);
                    resource.setData(backPartXHTML.getBytes(StandardCharsets.UTF_8));
                    //webview will be prepared while opening the file, not necessary here

                    int spineIndex = book.getSpine().getResourceIndex(oldResource);
                    book.addSpineResource(resource, spineIndex + 1);
                    openFileInEditor(resource, MediaType.XHTML);

                    bookBrowserManager.refreshBookBrowser();
                    currentXHTMLResource.set(resource);
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
        CodeEditor codeEditor = currentEditor.getValue();
        String selectedText = codeEditor.getSelection();
        Locale spellcheckLocale = preferencesManager.getLanguageSpellSelection().getLanguage().getLocaleWithCountryAndVariant();
        String uppercaseText = StringUtils.upperCase(selectedText, spellcheckLocale);
        codeEditor.replaceSelection(uppercaseText);
        codeEditor.requestFocus();
    }

    public void toLowerCase() {
        CodeEditor codeEditor = currentEditor.getValue();
        String selectedText = codeEditor.getSelection();
        Locale spellcheckLocale = preferencesManager.getLanguageSpellSelection().getLanguage().getLocaleWithCountryAndVariant();
        String lowercaseText = StringUtils.lowerCase(selectedText, spellcheckLocale);
        codeEditor.replaceSelection(lowercaseText);
        codeEditor.requestFocus();
    }

    public void refreshPreview() {
        if (currentEditorIsXHTML.getValue()) {
            CodeEditor xhtmlCodeEditor = currentEditor.getValue();
            XHTMLResource xhtmlResource = currentXHTMLResource.getValue();
            if (xhtmlCodeEditor != null && xhtmlResource != null) {
                xhtmlResource.setData(xhtmlCodeEditor.getCode().getBytes(StandardCharsets.UTF_8));
                xhtmlResource.prepareWebViewDocument(book.getVersion());
            }
        }
        needsRefresh.setValue(true);
    }

    public void totalRefreshPreview() {
        if (currentEditorIsXHTML.getValue()) {
            logger.debug("refreshing xhtml editor");
            CodeEditor xhtmlCodeEditor = currentEditor.getValue();
            XHTMLResource xhtmlResource = currentXHTMLResource.getValue();
            if (xhtmlCodeEditor != null && xhtmlResource != null) {
                xhtmlResource.setData(xhtmlCodeEditor.getCode().getBytes(StandardCharsets.UTF_8));
                xhtmlResource.prepareWebViewDocument(book.getVersion());
            }
            //trigger total refresh
            currentXHTMLResource.setValue(null);
            currentXHTMLResource.setValue(xhtmlResource);
        }
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
        return currentEditor.getValue();
    }

    public ObjectProperty<CodeEditor> currentEditorProperty() {
        return currentEditor;
    }

    public String formatAsXHTML(String xhtml) {
        Document document = null;
        try {
            document = XHTMLUtils.parseXHTMLDocument(xhtml);
        }
        catch (IOException | JDOMException e) {
            logger.error(e);
        }
        return XHTMLUtils.outputXHTMLDocumentAsString(document, false, book.getVersion());
    }

    public String repairXHTML(String xhtml) {
        return XHTMLUtils.repair(xhtml, book.getVersion());
    }

    public void refreshAll() {
        //refresh all is in progress, avoid firing listener
        refreshAllInProgress = true;
        //refresh was executed, and after execution avoid firing listener, will be reseted by listener
        refreshAll = true;
        CodeEditor previousCodeEditor = currentEditor.get();
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            Resource<?> resource = (Resource<?>) tab.getUserData();
            if (tab.getContent() instanceof CodeEditor) {
                CodeEditor editor = (CodeEditor) tab.getContent();
                currentEditor.setValue(editor);
                editor.setCode(new String(resource.getData(), StandardCharsets.UTF_8));
                editor.scrollTo(0);
            }
        }
        currentEditor.set(previousCodeEditor);
        refreshAllInProgress = false;
    }

    public void refreshEditorCode(Resource<?> resourceToUpdate) {
        openingEditorTab = true;
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            Resource<?> resource = (Resource<?>) tab.getUserData();
            if (resourceToUpdate.equals(resource)) {
                if (tab.getContent() instanceof CodeEditor) {
                    CodeEditor editor = (CodeEditor) tab.getContent();
                    int currentCursorPosition = editor.getAbsoluteCursorPosition();
                    editor.setCode(new String(resourceToUpdate.getData(), StandardCharsets.UTF_8));
                    editor.setAbsoluteCursorPosition(currentCursorPosition);
                }
            }
        }
        if (resourceToUpdate.getMediaType() == MediaType.XHTML) {
            suppressNextScheduledRefresh = true;
        }
        openingEditorTab = false;
    }

    public void refreshImageViewer(Resource<?> resourceToUpdate) {
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs) {
            Resource<?> resource = (Resource<?>) tab.getUserData();
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
