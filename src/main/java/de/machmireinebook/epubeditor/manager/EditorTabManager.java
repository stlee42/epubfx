package de.machmireinebook.epubeditor.manager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
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
import javafx.util.Duration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import org.jdom2.Content;
import org.jdom2.DocType;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.domain.Clip;
import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.editor.CssRichTextCodeEditor;
import de.machmireinebook.epubeditor.editor.EditorPosition;
import de.machmireinebook.epubeditor.editor.XMLTagPair;
import de.machmireinebook.epubeditor.editor.XhtmlRichTextCodeEditor;
import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.bookprocessor.HtmlCleanerBookProcessor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.ResourceDataException;
import de.machmireinebook.epubeditor.epublib.domain.XMLResource;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentReader;
import de.machmireinebook.epubeditor.gui.ExceptionDialog;
import de.machmireinebook.epubeditor.jdom2.XHTMLOutputProcessor;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 20:29
 */
@Singleton
public class EditorTabManager
{
    private static final Logger logger = Logger.getLogger(EditorTabManager.class);

    private TabPane tabPane;
    private ObjectProperty<CodeEditor> currentEditor = new SimpleObjectProperty<>();
    //getrennte Verwaltung der current resource für html und css, da der Previewer auf der html property lauscht und
    // wenn ein css bearbeitet wird, das letzte html-doument weiterhin im previewer angezeigt werden soll
    private ReadOnlyObjectWrapper<Resource> currentSearchableResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<Resource> currentXHTMLResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<Resource> currentCssResource = new ReadOnlyObjectWrapper<>();
    private ReadOnlyObjectWrapper<Resource> currentXMLResource = new ReadOnlyObjectWrapper<>();
    private RefreshPreviewScheduledService scheduledService = new RefreshPreviewScheduledService();
    private BooleanProperty needsRefresh = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty currentEditorIsXHTML = new SimpleBooleanProperty();
    private SimpleBooleanProperty canUndo = new SimpleBooleanProperty();
    private SimpleBooleanProperty canRedo = new SimpleBooleanProperty();
    private StringProperty cursorPosLabelProperty = new SimpleStringProperty();
    private BookBrowserManager bookBrowserManager;
    private Book book;
    private ContextMenu contextMenuXHTML;
    private ContextMenu contextMenuXML;
    private ContextMenu contextMenuCSS;

    private static final Pattern indentRegex = Pattern.compile("style\\s*=\\s*\"(.*)margin-left:([-\\.0-9]*)([^;]*)(;?)(.*)\\s*\"", Pattern.DOTALL);

    @Inject
    private ClipManager clipManager;

    private boolean openingEditorTab = false;

    public class ImageViewerPane extends ScrollPane implements Initializable
    {
        @FXML
        private ImageView imageView;
        @FXML
        private Label imagePropertiesLabel;
        @FXML
        private VBox vBox;
        private ImageResource imageResource;

        public ImageViewerPane()
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/image_view.fxml"), null, new JavaFXBuilderFactory(),
                    type -> BeanFactory.getInstance().getBean(type));
            loader.setRoot(this);
            loader.setController(this);

