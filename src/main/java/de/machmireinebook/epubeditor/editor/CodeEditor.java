package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:12
 */

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

import javafx.scene.web.WebView;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a
 * CodeMirror code editor in a WebView.
 * <p>
 * See http://codemirror.net for more information on using the codemirror editor.
 */
public interface CodeEditor
{
    WebView getWebview();
    String getEditingTemplate();
    MediaType getMediaType();

    //methods of the underlying editor component e.g. codemirror
    void setCode(String newCode);
    String getCode();
    EditorPosition getEditorCursorPosition();
    void insertAt(String replacement, EditorPosition pos);
    void replaceSelection(String replacement);
    void setCodeEditorSize(double width, double height);
    void scroll(int delta);
}