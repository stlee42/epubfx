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
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.helger.css.propertyvalue.CCSSValue;

import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.javafx.FXUtils;
import de.machmireinebook.epubeditor.manager.EditorTabManager;

/**
 * User: Michail Jungierek
 * Date: 06.07.2019
 * Time: 15:27
 */
public class InsertTableController implements Initializable, StandardController
{
    private static final Logger logger = Logger.getLogger(InsertTableController.class);
    public TextField cellPaddingTopTextField;
    public TextField cellPaddingRightTextField;
    public TextField cellPaddingBottomTextField;
    public TextField cellPaddingLeftTextField;
    public ToggleButton linkPaddingValuesButton;
    public TextField cellStyleTextField;
    @FXML
    private TextField captionTextField;
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
        backgroundColorPicker.setValue(Color.LIGHTGRAY);

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
        borderStyleComboBox.getSelectionModel().select(CCSSValue.SOLID);
        borderColorPicker.setValue(Color.BLACK);
        borderCollapsingComboBox.setItems(FXCollections.observableArrayList(CCSSValue.SEPARATE,
                CCSSValue.COLLAPSE));
        borderCollapsingComboBox.getSelectionModel().select(CCSSValue.COLLAPSE);

        cellPaddingRightTextField.disableProperty().bind(linkPaddingValuesButton.selectedProperty());
        cellPaddingBottomTextField.disableProperty().bind(linkPaddingValuesButton.selectedProperty());
        cellPaddingLeftTextField.disableProperty().bind(linkPaddingValuesButton.selectedProperty());
        cellPaddingTopTextField.setText("5");
        linkPaddingValuesButton.setSelected(true);

        setStandardStyle();
        setStandardCellStyle();

        instance = this;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
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

