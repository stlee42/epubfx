package de.machmireinebook.epubeditor.editor;

/*
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:12
 */

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;

import org.fxmisc.richtext.CodeArea;
import org.languagetool.rules.RuleMatch;

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

    void scroll(int delta);
    void scrollTo(int index);
    void scrollTo(EditorPosition pos);

    //methods of the java part of editor
    boolean isChangingCode();
    void setContextMenu(ContextMenu contextMenu);
    ObjectProperty<Worker.State> stateProperty();
    IntegerProperty cursorPositionProperty();
    ObservableValue<String> codeProperty();
    void undo();
    void redo();
    BooleanProperty canUndoProperty();
    BooleanProperty canRedoProperty();

    //methods of the underlying editor component
    void setCode(String newCode);
    String getCode();
    CodeArea getCodeArea();

    EditorPosition getCursorPosition();

    Integer getAbsoluteCursorPosition();
    void setAbsoluteCursorPosition(int position);
    void insertAt(Integer pos , String insertion);
    void select(int fromIndex, int toIndex);
    String getSelection();
    void replaceSelection(String replacement);

    String getRange(int start, int end);
    String getRange(IndexRange range);
    void replaceRange(IndexRange range, String replacement);

    void setCodeEditorSize(double width, double height);

    List<RuleMatch> spellCheck();
    void applySpellCheckResults(List<RuleMatch> matches);
    void clearUndoHistory();

    ReadOnlyStringProperty textInformationProperty();
    String getTextInformation();

}
