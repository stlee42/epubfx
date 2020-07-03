package de.machmireinebook.epubeditor.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Files;

import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.editor.EditorTabManager;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Guide;
import de.machmireinebook.epubeditor.epublib.domain.epub2.GuideReference;
import de.machmireinebook.epubeditor.epublib.resource.CSSResource;
import de.machmireinebook.epubeditor.epublib.resource.FontResource;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.resource.JavascriptResource;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.resource.XMLResource;
import de.machmireinebook.epubeditor.epublib.util.ResourceFilenameComparator;
import de.machmireinebook.epubeditor.gui.AddStylesheetController;
import de.machmireinebook.epubeditor.gui.MainController;
import de.machmireinebook.epubeditor.gui.StandardControllerFactory;
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

    private static final String CSS_FILE_ICON = "/icons/icons8_CSS_Filetype_96px.png";
    private static final String IMAGE_FILE_ICON = "/icons/icons8_Image_File_96px.png";
    private static final String XHTML_FILE_ICON = "/icons/icons8_Code_File_96px.png";
    private static final String OTHER_FILE_ICON = "/icons/icons8_File_48px.png";
    private static final int ICON_SIZE = 24;

    private TreeItem<Resource<?>> textItem;
    private TreeItem<Resource<?>> cssItem;
    private TreeItem<Resource<?>> imagesItem;
    private TreeItem<Resource<?>> fontsItem;
    private TreeItem<Resource<?>> miscContentItem;
    private TreeItem<Resource<?>> rootItem;

    private TreeView<Resource<?>> treeView;
    private EditorTabManager editorManager;
    private TreeItem<Resource<?>> ncxItem;
    private TreeItem<Resource<?>> opfItem;

    private final ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>(this, "currentBook");
    private StandardControllerFactory standardControllerFactory;
    private boolean isEditingFileName = false;

    @Inject
    private Provider<MainController> mainControllerProvider;
    @Inject
    private EpubEditorConfiguration configuration;

    @PostConstruct
    public void init() {
        currentBookProperty.addListener((observableValue, book, newBook) -> {
            if (newBook != null) {
                refreshBookBrowser();
                if (newBook.getSpine() != null) {
                    selectTextItem(newBook.getSpine().getResource(0));
                }
            }
        });
        standardControllerFactory = StandardControllerFactory.builder()
                .currentBookProperty(currentBookProperty)
                .stage(configuration.getMainWindow())
                .build();
    }

    private static class FolderSymbolListener implements ChangeListener<Boolean>
    {
        TreeItem<Resource<?>> item;

        private FolderSymbolListener(TreeItem<Resource<?>> item)
        {
            this.item = item;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
        {
            if (newValue)
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_Open_48px.png", BookBrowserManager.ICON_SIZE));
            }
            else
            {
                item.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", BookBrowserManager.ICON_SIZE));
            }
        }
    }

    public class BookBrowserTreeCellFactory implements Callback<TreeView<Resource<?>>, TreeCell<Resource<?>>>
    {
        @SuppressWarnings("unchecked")
        @Override
        public TreeCell<Resource<?>> call(TreeView<Resource<?>> resourceTreeView)
        {
            EditingTreeCell<Resource<?>> treeCell = new EditingTreeCell<>(true);

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
                TreeCell<Resource<?>> cell = (EditingTreeCell<Resource<?>>) mouseEvent.getSource();
                logger.info("dnd detected on item " + cell);
                Resource<?> res = cell.getItem();
                if (MediaType.XHTML.equals(res.getMediaType()))
                {
                    Dragboard dragBoard = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.put(DataFormat.URL, res.getHref());
                    dragBoard.setContent(content);
                    mouseEvent.consume();
                }
            });

            treeCell.setOnDragDropped(dragEvent -> {
                logger.info("dnd dropped of item " + dragEvent.getSource());
                Object object = dragEvent.getDragboard().getContent(DataFormat.URL);
                if (object != null)
                {
                    TreeCell<Resource<?>> draggedCell = (TreeCell<Resource<?>>) dragEvent.getGestureSource();
                    logger.info("dragged cell is " + draggedCell.getItem());
                    logger.info("cell dropped on is " + treeCell.getItem());

                    TreeItem<Resource<?>> draggedItem = draggedCell.getTreeItem();
                    textItem.getChildren().remove(draggedItem);
                    int index = textItem.getChildren().indexOf(treeCell.getTreeItem());
                    textItem.getChildren().add(index, draggedItem);
                    treeView.getSelectionModel().select(draggedItem);
                    //noch im spine moven
                    Book book = currentBookProperty().getValue();
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
                Resource<?> res = treeCell.getItem();
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

    public void setTreeView(TreeView<Resource<?>> initTreeView)
    {
        this.treeView = initTreeView;
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);

        treeView.setCellFactory(new BookBrowserTreeCellFactory());

        treeView.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY))
            {
                if (event.getClickCount() == 2)
                {
                    TreeItem<Resource<?>> item = treeView.getSelectionModel().getSelectedItem();
                    if (isImageItem(item)) {
                        editorManager.openImageFile((ImageResource) item.getValue());
                    } else if (!isFontItem(item)) {
                        editorManager.openFileInEditor(item.getValue());
                    }
                    event.consume();
                }
            }
            else if (event.getButton().equals(MouseButton.SECONDARY))
            {
                TreeItem<Resource<?>> item = treeView.getSelectionModel().getSelectedItem();
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
                else if (isFontItem(item))
                {
                    createFontItemContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
                else if (isMisContentItem(item))
                {
                    createMiscContentItemContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
                else if (item.equals(textItem))
                {
                    createXHTMLRootContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
                else if (item.equals(cssItem))
                {
                    createCssRootContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
                else if (item.equals(imagesItem))
                {
                    createImagesRootContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
                else if (item.equals(fontsItem))
                {
                    createFontsRootContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
                else if (item.equals(miscContentItem)) {
                    createMiscContentRootContextMenu(item).show(item.getGraphic(), event.getScreenX(), event.getScreenY());
                }
            }
        });
        
        treeView.setOnEditCommit(event -> {
            logger.info("editing end for new value " + event.getNewValue());
            Resource<?> resource = event.getNewValue();
            if (resource instanceof CSSResource || resource instanceof JavascriptResource
                    || resource instanceof XMLResource) {
                editorManager.refreshEditorCode(resource);
            }
            else if (resource instanceof ImageResource) {
                editorManager.refreshImageViewer(resource);
            }
            editorManager.totalRefreshPreview();
            Book book = currentBookProperty().getValue();
            book.setBookIsChanged(true);
            event.consume();
            isEditingFileName = false;
        });

        treeView.setOnEditCancel(event -> {
            logger.info("editing cancelled");
            isEditingFileName = false;
        });

        treeView.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            logger.info("key typed in tree view editor: " + keyCode);

            if (keyCode.equals(KeyCode.DELETE)) {
                logger.debug("Delete pressed");
                deleteSelectedItems();
            } else if (keyCode.equals(KeyCode.C) && event.isShortcutDown()) {
                logger.debug("Ctrl-C pressed");
                copyFileNameToClipboard();
            } else if (keyCode.equals(KeyCode.ENTER)) {
                if (!isEditingFileName) {
                    logger.debug("Enter pressed");
                    TreeItem<Resource<?>> item = treeView.getSelectionModel().getSelectedItem();
                    if (isImageItem(item)) {
                        editorManager.openImageFile((ImageResource) item.getValue());
                    } else if (!isFontItem(item)) {
                        editorManager.openFileInEditor(item.getValue());
                    }
                    event.consume();
                }
            }
        });

        Resource<?> textResource = new Resource<>("Text");
        textItem = new TreeItem<>(textResource);
        textItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", ICON_SIZE));
        textItem.expandedProperty().addListener(new FolderSymbolListener(textItem));
        rootItem.getChildren().add(textItem);

        Resource<?> cssResource = new Resource<>("Styles");
        cssItem = new TreeItem<>(cssResource);
        cssItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", ICON_SIZE));
        cssItem.expandedProperty().addListener(new FolderSymbolListener(cssItem));
        rootItem.getChildren().add(cssItem);

        Resource<?> imagesResource = new Resource<>("Images");
        imagesItem = new TreeItem<>(imagesResource);
        imagesItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", ICON_SIZE));
        imagesItem.expandedProperty().addListener(new FolderSymbolListener(imagesItem));
        rootItem.getChildren().add(imagesItem);

        Resource<?> fontsResource = new Resource<>("Fonts");
        fontsItem = new TreeItem<>(fontsResource);
        fontsItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", ICON_SIZE));
        fontsItem.expandedProperty().addListener(new FolderSymbolListener(fontsItem));
        rootItem.getChildren().add(fontsItem);

        Resource<?> miscResource = new Resource<>("Misc");
        miscContentItem = new TreeItem<>(miscResource);
        miscContentItem.setGraphic(FXUtils.getIcon("/icons/icons8_Folder_48px.png", ICON_SIZE));
        miscContentItem.expandedProperty().addListener(new FolderSymbolListener(miscContentItem));
        rootItem.getChildren().add(miscContentItem);
    }

    private ContextMenu createImagesRootContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        return menu;
    }

    private ContextMenu createFontsRootContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        return menu;
    }

    private ContextMenu createMiscContentRootContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        return menu;
    }

    private ContextMenu createCssRootContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Add empty CSS File");
        item.setUserData(treeItem);
        item.setOnAction(event -> addEmptyCssFile());
        menu.getItems().add(item);

        item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Select all");
        item.setUserData(treeItem);
        item.setOnAction(event -> selectAll(cssItem));
        menu.getItems().add(item);

        return menu;
    }

    private ContextMenu createXHTMLRootContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        item = new MenuItem("Add empty file");
        item.setUserData(treeItem);
        item.setOnAction(event -> addEmptyXHTMLFile(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Select all");
        item.setUserData(treeItem);
        item.setOnAction(event -> selectAll(treeItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        menu.getItems().add(item);

        return menu;
    }

    private ContextMenu createXHTMLItemContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Delete");
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
        item.setOnAction(event -> joinXHTMLItemWithPreviousItem(treeItem));
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

    private void addSemanticsMenuItems(Menu menu, TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        GuideReference.Semantics[] semantics = GuideReference.Semantics.values();
        Resource<?> resource = treeItem.getValue();
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
                }
                else
                {
                    removeSemanticsFromXHTMLFile(treeItem, semantic);
                }
                book.setBookIsChanged(true);
            });
            menu.getItems().add(item);
        }

    }

    private void addXhtmlOpenWithApplicationItems(Menu menu, TreeItem<Resource<?>> treeItem)
    {
        List<EpubEditorConfiguration.OpenWithApplication> applications = configuration.getXhtmlOpenWithApplications();
        for (EpubEditorConfiguration.OpenWithApplication application : applications)
        {
            MenuItem item = new MenuItem(application.getDisplayName());
            item.setUserData(treeItem);
            item.setOnAction(event -> openWithApplication(treeItem, application.getFileName()));
            menu.getItems().add(item);
        }
        MenuItem item = new MenuItem("Configure Application...");
        item.setUserData(treeItem);
        item.setOnAction(event -> configureApplicationForOpenXHTML(treeItem));
        menu.getItems().add(item);
    }

    private ContextMenu createCssItemContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Delete...");
        item.setUserData(treeItem);
        item.setOnAction(event -> deleteSelectedItems());
        item.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        menu.getItems().add(item);

        item = new MenuItem("Rename...");
        item.setUserData(treeItem);
        item.setOnAction(event -> renameItem(treeItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        menu.getItems().add(item);

        item = new MenuItem("Validate");
        item.setUserData(treeItem);
        item.setOnAction(event -> validateCss(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        Menu openWithItem = new Menu("Open with");
        menu.getItems().add(openWithItem);
        addCssOpenWithApplicationItems(openWithItem, treeItem);

        item = new MenuItem("Save as...");
        item.setUserData(treeItem);
        item.setOnAction(event -> saveAs(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Add empty CSS File");
        item.setUserData(treeItem);
        item.setOnAction(event -> addEmptyCssFile());
        menu.getItems().add(item);

        item = new MenuItem("Add copy");
        item.setUserData(treeItem);
        item.setOnAction(event -> addCopy(treeItem));
        menu.getItems().add(item);

        item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Select all");
        item.setUserData(treeItem);
        item.setOnAction(event -> selectAll(cssItem));
        menu.getItems().add(item);

        return menu;
    }

    private void addCssOpenWithApplicationItems(Menu menu, TreeItem<Resource<?>> treeItem)
    {
        List<EpubEditorConfiguration.OpenWithApplication> applications = configuration.getCssOpenWithApplications();
        for (EpubEditorConfiguration.OpenWithApplication application : applications)
        {
            MenuItem item = new MenuItem(application.getDisplayName());
            item.setUserData(treeItem);
            item.setOnAction(event -> openWithApplication(treeItem, application.getFileName()));
            menu.getItems().add(item);
        }
        MenuItem item = new MenuItem("Configure Application...");
        item.setUserData(treeItem);
        item.setOnAction(event -> configureApplicationForOpenCSS(treeItem));
        menu.getItems().add(item);
    }

    private ContextMenu createImageItemContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Delete...");
        item.setUserData(treeItem);
        item.setOnAction(event -> deleteSelectedItems());
        item.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        menu.getItems().add(item);

        item = new MenuItem("Rename...");
        item.setUserData(treeItem);
        item.setOnAction(event -> renameItem(treeItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        menu.getItems().add(item);

        Menu semantikItem = new Menu("Add Semantic...");
        menu.getItems().add(semantikItem);
        addImageSemanticMenuItems(semantikItem, treeItem);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        Menu openWithItem = new Menu("Open with");
        menu.getItems().add(openWithItem);
        addImageOpenWithApplicationItems(openWithItem, treeItem);

        item = new MenuItem("Save as...");
        item.setUserData(treeItem);
        item.setOnAction(event -> saveAs(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Add copy");
        item.setUserData(treeItem);
        item.setOnAction(event -> addCopy(treeItem));
        menu.getItems().add(item);

        item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        return menu;
    }

    private ContextMenu createFontItemContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Delete...");
        item.setUserData(treeItem);
        item.setOnAction(event -> deleteSelectedItems());
        item.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        menu.getItems().add(item);

        item = new MenuItem("Rename...");
        item.setUserData(treeItem);
        item.setOnAction(event -> renameItem(treeItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        menu.getItems().add(item);


        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Save as...");
        item.setUserData(treeItem);
        item.setOnAction(event -> saveAs(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        return menu;
    }

    private ContextMenu createMiscContentItemContextMenu(TreeItem<Resource<?>> treeItem)
    {
        ContextMenu menu = new ContextMenu();
        menu.setAutoFix(true);
        menu.setAutoHide(true);

        MenuItem item = new MenuItem("Delete...");
        item.setUserData(treeItem);
        item.setOnAction(event -> deleteSelectedItems());
        item.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        menu.getItems().add(item);

        item = new MenuItem("Rename...");
        item.setUserData(treeItem);
        item.setOnAction(event -> renameItem(treeItem));
        item.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        menu.getItems().add(item);


        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Save as...");
        item.setUserData(treeItem);
        item.setOnAction(event -> saveAs(treeItem));
        menu.getItems().add(item);

        item = new SeparatorMenuItem();
        menu.getItems().add(item);

        item = new MenuItem("Add existing files...");
        item.setUserData(treeItem);
        item.setOnAction(event -> mainControllerProvider.get().addExistingFiles(treeItem));
        menu.getItems().add(item);

        return menu;
    }

    private void addImageSemanticMenuItems(Menu menu, TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        ImageResource resource = (ImageResource)treeItem.getValue();
        CheckMenuItem item = new CheckMenuItem("Cover Image");
        item.setSelected(resource.coverProperty().getValue());
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

    private void addImageOpenWithApplicationItems(Menu menu, TreeItem<Resource<?>> treeItem)
    {
        List<EpubEditorConfiguration.OpenWithApplication> applications = configuration.getImageOpenWithApplications();
        for (EpubEditorConfiguration.OpenWithApplication application : applications)
        {
            MenuItem item = new MenuItem(application.getDisplayName());
            item.setUserData(treeItem);
            item.setOnAction(event -> openWithApplication(treeItem, application.getFileName()));
            menu.getItems().add(item);
        }
        MenuItem item = new MenuItem("Configure Application ...");
        item.setUserData(treeItem);
        item.setOnAction(event -> configureApplicationForOpenImage(treeItem));
        menu.getItems().add(item);
    }

    public void refreshBookBrowser()
    {
        Book book = currentBookProperty().getValue();

        textItem.getChildren().clear();
        cssItem.getChildren().clear();
        imagesItem.getChildren().clear();
        fontsItem.getChildren().clear();
        miscContentItem.getChildren().clear();

        rootItem.getChildren().remove(opfItem);
        rootItem.getChildren().remove(ncxItem);

        List<SpineReference> xhtmlResources = book.getSpine().getSpineReferences();
        for (SpineReference xhtmlResource : xhtmlResources)
        {
            TreeItem<Resource<?>> xhtmlItem = new TreeItem<>(xhtmlResource.getResource());
            xhtmlItem.setGraphic(FXUtils.getIcon(XHTML_FILE_ICON, ICON_SIZE));
            textItem.getChildren().add(xhtmlItem);
        }

        List<Resource<?>> cssResources = book.getResources().getCssResources();
        cssResources.sort(new ResourceFilenameComparator());
        for (Resource<?> cssResource : cssResources)
        {
            TreeItem<Resource<?>> item = new TreeItem<>(cssResource);
            item.setGraphic(FXUtils.getIcon(CSS_FILE_ICON, ICON_SIZE));
            cssItem.getChildren().add(item);
        }

        List<Resource<?>> fontResources = book.getResources().getFontResources();
        fontResources.sort(new ResourceFilenameComparator());
        for (Resource<?> fontResource : fontResources)
        {
            TreeItem<Resource<?>> item = new TreeItem<>(fontResource);
            item.setGraphic(getFontIcon((FontResource) fontResource));
            fontsItem.getChildren().add(item);
        }

        List<Resource<?>> miscResources = book.getResources().getMiscResources();
        miscResources.sort(new ResourceFilenameComparator());
        for (Resource<?> miscResource : miscResources) {
            TreeItem<Resource<?>> item = new TreeItem<>(miscResource);
            item.setGraphic(FXUtils.getIcon(OTHER_FILE_ICON, ICON_SIZE));
            miscContentItem.getChildren().add(item);
        }

        List<Resource<?>> imageResources = book.getResources().getImageResources();
        imageResources.sort(new ResourceFilenameComparator());
        for (Resource<?> imageResource : imageResources)
        {
            TreeItem<Resource<?>> item = new TreeItem<>(imageResource);
            if (imageResource.getMediaType().equals(MediaType.GIF))
            {
                item.setGraphic(FXUtils.getIcon(IMAGE_FILE_ICON, ICON_SIZE));
            }
            else if (imageResource.getMediaType().equals(MediaType.PNG))
            {
                item.setGraphic(FXUtils.getIcon(IMAGE_FILE_ICON, ICON_SIZE));
            }
            else if (imageResource.getMediaType().equals(MediaType.SVG))
            {
                item.setGraphic(FXUtils.getIcon(IMAGE_FILE_ICON, ICON_SIZE));
            }
            else if (imageResource.getMediaType().equals(MediaType.JPG))
            {
                item.setGraphic(FXUtils.getIcon(IMAGE_FILE_ICON, ICON_SIZE));
            }
            imagesItem.getChildren().add(item);
        }

        opfItem = new TreeItem<>();
        opfItem.valueProperty().bind(book.opfResourceProperty());
        opfItem.setGraphic(FXUtils.getIcon(XHTML_FILE_ICON, ICON_SIZE));
        rootItem.getChildren().add(opfItem);

        Resource<?> ncxResource = book.getNcxResource();
        if (ncxResource != null) //in case of epub 3 it could be null
        {
            ncxItem = new TreeItem<>(ncxResource);
            ncxItem.setGraphic(FXUtils.getIcon(XHTML_FILE_ICON, ICON_SIZE));
            rootItem.getChildren().add(ncxItem);
        }

        textItem.setExpanded(true);
        treeView.getSelectionModel().clearSelection();
        if (textItem.getChildren().size() > 0)
        {
            treeView.getSelectionModel().select(textItem.getChildren().get(0));
        }
        else
        {
            treeView.getSelectionModel().select(textItem);
        }

        //TODO adding misc resources
    }

    private void addTreeItem(Resource<?> resource) {
        TreeItem<Resource<?>> selectedTreeItem = treeView.getSelectionModel().getSelectedItem();
        int index;
        TreeItem<Resource<?>> treeItem = new TreeItem<>(resource);
        if (resource.getMediaType() == MediaType.XHTML) {
            index = textItem.getChildren().indexOf(selectedTreeItem);
            treeItem.setGraphic(FXUtils.getIcon(XHTML_FILE_ICON, ICON_SIZE));
            if (textItem.getChildren().size() > 0) {
                textItem.getChildren().add(index + 1, treeItem);
            } else {
                textItem.getChildren().add(treeItem);
            }
        }
        else if (resource.getMediaType() == MediaType.CSS) {
            index = cssItem.getChildren().indexOf(selectedTreeItem);
            treeItem.setGraphic(FXUtils.getIcon(CSS_FILE_ICON, ICON_SIZE));
            if (cssItem.getChildren().size() > 0) {
                cssItem.getChildren().add(index + 1, treeItem);
            } else {
                cssItem.getChildren().add(treeItem);
            }
        }
        else if (resource.getMediaType().isImage()) {
            index = imagesItem.getChildren().indexOf(selectedTreeItem);
            treeItem.setGraphic(FXUtils.getIcon(IMAGE_FILE_ICON, ICON_SIZE));
            if (imagesItem.getChildren().size() > 0) {
                imagesItem.getChildren().add(index + 1, treeItem);
            } else {
                imagesItem.getChildren().add(treeItem);
            }
        }
        else if (resource.getMediaType().isFont()) {
            index = fontsItem.getChildren().indexOf(selectedTreeItem);
            treeItem.setGraphic(getFontIcon((FontResource) resource));
            if (fontsItem.getChildren().size() > 0) {
                fontsItem.getChildren().add(index + 1, treeItem);
            } else {
                fontsItem.getChildren().add(treeItem);
            }
        } else {
            index = miscContentItem.getChildren().indexOf(selectedTreeItem);
            treeItem.setGraphic(FXUtils.getIcon(OTHER_FILE_ICON, ICON_SIZE));
            if (miscContentItem.getChildren().size() > 0) {
                miscContentItem.getChildren().add(index + 1, treeItem);
            } else {
                miscContentItem.getChildren().add(treeItem);
            }
        }
        int selectionIndex = treeView.getSelectionModel().getSelectedIndex();
        if (selectionIndex > -1) {
            treeView.getSelectionModel().clearAndSelect(selectionIndex + 1);
        } else {
            treeView.getSelectionModel().select(treeItem);
        }
    }

    private ImageView getFontIcon(FontResource fontResource) {
        if (fontResource.getMediaType().isTTFFont())
        {
            return FXUtils.getIcon("/icons/icons8_TTF_48px.png", ICON_SIZE);
        }
        else if (fontResource.getMediaType().isOpenTypeFont())
        {
            return FXUtils.getIcon("/icons/icons8_OTF_48px.png", ICON_SIZE);
        }
        else if (fontResource.getMediaType().isWoffFont())
        {
            return FXUtils.getIcon("/icons/icons8_WOFF_48px.png", ICON_SIZE);
        }
        else //default file icon
        {
            return FXUtils.getIcon(OTHER_FILE_ICON, ICON_SIZE);
        }
    }

    public void selectTextItem(Resource<?> resource)
    {
        if (resource == null)
        {
            return;
        }
        textItem.setExpanded(true);
        List<TreeItem<Resource<?>>> textItems = textItem.getChildren();
        for (TreeItem<Resource<?>> item : textItems)
        {
            if (item.getValue().equals(resource))
            {
                treeView.getSelectionModel().select(item);
                break;
            }
        }
    }

    public void selectTreeItem(Resource<?> resource)
    {
        if (resource == null) {
            return;
        }
        boolean found = findAndSelectChildItem(cssItem, resource);
        if (!found) {
            found = findAndSelectChildItem(textItem, resource);
        }
        if (!found) {
            found = findAndSelectChildItem(cssItem, resource);
        }
        if (!found) {
            found = findAndSelectChildItem(fontsItem, resource);
        }
        if (!found) {
            findAndSelectChildItem(miscContentItem, resource);
        }
    }

    private boolean findAndSelectChildItem(TreeItem<Resource<?>> parent, Resource<?> resource) {
        List<TreeItem<Resource<?>>> childrenItems = parent.getChildren();
        for (TreeItem<Resource<?>> item : childrenItems) {
            if (item.getValue().equals(resource)) {
                parent.setExpanded(true);
                treeView.getSelectionModel().clearSelection();
                treeView.getSelectionModel().select(item);
                return true;
            }
        }
        return false;
    }

    public void setEditorManager(EditorTabManager editorManager)
    {
        this.editorManager = editorManager;
    }

    public boolean isTextItem(TreeItem<Resource<?>> item)
    {
        return item.getParent().equals(textItem);
    }

    public boolean isCssItem(TreeItem<Resource<?>> item)
    {
        return item.getParent().equals(cssItem);
    }

    private boolean isImageItem(TreeItem<Resource<?>> item)
    {
        return item.getParent().equals(imagesItem);
    }

    private boolean isFontItem(TreeItem<Resource<?>> item)
    {
        return item.getParent().equals(fontsItem);
    }

    private boolean isMisContentItem(TreeItem<Resource<?>> item)
    {
        return item.getParent().equals(miscContentItem);
    }


    public boolean isXmlItem(TreeItem<Resource<?>> item)
    {
        return item.getParent().equals(rootItem)
                && !item.equals(textItem) && !item.equals(cssItem)
                && !item.equals(fontsItem) && !item.equals(imagesItem)
                && !item.equals(miscContentItem);
    }

    private void deleteSelectedItems()
    {
        List<TreeItem<Resource<?>>> selectedItems = treeView.getSelectionModel().getSelectedItems();
        List<TreeItem<Resource<?>>> toDelete = new ArrayList<>(selectedItems);
        for (TreeItem<Resource<?>> selectedItem : toDelete)
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
            else if (selectedItem.getParent() == miscContentItem)
            {
                deleteMiscContentItem(selectedItem);
            }
        }
    }

    private void deleteXHTMLItem(TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        book.removeSpineResource(treeItem.getValue());
        editorManager.refreshEditorCode(book.getOpfResource());
        editorManager.closeTab(treeItem.getValue());
        textItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    private void deleteCssItem(TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        book.removeResource(treeItem.getValue());
        editorManager.refreshEditorCode(book.getOpfResource());
        editorManager.closeTab(treeItem.getValue());
        cssItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    private void deleteImageItem(TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        book.removeResource(treeItem.getValue());
        editorManager.refreshEditorCode(book.getOpfResource());
        editorManager.closeTab(treeItem.getValue());
        imagesItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    private void deleteFontItem(TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        book.removeResource(treeItem.getValue());
        fontsItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    private void deleteMiscContentItem(TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        book.removeResource(treeItem.getValue());
        editorManager.refreshEditorCode(book.getOpfResource());
        editorManager.closeTab(treeItem.getValue());
        miscContentItem.getChildren().remove(treeItem);
        book.setBookIsChanged(true);
    }

    private void copyFileNameToClipboard() {
        TreeItem<Resource<?>> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem != null && textItem != treeItem && cssItem != treeItem && imagesItem != treeItem
            && fontsItem != treeItem && miscContentItem != treeItem) {
            Clipboard.getSystemClipboard().setContent(Collections.singletonMap(DataFormat.PLAIN_TEXT, treeItem.getValue().getFileName()));
        }
    }

    public void refreshOpf()
    {
        Book book = currentBookProperty().getValue();
        book.refreshOpfResource();
        editorManager.refreshEditorCode(book.getOpfResource());
    }

    public void refreshNcx()
    {
        Book book = currentBookProperty().getValue();
        book.refreshNcxResource();
        editorManager.refreshEditorCode(book.getNcxResource());
    }

    private void sortSelectedItems()
    {


    }

    public void addEmptyXHTMLFile()
    {
        TreeItem<Resource<?>> treeItem = null;
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

    public void addEmptyXHTMLFile(TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        String fileName = book.getNextStandardFileName(MediaType.XHTML);
        Resource<?> res;
        if (book.isEpub3() && book.isFixedLayout())
        {
            res = book.addResourceFromTemplate("/epub/template_fixed_layout.xhtml", "Text/" + fileName);
        }
        else
        {
            if (book.isEpub3()) {
                res = book.addResourceFromTemplate("/epub/template-epub3.html", "Text/" + fileName);
            } else {
                res = book.addResourceFromTemplate("/epub/template.xhtml", "Text/" + fileName);
            }
        }

        addTreeItem(res);

        int index = 0;
        if (treeItem != null)
        {
            index = textItem.getChildren().indexOf(treeItem);
        }
        book.addSpineResource(res, index + 1);
        book.setBookIsChanged(true);
        editorManager.refreshEditorCode(book.getOpfResource());

        editorManager.openFileInEditor(res, MediaType.XHTML);
    }

    private void addCopy(TreeItem<Resource<?>> treeItem)
    {
        Book book = currentBookProperty().getValue();
        Resource<?> oldResource = treeItem.getValue();
        Resource<?> newResource = book.addCopyOfResource(oldResource);
        book.setBookIsChanged(true);

        addTreeItem(newResource);

        editorManager.refreshEditorCode(book.getOpfResource());
        editorManager.openFileInEditor(newResource);
    }


    private void saveAs(TreeItem<Resource<?>> treeItem)
    {


    }

    private void addStylesheet()
    {
        ObservableList<TreeItem<Resource<?>>> selectedItems = treeView.getSelectionModel().getSelectedItems();
        List<Resource<?>> resources = new ArrayList<>();
        boolean allAreValid = true;
        for (TreeItem<Resource<?>> selectedItem : selectedItems)
        {
            if (((XHTMLResource)selectedItem.getValue()).isValidXML())
            {
                resources.add(selectedItem.getValue());
                Book book = currentBookProperty().getValue();
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
            Stage stylesheetWindow = standardControllerFactory.createStandardController("/add_stylesheet.fxml", AddStylesheetController.class);
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

    private void openWithApplication(TreeItem<Resource<?>> treeItem, String applicationExecutable)
    {
        Resource<?> resource = treeItem.getValue();
        File tmp = new File(Files.createTempDir(), resource.getFileName());

        try (FileOutputStream output = new FileOutputStream(tmp))
        {
            output.write(resource.getData());
            output.flush();

            Runtime.getRuntime().exec(applicationExecutable + " " + tmp);
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path path = tmp.toPath().getParent();
            path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            Task<Void> task = new Task<Void>()
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
                            Thread.currentThread().interrupt();
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
                                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                                Path filename = ev.context();
                                logger.info("getting modify event for file " + filename);
                                Platform.runLater(() -> {
                                    try (InputStream is = new FileInputStream(tmp))
                                    {
                                        byte[] data = IOUtils.toByteArray(is);
                                        if (data.length == 0)
                                        {
                                            logger.info("file " + filename + " looks like its writing from external application, ignore this event");
                                            return;
                                        }
                                        resource.setData(data);
                                        if (resource instanceof CSSResource || resource instanceof JavascriptResource
                                                ||  resource instanceof XMLResource)
                                        {
                                            editorManager.refreshEditorCode(resource);
                                        }
                                        else
                                        {
                                            editorManager.refreshImageViewer(resource);
                                        }
                                        Book book = currentBookProperty().getValue();
                                        book.setBookIsChanged(true);
                                    } catch (IOException e) {
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

    private void joinXHTMLItemWithPreviousItem(TreeItem<Resource<?>> treeItem)
    {
    }

    private void renameItem(TreeItem<Resource<?>> treeItem)
    {
        isEditingFileName = true;
        ((EditingTreeCell<?>)treeItem.getGraphic().getParent()).startEdit();
    }

    private void addSemanticsToXHTMLFile(TreeItem<Resource<?>> treeItem, GuideReference.Semantics semantic)
    {
        Book book = currentBookProperty().getValue();
        //hat der schon eine andere Semantik, die dann entfernen
        Resource<?> resource = treeItem.getValue();
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

    private void removeSemanticsFromXHTMLFile(TreeItem<Resource<?>> treeItem, GuideReference.Semantics semantic)
    {
        Book book = currentBookProperty().getValue();
        Resource<?> resource = treeItem.getValue();
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

    private void selectAll(TreeItem<Resource<?>> parentItem)
    {
        List<TreeItem<Resource<?>>> treeItems = parentItem.getChildren();
        int[] indices = new int[treeItems.size()];
        int i = 0;
        for (TreeItem<Resource<?>> item : treeItems)
        {
            indices[i] = treeView.getRow(item);
            i++;
        }
        treeView.getSelectionModel().selectIndices(indices[0], indices);
    }

    public void addEmptyCssFile()
    {
        Book book = currentBookProperty().getValue();
        String fileName = book.getNextStandardFileName(MediaType.CSS);
        Resource<?> res = book.addResourceFromTemplate("/epub/template.css", "Styles/" + fileName);
        String content = new String(res.getData(), StandardCharsets.UTF_8);
        content = content.replace("${Title}", book.getTitle());
        res.setData(content.getBytes(StandardCharsets.UTF_8));

        addTreeItem(res);

        book.addResource(res);
        book.setBookIsChanged(true);
        editorManager.refreshEditorCode(book.getOpfResource());
    }

    private void validateCss(TreeItem<Resource<?>> treeItem)
    {


    }

    private void configureApplicationForOpenXHTML(TreeItem<Resource<?>> treeItem)
    {


    }

    private void configureApplicationForOpenCSS(TreeItem<Resource<?>> treeItem)
    {


    }

    private void configureApplicationForOpenImage(TreeItem<Resource<?>> treeItem)
    {


    }

    public final ObjectProperty<Book> currentBookProperty() {
        return currentBookProperty;
    }

}
