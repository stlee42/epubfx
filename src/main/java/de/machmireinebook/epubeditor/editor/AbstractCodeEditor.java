package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:12
 */

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.sun.webkit.dom.KeyboardEventImpl;
import com.sun.webkit.dom.MouseEventImpl;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a
 * CodeMirror code editor in a WebView.
 * <p>
 * See http://codemirror.net for more information on using the codemirror editor.
 */
public abstract class AbstractCodeEditor extends AnchorPane implements CodeEditor
{
    private static final Logger logger = Logger.getLogger(AbstractCodeEditor.class);

    /**
     * a webview used to encapsulate the CodeMirror JavaScript.
     */
    private final WebView webview = new WebView();

    private UndoRedoManager<CodeVersion> undoRedoManager = new UndoRedoManager<>();
    private BooleanProperty canUndo = new SimpleBooleanProperty();
    private BooleanProperty canRedo = new SimpleBooleanProperty();
    private ObjectProperty<Worker.State> state = new SimpleObjectProperty<>();
    private ObjectProperty<EditorPosition> cursorPosition = new SimpleObjectProperty<>(new EditorPosition(0,0));
    private ReadOnlyObjectWrapper<String> code = new ReadOnlyObjectWrapper<>();

    private ContextMenu contextMenu;
    private SpellChecker spellChecker;

    public class CodeVersion
    {
        private String code;
        private EditorPosition cursorPosition;

        public CodeVersion(String code, EditorPosition cursorPosition)
        {
            this.code = code;
            this.cursorPosition = cursorPosition;
        }

        public String getCode()
        {
            return code;
        }

        public EditorPosition getCursorPosition()
        {
            return cursorPosition;
        }

