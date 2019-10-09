package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.epublib.domain.Book;

/**
 * User: Michail Jungierek
 * Date: 21.05.2018
 * Time: 20:03
 */
public class InsertSpecialCharactersController implements StandardController
{

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
