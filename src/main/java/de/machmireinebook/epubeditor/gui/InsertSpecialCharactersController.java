package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

import de.jensd.fx.glyphs.GlyphIcons;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.javafx.cells.TextGridCell;
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
    private ObjectProperty<Book> currentBook = new SimpleObjectProperty<>(this, "currentBook");
    private Stage stage;

    private static InsertSpecialCharactersController instance;

    public static InsertSpecialCharactersController getInstance()
    {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        gridView.setVerticalCellSpacing(5);
        gridView.setHorizontalCellSpacing(5);

        gridView.setCellFactory(gridView -> new TextGridCell());
        gridView.getItems().add("‚");
        gridView.getItems().add("‘");
        gridView.getItems().add("’");
        gridView.getItems().add("„");
        gridView.getItems().add("“");
        gridView.getItems().add("”");
        gridView.getItems().add("‹");
        gridView.getItems().add("›");
        gridView.getItems().add("«");
        gridView.getItems().add("»");
        gridView.getItems().add("‘");
        gridView.getItems().add("„");
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

    }

    public void onCancelAction(ActionEvent actionEvent) {

    }
}
