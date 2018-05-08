package de.machmireinebook.epubeditor.editor;

/*
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:12
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a
 * CodeMirror code editor in a WebView.
 * <p>
 * See http://codemirror.net for more information on using the codemirror editor.
 */
public interface CodeEditor
{
    MediaType getMediaType();

    //methods of the java part of editor
    void setContextMenu(ContextMenu contextMenu);
    ObjectProperty<Worker.State> stateProperty();
    IntegerProperty cursorPositionProperty();
    ObservableValue<String> codeProperty();
    void undo();
    void redo();
    BooleanProperty canUndoProperty();
    BooleanProperty canRedoProperty();

    //methods of the underlying editor component e.g. codemirror
    void setCode(String newCode);
    String getCode();
    Integer getEditorCursorPosition();
    void setEditorCursorPosition(Integer position);
    void insertAt(Integer pos , String insertion);
    void select(int fromIndex, int toIndex);
    String getSelection();
    void replaceSelection(String replacement);

    void scrollTo(int index);

    void setCodeEditorSize(double width, double height);
    void scroll(int delta);

    void spellCheck();
}