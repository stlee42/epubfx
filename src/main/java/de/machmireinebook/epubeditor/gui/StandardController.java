package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.epublib.domain.Book;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
