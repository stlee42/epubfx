package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.epublib.domain.Book;

/**
 * User: Michail Jungierek
 * Date: 13.07.2019
 * Time: 11:25
 */
public abstract class AbstractStandardController implements StandardController {

    protected Stage stage;
    protected ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>();

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public ObjectProperty<Book> currentBookProperty() {
        return currentBookProperty;
    }

    public void initialize(URL location, ResourceBundle resources) {
    }
}
