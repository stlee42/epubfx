package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.manager.EditorTabManager;

import org.apache.log4j.Logger;

/**
 * User: Michail Jungierek
 * Date: 06.07.2019
 * Time: 15:27
 */
public class InsertTableController implements Initializable, StandardController
{
    private static final Logger logger = Logger.getLogger(InsertTableController.class);

    @FXML
    private Spinner numberColumnsSpinner;
    @FXML
    private Spinner numberRowsSpinner;
    @FXML
    private CheckBox headerCheckBox;
    @FXML
    private ComboBox borderCollapsingComboBox;
    @FXML
    private TextField borderWithTextField;
    @FXML
    private ColorPicker borderColorPicker;
    @FXML
    private ComboBox borderStyleComboBox;
    @FXML
    private ColorPicker backgroundColorPicker;

    private Stage stage;
    private static InsertTableController instance;

    private String tableTopSnippet = "    <table>\n" +
            "      <tbody>\n" +
            "        <tr>\n";
    private String tdSnippet = "          <td>\n" +
            "            &#160;\n" +
            "          </td>\n";

    @Inject
    private EditorTabManager editorManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        instance = this;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnShown(event -> refresh());
    }

    @Override
    public Stage getStage()
    {
        return stage;
    }

    @Override
    public ObjectProperty<Book> currentBookProperty()
    {
        return null;
    }


    public static InsertTableController getInstance()
    {
        return instance;
    }

    public void refresh()
    {
    }

    public void onOkAction(ActionEvent actionEvent)
    {
        logger.info("insert media");
        String tableSnippet = "";

        CodeEditor editor = editorManager.getCurrentEditor();
        Integer cursorPosition = editor.getAbsoluteCursorPosition();
        editor.insertAt(cursorPosition, tableSnippet);
        editorManager.refreshPreview();

        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }
}
