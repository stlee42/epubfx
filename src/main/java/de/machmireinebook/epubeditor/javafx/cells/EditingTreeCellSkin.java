package de.machmireinebook.epubeditor.javafx.cells;

import javafx.scene.control.TreeCell;
import javafx.scene.control.skin.TreeCellSkin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;

public class EditingTreeCellSkin<T> extends TreeCellSkin<T>
{
    private final BehaviorBase<TreeCell<T>> behavior;

    public EditingTreeCellSkin(TreeCell<T> control)
    {
        super(control);
        behavior = new EditingTreeCellBehavior<>(control);
    }

    @Override
    public void dispose() {
        super.dispose();

        if (behavior != null) {
            behavior.dispose();
        }
    }
}
