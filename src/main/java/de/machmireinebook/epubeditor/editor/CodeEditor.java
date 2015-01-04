package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:12
 */

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;

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
    ObjectProperty<EditorPosition> cursorPositionProperty();
    ReadOnlyObjectProperty<String> codeProperty();
    void undo();
    void redo();
    BooleanProperty canUndoProperty();
    BooleanProperty canRedoProperty();

    //methods of the underlying editor component e.g. codemirror
    void setCode(String newCode);
    String getCode();
    EditorPosition getEditorCursorPosition();
    void insertAt(String replacement, EditorPosition pos);
    EditorRange getSelection();
    void replaceSelection(String replacement);
    void setCodeEditorSize(double width, double height);
    void scroll(int delta);
    void scrollTo(EditorPosition pos);

}