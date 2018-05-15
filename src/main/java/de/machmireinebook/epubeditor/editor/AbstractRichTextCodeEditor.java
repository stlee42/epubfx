package de.machmireinebook.epubeditor.editor;

import java.util.Collection;
import java.util.function.IntFunction;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.layout.AnchorPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;

/**
 * User: mjungierek
 * Date: 23.12.2014
 * Time: 19:55
 */
public abstract class AbstractRichTextCodeEditor extends AnchorPane implements CodeEditor
{
    private static final Logger logger = Logger.getLogger(AbstractRichTextCodeEditor.class);

    private CodeArea codeArea = new CodeArea();
    private BooleanProperty canUndo = new SimpleBooleanProperty();
    private BooleanProperty canRedo = new SimpleBooleanProperty();
    private ObjectProperty<Worker.State> state = new SimpleObjectProperty<>();
    private IntegerProperty cursorPosition = new SimpleIntegerProperty();
    // textInformationProperty
    private final ReadOnlyStringWrapper textInformation = new ReadOnlyStringWrapper(this, "textInformation");

    AbstractRichTextCodeEditor()
    {
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);

        getChildren().add(scrollPane);

        IntFunction<String> format = (digits -> " %" + digits + "d ");
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

        String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/common.css").toExternalForm();
        codeArea.getStylesheets().add(stylesheet);

        codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            cursorPosition.set(newValue);
            Collection<String> styles = codeArea.getStyleAtPosition(newValue);
            textInformation.set("Styles: " + StringUtils.join(styles, ","));
        });
    }

    public void setWrapText(boolean wrapText)
    {
        codeArea.setWrapText(wrapText);
    }


    protected abstract StyleSpans<? extends Collection<String>> computeHighlighting(String newText);

    @Override
    public ObjectProperty<Worker.State> stateProperty()
    {
        return state;
    }

    @Override
    public IntegerProperty cursorPositionProperty()
    {
        return cursorPosition;
    }

    @Override
    public ObservableValue<String> codeProperty()
    {
        return codeArea.textProperty();
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
        return codeArea.getText();
    }

    @Override
    public EditorPosition getCursorPosition()
    {
        return new EditorPosition(codeArea.getCurrentParagraph(), codeArea.getCaretColumn());
    }

    @Override
    public Integer getAbsoluteCursorPosition()
    {
        return codeArea.getCaretPosition();
    }

    @Override
    public void setAbsoluteCursorPosition(int position)
    {
        codeArea.moveTo(position);
    }

    @Override
    public void insertAt(Integer pos , String insertion)
    {
        codeArea.insertText(pos, insertion);
    }

    @Override
    public void replaceSelection(String replacement)
    {
        codeArea.replaceSelection(replacement);
    }


    @Override
    public String getRange(int start, int end)
    {
        return getRange(new IndexRange(start, end));
    }

    @Override
    public String getRange(IndexRange range)
    {
        return codeArea.getText(range);
    }

    @Override
    public void replaceRange(IndexRange range, String replacement)
    {
        codeArea.replaceText(range, replacement);
    }

    @Override
    public void setCodeEditorSize(double width, double height)
    {

    }

    @Override
    public String getSelection()
    {
        return codeArea.getSelectedText();
    }

    @Override
    public void select(int fromIndex, int toIndex)
    {
        codeArea.selectRange(fromIndex, toIndex);
        codeArea.requestFollowCaret();
    }

    @Override
    public void scroll(int delta)
    {

    }

    @Override
    public void scrollTo(int index)
    {
//        codeArea.scrollXBy(index);
    }

    @Override
    public void scrollTo(EditorPosition pos)
    {
        int line = 0;
        if (pos.getLine() > 0)
        {
            line = pos.getLine() - 1;
        }
        codeArea.showParagraphInViewport(line);
    }

    public void addStyleSheet(String styleSheet)
    {
        getStylesheets().add(styleSheet);
    }

    @Override
    public void setContextMenu(ContextMenu contextMenu)
    {
        codeArea.setContextMenu(contextMenu);
    }

    protected Paragraph<Collection<String>, String, Collection<String>> getCurrentParagraph()
    {
        return codeArea.getParagraph(codeArea.getCurrentParagraph());
    }

    protected Paragraph<Collection<String>, String, Collection<String>> getParagraph(int paragraphIndex)
    {
        return codeArea.getParagraph(paragraphIndex);
    }

    int getCurrentParagraphIndex()
    {
        return codeArea.getCurrentParagraph();
    }

    int getAbsolutePosition(int paragraphIndex, int columnIndex)
    {
        return codeArea.getAbsolutePosition(paragraphIndex, columnIndex);
    }

    int getNumberParagraphs()
    {
        return codeArea.getParagraphs().size();
    }

    protected CodeArea getCodeArea()
    {
        return codeArea;
    }

    @Override
    public final ReadOnlyStringProperty textInformationProperty() {
        return textInformation.getReadOnlyProperty();
    }
    @Override
    public final String getTextInformation() {
        return textInformation.get();
    }

}