        /**
         * Interessant ist für undo/redo nur der code die cursorposition ist nur für die userexperience,
         * eine reine cursoränderung soll nicht versioniert werden
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof CodeVersion))
            {
                return false;
            }

            CodeVersion that = (CodeVersion) o;

            if (code != null ? !code.equals(that.code) : that.code != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return code != null ? code.hashCode() : 0;
        }
    }

    public class OnChangeListener
    {
        public void changed(JSObject editor, JSObject changeObj)
        {
            logger.debug("on change event from editor");
            String editorCode = (String) webview.getEngine().executeScript("editor.getValue();");
            CodeVersion version = new CodeVersion(editorCode, getEditorCursorPosition());
            undoRedoManager.saveVersion(version);
            code.setValue(editorCode);
            spellCheck();
        }
    }

    public class SpellChecker
    {
        private MultiThreadedJLanguageTool langTool;
        public SpellChecker()
        {
            langTool = new MultiThreadedJLanguageTool(new GermanyGerman());
            try
            {
                langTool.activateDefaultPatternRules();
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }

        public boolean check(String word)
        {
            List<RuleMatch> matches = Collections.emptyList();
            try
            {
                matches = langTool.check(word);
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
            return matches.isEmpty();
        }

        public List<RuleMatch> checkText(String text)
        {
            List<RuleMatch> matches = Collections.emptyList();
            try
            {
                matches = langTool.check(text);
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
            return matches;
        }

        public void onViewPortChanged(Object editor, int from, int to)
        {
            logger.debug("viewport changed");
            Platform.runLater(() -> {
                int fromIndex = (int) webview.getEngine().executeScript("editor.indexFromPos({line:" + from + ",ch: 0});");
                int toIndex = (int) webview.getEngine().executeScript("editor.indexFromPos({line:" + (to + 1) +",ch: 0});");

                String text = getCode();
                text = text.substring(fromIndex, toIndex);
                List<RuleMatch> results = checkText(text);
                for (RuleMatch result : results)
                {
                    int resultFromIndex = result.getFromPos();
                    int resultToIndex = result.getToPos();
                    webview.getEngine().executeScript("editor.markText(editor.posFromIndex(" + (fromIndex + resultFromIndex) + "), editor.posFromIndex(" + (fromIndex + resultToIndex) + ")," +
                            "{\"className\": \"cm-spell-error\"})");
                }
            });
        }
    }

    /**
     * Create a new code editor.
     *
     * @param editingCode the initial code to be edited in the code editor.
     */
    public AbstractCodeEditor()
    {
        AnchorPane.setTopAnchor(webview, 0.0);
        AnchorPane.setLeftAnchor(webview, 0.0);
        AnchorPane.setBottomAnchor(webview, 0.0);
        AnchorPane.setRightAnchor(webview, 0.0);

        canUndo.bind(undoRedoManager.canUndoProperty());
        canRedo.bind(undoRedoManager.canRedoProperty());

        WebEngine engine = webview.getEngine();
        engine.loadContent(applyEditingTemplate());
        getChildren().add(webview);

        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(Worker.State.SUCCEEDED))
            {
                Document document = engine.getDocument();

                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("onChangeListener", new OnChangeListener());
                spellChecker = new SpellChecker();
                window.setMember("spellChecker", spellChecker);

                Element documentElement = document.getDocumentElement();
                ((EventTarget) documentElement).addEventListener("keyup", evt ->
                {
                    boolean isCtrlPressed = ((KeyboardEventImpl) evt).getCtrlKey();
                    int keyCode = ((KeyboardEventImpl) evt).getKeyCode();
                    String keyIdentifier = ((KeyboardEventImpl) evt).getKeyIdentifier();

                    logger.debug("key up in content editor: " + evt.getTarget() + " isCtrl " + isCtrlPressed + ", keyCode " + keyCode + " keyId " + keyIdentifier);

                    if (isCtrlPressed && (keyCode == 90 || keyCode == 89) || keyCodeIsModifierKey(keyCode))
                    {
                        return;
                    }
                    cursorPosition.set(getEditorCursorPosition());
                }, false);

                ((EventTarget) documentElement).addEventListener("keydown", evt ->
                {
                    boolean isCtrlPressed = ((KeyboardEventImpl) evt).getCtrlKey();
                    int keyCode = ((KeyboardEventImpl) evt).getKeyCode();
                    logger.info("key down in content editor: " + isCtrlPressed + "-" + keyCode + ", Cancelable " + evt.getCancelable());
                    //Ctrl-Z abfangen um eigenen Undo/Redo-Manager zu verwenden
                    if (isCtrlPressed && keyCode == 90)
                    {
                        logger.debug("Ctrl-Z gedrückt");
                        evt.preventDefault();
                        undo();
                    }
                    else if (isCtrlPressed && keyCode == 89)
                    {
                        logger.debug("Ctrl-Y gedrückt");
                        evt.preventDefault();
                        redo();
                    }
                    else if (isCtrlPressed && keyCode == 32)
                    {
                        logger.debug("Ctrl-SPACE gedrückt");
                        evt.preventDefault();
                        removeTags();
                    }

                }, false);

                ((EventTarget) documentElement).addEventListener("contextmenu", evt ->
                {
                    logger.debug("contextmenu event aufgefangen " + evt);
                    evt.preventDefault();
//                    contextMenu.setShowRelativeToWindow(true);
                    contextMenu.show(webview, ((MouseEventImpl) evt).getScreenX(), ((MouseEventImpl) evt).getScreenY());
                }, false);


                webview.setOnScroll(event -> {
                    Double delta = event.getDeltaY() * -1;
                    scroll(delta.intValue());
                });
                state.setValue(Worker.State.SUCCEEDED);
            }
        });
    }

    protected abstract String getEditingTemplate();

    /**
     * applies the editing template to the editing code to create the html+javascript source for a code editor.
     */
    private String applyEditingTemplate()
    {
        return getEditingTemplate();
    }

    /**
     * sets the current code in the editor
     */
    public void setCode(String newCode)
    {
        webview.getEngine().executeScript("editor.setValue('" + StringEscapeUtils.escapeJavaScript(newCode) + "');");
    }

    @Override
    public void spellCheck()
    {
/*        JSObject jsObject = (JSObject) webview.getEngine().executeScript("editor.getViewport();");
        int fromLine = (int) jsObject.getMember("from");
        int toLine = (int) jsObject.getMember("to");
        spellChecker.onViewPortChanged(null, fromLine, toLine);    */
    }

    /**
     * returns the current code in the editor
     */
    @Override
    public String getCode()
    {
        return code.getValue();
    }

    @Override
    public ReadOnlyObjectProperty<String> codeProperty()
    {
        return code.getReadOnlyProperty();
    }

    @Override
    public void undo()
    {
        CodeVersion codeVersion = undoRedoManager.undo();
        String undoCode = codeVersion.getCode();
        webview.getEngine().executeScript("editor.setValue('" + StringEscapeUtils.escapeJavaScript(undoCode) + "');");
        EditorPosition cursorPos = codeVersion.getCursorPosition();
        setEditorCursorPosition(cursorPos);
    }

    @Override
    public void redo()
    {
        CodeVersion codeVersion = undoRedoManager.redo();
        String undoCode = codeVersion.getCode();
        webview.getEngine().executeScript("editor.setValue('" + StringEscapeUtils.escapeJavaScript(undoCode) + "');");
        EditorPosition cursorPos = codeVersion.getCursorPosition();
        setEditorCursorPosition(cursorPos);
    }

    public BooleanProperty canUndoProperty()
    {
        return canUndo;
    }

    public BooleanProperty canRedoProperty()
    {
        return canRedo;
    }

    private void removeTags()
    {
        EditorRange selectedRange = getSelection();
        String selectedText = selectedRange.getSelection();
        selectedText = selectedText.replaceAll("<(.*?)>", "");
        replaceSelection(selectedText);
    }

    public EditorToken getTokenAt(EditorPosition pos)
    {
        JSObject jdoc = (JSObject) webview.getEngine().executeScript("editor.getTokenAt({line:" + pos.getLine() + ",ch:" + pos.getColumn() +"});");
        return new EditorToken((int)jdoc.getMember("start"),
                (int)jdoc.getMember("end"), (String)jdoc.getMember("string"), (String)jdoc.getMember("type"));
    }

    public EditorToken getTokenAt(int line, int column)
    {
        JSObject jdoc = (JSObject) webview.getEngine().executeScript("editor.getTokenAt({line:" + line + ",ch:" + column +"});");
        return new EditorToken((int)jdoc.getMember("start"),
                (int)jdoc.getMember("end"), (String)jdoc.getMember("string"), (String)jdoc.getMember("type"));
    }

    @Override
    public EditorPosition getEditorCursorPosition()
    {
        JSObject jdoc = (JSObject) webview.getEngine().executeScript("editor.getCursor();");
        return new EditorPosition((int)jdoc.getMember("line"),
                (int)jdoc.getMember("ch"));
    }

    @Override
    public int getEditorCursorIndex()
    {
        EditorPosition position = getEditorCursorPosition();
        return getIndexFromPosition(position);
    }

    @Override
    public void setEditorCursorPosition(EditorPosition position)
    {
        webview.getEngine().executeScript("editor.setCursor(" + position.toJson() + ");");
    }

    @Override
    public void select(int fromIndex, int toIndex)
    {
        JSObject fromPos = (JSObject) webview.getEngine().executeScript("editor.posFromIndex(" + fromIndex + ");");
        JSObject toPos = (JSObject) webview.getEngine().executeScript("editor.posFromIndex(" + toIndex + ");");
        String json = "editor.setSelection({line:" + fromPos.getMember("line") + ", ch:" + fromPos.getMember("ch") + "}, " +
                "{line:" + toPos.getMember("line") + ", ch:" + toPos.getMember("ch") + "}, {scroll: true, bias: 1})";
        webview.getEngine().executeScript(json);
    }

    @Override
    public EditorRange getSelection()
    {
        JSObject jdoc = (JSObject) webview.getEngine().executeScript("editor.getCursor('from');");
        EditorPosition from = new EditorPosition((int)jdoc.getMember("line"),
                (int)jdoc.getMember("ch"));
        jdoc = (JSObject) webview.getEngine().executeScript("editor.getCursor('to');");
        EditorPosition to = new EditorPosition((int)jdoc.getMember("line"),
                (int)jdoc.getMember("ch"));
        String selection = (String) webview.getEngine().executeScript("editor.getSelection();");
        return new EditorRange(from, to, selection);
    }

    public String getRange(EditorPosition from, EditorPosition to)
    {
        return (String)webview.getEngine().executeScript("editor.getRange(" +
                from.toJson() + "," +
                to.toJson() + ");");
    }

    public int getLastLine()
    {
        return (int)webview.getEngine().executeScript("editor.lastLine();");
    }

    public void replaceRange(String replacement, EditorPosition from, EditorPosition to)
    {
        //doc.replaceRange(replacement: string, from: {line, ch}, to: {line, ch}, ?origin: string)
        webview.getEngine().executeScript("editor.replaceRange('" + StringEscapeUtils.escapeJavaScript(replacement) + "'," +
                from.toJson() + "," +
                to.toJson() + ");");
    }

    @Override
    public void insertAt(String replacement, EditorPosition pos)
    {
        //doc.replaceRange(replacement: string, from: {line, ch}, to: {line, ch}, ?origin: string)
        webview.getEngine().executeScript("editor.replaceRange('" + StringEscapeUtils.escapeJavaScript(replacement) + "'," +
                pos.toJson() + ");");
    }

    @Override
    public void replaceSelection(String replacement)
    {
        EditorRange range = getSelection();
        //doc.replaceRange(replacement: string, from: {line, ch}, to: {line, ch}, ?origin: string)
        webview.getEngine().executeScript("editor.replaceRange('" + StringEscapeUtils.escapeJavaScript(replacement) + "'," +
                range.getFrom().toJson() + "," +
                range.getTo().toJson() + ");");
    }

    public int getIndexFromPosition(EditorPosition pos)
    {
        return (int)webview.getEngine().executeScript("editor.indexFromPos(" + pos.toJson() + ");");
    }

    public int getLineLength(int lineNumber)
    {
        return (int)webview.getEngine().executeScript("editor.getLine(" + lineNumber + ").length;");
    }

    @Override
    public void scroll(int delta)
    {
        JSObject jdoc = (JSObject) webview.getEngine().executeScript("editor.getScrollInfo();");
        int top = (int)jdoc.getMember("top");
        int newTop = top + delta;
        webview.getEngine().executeScript("editor.scrollTo(null, " + newTop + ");");
    }

    @Override
    public void scrollTo(EditorPosition pos)
    {
        double halfHeight = getHeight() / 2;
        webview.getEngine().executeScript("editor.scrollIntoView(" + pos.toJson() + "," + halfHeight + ");");
    }

    @Override
    public void scrollTo(int index)
    {
        double halfHeight = getHeight() / 2;
        JSObject jsObject = (JSObject)webview.getEngine().executeScript("editor.posFromIndex(" + index + ");");
        webview.getEngine().executeScript("editor.scrollIntoView({line:" + jsObject.getMember("line") + ", ch:" + jsObject.getMember("ch") + "}," + halfHeight + ");");
    }

    public void setCodeEditorSize(double width, double height)
    {
        webview.getEngine().executeScript("editor.setSize(" + width + "," + height + ");");
    }

    public Worker.State getState()
    {
        return state.get();
    }

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

    private boolean keyCodeIsModifierKey(int keyCode)
    {
        return keyCode == 16 || keyCode == 17 || keyCode == 18 || keyCode == 157 || keyCode == 524 || keyCode == 65406 ||
                keyCode == 768;
    }

    @Override
    public void setContextMenu(ContextMenu contextMenu)
    {
        this.contextMenu = contextMenu;
    }

    @Override
    public void requestFocus()
    {
        webview.requestFocus();
    }
}