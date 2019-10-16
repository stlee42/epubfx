package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

import de.jensd.fx.glyphs.GlyphIcons;
import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.javafx.cells.TextGridCell;
import de.machmireinebook.epubeditor.manager.EditorTabManager;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.cell.ColorGridCell;

/**
 * User: Michail Jungierek
 * Date: 21.05.2018
 * Time: 20:03
 */
public class InsertSpecialCharactersController implements StandardController
{

    public GridView<String> gridView;
    public Label unicodeNameLabel;
    public TextField htmlCodeTextField;
    private ObjectProperty<Book> currentBook = new SimpleObjectProperty<>(this, "currentBook");
    private Stage stage;

    private String currentCharacter;

    @Inject
    private EditorTabManager editorTabManager;

    private static InsertSpecialCharactersController instance;
    private static final List<String> CHARACTERS = Arrays.asList("‚","‘","’","„","“","”","‹","›","«","»","—","–","§","¶","†","‡",
            "&amp;","&lt;","&gt;","©","®","™","←","→","•","·","°",
            "±","−","×","÷","¼","½","¾","¡","¨","´","¸","ˆ","˜","À","Á","Â","Ã","Ä","Å","Æ","Ç","È","É","Ê","Ë","Ì","Í",
            "Î","Ï","Ð","Ñ","Ò","Ó","Ô","Õ","Ö","Ø","Œ","Š","Ù","Ú","Û","Ü","Ý","Ÿ","Þ","ß","à","á","â","ã","ä","å","æ",
            "ç","è","é","ê","ë","ì","í","î","ï","ð","ñ","ò","ó","ô","õ","ö","ø","œ","š","ù","ú","û","ü","ý","ÿ","þ","ª","º","∞");

    public static InsertSpecialCharactersController getInstance()
    {
        return instance;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        gridView.setVerticalCellSpacing(5);
        gridView.setHorizontalCellSpacing(5);

        gridView.setCellFactory(gridView -> {
            TextGridCell gridCell =new TextGridCell();
            gridCell.setOnMouseClicked(mouseEvent -> {
                currentCharacter = gridCell.getText();
                int codePoint = Character.codePointAt(currentCharacter, 0);
                unicodeNameLabel.setText(Character.getName(codePoint));
                htmlCodeTextField.setText("&#" + codePoint + ";");
            });
            return gridCell;
        });
        gridView.getItems().addAll(CHARACTERS);
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
