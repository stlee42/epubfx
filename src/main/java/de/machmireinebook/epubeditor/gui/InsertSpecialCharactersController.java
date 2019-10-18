package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.javafx.cells.TextGridCell;
import de.machmireinebook.epubeditor.editor.EditorTabManager;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

/**
 * User: Michail Jungierek
 * Date: 21.05.2018
 * Time: 20:03
 */
public class InsertSpecialCharactersController implements StandardController
{

    @FXML
    private GridView<String> gridViewSymbols;
    @FXML
    private Label unicodeNameLabel;
    @FXML
    private TextField htmlCodeTextField;
    @FXML
    private GridView<String> gridViewGreek;
    @FXML
    private GridView<String> gridViewMath;
    @FXML
    private GridView<String> gridViewUnits;
    private ObjectProperty<Book> currentBook = new SimpleObjectProperty<>(this, "currentBook");
    private Stage stage;

    private TextGridCell selectedGridCell;
    private String currentCharacter;

    @Inject
    private EditorTabManager editorTabManager;

    private static InsertSpecialCharactersController instance;
    private static final List<String> GENERAL_SYMBOLS = Arrays.asList("‚","‘","’","„","“","”","‹","›","«","»","—","–","§","¶","†","‡",
            "&","<",">","©","®","™","←","→","•","·","…",
            "¿","¡","¨","´","¸","ˆ","˜",
            //latin letters
            "À","Á","Â","Ã","Ä","Å","Æ","Ç","È","É","Ê","Ë","Ì","Í",
            "Î","Ï","Ð","Ñ","Ò","Ó","Ô","Õ","Ö","Ø","Œ","Š","Ù","Ú","Û","Ü","Ý","Ÿ","Þ","ß","à","á","â","ã","ä","å","æ",
            "ç","è","é","ê","ë","ì","í","î","ï","ð","ñ","ò","ó","ô","õ","ö","ø","œ","š","ù","ú","û","ü","ý","ÿ","þ","ª","º"
    );

    private static final List<String> GREEK_LETTERS = Arrays.asList("Α","α","Β","β","Χ","χ","Δ","δ","Ε","ε","Η","η","Γ",
            "γ","Ι","ι","Κ","κ","Λ","λ","Μ","μ","Ν","ν","Ω","ω","Ο","ο","Φ","φ","π","″","′","Ψ","ψ","Ρ","ρ","Σ","σ","Τ",
            "τ","Θ","θ","Υ","υ","Ξ","ξ","Ζ","ζ");

    //  ,
    private static final List<String> MATH_SYMBOLS = Arrays.asList("±","−","×","÷","¼","½","¾","⅓","⅔","⅛","⅜","⅝","⅞",
            "∞","ℵ","∧","∨","∩","∪","≅","↵","⇓","⇑","↓","↑",
        "∅","≡","∃","ƒ","∀","⁄","⇔","↔","ℑ","∫","∈","⇐","⇒","〈","〉","⌈","⌉","≤","≥","⌊","⌋",
        "∗","◊","¯","∇","≠","∋","¬","∉","⊄","‾","⊕","⊗","∂","‰","⊥","ϖ","∏","∝","√","ℜ","⋅","ς","∼","⊂","⊃","⊆","⊇",
        "∑","¹","²","³","∴","ϑ","ϒ","℘"
    );

    private static final List<String> UNIT_AND_CURRENCY_SIGNS = Arrays.asList("°", "′", "″","µ","¤","¥","¢","£","€"
    );

    public static InsertSpecialCharactersController getInstance()
    {
        return instance;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        initGridView(gridViewSymbols, GENERAL_SYMBOLS);
        initGridView(gridViewGreek, GREEK_LETTERS);
        initGridView(gridViewMath, MATH_SYMBOLS);
        initGridView(gridViewUnits, UNIT_AND_CURRENCY_SIGNS);

        instance = this;
    }

    private void initGridView(GridView<String> gridView, List<String> symbolList) {
        gridView.setVerticalCellSpacing(5);
        gridView.setHorizontalCellSpacing(5);
        gridView.setCellFactory(grid -> {
            TextGridCell gridCell = new TextGridCell();
            gridCell.setOnMouseClicked(mouseEvent -> {
                currentCharacter = gridCell.getText();
                int codePoint = Character.codePointAt(currentCharacter, 0);
                unicodeNameLabel.setText(Character.getName(codePoint));
                htmlCodeTextField.setText("&#" + codePoint + ";");

                gridCell.setRectangleFill(Color.DODGERBLUE);
                if (selectedGridCell != null) {
                    selectedGridCell.setRectangleFill(Color.WHITE);
                }
                selectedGridCell = gridCell;
            });

            return gridCell;
        });
        gridView.getItems().addAll(symbolList);
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
        return currentBook;
    }

    public void onOkAction(ActionEvent actionEvent) {
        editorTabManager.insertAtCursorPositionOrReplaceSelection(currentCharacter);
        //dialog stays open to insert more values
    }

    public void onCancelAction(ActionEvent actionEvent) {
        stage.close();
    }
}
