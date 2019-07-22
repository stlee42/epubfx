package de.machmireinebook.epubeditor.javafx.cells;

import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;

/**
 * User: Michail Jungierek
 * Date: 22.07.2019
 * Time: 19:43
 */
public class EditingTreeCellTextFieldSkin extends TextFieldSkin {
    /**
     * Creates a new TextFieldSkin instance, installing the necessary child
     * nodes into the Control {@link Control#getChildren() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public EditingTreeCellTextFieldSkin(TextField control) {
        super(control);
    }
}
