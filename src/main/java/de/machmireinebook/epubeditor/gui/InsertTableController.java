package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.manager.EditorTabManager;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.helger.css.propertyvalue.CCSSValue;

/**
 * User: Michail Jungierek
 * Date: 06.07.2019
 * Time: 15:27
 */
public class InsertTableController implements Initializable, StandardController
{
    private static final Logger logger = Logger.getLogger(InsertTableController.class);
    @FXML
    private TextField styleClassTextField;
    @FXML
    private TextField styleTextField;
    @FXML
    private Spinner<Integer> numberColumnsSpinner;
    @FXML
    private Spinner<Integer> numberRowsSpinner;
    @FXML
    private CheckBox headerCheckBox;
    @FXML
    private ComboBox<String> borderCollapsingComboBox;
    @FXML
    private TextField borderWidthTextField;
    @FXML
    private ColorPicker borderColorPicker;
    @FXML
    private ComboBox<String> borderStyleComboBox;
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
        backgroundColorPicker.disableProperty().bind(headerCheckBox.selectedProperty().not());

        borderWidthTextField.setText("1");
        borderStyleComboBox.setItems(FXCollections.observableArrayList(CCSSValue.NONE,
                CCSSValue.HIDDEN,
                CCSSValue.DOTTED,
                CCSSValue.DASHED,
                CCSSValue.SOLID,
                CCSSValue.DOUBLE,
                CCSSValue.GROOVE,
                CCSSValue.RIDGE,
                CCSSValue.INSET,
                CCSSValue.OUTSET));
        borderCollapsingComboBox.setItems(FXCollections.observableArrayList(CCSSValue.SEPARATE,
                CCSSValue.COLLAPSE));

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
            tableSnippet = tableSnippet.replace("${style}", "style=\"" + styleTextField.getText() + "\"");
            tableSnippet = tableSnippet.replace("${style-class}", "class=\"" + styleClassTextField.getText() + "\"");

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

    public void changeStyle(ActionEvent actionEvent)
    {
        String currentStyle = styleTextField.getText();
        if (actionEvent.getSource() == borderWidthTextField) {
            String style = "border-width: " + borderWidthTextField.getText() + "px;";
            if (currentStyle.contains("border-width")) {
                currentStyle = currentStyle.replaceAll("border-width(\\w*):(.*);", style);
            } else {
                currentStyle += style;
            }
        }
        if (actionEvent.getSource() == borderColorPicker) {
            String style = "border-color: " + toHexString(borderColorPicker.getValue()) + ";";
            if (currentStyle.contains("border-color")) {
                currentStyle = currentStyle.replaceAll("border-color(\\w*):(.*);", style);
            } else {
                currentStyle += style;
            }
        }
        if (actionEvent.getSource() == borderStyleComboBox) {
            String style = "border-style: " + borderStyleComboBox.getValue() + ";";
            if (currentStyle.contains("border-style")) {
                currentStyle = currentStyle.replaceAll("border-style(\\w*):(.*);", style);
            } else {
                currentStyle += style;
            }
        }
        styleTextField.setText(currentStyle);
    }

    // Helper method
    private String format(double val) {
        String in = Integer.toHexString((int) Math.round(val * 255));
        return in.length() == 1 ? "0" + in : in;
    }

    public String toHexString(Color value) {
        return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()) + format(value.getOpacity()))
                .toUpperCase();
    }
}
