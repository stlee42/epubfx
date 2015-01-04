package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:12
 */

import com.sun.webkit.dom.KeyboardEventImpl;
import com.sun.webkit.dom.MouseEventImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
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
    public static final Logger logger = Logger.getLogger(AbstractCodeEditor.class);

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
        }
    }

    /**
     * Create a new code editor.
     *
     * @param editingCode the initial code to be edited in the code editor.
     */
    AbstractCodeEditor()
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

        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>()
        {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue)
            {
                if (newValue.equals(Worker.State.SUCCEEDED))
                {
                    Document document = engine.getDocument();

                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("onChangeListener", new OnChangeListener());

                    Element documentElement = document.getDocumentElement();
                    ((EventTarget) documentElement).addEventListener("keyup", evt ->
                    {
                        boolean isCtrlPressed = ((KeyboardEventImpl) evt).getCtrlKey();
                        int keyCode = ((KeyboardEventImpl) evt).getKeyCode();
                        String keyIdentifier = ((KeyboardEventImpl) evt).getKeyIdentifier();

                        logger.debug("key up in content editor: " + evt.getTarget() + " isCtrl " + isCtrlPressed + ", keyCode " + keyCode + " keyId " + keyIdentifier);

                        if (isCtrlPressed && (keyCode == 90 || keyCode == 89) || keyCodeIsModifierKey(keyCode))
                        {
                            logger.debug("Ctrl-Z, Ctrl-Y key up wird in keyup ignoriert. da bereits verarbeitet");
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
                    }, false);

                    ((EventTarget) documentElement).addEventListener("contextmenu", evt ->
                    {
                        logger.debug("contextmenu event aufgefangen " + evt);
                        evt.preventDefault();
                        contextMenu.setImpl_showRelativeToWindow(true);
                        contextMenu.show(webview, ((MouseEventImpl) evt).getScreenX(), ((MouseEventImpl) evt).getScreenY());
                    }, false);


                    webview.setOnScroll(new EventHandler<ScrollEvent>()
                    {
                        @Override
                        public void handle(ScrollEvent event)
                        {
                            Double delta = event.getDeltaY() * -1;
                            scroll(delta.intValue());
                        }
                    });
                    state.setValue(Worker.State.SUCCEEDED);
                }
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

    public void setEditorCursorPosition(EditorPosition position)
    {
        webview.getEngine().executeScript("editor.setCursor(" + position.toJson() + ");");
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
}