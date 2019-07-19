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
import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;

import org.fxmisc.richtext.CodeArea;
import org.languagetool.rules.RuleMatch;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a
 * richtextFX code editor
 */
public interface CodeEditor
{
    MediaType getMediaType();

    void setContextMenu(ContextMenu contextMenu);
    void setCodeEditorSize(double width, double height);
    ObjectProperty<Worker.State> stateProperty();
    void requestFocus();

    void undo();
    void redo();
    void clearUndoHistory();
    BooleanProperty canUndoProperty();
    BooleanProperty canRedoProperty();

    void setCode(String newCode);
    String getCode();
    CodeArea getCodeArea();
    boolean isChangingCode();
    void resetChangingCode();

    void scrollTo(int index);
    void scrollTo(EditorPosition pos);
    EditorPosition getCursorPosition();
    IntegerProperty cursorPositionProperty();
    Integer getAbsoluteCursorPosition();
    void setAbsoluteCursorPosition(int position);

    void insertAt(Integer pos , String insertion);
    void select(int fromIndex, int toIndex);
    String getSelection();
    void replaceSelection(String replacement);

    String getRange(int start, int end);
    String getRange(IndexRange range);
    void replaceRange(IndexRange range, String replacement);

    List<RuleMatch> spellCheck();
    void applySpellCheckResults(List<RuleMatch> matches);

    String getTextInformation();

    void shutdown();
}
