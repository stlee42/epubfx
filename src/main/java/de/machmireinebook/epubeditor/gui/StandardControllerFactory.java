package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Named;
import javax.inject.Singleton;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import lombok.Builder;

@Builder
public class StandardControllerFactory {
    private static final Logger logger = Logger.getLogger(StandardControllerFactory.class);

    private Stage stage;
    private ObjectProperty<Book> currentBookProperty;

    public void createAndOpenStandardController(String fxmlFile, Class<? extends StandardController> controllerClass) {
        Stage windowStage = createStandardController(fxmlFile, controllerClass);
        windowStage.show();
    }

    public Stage createStandardController(String fxmlFile, Class<? extends StandardController> controllerClass)
    {
        Method staticMethod;
        StandardController controller;
        try
        {
            staticMethod = controllerClass.getMethod("getInstance");
            controller = (StandardController) staticMethod.invoke(controllerClass);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            logger.error("", e);
            return null;
        }

        Stage windowStage = null;
        if (controller == null)
        {
            try
            {
                windowStage = new Stage(StageStyle.UTILITY);

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile), null, new JavaFXBuilderFactory(),
                        type -> BeanFactory.getInstance().getBean(type));

                Pane root = loader.load();
                Scene scene = new Scene(root);
                windowStage.setScene(scene);
                windowStage.initOwner(stage);
                windowStage.initModality(Modality.APPLICATION_MODAL);

                controller = (StandardController) staticMethod.invoke(controllerClass);
                controller.currentBookProperty().bind(currentBookProperty);
                controller.setStage(windowStage);
            }
            catch (IOException | IllegalAccessException | InvocationTargetException e)
            {
                logger.error("", e);
            }
        }
        else
        {
            windowStage = controller.getStage();
        }
        return windowStage;
    }
}
