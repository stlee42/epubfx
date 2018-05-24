package de.machmireinebook.epubeditor.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Files;

import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.CSSResource;
import de.machmireinebook.epubeditor.epublib.domain.Guide;
import de.machmireinebook.epubeditor.epublib.domain.GuideReference;
import de.machmireinebook.epubeditor.epublib.domain.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.JavascriptResource;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.domain.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.domain.XMLResource;
import de.machmireinebook.epubeditor.epublib.util.ResourceFilenameComparator;
import de.machmireinebook.epubeditor.gui.AddStylesheetController;
import de.machmireinebook.epubeditor.gui.EpubEditorMainController;
import de.machmireinebook.epubeditor.javafx.FXUtils;
import de.machmireinebook.epubeditor.javafx.cells.EditingTreeCell;

/**
 * User: mjungierek
 * Date: 23.07.2014
 * Time: 22:17
 */
@Singleton
public class BookBrowserManager
{
    private static final Logger logger = Logger.getLogger(BookBrowserManager.class);

    private TreeItem<Resource> textItem;
    private TreeItem<Resource> cssItem;
    private TreeItem<Resource> imagesItem;
    private TreeItem<Resource> fontsItem;
    private TreeItem<Resource> rootItem;

    private Book book;
    private TreeView<Resource> treeView;
    private EditorTabManager editorManager;
    private TreeItem<Resource> ncxItem;
    private TreeItem<Resource> opfItem;

    @Inject
    private Provider<EpubEditorMainController> mainControllerProvider;

    @Inject
    private EpubEditorConfiguration configuration;

    private class FolderSymbolListener implements ChangeListener<Boolean>
    {
        TreeItem<Resource> item;

