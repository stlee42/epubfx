package de.machmireinebook.epubeditor.editor;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.wellbehaved.event.Nodes;
import org.languagetool.rules.RuleMatch;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

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
    private boolean isChangingCode = false;
    private ExecutorService taskExecutor;
    PreferencesManager preferencesManager = BeanFactory.getInstance().getBean(PreferencesManager.class);

    // textInformationProperty
    private final ReadOnlyStringWrapper textInformation = new ReadOnlyStringWrapper(this, "textInformation");

    protected MediaType mediaType;
    private int durationHighlightingComputation = 10;

    AbstractRichTextCodeEditor()
    {
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);

        getChildren().add(scrollPane);

        taskExecutor = Executors.newSingleThreadExecutor();

        IntFunction<String> format = (digits -> " %" + digits + "d ");
        IntFunction<Node> factory = LineNumberFactory.get(codeArea, format);
        codeArea.setParagraphGraphicFactory(factory);

        codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(durationHighlightingComputation))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(codeArea.multiPlainChanges())
                .filterMap(tryTask -> {
                    if (tryTask.isSuccess()) {
                        //set up the duration after first successfull highlighting after opening file
                        durationHighlightingComputation = 500;
                        return Optional.of(tryTask.get());
                    } else {
                        tryTask.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);

        Platform.runLater(() -> {
            state.setValue(Worker.State.SUCCEEDED);
        });

        canUndo.bind(codeArea.getUndoManager().undoAvailableProperty());
        canRedo.bind(codeArea.getUndoManager().redoAvailableProperty());

        String stylesheet = AbstractRichTextCodeEditor.class.getResource("/editor-css/common.css").toExternalForm();
        codeArea.getStylesheets().add(stylesheet);

        codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            cursorPosition.set(newValue);
            Collection<String> styles = codeArea.getStyleOfChar(newValue);
            textInformation.set("Styles: " + StringUtils.join(styles, ","));
        });

        //setup special keys
        CodeArea codeArea = getCodeArea();
        Nodes.removeInputMap(codeArea, consume(keyPressed(KeyCode.TAB), this::insertTab));
        Nodes.addInputMap(codeArea, sequence(
                                    consume(keyPressed(KeyCode.TAB), this::insertTab),
                                    consume(keyPressed(KeyCode.TAB, KeyCombination.SHIFT_DOWN), this::shiftTabPressed)));

        codeArea.setOnKeyTyped(e -> {
            String character = e.getCharacter();
            switch (character) {
                case "{":
                    completePair("}");
                    break;
                case "\"":
                    completePair("\"");
                    break;
                case "(":
                    completePair(")");
                    break;
                case "[":
                    completePair("]");
                    break;
            }
        });
    }

    @Override
    public MediaType getMediaType()
    {
        return mediaType;
    }

    private void insertTab(KeyEvent event) {
        logger.info("insert tab");
        if (!preferencesManager.isUseTab()) {
            insertAt(getAbsoluteCursorPosition(), StringUtils.repeat(' ', preferencesManager.getTabSize()));
        } else {
            insertAt(getAbsoluteCursorPosition(), "\t");
        }
    }

    private void shiftTabPressed(KeyEvent event) {

    }

    protected void completePair(String closingPart) {
        insertAt(getAbsoluteCursorPosition(), closingPart);
        setAbsoluteCursorPosition(getAbsoluteCursorPosition() - closingPart.length());
    }

    public void setWrapText(boolean wrapText)
    {
        codeArea.setWrapText(wrapText);
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return computeHighlighting(text);
            }
        };
        taskExecutor.execute(task);
        return task;
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
        if (preferencesManager.isSpellcheck()) {
            Task<List<RuleMatch>> spellcheckTask = spellCheckAsync();

            spellcheckTask.setOnSucceeded(event -> {
                List<RuleMatch> matches = spellcheckTask.getValue();
                applySpellCheckResults(matches);
            });
            spellcheckTask.setOnFailed(event -> {
                logger.error("error while executing spell check", spellcheckTask.getException());
            });
        }
    }

    private Task<List<RuleMatch>> spellCheckAsync() {
        Task<List<RuleMatch>> task = new Task<>() {
            @Override
            protected List<RuleMatch> call() {
                return spellCheck();
            }
        };
        taskExecutor.execute(task);
        return task;
    }

    protected abstract StyleSpans<Collection<String>> computeHighlighting(String text);

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
        isChangingCode = true; //this change of code is not relevant for listeners
        codeArea.clear();
        isChangingCode = false;
        codeArea.insertText(0, newCode);
    }

    @Override
    public String getCode()
    {
        return codeArea.getText();
    }

    public void requestFocus() {
        Platform.runLater(() -> {
            if (codeArea.getScene() != null)
                codeArea.requestFocus();
            else {
                // text area still does not have a scene
                // --> use listener on scene to make sure that text area receives focus
                ChangeListener<Scene> l = new ChangeListener<Scene>() {
                    @Override
                    public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
                        codeArea.sceneProperty().removeListener(this);
                        codeArea.requestFocus();
                    }
                };
                codeArea.sceneProperty().addListener(l);
            }
        });
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
    public void insertAt(Integer pos, String insertion)
    {
        if (pos > codeArea.getLength()) {
            codeArea.appendText(insertion);
        } else {
            codeArea.insertText(pos, insertion);
        }
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

    public IndexRange getSelectedRange()
    {
        return codeArea.getSelection();
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
        codeArea.showParagraphAtTop(line);
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

    public CodeArea getCodeArea()
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

    @Override
    public boolean isChangingCode()
    {
        return isChangingCode;
    }

    @Override
    public void clearUndoHistory()
    {
        codeArea.getUndoManager().forgetHistory();
    }
}
