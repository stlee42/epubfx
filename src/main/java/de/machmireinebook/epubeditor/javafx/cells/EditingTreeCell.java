package de.machmireinebook.epubeditor.javafx.cells;


import javafx.application.Platform;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.scene.input.KeyCode;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.ToStringConvertible;

/**
 * User: mjungierek
 * Date: 18.08.2014
 * Time: 00:23
 */
public class EditingTreeCell<T extends ToStringConvertible> extends TreeCell<T>
{
    private static final Logger logger = Logger.getLogger(EditingTreeCell.class);

    private TextField textField;

    public EditingTreeCell()
    {
        super();
    }

    @Override
    protected Skin<?> createDefaultSkin()
    {
        return new EditingTreeCellSkin<>(this);
    }

    @Override
    public void startEdit()
    {
        final TreeView<T> tree = getTreeView();
        if (!isEditable() || (tree != null && ! tree.isEditable()))
        {
            return;
        }
        super.startEdit();
        if(textField == null )
        {
            createTextField();
        }
        setText(null);
        setGraphic(textField);
        textField.setText(getString());
        textField.selectAll();
        Platform.runLater(textField::requestFocus);
    }

    @Override
    public void cancelEdit()
    {
        textField.setText(getString());
        setText(getString());
        if (getTreeItem() != null)
        {
            setGraphic(getTreeItem().getGraphic());
        }
        else
        {
            setGraphic(null);
        }
        super.cancelEdit();
    }

    @Override
    public void updateItem(T item, boolean empty)
    {
        super.updateItem(item, false);

        if (empty)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {
            if (isEditing())
            {
                if (textField != null)
                {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            }
            else
            {
                setText(getString());
                if (getTreeItem() != null)
                {
                    setGraphic(getTreeItem().getGraphic());
                }
                else
                {
                    setGraphic(null);
                }
            }
        }
    }

    private void createTextField()
    {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphic().getLayoutBounds().getWidth() - this.getGraphicTextGap());
        logger.debug("width " +  this.getWidth() + ", min " +  this.getMinWidth() + ", max " +  this.getMaxWidth() + ", pref " +  this.getPrefWidth());
        textField.focusedProperty().addListener((value, bool1, bool2) -> {
            if (!bool2)
            {
                T item = getItem();
                item.convertFromString(textField.getText());
                commitEdit(item);
            }
        });

        textField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER)
            {
                String value = textField.getText();
                if (value != null)
                {
                    T item = getItem();
                    item.convertFromString(value);
                    commitEdit(item);
                }
                else
                {
                    commitEdit(null);
                }
            }
            else if (event.getCode() == KeyCode.ESCAPE)
            {
                cancelEdit();
            }
        });
        textField.getStyleClass().add("editing-tree-cell-textfield");
    }


    private String getString()
    {
        return getItem() == null ? "" : getItem().convertToString();
    }

};
