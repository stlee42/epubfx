package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.manager.EditorTabManager;

import org.apache.commons.io.IOUtils;
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
    private Spinner<Integer> numberColumnsSpinner;
    @FXML
    private Spinner<Integer> numberRowsSpinner;
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
    private ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>();

    @Inject
    private EditorTabManager editorManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        numberColumnsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 3));
        numberRowsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 2));
        headerCheckBox.setSelected(true);
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
        return currentBookProperty;
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
        logger.info("insert table");
        try
        {
            String tableSnippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/table.html"), "UTF-8");
            String columnHeaderSnippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/table-column-header.html"), "UTF-8");
            String tableRowSnippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/table-row.html"), "UTF-8");
            String tableColumnDataSnippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/table-column-data.html"), "UTF-8");

            if (headerCheckBox.isSelected()) {
                StringBuilder columnHeaderInsertsBuilder = new StringBuilder();
                for (int i = 0; i < numberColumnsSpinner.getValue(); i++)
                {
                    columnHeaderInsertsBuilder.append(columnHeaderSnippet.replace("${column-name}", "Column " + i));
                }
                String columnHeaderInserts = columnHeaderInsertsBuilder.toString();
                tableSnippet = tableSnippet.replace("${column-header}", columnHeaderInserts);
            } else {
                tableSnippet = tableSnippet.replaceAll("<thead>(.*)</thead>", "");
            }

            StringBuilder tableRows = new StringBuilder();
            for (int i = 0; i < numberRowsSpinner.getValue(); i++)
            {
                tableRows.append(tableRowSnippet);
                StringBuilder tableColumns = new StringBuilder();
                for (int j = 0; j < numberColumnsSpinner.getValue(); j++)
                {
                    tableColumns.append(tableColumnDataSnippet.replace("${column-content}", "column " + j + ", row " + i));
                }
                tableRows = new StringBuilder(tableRows.toString().replace("${columns}", tableColumns.toString()));
            }
            tableSnippet = tableSnippet.replace("${table-rows}", tableRows);

            CodeEditor editor = editorManager.getCurrentEditor();
            Integer cursorPosition = editor.getAbsoluteCursorPosition();
            editor.insertAt(cursorPosition, tableSnippet);
            editorManager.refreshPreview();
        }
        catch (IOException e)
        {
            logger.error("error while creating snippet for inserting table", e);
        }


        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }
}
