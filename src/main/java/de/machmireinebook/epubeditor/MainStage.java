package de.machmireinebook.epubeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.apache.commons.lang3.BooleanUtils;

import de.machmireinebook.epubeditor.gui.UIHelper;

/**
 * User: mjungierek
 * Date: 04.01.2016
 * Time: 17:37
 */
public class MainStage extends Stage
{
    public void init(StageStyle style, Image applicationIcon) throws IOException
    {
        initStyle(style);

        FXMLLoader loader = new FXMLLoader(UIHelper.class.getResource("/main.fxml"), null, new JavaFXBuilderFactory(),
                type -> BeanFactory.getInstance().getBean(type));
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);

        if (getClass().getResource("/application.css") != null)
        {
            scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
        }

        setScene(scene);
        //set icon of the application
        getIcons().add(applicationIcon);

        if (new File("application.properties").exists())
        {
            FileInputStream fis = new FileInputStream("application.properties");
            Properties prop = new Properties();
            prop.load(fis);
            Boolean isFullscreen = BooleanUtils.toBoolean((String) prop.get("isFullscreen"));
            Double width = Double.valueOf((String) prop.get("width"));
            Double height = Double.valueOf((String) prop.get("height"));
            Double x = Double.valueOf((String) prop.get("x"));
            Double y = Double.valueOf((String) prop.get("y"));

            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            if (isFullscreen)
            {
                setFullScreen(true);
            }
            else if (x >= 0.0 && y >= 0.0 && x < primaryScreenBounds.getWidth() - 100 && y < primaryScreenBounds.getHeight() - 100) // verhindern dass Fenster ausserhalb des sichtbaren Bereichs geöffnet wird
            {
                setWidth(width);
                setHeight(height);
                setX(x);
                setY(y);
            }
        }
    }
}