        private FolderSymbolListener(TreeItem<Resource> item)
        {
            this.item = item;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
        {
            if (newValue)
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_Open_48px.png", 24));
            }
            else
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", 24));
            }
        }
    }

    public class BookBrowserTreeCellFactory implements Callback<TreeView<Resource>, TreeCell<Resource>>
    {
        private final DataFormat OBJECT_DATA_FORMAT = new DataFormat("application/java-object");

        @Override
        public TreeCell<Resource> call(TreeView<Resource> resourceTreeView)
        {
            EditingTreeCell<Resource> treeCell = new EditingTreeCell<>();

            treeCell.itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !MediaType.XML.equals(newValue.getMediaType())
                        && !MediaType.OPF.equals(newValue.getMediaType()) && !MediaType.NCX.equals(newValue.getMediaType()))
                {
                    treeCell.setEditable(true);
                }
                else
                {
                    treeCell.setEditable(false);
                }
            });

            treeCell.setOnDragDetected(mouseEvent -> {
                TreeCell<Resource> cell = (TreeCell<Resource>) mouseEvent.getSource();
                logger.info("dnd detected on item " + cell);
                Resource res = cell.getItem();
                if (MediaType.XHTML.equals(res.getMediaType()))
                {
                    Dragboard dragBoard = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.put(OBJECT_DATA_FORMAT, res);
                    dragBoard.setContent(content);
                    mouseEvent.consume();
                }
            });

            treeCell.setOnDragDropped(dragEvent -> {
                logger.info("dnd dropped of item " + dragEvent.getSource());
                Object o = dragEvent.getDragboard().getContent(OBJECT_DATA_FORMAT);
                if (o != null)
                {
                    TreeCell<Resource> draggedCell = (TreeCell<Resource>) dragEvent.getGestureSource();
                    logger.info("dragged cell is " + draggedCell.getItem());
                    logger.info("cell dropped on is " + treeCell.getItem());

                    TreeItem<Resource> draggedItem = draggedCell.getTreeItem();
                    textItem.getChildren().remove(draggedItem);
                    int index = textItem.getChildren().indexOf(treeCell.getTreeItem());
                    textItem.getChildren().add(index, draggedItem);
                    treeView.getSelectionModel().select(draggedItem);
                    //noch im spine moven
                    book.getSpine().moveSpineReference(draggedItem.getValue(), treeCell.getItem());

                    treeView.getSelectionModel().clearSelection();
                    treeView.getSelectionModel().select(draggedCell.getTreeItem());
                    refreshOpf();
                    book.setBookIsChanged(true);
                }
                // remove all dnd effects
                treeCell.setEffect(null);
                treeCell.getStyleClass().remove("dnd-below");

                dragEvent.consume();
            });

            treeCell.setOnDragOver(event -> {
                Resource res = treeCell.getItem();
                if (MediaType.XHTML.equals(res.getMediaType()))
                {
                    Point2D sceneCoordinates = treeCell.localToScene(0d, 0d);

                    double height = treeCell.getHeight();

                    // get the y coordinate within the control
                    double y = event.getSceneY() - (sceneCoordinates.getY());


                    // set the dnd effect for the required action
                    if (y > (height * .75d))
                    {
                        treeCell.getStyleClass().add("dnd-below");
                        treeCell.setEffect(null);
                    }
                    else
                    {
                        treeCell.getStyleClass().remove("dnd-below");

                        InnerShadow shadow = new InnerShadow();
                        shadow.setOffsetX(1.0);
                        shadow.setColor(Color.web("#666666"));
                        shadow.setOffsetY(1.0);
                        treeCell.setEffect(shadow);
                    }
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            });

            treeCell.setOnDragExited(event -> {
                // remove all dnd effects
                treeCell.setEffect(null);
                treeCell.getStyleClass().remove("dnd-below");
            });

            return treeCell;
        }
    }

    public void setTreeView(TreeView<Resource> treeView)
    {
        this.treeView = treeView;
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);

        treeView.setCellFactory(new BookBrowserTreeCellFactory());

        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.PRIMARY))
            {
                if (event.getClickCount() == 2)
                {
                    TreeItem<Resource> item = treeView.getSelectionModel().getSelectedItem();
                    if (isTextItem(item))
                    {
                        editorManager.openFileInEditor(item.getValue(), MediaType.XHTML);
                    }
                    else if (isCssItem(item))
                    {
                        editorManager.openFileInEditor(item.getValue(), MediaType.CSS);
                    }
                    else if (isXmlItem(item))
                    {
                        editorManager.openFileInEditor(item.getValue(), MediaType.XML);
                    }
                    else if (isImageItem(item))
                    {
                        editorManager.openImageFile(item.getValue());
                    }
                    event.consume();
                }
            }
            else if (event.getButton().equals(MouseButton.SECONDARY))
            {
                TreeItem<Resource> item = treeView.getSelectionModel().getSelectedItem();
                if (isTextItem(item))
                {
                    createXHTMLItemContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
                else if (isCssItem(item))
                {
                    createCssItemContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
                else if (isImageItem(item))
                {
                    createImageItemContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
            }
        });
        treeView.setOnEditCommit(event -> {
            logger.info("editing end for new value " + event.getNewValue());
            editorManager.refreshAll();
            book.setBookIsChanged(true);
        });
        treeView.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            logger.info("key typed in tree view editor: " + keyCode);

            if (keyCode.equals(KeyCode.DELETE))
            {
                logger.debug("Delete gedrückt");
                deleteSelectedItems();
            }
        });


        Resource textResource = new Resource("Text");
        textItem = new TreeItem<>(textResource);
        textItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", 24));
        textItem.expandedProperty().addListener(new FolderSymbolListener(textItem));
        rootItem.getChildren().add(textItem);

        Resource cssResource = new Resource("Styles");
        cssItem = new TreeItem<>(cssResource);
        cssItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", 24));
        cssItem.expandedProperty().addListener(new FolderSymbolListener(cssItem));
        rootItem.getChildren().add(cssItem);

        Resource imagesResource = new Resource("Bilder");
        imagesItem = new TreeItem<>(imagesResource);
        imagesItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", 24));
        imagesItem.expandedProperty().addListener(new FolderSymbolListener(imagesItem));
        rootItem.getChildren().add(imagesItem);

        Resource fontsResource = new Resource("Fonts");
        fontsItem = new TreeItem<>(fontsResource);
        fontsItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", 24));
        fontsItem.expandedProperty().addListener(new FolderSymbolListener(fontsItem));
        rootItem.getChildren().add(fontsItem);
    }

    private ContextMenu createXHTMLItemContextMenu(TreeItem<Resource> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Delete...");
        item.setUserData(treeItem);
        item.setOnAction(event -> deleteSelectedItems());
        item.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE));
        menu.getItems().add(item);

        item = new MenuItem("Rename...");
        item.setUserData(treeItem);
        item.setOnAction(event -> renameItem(treeItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        menu.getItems().add(item);

        item = new MenuItem("Join");
        item.setUserData(treeItem);
        item.setOnAction(event -> joinXHTMLItemWitPreviousItem(treeItem));
        menu.getItems().add(item);

        item = new MenuItem("Sort");
        item.setUserData(treeItem);
        item.setOnAction(event -> sortSelectedItems());
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Add Stylesheet...");
        item.setUserData(treeItem);
        item.setOnAction(event -> addStylesheet());
        menu.getItems().add(item);

        Menu semantikMenu = new Menu("Add Semantics");
        menu.getItems().add(semantikMenu);
        addSemanticsMenuItems(semantikMenu, treeItem);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        Menu openWithItem = new Menu("Open with");
        menu.getItems().add(openWithItem);
        addXhtmlOpenWithApplicationItems(openWithItem, treeItem);

        item = new MenuItem("Save as...");
        item.setUserData(treeItem);
        item.setOnAction(event -> saveAs(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Add existing Files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        item = new MenuItem("Add empty File");
        item.setUserData(treeItem);
        item.setOnAction(event -> addEmptyXHTMLFile(treeItem));
        menu.getItems().add(item);

        item = new MenuItem("Add Copy");
        item.setUserData(treeItem);
        item.setOnAction(event -> addCopy(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Select all");
        item.setUserData(treeItem);
        item.setOnAction(event -> selectAll(textItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        menu.getItems().add(item);

        return menu;
    }

    private void addSemanticsMenuItems(Menu menu, TreeItem<Resource> treeItem)
    {
        GuideReference.Semantics[] semantics = GuideReference.Semantics.values();
        Resource resource = treeItem.getValue();
        Guide guide = book.getGuide();
        for (GuideReference.Semantics semantic : semantics)
        {
            CheckMenuItem item = new CheckMenuItem(semantic.getDescription());
            List<GuideReference> typeReferences = guide.getGuideReferencesByType(semantic);
            for (GuideReference typeReference : typeReferences)
            {
                if (typeReference.getResource().equals(resource))
                {
                    item.setSelected(true);
                }
            }
            item.setUserData(treeItem);
            item.setOnAction(event -> {
                if (item.isSelected())
                {
                    addSemanticsToXHTMLFile(treeItem, semantic);
                    book.setBookIsChanged(true);
                }
                else
                {
                    removeSemanticsFromXHTMLFile(treeItem, semantic);
                    book.setBookIsChanged(true);
                }
            });
            menu.getItems().add(item);
        }

    }

    private void addXhtmlOpenWithApplicationItems(Menu menu, TreeItem<Resource> treeItem)
    {
        List<EpubEditorConfiguration.OpenWithApplication> applications = configuration.getXhtmlOpenWithApplications();
        for (EpubEditorConfiguration.OpenWithApplication application : applications)
        {
            MenuItem item = new MenuItem(application.getDisplayName());
            item.setUserData(treeItem);
            item.setOnAction(event -> openWithApplication(treeItem, application.getFileName()));
            menu.getItems().add(item);
        }
        MenuItem item = new MenuItem("Anwendung konfigurieren...");
        item.setUserData(treeItem);
        item.setOnAction(event -> configureApplicationForOpenXHTML(treeItem));
        menu.getItems().add(item);
    }

    private ContextMenu createCssItemContextMenu(TreeItem<Resource> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Löschen...");
        item.setUserData(treeItem);
        item.setOnAction(event -> deleteSelectedItems());
        item.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        menu.getItems().add(item);

        item = new MenuItem("Umbenennen...");
        item.setUserData(treeItem);
        item.setOnAction(event -> renameItem(treeItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        menu.getItems().add(item);

        item = new MenuItem("Validieren");
        item.setUserData(treeItem);
        item.setOnAction(event -> validateCss(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        Menu openWithItem = new Menu("Öffnen mit");
        menu.getItems().add(openWithItem);
        addCssOpenWithApplicationItems(openWithItem, treeItem);

        item = new MenuItem("Speichern unter...");
        item.setUserData(treeItem);
        item.setOnAction(event -> saveAs(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Add empty CSS File");
        item.setUserData(treeItem);
        item.setOnAction(event -> addEmptyCssFile());
        menu.getItems().add(item);

        item = new MenuItem("Kopie hinzufügen");
        item.setUserData(treeItem);
        item.setOnAction(event -> addCopy(treeItem));
        menu.getItems().add(item);

        item = new MenuItem("Bestehende Dateien hinzufügen...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Alles auswählen");
        item.setUserData(treeItem);
        item.setOnAction(event -> selectAll(cssItem));
        menu.getItems().add(item);

        return menu;
    }

    private void addCssOpenWithApplicationItems(Menu menu, TreeItem<Resource> treeItem)
    {
        List<EpubEditorConfiguration.OpenWithApplication> applications = configuration.getCssOpenWithApplications();
        for (EpubEditorConfiguration.OpenWithApplication application : applications)
        {
            MenuItem item = new MenuItem(application.getDisplayName());
            item.setUserData(treeItem);
            item.setOnAction(event -> openWithApplication(treeItem, application.getFileName()));
            menu.getItems().add(item);
        }
        MenuItem item = new MenuItem("Weitere Anwendung konfigurieren...");
        item.setUserData(treeItem);
        item.setOnAction(event -> configureApplicationForOpenCSS(treeItem));
        menu.getItems().add(item);
    }

    private ContextMenu createImageItemContextMenu(TreeItem<Resource> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Löschen...");
        item.setUserData(treeItem);
        item.setOnAction(event -> deleteSelectedItems());
        item.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        menu.getItems().add(item);

        item = new MenuItem("Umbenennen...");
        item.setUserData(treeItem);
        item.setOnAction(event -> renameItem(treeItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        menu.getItems().add(item);

        Menu semantikItem = new Menu("Semantik hinzufügen...");
        menu.getItems().add(semantikItem);
        addImageSemantikMenuItems(semantikItem, treeItem);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        Menu openWithItem = new Menu("Öffnen mit");
        menu.getItems().add(openWithItem);
        addImageOpenWithApplicationItems(openWithItem, treeItem);

        item = new MenuItem("Speichern unter...");
        item.setUserData(treeItem);
        item.setOnAction(event -> saveAs(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Bestehende Dateien hinzufügen...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        return menu;
    }

    private void addImageSemantikMenuItems(Menu menu, TreeItem<Resource> treeItem)
    {
        ImageResource resource = (ImageResource)treeItem.getValue();
        CheckMenuItem item = new CheckMenuItem("Cover Image");
        if (resource.coverProperty().getValue())
        {
            item.setSelected(true);
        }
        else
        {
            item.setSelected(false);
        }
        item.setUserData(treeItem);
        item.setOnAction(event -> {
            if (item.isSelected())
            {
                resource.coverProperty().setValue(true);
                book.setBookIsChanged(true);
            }
            else
            {
                resource.coverProperty().setValue(false);
                book.setBookIsChanged(true);
            }
        });
        menu.getItems().add(item);
    }

    private void addImageOpenWithApplicationItems(Menu menu, TreeItem<Resource> treeItem)
    {
        List<EpubEditorConfiguration.OpenWithApplication> applications = configuration.getImageOpenWithApplications();
        for (EpubEditorConfiguration.OpenWithApplication application : applications)
        {
            MenuItem item = new MenuItem(application.getDisplayName());
            item.setUserData(treeItem);
            item.setOnAction(event -> openWithApplication(treeItem, application.getFileName()));
            menu.getItems().add(item);
        }
        MenuItem item = new MenuItem("Weitere Anwendung konfigurieren...");
        item.setUserData(treeItem);
        item.setOnAction(event -> configureApplicationForOpenImage(treeItem));
        menu.getItems().add(item);
    }

    public void refreshBookBrowser()
    {
        textItem.getChildren().clear();
        cssItem.getChildren().clear();
        imagesItem.getChildren().clear();
        fontsItem.getChildren().clear();

        rootItem.getChildren().remove(opfItem);
        rootItem.getChildren().remove(ncxItem);

        List<SpineReference> xhtmlResources = book.getSpine().getSpineReferences();
        for (SpineReference xhtmlResource : xhtmlResources)
        {
            TreeItem<Resource> xhtmlItem = new TreeItem<>(xhtmlResource.getResource());
            xhtmlItem.setGraphic(FXUtils.getIcon("/icons/icons8_Code_File_96px.png", 24));
            textItem.getChildren().add(xhtmlItem);
        }

        List<Resource> cssResources = book.getResources().getResourcesByMediaType(MediaType.CSS);
        cssResources.sort(new ResourceFilenameComparator());
        for (Resource cssResource : cssResources)
        {
            TreeItem<Resource> item = new TreeItem<>(cssResource);
            item.setGraphic(FXUtils.getIcon("/icons/icons8_CSS_Filetype_96px.png", 24));
            cssItem.getChildren().add(item);
        }

        List<Resource> fontResources = book.getResources().getFontResources();
        fontResources.sort(new ResourceFilenameComparator());
        for (Resource fontResource : fontResources)
        {
            TreeItem<Resource> item = new TreeItem<>(fontResource);
            if (MediaType.TTF.equals(fontResource.getMediaType()))
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_TTF_48px.png", 24));
            }
            else if (MediaType.OPENTYPE_UNTIL_3.equals(fontResource.getMediaType()) || MediaType.OPENTYPE_SINCE_3_1.equals(fontResource.getMediaType()))
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_OTF_48px.png", 24));
            }
            else if (MediaType.WOFF.equals(fontResource.getMediaType()) || MediaType.WOFF2.equals(fontResource.getMediaType()))
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_WOFF_48px.png", 24));
            }
            fontsItem.getChildren().add(item);
        }

        List<Resource> imageResources = book.getResources().getResourcesByMediaTypes(new MediaType[]{
                MediaType.GIF,
                MediaType.PNG,
                MediaType.SVG,
                MediaType.JPG});
        imageResources.sort(new ResourceFilenameComparator());
        for (Resource imageResource : imageResources)
        {
            TreeItem<Resource> item = new TreeItem<>(imageResource);
            if (imageResource.getMediaType().equals(MediaType.GIF))
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_Image_File_96px.png", 24));
            }
            else if (imageResource.getMediaType().equals(MediaType.PNG))
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_Image_File_96px.png", 24));
            }
            else if (imageResource.getMediaType().equals(MediaType.SVG))
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_Image_File_96px.png", 24));
            }
            else if (imageResource.getMediaType().equals(MediaType.JPG))
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_Image_File_96px.png", 24));
            }
            imagesItem.getChildren().add(item);
        }

        opfItem = new TreeItem<>();
        opfItem.valueProperty().bind(book.opfResourceProperty());
        opfItem.setGraphic(FXUtils.getIcon("/icons/icons8_Code_File_96px.png", 24));
        rootItem.getChildren().add(opfItem);

        Resource ncxResource = book.getNcxResource();
        if (ncxResource != null) //in case of epub it could be null
        {
            ncxItem = new TreeItem<>(ncxResource);
            ncxItem.setGraphic(FXUtils.getIcon("/icons/icons8_Code_File_96px.png", 24));
            rootItem.getChildren().add(ncxItem);
        }

        textItem.expandedProperty().setValue(true);
        treeView.getSelectionModel().clearSelection();
        if (textItem.getChildren().size() > 0)
        {
            treeView.getSelectionModel().select(textItem.getChildren().get(0));
        }
        else
        {
            treeView.getSelectionModel().select(textItem);
        }
    }

    public void selectTextItem(Resource resource)
    {
        if (resource == null)
        {
            return;
        }
        textItem.expandedProperty().setValue(true);
        List<TreeItem<Resource>> textItems = textItem.getChildren();
        for (TreeItem<Resource> item : textItems)
        {
            if (item.getValue().equals(resource))
            {
                treeView.getSelectionModel().select(item);
                break;
            }
        }
    }

    public void setEditorManager(EditorTabManager editorManager)
    {
        this.editorManager = editorManager;
    }

    public void setBook(Book book)
    {
        this.book = book;
        refreshBookBrowser();
        selectTextItem(book.getSpine().getResource(0));
    }

    public boolean isTextItem(TreeItem<Resource> item)
    {
        return item.getParent().equals(textItem);
    }

    public boolean isCssItem(TreeItem<Resource> item)
    {
        return item.getParent().equals(cssItem);
    }

    private boolean isImageItem(TreeItem<Resource> item)
    {
        return item.getParent().equals(imagesItem);
    }

    public boolean isXmlItem(TreeItem<Resource> item)
    {
        return item.getParent().equals(rootItem) &&
                !item.equals(textItem) && !item.equals(cssItem) && !item.equals(fontsItem) && !item.equals(imagesItem);
    }

    private void deleteSelectedItems()
    {
        List<TreeItem<Resource>> selectedItems = treeView.getSelectionModel().getSelectedItems();
        for (TreeItem<Resource> selectedItem : selectedItems)
        {
            if (selectedItem.getValue().getMediaType().equals(MediaType.CSS))
            {
                deleteCssItem(selectedItem);
            }
            else if (selectedItem.getValue().getMediaType().equals(MediaType.XHTML))
            {
                deleteXHTMLItem(selectedItem);
            }
            else if (selectedItem.getValue().getMediaType().isBitmapImage())
            {
                deleteImageItem(selectedItem);
            }
            else if (selectedItem.getValue().getMediaType().isFont())
            {
                deleteFontItem(selectedItem);
            }
        }
    }

    private void deleteXHTMLItem(TreeItem<Resource> treeItem)
    {
        book.removeSpineResource(treeItem.getValue());
        editorManager.refreshEditorCode(book.getOpfResource());
        textItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    private void deleteCssItem(TreeItem<Resource> treeItem)
    {
        book.removeResource(treeItem.getValue());
        editorManager.refreshEditorCode(book.getOpfResource());
        cssItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    private void deleteImageItem(TreeItem<Resource> treeItem)
    {
        book.removeResource(treeItem.getValue());
        editorManager.refreshEditorCode(book.getOpfResource());
        imagesItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    private void deleteFontItem(TreeItem<Resource> treeItem)
    {
        book.removeResource(treeItem.getValue());
        editorManager.refreshEditorCode(book.getOpfResource());
        fontsItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    public void refreshOpf()
    {
        book.refreshOpfResource();
        editorManager.refreshEditorCode(book.getOpfResource());
    }

    private void sortSelectedItems()
    {


    }

    public void addEmptyXHTMLFile()
    {
        TreeItem<Resource> treeItem = null;
        if (textItem.getChildren().size() > 0)
        {
            treeItem = treeView.getSelectionModel().getSelectedItem();
            if (treeItem == null || !MediaType.XHTML.equals(treeItem.getValue().getMediaType()))
            {
                treeItem = textItem.getChildren().get(textItem.getChildren().size() - 1);
            }
        }
        addEmptyXHTMLFile(treeItem);
    }

    public void addEmptyXHTMLFile(TreeItem<Resource> treeItem)
    {
        String fileName = book.getNextStandardFileName(MediaType.XHTML);
        Resource res;
        if (book.isEpub3() && book.isFixedLayout())
        {
            res = book.addResourceFromTemplate("/epub/template_fixed_layout.xhtml", "Text/" + fileName);
        }
        else
        {
            res = book.addResourceFromTemplate("/epub/template.xhtml", "Text/" + fileName);
        }

        int index = 0;
        if (treeItem != null)
        {
            index = textItem.getChildren().indexOf(treeItem);
        }
        TreeItem<Resource> xhtmlItem = new TreeItem<>(res);
        xhtmlItem.setGraphic(FXUtils.getIcon("/icons/icons8_Code_File_96px.png", 24));
        if (textItem.getChildren().size() > 0)
        {
            textItem.getChildren().add(index + 1, xhtmlItem);
            treeView.getSelectionModel().clearAndSelect(index + 2);
        }
        else
        {
            textItem.getChildren().add(xhtmlItem);
            treeView.getSelectionModel().clearAndSelect(0);
        }

        book.addSpineResource(res, index + 1);
        book.setBookIsChanged(true);
        editorManager.refreshEditorCode(book.getOpfResource());

        editorManager.openFileInEditor(res, MediaType.XHTML);
    }

    private void addCopy(TreeItem<Resource> treeItem)
    {
    }


    private void saveAs(TreeItem<Resource> treeItem)
    {


    }

    private void addStylesheet()
    {
        ObservableList<TreeItem<Resource>> selectedItems = treeView.getSelectionModel().getSelectedItems();
        List<Resource> resources = new ArrayList<>();
        boolean allAreValid = true;
        for (TreeItem<Resource> selectedItem : selectedItems)
        {
            if (((XHTMLResource)selectedItem.getValue()).isValidXML())
            {
                resources.add(selectedItem.getValue());
                book.setBookIsChanged(true);
            }
            else
            {
                allAreValid = false;
                break;
            }
        }

        if (allAreValid)
        {
            Stage stylesheetWindow = mainControllerProvider.get().createStandardController("/add_stylesheet.fxml", AddStylesheetController.class);
            AddStylesheetController controller = AddStylesheetController.getInstance();
            controller.setEditorManager(editorManager);
            controller.setXHTMLResources(resources);
            stylesheetWindow.show();
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Stylesheet hinzufügen");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("Kann Stylesheet nicht hinzufügen, da Datei kein gültiges XHTML ist.");
            alert.showAndWait();
        }
    }

    private void openWithApplication(TreeItem<Resource> treeItem, String applicationExecutable)
    {
        Resource resource = treeItem.getValue();
        try
        {
            File tmp = new File(Files.createTempDir(), resource.getFileName());
            FileOutputStream output = new FileOutputStream(tmp);
            output.write(resource.getData());
            output.flush();
            output.close();

            Runtime.getRuntime().exec(applicationExecutable + " " + tmp);
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path path = tmp.toPath().getParent();
            path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            Task task = new Task<Void>()
            {
                @Override
                public Void call()
                {
                    for (;;)
                    {
                        // wait for key to be signaled
                        WatchKey key;
                        try
                        {
                            key = watcher.take();
                        }
                        catch (InterruptedException x)
                        {
                            return null;
                        }

                        for (WatchEvent<?> event : key.pollEvents())
                        {
                            WatchEvent.Kind<?> kind = event.kind();

                            // This key is registered only
                            // for ENTRY_CREATE events,
                            // but an OVERFLOW event can
                            // occur regardless if events
                            // are lost or discarded.
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
                            {
                                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                                Path filename = ev.context();
                                logger.info("getting modify event for file " + filename);
                                Platform.runLater(() -> {
                                    try
                                    {
                                        byte[] data = IOUtils.toByteArray(new FileInputStream(tmp));
                                        if (data.length == 0)
                                        {
                                            logger.info("file " + filename + " looks like its writing from external application, ignore this event");
                                            return;
                                        }
                                        resource.setData(data);
                                        if (resource instanceof XHTMLResource || resource instanceof CSSResource || resource instanceof JavascriptResource
                                                ||  resource instanceof XMLResource)
                                        {
                                            editorManager.refreshEditorCode(resource);
                                        }
                                        else
                                        {
                                            editorManager.refreshImageViewer(resource);
                                        }
                                        book.setBookIsChanged(true);
                                    }
                                    catch (IOException e)
                                    {
                                        logger.error("error while reading content written by external program", e);
                                    }
                                });

                            }
                        }

                        // Reset the key -- this step is critical if you want to
                        // receive further watch events.  If the key is no longer valid,
                        // the directory is inaccessible so exit the loop.
                        boolean valid = key.reset();
                        if (!valid)
                        {
                            logger.info("end watching for modified file ");
                            break;
                        }
                    }
                    try
                    {
                        watcher.close();
                    }
                    catch (IOException e)
                    {
                        logger.error("error while closing watcher service", e);
                    }
                    return null;
                }
            };
            new Thread(task).start();
        }
        catch (IOException e)
        {
            logger.error("error while opening file in external program", e);
        }
    }

    private void joinXHTMLItemWitPreviousItem(TreeItem<Resource> treeItem)
    {
    }

    private void renameItem(TreeItem<Resource> treeItem)
    {
        ((EditingTreeCell)treeItem.getGraphic().getParent()).startEdit();
    }

    private void addSemanticsToXHTMLFile(TreeItem<Resource> treeItem, GuideReference.Semantics semantic)
    {
        //hat der schon eine andere Semantik, die dann entfernen
        Resource resource = treeItem.getValue();
        Guide guide = book.getGuide();
        List<GuideReference> typeReferences = guide.getReferences();
        GuideReference toRemove = null;
        for (GuideReference typeReference : typeReferences)
        {
            if (typeReference.getResource().equals(resource))
            {
                toRemove = typeReference;
                break;
            }
        }
        if (toRemove != null)
        {
            guide.getReferences().remove(toRemove);
        }

        //jetzt die neue Semantik ergänzen
        GuideReference reference = new GuideReference(resource, semantic, semantic.getName());
        guide.addReference(reference);
        refreshOpf();
        book.setBookIsChanged(true);
    }

    private void removeSemanticsFromXHTMLFile(TreeItem<Resource> treeItem, GuideReference.Semantics semantic)
    {
        Resource resource = treeItem.getValue();
        Guide guide = book.getGuide();
        List<GuideReference> typeReferences = guide.getGuideReferencesByType(semantic);
        GuideReference toRemove = null;
        for (GuideReference typeReference : typeReferences)
        {
            if (typeReference.getResource().equals(resource))
            {
                toRemove = typeReference;
                break;
            }
        }
        if (toRemove != null)
        {
            guide.getReferences().remove(toRemove);
        }
        refreshOpf();
        book.setBookIsChanged(true);
    }

    private void selectAll(TreeItem<Resource> parentItem)
    {
        List<TreeItem<Resource>> treeItems = parentItem.getChildren();
        int[] indices = new int[treeItems.size()];
        int i = 0;
        for (TreeItem<Resource> item : treeItems)
        {
            indices[i] = treeView.getRow(item);
            i++;
        }
        treeView.getSelectionModel().selectIndices(indices[0], indices);
    }

    public void addEmptyCssFile()
    {
        String fileName = book.getNextStandardFileName(MediaType.CSS);
        Resource res = book.addResourceFromTemplate("/epub/template.css", "Styles/" + fileName);
        try
        {
            String content = new String(res.getData(), "UTF-8");
            content = content.replace("${Title}", book.getTitle());
            res.setData(content.getBytes("UTF-8"));
        }
        catch (IOException e)
        {
            //never happens
        }

        TreeItem<Resource> emptyItem = new TreeItem<>(res);
        emptyItem.setGraphic(FXUtils.getIcon("/icons/document_gear.png", 24));
        cssItem.getChildren().add(emptyItem);

        book.addResource(res);
        book.setBookIsChanged(true);
        editorManager.refreshEditorCode(book.getOpfResource());
    }

    private void validateCss(TreeItem<Resource> treeItem)
    {


    }

    private void configureApplicationForOpenXHTML(TreeItem<Resource> treeItem)
    {


    }

    private void configureApplicationForOpenCSS(TreeItem<Resource> treeItem)
    {


    }

    private void configureApplicationForOpenImage(TreeItem<Resource> treeItem)
    {


    }

}
