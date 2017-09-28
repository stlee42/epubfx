package de.machmireinebook.epubeditor.manager;

import java.util.List;

import javax.inject.Singleton;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TOCReference;
import de.machmireinebook.epubeditor.epublib.domain.TableOfContents;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 27.07.2014
 * Time: 12:35
 */
@Singleton
public class TOCViewManager
{
    private static final Logger logger = Logger.getLogger(TOCViewManager.class);

    private TreeView<TOCReference> treeView;
    private Book book;
    private EditorTabManager editorManager;
    private TreeItem<TOCReference> rootItem;

    public void setTreeView(TreeView<TOCReference> treeView)
    {
        this.treeView = treeView;
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
                    TreeItem<TOCReference> item = treeView.getSelectionModel().getSelectedItem();
                    TOCReference tocRef = item.getValue();
                    Resource res = tocRef.getResource();
                    if (res == null)
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("File not exists");
                        alert.getDialogPane().setHeader(null);
                        alert.getDialogPane().setHeaderText(null);
                        alert.setContentText("The file attached to toc item " + tocRef.getNcxReference()  + " don't exists and can not be open.");
                        alert.showAndWait();

                        return;
                    }
                    editorManager.openFileInEditor(res, MediaType.XHTML);
                }
            }
        });

    }

    public void setBook(Book book)
    {
        this.book = book;
        rootItem.getChildren().clear();

        TableOfContents toc = book.getTableOfContents();

        List<TOCReference> references = toc.getTocReferences();
        for (TOCReference reference : references)
        {
            TreeItem<TOCReference> tocItem = new TreeItem<>(reference);
            rootItem.getChildren().add(tocItem);
            if (reference.hasChildren())
            {
                addChildren(tocItem);
            }
        }
    }

    private void addChildren(TreeItem<TOCReference> parentItem)
    {
        TOCReference parent = parentItem.getValue();
        List<TOCReference> children = parent.getChildren();
        for (TOCReference child : children)
        {
            TreeItem<TOCReference> childItem = new TreeItem<>(child);
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
