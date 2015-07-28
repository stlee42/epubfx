package de.machmireinebook.epubeditor.javafx.cells;

import java.util.Collections;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import javafx.scene.Node;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import org.apache.log4j.Logger;

public class EditingTreeCellBehavior<T> extends CellBehaviorBase<TreeCell<T>>
{
    public static final Logger logger = Logger.getLogger(EditingTreeCellBehavior.class);

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public EditingTreeCellBehavior(final TreeCell<T> control) {
        super(control, Collections.emptyList());
    }



    /***************************************************************************
     *                                                                         *
     * Private implementation                                                  *
     *                                                                         *
     **************************************************************************/

    @Override
    protected MultipleSelectionModel<TreeItem<T>> getSelectionModel() {
        return getCellContainer().getSelectionModel();
    }

    @Override
    protected FocusModel<TreeItem<T>> getFocusModel() {
        return getCellContainer().getFocusModel();
    }

    @Override
    protected TreeView<T> getCellContainer() {
        return getControl().getTreeView();
    }

    @Override
    protected void edit(TreeCell<T> cell) {
        TreeItem<T> treeItem = cell == null ? null : cell.getTreeItem();
        getCellContainer().edit(treeItem);
    }

    @Override
    protected void handleClicks(MouseButton button, int clickCount, boolean isAlreadySelected) {
        // handle editing, which only occurs with the primary mouse button
        TreeItem<T> treeItem = getControl().getTreeItem();
        if (button == MouseButton.PRIMARY) {
            if (clickCount == 1 && isAlreadySelected) {
                edit(null);
            } else if (clickCount == 1) {
                // cancel editing
                edit(null);
            } else if (clickCount == 2 && treeItem.isLeaf()) {
                // wir wollen nicht edit bei Doppelklick
                logger.info("double click no edit");
                edit(null);
            } else if (clickCount % 2 == 0) {
                // try to expand/collapse branch tree item
                treeItem.setExpanded(! treeItem.isExpanded());
            }
        }
    }

    @Override
    protected boolean handleDisclosureNode(double x, double y) {
        TreeCell<T> treeCell = getControl();
        Node disclosureNode = treeCell.getDisclosureNode();
        if (disclosureNode != null) {
            if (disclosureNode.getBoundsInParent().contains(x, y)) {
                if (treeCell.getTreeItem() != null) {
                    treeCell.getTreeItem().setExpanded(! treeCell.getTreeItem().isExpanded());
                }
                return true;
            }
        }
        return false;
    }
}