            try
            {
                loader.load();
            }
            catch (IOException e)
            {
                ExceptionDialog.showAndWait(e, null,  "Bild anzeigen", "Fehler beim Öffnen eines Bildes.");
                logger.error("", e);
            }
        }

        @Override
        public void initialize(URL location, ResourceBundle resources)
        {
            vBox.minWidthProperty().bind(this.widthProperty());
            vBox.minHeightProperty().bind(this.heightProperty());
        }

        public Label getImagePropertiesLabel()
        {
            return imagePropertiesLabel;
        }

        public void setImagePropertiesLabel(Label imagePropertiesLabel)
        {
            this.imagePropertiesLabel = imagePropertiesLabel;
        }

        public ImageView getImageView()
        {
            return imageView;
        }

        public void setImageView(ImageView imageView)
        {
            this.imageView = imageView;
        }

        public ImageResource getImageResource()
        {
            return imageResource;
        }

        public void setImageResource(ImageResource imageResource)
        {
            this.imageResource = imageResource;
        }
    }

    protected class RefreshPreviewScheduledService extends ScheduledService<Boolean>
    {
        public RefreshPreviewScheduledService()
        {
            setDelay(Duration.seconds(1));
            setRestartOnFailure(false);
        }

        @Override
        protected Task<Boolean> createTask()
        {
            return new Task<Boolean>()
            {
                protected Boolean call()
                {
                    logger.info("scheduled refresh task, one second after last change");
                    Platform.runLater(() -> {
                        needsRefresh.setValue(true);
                        needsRefresh.setValue(false);
                    });
                    cancel();
                    return true;
                }
            };
        }
    }

    @PostConstruct
    public void init()
    {
        currentEditor.addListener((observable, oldValue, newValue) -> {
            canUndo.unbind();
            canRedo.unbind();
            if (newValue != null)
            {
                currentEditorIsXHTML.setValue(currentEditor.getValue().getMediaType().equals(MediaType.XHTML));
                canUndo.bind(currentEditor.getValue().canUndoProperty());
                canRedo.bind(currentEditor.getValue().canRedoProperty());
            }
            else
            {
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

        Menu clipsItem = new Menu("Clips");
        clipManager.getClipsRoot().addEventHandler(TreeItem.<Clip>childrenModificationEvent(), event -> {
            clipsItem.getItems().clear();
            writeClipMenuItemChildren(clipManager.getClipsRoot(), clipsItem);
        });
        contextMenuXHTML.getItems().add(clipsItem);
        contextMenuXHTML.getItems().add(separatorItem);

        MenuItem itemRepairHTML = new MenuItem("HTML reparieren");
        itemRepairHTML.setOnAction(e -> {
            beautifyOrRepairHTML("repair");
        });
        contextMenuXHTML.getItems().add(itemRepairHTML);

        MenuItem itemBeautifyHTML = new MenuItem("HTML formatieren");
        itemBeautifyHTML.setOnAction(e -> {
            beautifyOrRepairHTML("format");
        });
        contextMenuXHTML.getItems().add(itemBeautifyHTML);

        contextMenuXHTML.getItems().add(separatorItem);

        MenuItem openInExternalBrowserItem = new MenuItem("In externem Browser öffnen");
        openInExternalBrowserItem.setOnAction(e -> {
            openInExternalBrowser(currentEditor);
        });
        contextMenuXHTML.getItems().add(openInExternalBrowserItem);

        //XML menu
        contextMenuXML = new ContextMenu();
        contextMenuXML.getStyleClass().add("context-menu");
        contextMenuXML.setAutoFix(true);
        contextMenuXML.setAutoHide(true);

        MenuItem generateUuidMenuItem = new MenuItem("Neue UUID generieren");
        generateUuidMenuItem.setOnAction(e -> {
            book.getMetadata().generateNewUuid();
            bookBrowserManager.refreshOpf();
        });
        contextMenuXML.getItems().add(generateUuidMenuItem);
        currentXMLResource.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && currentXMLResource.get().mediaTypeProperty().getValue().equals(MediaType.OPF))
            {
                generateUuidMenuItem.visibleProperty().setValue(true);
            }
            else
            {
                generateUuidMenuItem.visibleProperty().setValue(false);
            }
        });

        MenuItem separatorItem2 = new SeparatorMenuItem();
        contextMenuXML.getItems().add(separatorItem2);
        separatorItem2.visibleProperty().bind(generateUuidMenuItem.visibleProperty());

        MenuItem itemRepairXML = new MenuItem("XML reparieren");
        itemRepairXML.setOnAction(e -> {
            beautifyOrRepairXML("repair");
        });
        contextMenuXML.getItems().add(itemRepairXML);

        MenuItem itemBeautifyXML = new MenuItem("XML formatieren");
        itemBeautifyXML.setOnAction(e -> {
            beautifyOrRepairXML("format");
        });
        contextMenuXML.getItems().add(itemBeautifyXML);

        //css menu
        contextMenuCSS = new ContextMenu();
        contextMenuCSS.getStyleClass().add("context-menu");
        contextMenuCSS.setAutoFix(true);
        contextMenuCSS.setAutoHide(true);
        MenuItem formatCSSOneLineItem = new MenuItem("Styles in je einer Zeile formatieren");
        formatCSSOneLineItem.setOnAction(e -> beautifyCSS("one_line"));
        contextMenuCSS.getItems().add(formatCSSOneLineItem);

        MenuItem formatCSSMultipleLinesItem = new MenuItem("Styles in mehreren Zeilen formatieren");
        formatCSSMultipleLinesItem.setOnAction(e -> beautifyCSS("multiple_lines"));
        contextMenuCSS.getItems().add(formatCSSMultipleLinesItem);
    }

    private void writeClipMenuItemChildren(TreeItem<Clip> parentTreeItem, Menu parentMenu)
    {
        List<TreeItem<Clip>> children = parentTreeItem.getChildren();
        for (TreeItem<Clip> child : children)
        {
            if (child.getValue().isGroup())
            {
                Menu menu = new Menu(child.getValue().getName());
                parentMenu.getItems().add(menu);
                writeClipMenuItemChildren(child, menu);
            }
            else
            {
                MenuItem menuItem = new MenuItem(child.getValue().getName());
                parentMenu.getItems().add(menuItem);
                menuItem.setOnAction(event -> {
                    insertClip(child.getValue());
                });
            }
        }
    }

    private void insertClip(Clip clip)
    {
        CodeEditor editor = currentEditor.getValue();
        String selection = editor.getSelection();
        String insertedClip = selection.replaceAll("^(.*)$", clip.getContent());
        editor.replaceSelection(insertedClip);
        book.setBookIsChanged(true);
    }

    private void beautifyCSS(String type)
    {


    }

    private void openInExternalBrowser(ObjectProperty<CodeEditor> currentEditor)
    {


    }

    private void beautifyOrRepairHTML(String mode)
    {
        logger.info("beautifying html");
        try
        {
            CodeEditor editor = currentEditor.getValue();
            Integer currentCursorPosition = editor.getAbsoluteCursorPosition();
            String code = editor.getCode();
            if (currentEditorIsXHTML.get())
            {
                try
                {
                    Resource resource = currentXHTMLResource.get();
                    resource.setData(code.getBytes("UTF-8"));
                    switch (mode)
                    {
                        case "format": code = formatAsXHTML(code);
                                       break;
                        case "repair": code = repairXHTML(code);
                            break;
                    }
                    resource.setData(code.getBytes("UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    //never happens
                }
            }
            editor.setCode(code);
            editor.setAbsoluteCursorPosition(currentCursorPosition);
            editor.scrollTo(currentCursorPosition);
            book.setBookIsChanged(true);
        }
        catch (IOException | JDOMException e)
        {
            logger.error("", e);
            ExceptionDialog.showAndWait(e, null,  "Formatierung nicht möglich", "Kann Datei nicht formatieren. Bitte die Fehlermeldung an den Hersteller weitergeben.");
        }
    }

    private void beautifyOrRepairXML(String mode)
    {
    }

    public void openImageFile(Resource resource)
    {
        Tab tab = new Tab();
        tab.setClosable(true);
        if (resource == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Datei nicht vorhanden");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("Die angeforderte Datei ist nicht vorhanden und kann deshalb nicht geöffnet werden.");
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


    private boolean isTabAlreadyOpen(Resource resource)
    {
        boolean found = false;
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs)
        {
            if (tab.getUserData().equals(resource))
            {
                tabPane.getSelectionModel().select(tab);
                found = true;
            }
        }
        return found;
    }

    public void openFileInEditor(Resource resource, MediaType mediaType) throws IllegalArgumentException
    {
        if (!isTabAlreadyOpen(resource))
        {
            Tab tab = new Tab();
            tab.setClosable(true);
            if (resource == null)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Datei nicht vorhanden");
                alert.getDialogPane().setHeader(null);
                alert.getDialogPane().setHeaderText(null);
                alert.setContentText("Die angeforderte Datei ist nicht vorhanden und kann deshalb nicht geöffnet werden.");
                alert.showAndWait();

                return;
            }
            tab.setText(resource.getFileName());
            resource.hrefProperty().addListener((observable, oldValue, newValue) -> {
                tab.setText(resource.getFileName());
            });

            String content = "";
            try
            {
                content = new String(resource.getData(), resource.getInputEncoding());
            }
            catch (IOException e)
            {
                logger.error("", e);
            }

            CodeEditor editor;
            if (mediaType.equals(MediaType.CSS))
            {
                editor = new CssRichTextCodeEditor();
                editor.setContextMenu(contextMenuCSS);
            }
            else if (mediaType.equals(MediaType.XHTML))
            {
                editor = new XhtmlRichTextCodeEditor(mediaType);
                editor.setContextMenu(contextMenuXHTML);
            }
            else if (mediaType.equals(MediaType.XML))
            {
                editor = new XhtmlRichTextCodeEditor(mediaType);
                editor.setContextMenu(contextMenuXML);
            }
            else
            {
                throw new IllegalArgumentException("no editor for mediatype " + mediaType.getName());
            }

            tab.setContent((Node) editor);
            tab.setUserData(resource);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

            final String code = content;
            editor.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.equals(Worker.State.SUCCEEDED))
                {
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
                    ((AnchorPane) editor).requestFocus();
                    openingEditorTab = false;
                }
            });

            editor.cursorPositionProperty().addListener((observable, oldValue, newValue) -> {
                EditorPosition cursorPosition = editor.getCursorPosition();
                String textIformation = editor.getTextInformation();
                cursorPosLabelProperty.set("Absolute: " + String.valueOf(newValue) + ", Relative: " + (cursorPosition.getLine() + 1) + ":" + (cursorPosition.getColumn() + 1)
                        + " | Text Information: " + textIformation);
            });

            editor.codeProperty().addListener((observable1, oldValue, newValue) -> {
                if (openingEditorTab || editor.isChangingCode())
                {
                    return;
                }
                 if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
                {
                    try
                    {
                        currentXHTMLResource.get().setData(newValue.getBytes("UTF-8"));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        //never happens
                    }
                }
                else if (currentEditor.getValue().getMediaType().equals(MediaType.CSS))
                {
                    try
                    {
                        currentCssResource.get().setData(newValue.getBytes("UTF-8"));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        //never happens
                    }
                }
                else if (currentEditor.getValue().getMediaType().equals(MediaType.XML))
                {
                    try
                    {
                        currentXMLResource.get().setData(newValue.getBytes("UTF-8"));
                        if (((XMLResource)resource).isValidXML() && MediaType.OPF.equals(resource.getMediaType()))
                        {
                            PackageDocumentReader.read(resource, book);
                        }
                    }
                    catch (JDOMException | IOException e)
                    {
                        logger.error("", e);
                    }
                }

                if (scheduledService.getState().equals(Worker.State.READY))
                {
                    scheduledService.start();
                }
                else
                {
                    scheduledService.restart();
                }
                book.setBookIsChanged(true);
            });
        }
    }

    public Resource getCurrentSearchableResource()
    {
        return currentSearchableResource.get();
    }

    public ReadOnlyObjectProperty<Resource> currentSearchableResourceProperty()
    {
        return currentSearchableResource.getReadOnlyProperty();
    }

    public Resource getCurrentXHTMLResource()
    {
        return currentXHTMLResource.get();
    }

    public ReadOnlyObjectProperty<Resource> currentXHTMLResourceProperty()
    {
        return currentXHTMLResource.getReadOnlyProperty();
    }

    public Resource getCurrentCssResource()
    {
        return currentCssResource.get();
    }

    public ReadOnlyObjectProperty<Resource> currentCssResourceProperty()
    {
        return currentCssResource.getReadOnlyProperty();
    }

    public Resource getCurrentXMLResource()
    {
        return currentXMLResource.get();
    }

    public ReadOnlyObjectProperty<Resource> currentXMLResourceProperty()
    {
        return currentXMLResource.getReadOnlyProperty();
    }

    public BooleanProperty needsRefreshProperty()
    {
        return needsRefresh;
    }

    public void setTabPane(TabPane tabPane)
    {
        this.tabPane = tabPane;

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Resource resource;
            if (newValue != null && newValue.getContent() instanceof CodeEditor)
            {
                CodeEditor selectedEditor = (CodeEditor) newValue.getContent();
                resource = (Resource) newValue.getUserData();
                currentSearchableResource.set(resource);
                currentEditor.setValue(selectedEditor);

                if (selectedEditor.getMediaType().equals(MediaType.XHTML))
                {
                    currentXHTMLResource.set(resource);
                }
                else if (selectedEditor.getMediaType().equals(MediaType.CSS))
                {
                    currentCssResource.set(resource);
                }
                else if (selectedEditor.getMediaType().equals(MediaType.XML))
                {
                    currentXMLResource.set(resource);
                }
            }
        });
    }

    public void surroundParagraphWithTag(String tagName)
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            Optional<XMLTagPair> optional = xhtmlCodeEditor.findSurroundingTags(new XhtmlRichTextCodeEditor.BlockTagInspector());
            optional.ifPresent(pair -> {
                logger.info("found xml block tag " + pair.getTagName());
                //erst das schließende Tag ersetzen, da sich sonst die Koordinaten verschieben können
                xhtmlCodeEditor.replaceRange(pair.getCloseTagRange(),  tagName);
                xhtmlCodeEditor.replaceRange(pair.getOpenTagRange(), tagName);
                refreshPreview();
            });
        }
    }

    public void insertStyle(String styleName, String value)
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();

            Optional<XMLTagPair> optional = xhtmlCodeEditor.findSurroundingTags(new XhtmlRichTextCodeEditor.BlockTagInspector());
            optional.ifPresent(pair -> {
                logger.info("found xml block tag " + pair.getTagName());
                String tagAtttributes = xhtmlCodeEditor.getRange(new IndexRange(pair.getOpenTagRange().getEnd(), pair.getTagAttributesEnd()));
                if (tagAtttributes.contains("style=")) //wenn bereits styles vorhanden, dann diese modifizieren
                {
                    if (tagAtttributes.contains(styleName)) //replace old value of style with new one
                    {
                        tagAtttributes = tagAtttributes.replaceAll("style\\s*=\\s*\"(.*)" + styleName + ":([^;]*)(;?)(.*)\\s*\"",
                                "style=\"$1" + styleName + ":" + value + "$3$4\"");
                    }
                    else //otherwise append style
                    {
                        tagAtttributes = tagAtttributes.replaceAll("style\\s*=\\s*\"(.*)\"",
                                "style=\"$1;" + styleName + ":" + value + "\"");
                    }
                    xhtmlCodeEditor.replaceRange(new IndexRange(pair.getOpenTagRange().getEnd(), pair.getTagAttributesEnd()), tagAtttributes);
                }
                else
                {
                    int pos = pair.getOpenTagRange().getEnd();
                    xhtmlCodeEditor.insertAt(pos, " style=\"" + styleName +":" + value + "\"");
                }
                refreshPreview();
                xhtmlCodeEditor.requestFocus();
            });
        }
    }

    public void surroundSelectionWithTag(String tagName)
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            String selection = currentEditor.get().getSelection();
            currentEditor.get().replaceSelection("<" + tagName + ">" + selection + "</" + tagName + ">");
            refreshPreview();
            currentEditor.get();
        }
    }

    public void increaseIndent()
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            Optional<XMLTagPair> optional = xhtmlCodeEditor.findSurroundingTags(new XhtmlRichTextCodeEditor.BlockTagInspector());
            optional.ifPresent(pair -> {
                logger.info("found xml block tag " + pair.getTagName());
                String tagAtttributes = xhtmlCodeEditor.getRange(new IndexRange(pair.getOpenTagRange().getEnd(), pair.getTagAttributesEnd()));

                Matcher regexMatcher = indentRegex.matcher(tagAtttributes);
                if (regexMatcher.find())
                {
                    String currentIndentStr = regexMatcher.group(2);
                    int currentIndent = NumberUtils.toInt(currentIndentStr, 0);
                    String currentUnit = regexMatcher.group(3);
                    switch(currentUnit)
                    {
                        case "%":
                        case "rem":
                        case "em":  currentIndent++;
                            break;
                        case "px":  currentIndent = currentIndent + 10;
                            break;
                    }
                    insertStyle("margin-left", currentIndent + currentUnit);
                }
                else
                {
                    insertStyle("margin-left", "1em");
                }
            });
        }
    }

    public void decreaseIndent()
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {

            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            Optional<XMLTagPair> optional = xhtmlCodeEditor.findSurroundingTags(new XhtmlRichTextCodeEditor.BlockTagInspector());
            optional.ifPresent(pair -> {
                logger.info("found xml block tag " + pair.getTagName());
                String tagAtttributes = xhtmlCodeEditor.getRange(pair.getOpenTagRange().getEnd(), pair.getTagAttributesEnd());

                Matcher regexMatcher = indentRegex.matcher(tagAtttributes);
                if (regexMatcher.find())
                {
                    String currentIndentStr = regexMatcher.group(2);
                    int currentIndent = NumberUtils.toInt(currentIndentStr, 0);
                    String currentUnit = regexMatcher.group(3);
                    switch(currentUnit)
                    {
                        case "%":
                        case "rem":
                        case "em":  currentIndent--;
                            break;
                        case "px":  currentIndent = currentIndent - 10;
                            break;
                    }
                    insertStyle("margin-left", currentIndent + currentUnit);
                }
                else
                {
                    insertStyle("margin-left", "-1em");
                }
            });
        }
    }

    public boolean splitXHTMLFile()
    {
        boolean result = false;
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            Optional<XMLTagPair> optional = xhtmlCodeEditor.findSurroundingTags(tagName -> "head".equals(tagName) || "body".equals(tagName) || "html".equals(tagName));
            if (optional.isPresent())
            {
                XMLTagPair pair = optional.get();
                if ("head".equals(pair.getTagName()) || "html".equals(pair.getTagName()) || StringUtils.isEmpty(pair.getTagName()))
                {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Teilung nicht möglich");
                    alert.getDialogPane().setHeader(null);
                    alert.getDialogPane().setHeaderText(null);
                    alert.setContentText("Kann Datei nicht an dieser Position teilen. Eine Teilung ist nur innerhalb des XHTML-Bodys möglich.");
                    alert.showAndWait();

                    return false;
                }
                logger.debug("umgebendes pair " + pair.getTagName());
                //wir sind innerhalb des Body
                int index = xhtmlCodeEditor.getAbsoluteCursorPosition();
                try
                {
                    String originalCode = xhtmlCodeEditor.getCode();
                    org.jdom2.Document originalDocument = XHTMLUtils.parseXHTMLDocument(originalCode);
                    List<Content> originalHeadContent = getOriginalHeadContent(originalDocument);

                    byte[] frontPart = originalCode.substring(0, index).getBytes("UTF-8");
                    Resource oldResource = currentXHTMLResource.getValue();
                    oldResource.setData(frontPart);
                    HtmlCleanerBookProcessor processor = new HtmlCleanerBookProcessor();
                    processor.processResource(oldResource);
                    xhtmlCodeEditor.setCode(new String(oldResource.getData(), "UTF-8"));

                    byte[] backPart = originalCode.substring(index, originalCode.length() - 1).getBytes("UTF-8");
                    String fileName = book.getNextStandardFileName(MediaType.XHTML);
                    Resource resource = MediaType.XHTML.getResourceFactory().createResource("Text/" + fileName);
                    byte[] backPartXHTML = XHTMLUtils.repairWithHead(backPart, originalHeadContent);
                    resource.setData(backPartXHTML);

                    int spineIndex = book.getSpine().getResourceIndex(oldResource);
                    book.addSpineResource(resource, spineIndex + 1);
                    openFileInEditor(resource, MediaType.XHTML);

                    bookBrowserManager.refreshBookBrowser();
                    needsRefresh.setValue(true);
                    needsRefresh.setValue(false);
                }
                catch (IOException | JDOMException | ResourceDataException e)
                {
                    logger.error("", e);
                    ExceptionDialog.showAndWait(e, null, "Teilung nicht möglich", "Kann Datei nicht teilen. Bitte Fehlermeldung an den Hersteller übermitteln.");
                }

                result = true;
            }
        }
        return result;
    }

    private List<Content> getOriginalHeadContent(org.jdom2.Document doc)
    {
        org.jdom2.Element root = doc.getRootElement();
        List<Content> contentList = new ArrayList<>();
        if (root != null)
        {
            org.jdom2.Element headElement = root.getChild("head", Constants.NAMESPACE_XHTML);
            if (headElement != null)
            {
                List<Content> contents = headElement.getContent();
                contentList.addAll(contents);
            }
        }
        //erst ausserhalb der Schleife detachen
        for (Content content : contentList)
        {
            content.detach();
        }
        return contentList;
    }

    public void refreshPreview()
    {
        CodeEditor xhtmlCodeEditor = currentEditor.getValue();
        if (xhtmlCodeEditor != null && currentXHTMLResource.get() != null)
        {
            try
            {
                currentXHTMLResource.get().setData(xhtmlCodeEditor.getCode().getBytes("UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                //never happens
            }
            needsRefresh.setValue(true);
            needsRefresh.setValue(false);
        }
    }

    public void reset()
    {
    }

    public ObservableBooleanValue currentEditorIsXHTMLProperty()
    {
        return currentEditorIsXHTML;
    }

    public boolean getCanRedo()
    {
        return canRedo.get();
    }

    public SimpleBooleanProperty canRedoProperty()
    {
        return canRedo;
    }

    public boolean getCanUndo()
    {
        return canUndo.get();
    }

    public SimpleBooleanProperty canUndoProperty()
    {
        return canUndo;
    }

    public void setBook(Book book)
    {
        this.book = book;
    }

    public void setBookBrowserManager(BookBrowserManager bookBrowserManager)
    {
        this.bookBrowserManager = bookBrowserManager;
    }

    public CodeEditor getCurrentEditor()
    {
        return currentEditor.get();
    }

    public ObjectProperty<CodeEditor> currentEditorProperty()
    {
        return currentEditor;
    }

    public String formatAsXHTML(String xhtml) throws IOException, JDOMException
    {
        DocType doctype = new DocType("html", "-//W3C//DTD XHTML 1.1//EN", "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
        Namespace namespace = Namespace.getNamespace("http://www.w3.org/1999/xhtml");

        org.jdom2.Document document = XHTMLUtils.parseXHTMLDocument(xhtml);

        org.jdom2.Element root = document.getRootElement();
        root.setNamespace(namespace);
        root.addNamespaceDeclaration(namespace);
        IteratorIterable<org.jdom2.Element> elements = root.getDescendants(Filters.element());
        for (org.jdom2.Element element : elements)
        {
            if (element.getNamespace() == null)
            {
                element.setNamespace(Constants.NAMESPACE_XHTML);
            }
        }
        document.setDocType(doctype);

        XMLOutputter outputter = new XMLOutputter();
        Format xmlFormat = Format.getPrettyFormat();
        outputter.setFormat(xmlFormat);
        outputter.setXMLOutputProcessor(new XHTMLOutputProcessor());
        String result = outputter.outputString(document);
        return result;
    }

    public String repairXHTML(String xhtml) throws IOException, JDOMException
    {
        return XHTMLUtils.repair(xhtml);
    }

    public void refreshAll()
    {
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs)
        {
            Resource resource = (Resource) tab.getUserData();
            if (tab.getContent() instanceof  CodeEditor)
            {
                CodeEditor editor = (CodeEditor) tab.getContent();
                try
                {
                    editor.setCode(new String(resource.getData(), "UTF-8"));
                }
                catch (IOException e)
                {
                    logger.error("", e);
                }
            }
        }
    }

    public void refreshEditorCode(Resource resourceToUpdate)
    {
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs)
        {
            Resource resource = (Resource) tab.getUserData();
            if(resourceToUpdate.equals(resource))
            {
                CodeEditor editor = (CodeEditor)tab.getContent();
                try
                {

                    editor.setCode(new String(resourceToUpdate.getData(), "UTF-8"));
                }
                catch (IOException e)
                {
                    logger.error("", e);
                }
            }
        }
    }

    public void refreshImageViewer(Resource resourceToUpdate)
    {
        List<Tab> tabs = tabPane.getTabs();
        for (Tab tab : tabs)
        {
            Resource resource = (Resource) tab.getUserData();
            if (resourceToUpdate.equals(resource))
            {
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

    public boolean isInsertablePosition()
    {
        boolean result = false;
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            Optional<XMLTagPair> optional = xhtmlCodeEditor.findSurroundingTags(tagName -> "head".equals(tagName) || "body".equals(tagName) || "html".equals(tagName));
            result = !(!optional.isPresent() || "head".equals(optional.get().getTagName()) || "html".equals(optional.get().getTagName()) || StringUtils.isEmpty(optional.get().getTagName()));
        }
        return result;
    }

    public void scrollTo(Deque<ElementPosition> nodeChain)
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML) && nodeChain.size() > 0)
        {
            XhtmlRichTextCodeEditor xhtmlCodeEditor = (XhtmlRichTextCodeEditor) currentEditor.getValue();
            String code = xhtmlCodeEditor.getCode();
            LocatedJDOMFactory factory = new LocatedJDOMFactory();
            try
            {
                org.jdom2.Document document = XHTMLUtils.parseXHTMLDocument(code, factory);
                org.jdom2.Element currentElement = document.getRootElement();
                ElementPosition currentElementPosition = nodeChain.pop();
                while(currentElementPosition != null)
                {
                    IteratorIterable<org.jdom2.Element> children;
                    if (StringUtils.isNotEmpty(currentElementPosition.getNamespaceUri()))
                    {
                        List<Namespace> namespaces = currentElement.getNamespacesInScope();
                        Namespace currentNamespace = null;
                        for (Namespace namespace : namespaces)
                        {
                            if (namespace.getURI().equals(currentElementPosition.getNamespaceUri()))
                            {
                                currentNamespace = namespace;
                                break;
                            }
                        }
                        Filter<org.jdom2.Element> filter = Filters.element(currentElementPosition.getNodeName(), currentNamespace);
                        children = currentElement.getDescendants(filter);
                    }
                    else
                    {
                        Filter<org.jdom2.Element> filter = Filters.element(currentElementPosition.getNodeName());
                        children = currentElement.getDescendants(filter);
                    }

                    int currentNumber = 0;
                    for (org.jdom2.Element child : children)
                    {
                        if (currentNumber == currentElementPosition.getPosition())
                        {
                            currentElement = child;
                            break;
                        }
                        currentNumber++;
                    }

                    try
                    {
                        currentElementPosition = nodeChain.pop();
                    }
                    catch (NoSuchElementException e)
                    {
                        logger.info("no more element in node chain");
                        currentElementPosition = null;
                    }
                }

                LocatedElement locatedElement = (LocatedElement) currentElement;
                EditorPosition pos = new EditorPosition(locatedElement.getLine() - 1, locatedElement.getColumn());
                logger.info("pos for scrolling to is " + pos.toJson());
                xhtmlCodeEditor.scrollTo(pos);
            }
            catch (IOException | JDOMException e)
            {
                logger.error("", e);
            }
        }
    }

    public ObservableValue<? extends String> cursorPosLabelProperty()
    {
        return cursorPosLabelProperty;
    }
}
