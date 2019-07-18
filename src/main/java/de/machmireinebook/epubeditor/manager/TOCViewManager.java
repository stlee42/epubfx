package de.machmireinebook.epubeditor.manager;

import java.util.List;

import javax.inject.Singleton;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.apache.log4j.Logger;

import org.jdom2.Document;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TableOfContents;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;

/**
 * User: mjungierek
 * Date: 27.07.2014
 * Time: 12:35
 */
@Singleton
public class TOCViewManager
{
    private static final Logger logger = Logger.getLogger(TOCViewManager.class);

    private TreeView<TocEntry> treeView;
    private EditorTabManager editorManager;
    private TreeItem<TocEntry> rootItem;

    // bookProperty
    private final ObjectProperty<Book> bookProperty = new SimpleObjectProperty<>(this, "book");
    public final ObjectProperty<Book> bookProperty() {
       return bookProperty;
    }
    public final Book getBook() {
       return bookProperty.get();
    }

    public void setTreeView(TreeView<TocEntry> treeView)
    {
        this.treeView = treeView;
        rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.PRIMARY))
            {
                if (event.getClickCount() == 2)
                {
                    TreeItem<TocEntry> item = treeView.getSelectionModel().getSelectedItem();
                    TocEntry tocRef = item.getValue();
                    Resource res = tocRef.getResource();
                    if (res == null)
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("File not exists");
                        alert.getDialogPane().setHeader(null);
                        alert.getDialogPane().setHeaderText(null);
                        alert.setContentText("The file attached to toc item " + tocRef.getReference()  + " don't exists and can not be open.");
                        alert.showAndWait();

                        return;
                    }
                    editorManager.openFileInEditor(res, MediaType.XHTML);
                }
            }
        });

        bookProperty.addListener((observable, oldValue, newValue) -> {
                bookChanged();
        });

    }

    private void bookChanged()
    {
        rootItem.getChildren().clear();
        if (bookProperty.get() != null)
        {
            TableOfContents toc = bookProperty.get().getTableOfContents();

            List<TocEntry<? extends TocEntry, Document>> references = toc.getTocReferences();
            for (TocEntry reference : references)
            {
                TreeItem<TocEntry> tocItem = new TreeItem<>(reference);
                rootItem.getChildren().add(tocItem);
                if (reference.hasChildren())
                {
                    addChildren(tocItem);
                }
            }
        }
    }

    private void addChildren(TreeItem<TocEntry> parentItem)
    {
        TocEntry parent = parentItem.getValue();
        List<TocEntry<? extends TocEntry, Document>> children = parent.getChildren();
        for (TocEntry<? extends TocEntry, Document> child : children)
        {
            TreeItem<TocEntry> childItem = new TreeItem<>(child);
            parentItem.getChildren().add(childItem);
            if (child.hasChildren())
            {
                addChildren(childItem);
            }
        }
    }

    public void setEditorManager(EditorTabManager editorManager)
    {
        this.editorManager = editorManager;
    }
}
