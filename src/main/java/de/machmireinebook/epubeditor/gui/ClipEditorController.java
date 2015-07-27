package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import de.machmireinebook.epubeditor.cdi.ClipManagerProducer;
import de.machmireinebook.epubeditor.domain.Clip;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.manager.ClipManager;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 03.01.2015
 * Time: 00:25
 */
public class ClipEditorController implements StandardController
{
    public static final Logger logger = Logger.getLogger(ClipEditorController.class);

    @FXML
    private TextField filterTextField;
    @FXML
    private TreeTableView<Clip> treeTableView;

    private ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>();
    private Stage stage;

    @Inject
    @ClipManagerProducer
    private ClipManager clipManager;

    private static ClipEditorController instance;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        treeTableView.setEditable(true);
        ContextMenu contextMenu = treeTableView.getContextMenu();
        treeTableView.setContextMenu(null);

        TreeTableColumn<Clip, String> tcName = (TreeTableColumn<Clip, String>)treeTableView.getColumns().get(0);
        tcName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        tcName.setEditable(true);
        tcName.setCellFactory(param -> {
            TextFieldTreeTableCell<Clip, String> cell = new TextFieldTreeTableCell<>(new DefaultStringConverter());
            cell.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    e -> {
                        if (e.getButton() == MouseButton.SECONDARY)
                        {
                            contextMenu.setImpl_showRelativeToWindow(false);
                            contextMenu.show(cell, e.getScreenX(), e.getScreenY());
                        }
                    });
            return cell;
        });
        tcName.setOnEditCommit(event -> {
            String value = event.getNewValue();
            TreeItem<Clip> treeItem = event.getRowValue();
            Clip element = treeItem.getValue();
            element.setName(value);
            int rowNumber = treeTableView.getRow(treeItem);
            //focus beim ttv behalten
            Platform.runLater(() -> {
                treeTableView.requestFocus();
                treeTableView.getSelectionModel().select(rowNumber);
                treeTableView.getFocusModel().focus(rowNumber);
            });

        });

        TreeTableColumn<Clip, String> tcContent = (TreeTableColumn<Clip, String>)treeTableView.getColumns().get(1);
        tcContent.setCellValueFactory(new TreeItemPropertyValueFactory<>("content"));
        tcContent.setEditable(true);
        tcContent.setCellFactory(param -> {
            TextFieldTreeTableCell<Clip, String> cell = new TextFieldTreeTableCell<>(new DefaultStringConverter());
            cell.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    e -> {
                        if (e.getButton() == MouseButton.SECONDARY)
                        {
                            contextMenu.setImpl_showRelativeToWindow(false);
                            contextMenu.show(cell, e.getScreenX(), e.getScreenY());
                        }
                    });
            return cell;
        });
        tcContent.setOnEditCommit(event -> {
            String value = event.getNewValue();
            TreeItem<Clip> treeItem = event.getRowValue();
            Clip clip = treeItem.getValue();
            int rowNumber = treeTableView.getRow(treeItem);
            clip.setContent(value);
            //focus beim ttv behalten
            Platform.runLater(() -> {
                treeTableView.requestFocus();
                treeTableView.getSelectionModel().select(rowNumber);
                treeTableView.getFocusModel().focus(rowNumber);
            });
        });

        treeTableView.setShowRoot(false);

        instance = this;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnShowing(event -> {
            TreeItem<Clip> root = deepcopy(clipManager.getClipsRoot());
            treeTableView.setRoot(root);
        });
    }

    private  TreeItem<Clip> deepcopy(TreeItem<Clip> item) {
        TreeItem<Clip> copy = new TreeItem<>(item.getValue());
        for (TreeItem<Clip> child : item.getChildren())
        {
            copy.getChildren().add(deepcopy(child));
        }
        return copy;
    }

    @Override
    public Stage getStage()
    {
        return stage;
    }

    @Override
    public ObjectProperty<Book> currentBookProperty()
    {
        return currentBookProperty;
    }

    public static ClipEditorController getInstance()
    {
        return instance;
    }

    public void saveClips(ActionEvent actionEvent)
    {
        clipManager.setClipsRoot(treeTableView.getRoot());
        stage.close();
    }

    public void cancelClips(ActionEvent actionEvent)
    {
        stage.close();
    }

    public void filterAction(ActionEvent actionEvent)
    {
        

    }

    public void editClipAction(ActionEvent actionEvent)
    {
        MenuItem item = (MenuItem) actionEvent.getSource();
        ContextMenu menu = item.getParentPopup();
        TextFieldTreeTableCell cell = (TextFieldTreeTableCell) menu.getOwnerNode();
        logger.debug("owner node " + cell);
        cell.startEdit();
    }

    public void addClip(ActionEvent actionEvent)
    {
        addClip(false);
    }

    public void addClipGroup(ActionEvent actionEvent)
    {
        addClip(true);
    }

    public void addClip(boolean isGroup)
    {
        int selectedIndex = treeTableView.getSelectionModel().getSelectedIndex();
        TreeItem<Clip> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
        Clip clip;
        if (isGroup)
        {
            clip = new Clip("Name", true);
        }
        else
        {
            clip = new Clip("Name", "");
        }
        TreeItem<Clip> treeItem = new TreeItem<>(clip);
        selectedItem.getParent().getChildren().add(selectedIndex + 1, treeItem);
        //focus auf neue Zeile setzen
        Platform.runLater(() -> {
            treeTableView.requestFocus();
            treeTableView.getSelectionModel().select(treeItem);
            int index = treeTableView.getSelectionModel().getSelectedIndex();
            treeTableView.getFocusModel().focus(index);
            //treeTableView.edit(index, treeTableView.getColumns().get(0));
        });
    }

    public void cutClip(ActionEvent actionEvent)
    {
        

    }

    public void copyClip(ActionEvent actionEvent)
    {
    }

    public void pasteClip(ActionEvent actionEvent)
    {
    }

    public void deleteClip(ActionEvent actionEvent)
    {
        TreeItem<Clip> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
        selectedItem.getParent().getChildren().remove(selectedItem);
    }

    public void collabsAll(ActionEvent actionEvent)
    {
    }

    public void importClips(ActionEvent actionEvent)
    {
    }

    public void exportClips(ActionEvent actionEvent)
    {
    }

    public void expandAll(ActionEvent actionEvent)
    {
    }

    public void insertClipIntoText(ActionEvent actionEvent)
    {
    }

    public void clipUpAction(ActionEvent actionEvent)
    {
        TreeItem<Clip> selectedItem = treeTableView.getSelectionModel().getSelectedItem();

        TreeItem<Clip> parent = selectedItem.getParent();
        ObservableList<TreeItem<Clip>> children = parent.getChildren();
        int index = children.indexOf(selectedItem);
        children.remove(selectedItem);
        int newIndex = index - 1;
        if (newIndex < 0)
        {
            newIndex = 0;
        }
        children.add(newIndex, selectedItem);

        treeTableView.getSelectionModel().select(selectedItem);
    }

    public void clipRightAction(ActionEvent actionEvent)
    {
    }

    public void clipDownAction(ActionEvent actionEvent)
    {
        TreeItem<Clip> selectedItem = treeTableView.getSelectionModel().getSelectedItem();

        TreeItem<Clip> parent = selectedItem.getParent();
        ObservableList<TreeItem<Clip>> children = parent.getChildren();
        int index = children.indexOf(selectedItem);
        children.remove(selectedItem);
        int newIndex = index + 1;
        if (newIndex > children.size())
        {
            newIndex = children.size();
        }
        children.add(newIndex, selectedItem);

        treeTableView.getSelectionModel().select(selectedItem);
    }

    public void clipLeftAction(ActionEvent actionEvent)
    {


    }
}
