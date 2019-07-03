package de.machmireinebook.epubeditor.javafx.cells;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;

import org.apache.log4j.Logger;

import com.sun.javafx.scene.control.behavior.TreeCellBehavior;

public class EditingTreeCellBehavior<T> extends TreeCellBehavior<T>
{
    private static final Logger logger = Logger.getLogger(EditingTreeCellBehavior.class);

    public EditingTreeCellBehavior(final TreeCell<T> control) {
        super(control);
    }

    @Override
    protected void handleClicks(MouseButton button, int clickCount, boolean isAlreadySelected) {
        // handle editing, which only occurs with the primary mouse button
        TreeItem<T> treeItem = getNode().getTreeItem();
        if (button == MouseButton.PRIMARY) {
            if (clickCount == 1) {
                // dont edit or cancel edit
                edit(null);
            } else if (clickCount == 2 && treeItem.isLeaf()) {
                // don't edit on double click, but open the file
                logger.info("double click no edit");
                edit(null);
            } else if (clickCount % 2 == 0) {
                // try to expand/collapse branch tree item
                treeItem.setExpanded(!treeItem.isExpanded());
            }
        }
    }

    @Override
    protected boolean handleDisclosureNode(double x, double y) {
/*        TreeCell<T> treeCell = getNode();
        Node disclosureNode = treeCell.getDisclosureNode();
        if (disclosureNode != null) {
            if (disclosureNode.getBoundsInParent().contains(x, y)) {
                if (treeCell.getTreeItem() != null) {
                    logger.info("treeItem " + treeCell.getTreeItem() +", was expanded: " + treeCell.getTreeItem().isExpanded());
                    treeCell.getTreeItem().setExpanded(! treeCell.getTreeItem().isExpanded());
                    logger.info("treeItem is now expanded: " + treeCell.getTreeItem().isExpanded());
                }
                return true;
            }
        }               */
        return false;
    }

}
