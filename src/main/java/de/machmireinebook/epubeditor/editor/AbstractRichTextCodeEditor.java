package de.machmireinebook.epubeditor.editor;

import java.util.Collection;
import java.util.function.IntFunction;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleSpans;

/**
 * User: mjungierek
 * Date: 23.12.2014
 * Time: 19:55
 */
public abstract class AbstractRichTextCodeEditor extends AnchorPane implements CodeEditor
{
    private CodeArea codeArea = new CodeArea();
    private BooleanProperty canUndo = new SimpleBooleanProperty();
    private BooleanProperty canRedo = new SimpleBooleanProperty();
    private ObjectProperty<Worker.State> state = new SimpleObjectProperty<>();
    private ObjectProperty<EditorPosition> cursorPosition = new SimpleObjectProperty<>(new EditorPosition(0,0));
    private ReadOnlyObjectWrapper<String> code = new ReadOnlyObjectWrapper<>();

    AbstractRichTextCodeEditor()
    {
        AnchorPane.setTopAnchor(codeArea, 0.0);
        AnchorPane.setLeftAnchor(codeArea, 0.0);
        AnchorPane.setBottomAnchor(codeArea, 0.0);
        AnchorPane.setRightAnchor(codeArea, 0.0);

        getChildren().add(codeArea);

        IntFunction<String> format = (digits -> " %" + digits + "d ");
/*        String stylesheet = AbstractRichTextCodeEditor.class.getResource("java-keywords.css").toExternalForm();     */
        IntFunction<Node> factory = LineNumberFactory.get(codeArea, format);
        codeArea.setParagraphGraphicFactory(factory);

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });
        Platform.runLater(() -> {
            state.setValue(Worker.State.SUCCEEDED);
        });

        canUndo.bind(codeArea.getUndoManager().undoAvailableProperty());
        canRedo.bind(codeArea.getUndoManager().redoAvailableProperty());
        codeArea.fontProperty().set(Font.font("Source Code Pro", 14));
    }

    protected abstract StyleSpans<? extends Collection<String>> computeHighlighting(String newText);

    @Override
    public ObjectProperty<Worker.State> stateProperty()
    {
        return state;
    }

    @Override
    public ObjectProperty<EditorPosition> cursorPositionProperty()
    {
        return cursorPosition;
    }

    @Override
    public ReadOnlyObjectProperty<String> codeProperty()
    {
        return code;
    }

    @Override
    public void undo()
    {
        codeArea.undo();
    }

    @Override
    public void redo()
    {
        codeArea.redo();
    }

    @Override
    public BooleanProperty canUndoProperty()
    {
        return canUndo;
    }

    @Override
    public BooleanProperty canRedoProperty()
    {
        return canRedo;
    }

    @Override
    public void setCode(String newCode)
    {
        codeArea.replaceText(0, 0, newCode);
    }

    @Override
    public String getCode()
    {
        return code.getValue();
    }

    @Override
    public EditorPosition getEditorCursorPosition()
    {
        return cursorPosition.getValue();
    }

    @Override
    public void insertAt(String replacement, EditorPosition pos)
    {

    }

    @Override
    public void replaceSelection(String replacement)
    {

    }

    @Override
    public void setCodeEditorSize(double width, double height)
    {

    }

    @Override
    public void scroll(int delta)
    {

    }
}
