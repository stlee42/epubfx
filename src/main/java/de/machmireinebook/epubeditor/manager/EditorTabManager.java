package de.machmireinebook.epubeditor.manager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import javax.inject.Named;

import de.machmireinebook.commons.cdi.BeanFactory;
import de.machmireinebook.commons.jdom2.XHTMLOutputProcessor;
import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.editor.CssCodeEditor;
import de.machmireinebook.epubeditor.editor.EditorPosition;
import de.machmireinebook.epubeditor.editor.EditorRange;
import de.machmireinebook.epubeditor.editor.EditorToken;
import de.machmireinebook.epubeditor.editor.XHTMLCodeEditor;
import de.machmireinebook.epubeditor.editor.XMLCodeEditor;
import de.machmireinebook.epubeditor.editor.XMLTagPair;
import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.bookprocessor.HtmlCleanerBookProcessor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.ResourceDataException;
import de.machmireinebook.epubeditor.epublib.domain.XMLResource;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentReader;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import com.sun.webkit.dom.KeyboardEventImpl;
import com.sun.webkit.dom.MouseEventImpl;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.Dialogs;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 20:29
 */
@Named
public class EditorTabManager
{
    public static final Logger logger = Logger.getLogger(EditorTabManager.class);


    private TabPane tabPane;
    private ObjectProperty<CodeEditor> currentEditor = new SimpleObjectProperty<>();
    //getrennte Verwaltung der current resource für html und css, da der Previewer auf der html property lauscht und
    // wenn ein css bearbeitet wird, das letzte html-doument weiterhin im previewer angezeigt werden soll
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
    private ContextMenu contextMenuCSS;

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
                Dialogs.create()
                        .owner(tabPane)
                        .title("Bild anzeigen")
                        .masthead(null)
                        .message("Fehler beim Öffnen eines Bildes.")
                        .showException(e);
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
                    logger.info("scheduled refresh task, one second after last key up");
                    Platform.runLater(() -> {
                        String code = currentEditor.getValue().getCode();
                        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
                        {
                            try
                            {
                                currentXHTMLResource.get().setData(code.getBytes("UTF-8"));
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
                                currentCssResource.get().setData(code.getBytes("UTF-8"));
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
                                Resource resource = currentXMLResource.get();
                                resource.setData(code.getBytes("UTF-8"));
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
                        needsRefresh.setValue(true);
                        needsRefresh.setValue(false);
                    });
                    cancel();
                    return true;
                }
            };
        }
    }

    public EditorTabManager()
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

        //Html menu
        contextMenuXHTML = new ContextMenu();
        contextMenuXHTML.setAutoFix(true);
        contextMenuXHTML.setAutoHide(true);
        MenuItem itemRepairHTML = new MenuItem("HTML reparieren");
        itemRepairHTML.setOnAction(e -> {
            beautifyOrRepairHTML("repair");
        });
        contextMenuXHTML.getItems().add(itemRepairHTML);

        MenuItem itemBeautifyHTML = new MenuItem("HTML neuformatieren");
        itemBeautifyHTML.setOnAction(e -> {
            beautifyOrRepairHTML("format");
        });
        contextMenuXHTML.getItems().add(itemBeautifyHTML);

        MenuItem separatorItem = new SeparatorMenuItem();
        contextMenuXHTML.getItems().add(separatorItem);

        MenuItem openInExternalBrowserItem = new MenuItem("In externem Browser öffnen");
        openInExternalBrowserItem.setOnAction(e -> {
            openInExternalBrowser(currentEditor);
        });
        contextMenuXHTML.getItems().add(openInExternalBrowserItem);

        //css menu
        contextMenuCSS = new ContextMenu();
        contextMenuCSS.setAutoFix(true);
        contextMenuCSS.setAutoHide(true);
        MenuItem formatCSSOneLineItem = new MenuItem("Stylse jeweils in einer Zeile formatieren");
        formatCSSOneLineItem.setOnAction(e -> {
            beautifyCSS("one_line");
        });
        contextMenuCSS.getItems().add(formatCSSOneLineItem);

        MenuItem formatCSSMultipleLinesItem = new MenuItem("Styles jeweils in mehrerer Zeilen formatieren ");
        formatCSSMultipleLinesItem.setOnAction(e -> {
            beautifyCSS("multiple_lines");
        });
        contextMenuCSS.getItems().add(formatCSSMultipleLinesItem);
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
            String code = currentEditor.getValue().getCode();
            if (currentEditorIsXHTML.get())
            {
                try
                {
                    currentXHTMLResource.get().setData(code.getBytes("UTF-8"));
                    switch (mode)
                    {
                        case "format": code = formatAsXHTML(code);
                                       break;
                        case "repair": code = repairXHTML(code);
                            break;
                    }
                    currentXHTMLResource.get().setData(code.getBytes("UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    //never happens
                }
            }
            currentEditor.getValue().setCode(code);
            needsRefresh.setValue(true);
            needsRefresh.setValue(false);
        }
        catch (IOException | JDOMException e)
        {
            logger.error("", e);
            Dialogs.create()
                    .owner(tabPane)
                    .title("Formatierung nicht möglich")
                    .message("Kann Datei nicht formatieren. Bitte die Fehlermeldung an den Hersteller weitergeben.")
                    .showException(e);
        }
    }

    public void openXHTMLFileInEditor(Resource xhtmlResource)
    {
        if (!isTabAlreadyOpen(xhtmlResource))
        {
            try
            {
                openFileInEditor(xhtmlResource, MediaType.XHTML);
            }
            catch (IllegalArgumentException e)
            {
                logger.error("shoud never happen", e);
            }
        }
    }

    public void openXMLFileInEditor(Resource xmlResource)
    {
        if (!isTabAlreadyOpen(xmlResource))
        {
            try
            {
                openFileInEditor(xmlResource, MediaType.XML);
            }
            catch (IllegalArgumentException e)
            {
                logger.error("shoud never happen", e);
            }
        }
    }

    public void openCssFileInEditor(Resource cssResource)
    {
        if (!isTabAlreadyOpen(cssResource))
        {
            try
            {
                openFileInEditor(cssResource, MediaType.CSS);
            }
            catch (IllegalArgumentException e)
            {
                logger.error("shoud never happen", e);
            }
        }
    }

    public void openImageFile(Resource resource)
    {
        Tab tab = new Tab();
        tab.setClosable(true);
        if (resource == null)
        {
            Dialogs.create()
                    .owner(tabPane)
                    .title("Datei nicht vorhanden")
                    .message("Die angeforderte Datei ist nicht vorhanden und kann deshalb nicht geöffnet werden.")
                    .showError();
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

    private void openFileInEditor(Resource resource, MediaType mediaType) throws IllegalArgumentException
    {
        Tab tab = new Tab();
        tab.setClosable(true);
        if (resource == null)
        {
            Dialogs.create()
                    .owner(tabPane)
                    .title("Datei nicht vorhanden")
                    .message("Die angeforderte Datei ist nicht vorhanden und kann deshalb nicht geöffnet werden.")
                    .showError();
            return;
        }
        tab.setText(resource.getFileName());

        String content = "";
        try
        {
            content = new String(resource.getData(), "UTF-8");
        }
        catch (IOException e)
        {
            logger.error(e);
        }

        CodeEditor editor;
        if (mediaType.equals(MediaType.CSS))
        {
            editor = new CssCodeEditor();
        }
        else if (mediaType.equals(MediaType.XHTML))
        {
            editor = new XHTMLCodeEditor();
        }
        else if (mediaType.equals(MediaType.XML))
        {
            editor = new XMLCodeEditor();
        }
        else
        {
            throw new IllegalArgumentException("no editor for mediatype " + mediaType.getName());
        }

        tab.setContent((Node) editor);
        tab.setUserData(resource);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        WebView webView = editor.getWebview();
        WebEngine engine = editor.getWebview().getEngine();

        final String code = content;
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>()
        {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue)
            {
                if (newValue.equals(Worker.State.SUCCEEDED))
                {
                    editor.setCode(code);
                    Document document = engine.getDocument();
                    Element documentElement = document.getDocumentElement();
                    ((EventTarget) documentElement).addEventListener("keyup", evt ->
                    {
                        logger.info("key up in content editor: " + evt.getTarget());
                        EditorPosition cursorPos = currentEditor.get().getEditorCursorPosition();
                        cursorPosLabelProperty.set(cursorPos.getLine() + ":" + cursorPos.getColumn());
                        if (scheduledService.getState().equals(Worker.State.READY))
                        {
                            scheduledService.start();
                        }
                        else
                        {
                            scheduledService.restart();
                        }
                    }, false);

                    ((EventTarget) documentElement).addEventListener("keydown", evt ->
                    {
                        boolean isCtrlPressed = ((KeyboardEventImpl) evt).getCtrlKey();
                        int keyCode = ((KeyboardEventImpl) evt).getKeyCode();
                        logger.info("key down in content editor: " + isCtrlPressed + "-" + keyCode + ", Cancelable " + evt.getCancelable());
                        //Ctrl-Z abfangen um eigenen Undo/Redo-Manager zu verwenden
                        if (isCtrlPressed && keyCode == 90)
                        {
                            logger.info("Ctrl-Z gedrückt");
                            evt.preventDefault();
                            currentEditor.getValue().undo();
                        }
                    }, false);

                    ((EventTarget) documentElement).addEventListener("contextmenu", evt ->
                    {
                        logger.info("contextmenu event aufgefangen " + evt);
                        evt.preventDefault();
                        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
                        {
                            contextMenuXHTML.show(tabPane, ((MouseEventImpl) evt).getScreenX(), ((MouseEventImpl) evt).getScreenY());
                        }
                        else if (currentEditor.getValue().getMediaType().equals(MediaType.CSS))
                        {
                            contextMenuCSS.show(tabPane, ((MouseEventImpl) evt).getScreenX(), ((MouseEventImpl) evt).getScreenY());
                        }
                    }, false);

                    editor.setCodeEditorSize(((AnchorPane) editor).getWidth() - 20, ((AnchorPane) editor).getHeight() - 20);
                    ((AnchorPane) editor).widthProperty().addListener(new ChangeListener<Number>()
                    {
                        @Override
                        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                        {
                            editor.setCodeEditorSize(newValue.doubleValue() - 20, ((AnchorPane) editor).getHeight() - 20);
                        }
                    });
                    ((AnchorPane) editor).heightProperty().addListener(new ChangeListener<Number>()
                    {
                        @Override
                        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                        {
                            editor.setCodeEditorSize(((AnchorPane) editor).getWidth() - 20, newValue.doubleValue() - 20);
                        }
                    });

                    webView.setOnScroll(new EventHandler<ScrollEvent>()
                    {
                        @Override
                        public void handle(ScrollEvent event)
                        {
                            Double delta = event.getDeltaY() * -1;
                            editor.scroll(delta.intValue());
                        }
                    });

                }
            }
        });
    }

    public Resource getCurrentXHTMLResource()
    {
        return currentXHTMLResource.get();
    }

    public ReadOnlyObjectProperty<Resource> currentXHTMLResourceProperty()
    {
        return currentXHTMLResource.getReadOnlyProperty();
    }

    public BooleanProperty needsRefreshProperty()
    {
        return needsRefresh;
    }

    public void setTabPane(TabPane tabPane)
    {
        this.tabPane = tabPane;

        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>()
        {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue)
            {
                Resource resource;
                if (newValue != null && newValue.getContent() instanceof CodeEditor)
                {
                    resource = (Resource) newValue.getUserData();
                    currentEditor.setValue((CodeEditor) tabPane.getSelectionModel().getSelectedItem().getContent());

                    if (newValue.getContent() instanceof XHTMLCodeEditor)
                    {
                        currentXHTMLResource.set(resource);
                    }
                    else if (newValue.getContent() instanceof CssCodeEditor)
                    {
                        currentCssResource.set(resource);
                    }
                    else if (newValue.getContent() instanceof XMLCodeEditor)
                    {
                        currentXMLResource.set(resource);
                    }
                }
            }
        });
    }

    public void surroundParagraphWithTag(String tagName)
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XHTMLCodeEditor xhtmlCodeEditor = (XHTMLCodeEditor) currentEditor.getValue();
            XMLTagPair pair = xhtmlCodeEditor.findSurroundingTags(new XHTMLCodeEditor.BlockTagInspector());
            if (pair != null)
            {
                logger.info("found xml block tag " + pair.getTagName());
                //erst das schließende Tag ersetzen, da sich sonst die Koordinaten verschieben können
                xhtmlCodeEditor.replaceRange(tagName, pair.getCloseTagBegin(), pair.getCloseTagEnd());
                xhtmlCodeEditor.replaceRange(tagName, pair.getOpenTagBegin(), pair.getOpenTagEnd());
                refreshPreview();
            }
        }
    }

    public void surroundSelectionWithTag(String tagName)
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XHTMLCodeEditor xhtmlCodeEditor = (XHTMLCodeEditor) currentEditor.getValue();
            EditorRange range = xhtmlCodeEditor.getSelection();
            xhtmlCodeEditor.replaceRange("<" + tagName + ">" + range.getSelection() + "</" + tagName + ">",
                    range.getFrom(), range.getTo());
            refreshPreview();
        }
    }

    public boolean splitXHTMLFile()
    {
        boolean result = false;
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XHTMLCodeEditor xhtmlCodeEditor = (XHTMLCodeEditor) currentEditor.getValue();
            EditorPosition pos = xhtmlCodeEditor.getEditorCursorPosition();
            XMLTagPair pair = xhtmlCodeEditor.findSurroundingTags(new XHTMLCodeEditor.TagInspector()
            {
                @Override
                public boolean isTagFound(EditorToken token)
                {
                    String type = token.getType();
                    if ("tag".equals(type))
                    {
                        String content = token.getContent();
                        if ("head".equals(content) || "body".equals(content) || "html".equals(content))
                        {
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (pair == null || "head".equals(pair.getTagName()) || "html".equals(pair.getTagName()) || StringUtils.isEmpty(pair.getTagName()))
            {
                Dialogs.create()
                        .owner(tabPane)
                        .title("Teilung nicht möglich")
                        .message("Kann Datei nicht an dieser Position teilen. Eine Teilung ist nur innerhalb des XHTML-Bodys möglich.")
                        .showWarning();
                return false;
            }
            logger.debug("umgebendes pair " + pair);
            //wir sind innerhalb des Body
            int index = xhtmlCodeEditor.getIndexFromPosition(pos);
            try
            {
                String originalCode = xhtmlCodeEditor.getCode();
                org.jdom2.Document originalDocument = XHTMLUtils.parseXHTMLDocument(originalCode);
                List<Content> originalHeadContent = getOriginalHeadContent(originalDocument);

                byte[] frontPart = originalCode.substring(0, index).getBytes("UTF-8");
                Resource oldResource = currentXHTMLResource.getValue();
                oldResource.setNoValidData(frontPart);
                HtmlCleanerBookProcessor processor = new HtmlCleanerBookProcessor();
                processor.processResource(oldResource);
                xhtmlCodeEditor.setCode(new String(oldResource.getData(), "UTF-8"));

                byte[] backPart = originalCode.substring(index, originalCode.length() - 1).getBytes("UTF-8");
                String fileName = book.getNextStandardFileName(MediaType.XHTML);
                Resource resource = MediaType.XHTML.getResourceFactory().createResource("Text/" + fileName);
                byte[] backPartXHTML = XHTMLUtils.repairWithHead(backPart, originalHeadContent);
                resource.setData(backPartXHTML);

                int spineIndex = book.getSpine().getResourceIndex(oldResource);
                book.addSpineResource(resource, spineIndex);
                openXHTMLFileInEditor(resource);

                bookBrowserManager.refreshBookBrowser();
                needsRefresh.setValue(true);
                needsRefresh.setValue(false);
            }
            catch (IOException | JDOMException | ResourceDataException e )
            {
                logger.error("", e);
                Dialogs.create()
                        .owner(tabPane)
                        .title("Teilung nicht möglich")
                        .message("Kann Datei nicht teilen. Bitte Fehlermeldung an den Hersteller übermitteln.")
                        .showException(e);
            }

            result = true;
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
                for (Content content : contents)
                {
                    contentList.add(content);
                }
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
        XHTMLCodeEditor xhtmlCodeEditor = (XHTMLCodeEditor) currentEditor.getValue();
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
            XHTMLCodeEditor xhtmlCodeEditor = (XHTMLCodeEditor) currentEditor.getValue();
            XMLTagPair pair = xhtmlCodeEditor.findSurroundingTags(new XHTMLCodeEditor.TagInspector()
            {
                @Override
                public boolean isTagFound(EditorToken token)
                {
                    String type = token.getType();
                    if ("tag".equals(type))
                    {
                        String content = token.getContent();
                        if ("head".equals(content) || "body".equals(content) || "html".equals(content))
                        {
                            return true;
                        }
                    }
                    return false;
                }
            });
            result = !(pair == null || "head".equals(pair.getTagName()) || "html".equals(pair.getTagName()) || StringUtils.isEmpty(pair.getTagName()));
        }
        return result;
    }

    public void scrollTo(Deque<ElementPosition> nodeChain)
    {
        if (currentEditor.getValue().getMediaType().equals(MediaType.XHTML))
        {
            XHTMLCodeEditor xhtmlCodeEditor = (XHTMLCodeEditor) currentEditor.getValue();
            String code = xhtmlCodeEditor.getCode();
/*            int index = code.indexOf(html);
            logger.info("index of clicked html " + index + " html: " + html);
            xhtmlCodeEditor.scrollTo(index);*/
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
