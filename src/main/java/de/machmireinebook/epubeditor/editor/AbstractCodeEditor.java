package de.machmireinebook.epubeditor.editor;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:12
 */

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a
 * CodeMirror code editor in a WebView.
 * <p>
 * See http://codemirror.net for more information on using the codemirror editor.
 */
public abstract class AbstractCodeEditor extends AnchorPane implements CodeEditor
{
    /**
     * a webview used to encapsulate the CodeMirror JavaScript.
     */
    private final WebView webview = new WebView();

    private UndoRedoManager<CodeVersion> undoRedoManager = new UndoRedoManager<>();

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
    }

    public WebView getWebview()
    {
        return webview;
    }

    /**
     * applies the editing template to the editing code to create the html+javascript source for a code editor.
     */
    private String applyEditingTemplate()
    {
        return getEditingTemplate();
    }

    /**
     * sets the current code in the editor and creates an editing snapshot of the code which can be reverted to.
     */
    public void setCode(String newCode)
    {
        CodeVersion version = new CodeVersion(newCode, getEditorCursorPosition());
        undoRedoManager.saveVersion(version);
        webview.getEngine().executeScript("editor.setValue('" + StringEscapeUtils.escapeJavaScript(newCode) + "');");
    }

    /**
     * returns the current code in the editor and updates an editing snapshot of the code which can be reverted to.
     */
    public String getCode()
    {
        String code = (String) webview.getEngine().executeScript("editor.getValue();");
        CodeVersion version = new CodeVersion(code, getEditorCursorPosition());
        undoRedoManager.saveVersion(version);
        return code;
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

    public EditorPosition getEditorCursorPosition()
    {
        JSObject jdoc = (JSObject) webview.getEngine().executeScript("editor.getCursor();");
        return new EditorPosition((int)jdoc.getMember("line"),
                (int)jdoc.getMember("ch"));
    }

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

    public void insertAt(String replacement, EditorPosition pos)
    {
        //doc.replaceRange(replacement: string, from: {line, ch}, to: {line, ch}, ?origin: string)
        webview.getEngine().executeScript("editor.replaceRange('" + StringEscapeUtils.escapeJavaScript(replacement) + "'," +
                pos.toJson() + ");");
    }

    public void replaceSelection(String replacement)
    {
        //doc.replaceRange(replacement: string, from: {line, ch}, to: {line, ch}, ?origin: string)
        webview.getEngine().executeScript("editor.replaceRange('" + StringEscapeUtils.escapeJavaScript(replacement) + "');");
    }

    public int getIndexFromPosition(EditorPosition pos)
    {
        return (int)webview.getEngine().executeScript("editor.indexFromPos(" + pos.toJson() + ");");
    }

    public int getLineLength(int lineNumber)
    {
        return (int)webview.getEngine().executeScript("editor.getLine(" + lineNumber + ").length;");
    }

    public void scroll(int delta)
    {
        JSObject jdoc = (JSObject) webview.getEngine().executeScript("editor.getScrollInfo();");
        int top = (int)jdoc.getMember("top");
        int newTop = top + delta;
        webview.getEngine().executeScript("editor.scrollTo(null, " + newTop + ");");
    }

    public void scrollTo(EditorPosition pos)
    {
        double halfHeight = getHeight() / 2;
        webview.getEngine().executeScript("editor.scrollIntoView(" + pos.toJson() + "," + halfHeight + ");");
    }

    public void setCodeEditorSize(double width, double height)
    {
        webview.getEngine().executeScript("editor.setSize(" + width + "," + height + ");");
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

        webview.getEngine().loadContent(applyEditingTemplate());

        this.getChildren().add(webview);
    }

    public UndoRedoManager<CodeVersion> getUndoRedoManager()
    {
        return undoRedoManager;
    }
}