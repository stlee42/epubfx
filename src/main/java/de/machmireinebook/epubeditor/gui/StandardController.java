package de.machmireinebook.epubeditor.gui;

import de.machmireinebook.epubeditor.epublib.domain.Book;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 * User: mjungierek
 * Date: 31.12.13
 * Time: 22:25
 */
public interface StandardController extends Initializable
{
    void setStage(Stage stage);
    Stage getStage();
    ObjectProperty<Book> currentBookProperty();
}
