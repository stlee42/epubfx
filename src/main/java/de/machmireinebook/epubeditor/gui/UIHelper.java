package de.machmireinebook.epubeditor.gui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.BeanFactory;
import de.machmireinebook.epubeditor.EpubEditorConfiguration;

/**
 * User: mjungierek
 * Date: 21.12.2014
 * Time: 20:11
 */
public class UIHelper
{
    private static final Logger logger = Logger.getLogger(UIHelper.class);

    public Stage createChooserWindow()
    {
        Stage chooserWindowStage = new Stage(StageStyle.UTILITY);
        try
        {
            FXMLLoader loader = new FXMLLoader(UIHelper.class.getResource("/ChooserWindow.fxml"), null, new JavaFXBuilderFactory(),
                    type -> BeanFactory.getInstance().getBean(type));
            Pane root = loader.load();
            Scene scene = new Scene(root);

            ChooserWindowController chooserWindowController = ChooserWindowController.getInstance();
            chooserWindowController.setChooserWindow(chooserWindowStage);

            chooserWindowStage.setScene(scene);
            EpubEditorConfiguration configuration =  BeanFactory.getInstance().getBean(EpubEditorConfiguration.class);
            chooserWindowStage.initOwner(configuration.getMainWindow());
            chooserWindowStage.initModality(Modality.APPLICATION_MODAL);
            chooserWindowStage.setTitle("Auswahl");
        }
        catch (IOException e)
        {
            logger.error("cannot open chooser window", e);
        }
        return chooserWindowStage;
    }


}