    public void onOkAction(ActionEvent actionEvent)
    {
        logger.info("insert table");
        try
        {
            String tableSnippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/table.html"), "UTF-8");
            String columnHeaderSnippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/table-column-header.html"), "UTF-8");
            String tableRowSnippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/table-row.html"), "UTF-8");
            String tableColumnDataSnippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/table-column-data.html"), "UTF-8");
            tableColumnDataSnippet.replace("${cell-style}", cellStyleTextField.getText());

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

            if (StringUtils.isNotEmpty(captionTextField.getText())) {
                tableSnippet = tableSnippet.replace("${caption}", "class=\"" + captionTextField.getText() + "\"");
            } else {
                tableSnippet = tableSnippet.replaceAll("<caption>(.*)</caption>", "");
            }

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

    private void setStandardStyle() {
        String style = "border-width: " + borderWidthTextField.getText() + "px;";
        style += "border-color: " + toHexString(borderColorPicker.getValue()) + ";";
        style += "border-style: " + borderStyleComboBox.getValue() + ";";
        style += "border-collapse: " + borderCollapsingComboBox.getValue() + ";";
        styleTextField.setText(style);
    }

    private void setStandardCellStyle() {
        String style = "border-width: " + borderWidthTextField.getText() + "px;";
        style += "border-color: " + toHexString(borderColorPicker.getValue()) + ";";
        style += "border-style: " + borderStyleComboBox.getValue() + ";";
        style += "border-collapse: " + borderCollapsingComboBox.getValue() + ";";
        style += "padding: " + cellPaddingTopTextField.getText() + ";";
        cellStyleTextField.setText(style);
    }

    public void changeStyle(ActionEvent actionEvent)
    {
        String currentStyle = styleTextField.getText();
        currentStyle = getBorderStyles(actionEvent, currentStyle);
        styleTextField.setText(currentStyle);
        String currentCellStyle = cellStyleTextField.getText();
        currentCellStyle = getBorderStyles(actionEvent, currentCellStyle);
        styleTextField.setText(currentCellStyle);
    }

    private String getBorderStyles(ActionEvent actionEvent, String currentStyle) {
        if (actionEvent.getSource() == borderWidthTextField) {
            String style = "border-width: " + borderWidthTextField.getText() + "px;";
            if (currentStyle.contains("border-width")) {
                currentStyle = currentStyle.replaceAll("border-width(\\w*):(.*);", style);
            }
            else {
                currentStyle += style;
            }
        }
        if (actionEvent.getSource() == borderColorPicker) {
            String style = "border-color: " + toHexString(borderColorPicker.getValue()) + ";";
            if (currentStyle.contains("border-color")) {
                currentStyle = currentStyle.replaceAll("border-color(\\w*):(.*);", style);
            }
            else {
                currentStyle += style;
            }
        }
        if (actionEvent.getSource() == borderStyleComboBox) {
            String style = "border-style: " + borderStyleComboBox.getValue() + ";";
            if (currentStyle.contains("border-style")) {
                currentStyle = currentStyle.replaceAll("border-style(\\w*):(.*);", style);
            }
            else {
                currentStyle += style;
            }
        }
        if (actionEvent.getSource() == borderCollapsingComboBox) {
            String style = "border-collapse: " + borderCollapsingComboBox.getValue() + ";";
            if (currentStyle.contains("border-collapse")) {
                currentStyle = currentStyle.replaceAll("border-collapse(\\w*):(.*);", style);
            }
            else {
                currentStyle += style;
            }
        }
        return currentStyle;
    }

    public void changeCellStyle(ActionEvent actionEvent)
    {
        String currentCellStyle = cellStyleTextField.getText();
        currentCellStyle = getBorderStyles(actionEvent, currentCellStyle);

        if (linkPaddingValuesButton.isSelected() && actionEvent.getSource() == cellPaddingTopTextField) {
            String paddingStyle = "padding: " + cellPaddingTopTextField.getText() + ";";
            if (currentCellStyle.contains("padding")) {
                currentCellStyle = currentCellStyle.replaceAll("padding(\\w*):(.*);", paddingStyle);
            }
            else {
                currentCellStyle += paddingStyle;
            }
            //remove single padding styles if exists
            if (currentCellStyle.contains("padding-top")) {
                currentCellStyle = currentCellStyle.replaceAll("padding-top(\\w*):(.*);", "");
            }
            if (currentCellStyle.contains("padding-right")) {
                currentCellStyle = currentCellStyle.replaceAll("padding-right(\\w*):(.*);", "");
            }
            if (currentCellStyle.contains("padding-bottom")) {
                currentCellStyle = currentCellStyle.replaceAll("padding-bottom(\\w*):(.*);", "");
            }
            if (currentCellStyle.contains("padding-left")) {
                currentCellStyle = currentCellStyle.replaceAll("padding-left(\\w*):(.*);", "");
            }
        }
        else if (!linkPaddingValuesButton.isSelected()) {
            //remove compact padding style if exists
            if (currentCellStyle.contains("padding")) {
                currentCellStyle = currentCellStyle.replaceAll("padding(\\w*):(.*);", "");
            }
            String paddingStyle = "padding-top: " + cellPaddingTopTextField.getText() + ";";
            if (currentCellStyle.contains("padding-top")) {
                currentCellStyle = currentCellStyle.replaceAll("padding-top(\\w*):(.*);", "");
            }
            else {
                currentCellStyle += paddingStyle;
            }
            paddingStyle = "padding-right: " + cellPaddingRightTextField.getText() + ";";
            if (currentCellStyle.contains("padding-top")) {
                currentCellStyle = currentCellStyle.replaceAll("padding-right(\\w*):(.*);", "");
            }
            else {
                currentCellStyle += paddingStyle;
            }
            paddingStyle = "padding-bottom: " + cellPaddingBottomTextField.getText() + ";";
            if (currentCellStyle.contains("padding-top")) {
                currentCellStyle = currentCellStyle.replaceAll("padding-bottom(\\w*):(.*);", "");
            }
            else {
                currentCellStyle += paddingStyle;
            }
            paddingStyle = "padding-left: " + cellPaddingLeftTextField.getText() + ";";
            if (currentCellStyle.contains("padding-top")) {
                currentCellStyle = currentCellStyle.replaceAll("padding-left(\\w*):(.*);", "");
            }
            else {
                currentCellStyle += paddingStyle;
            }
        }

        styleTextField.setText(currentCellStyle);
    }

    // Helper method
    private String format(double val) {
        String in = Integer.toHexString((int) Math.round(val * 255));
        return in.length() == 1 ? "0" + in : in;
    }

    private String toHexString(Color value) {
        return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()) + format(value.getOpacity()))
                .toUpperCase();
    }

    public void linkPaddingValues(ActionEvent actionEvent) {
        if (linkPaddingValuesButton.isSelected()) {
            cellPaddingTopTextField.setPromptText("all");
            cellPaddingRightTextField.textProperty().bind(cellPaddingTopTextField.textProperty());
            cellPaddingBottomTextField.textProperty().bind(cellPaddingTopTextField.textProperty());
            cellPaddingLeftTextField.textProperty().bind(cellPaddingTopTextField.textProperty());
            linkPaddingValuesButton.setGraphic(FXUtils.getIcon("/icons/icons8_Link_96px.png", 12));
        } else {
            cellPaddingTopTextField.setPromptText("top");
            cellPaddingRightTextField.textProperty().unbind();
            cellPaddingBottomTextField.textProperty().unbind();
            cellPaddingLeftTextField.textProperty().unbind();
            linkPaddingValuesButton.setGraphic(FXUtils.getIcon("/icons/icons8_Broken_Link_96px.png", 12));
        }
    }
}
