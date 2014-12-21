package de.machmireinebook.epubeditor.manager;

import java.io.IOException;

import de.machmireinebook.commons.cdi.BeanFactory;
import de.machmireinebook.commons.lang.NumberUtils;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.Resource;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.Dialogs;

/**
 * User: mjungierek
 * Date: 20.12.2014
 * Time: 12:56
 */
public class ImageViewerManager
{
    public static final Logger logger = Logger.getLogger(ImageViewerManager.class);

    @FXML
    private ImageView imageView;
    @FXML
    private Label imagePropertiesLabel;

    private TabPane tabPane;
    private Book book;

    public void setTabPane(TabPane tabPane)
    {
        this.tabPane = tabPane;
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    }

    public void setBook(Book book)
    {
        this.book = book;
    }

    public void openImageFile(Resource resource)
    {
        Tab tab = new Tab();
        tab.setClosable(true);
        if (resource == null)
        {
            Dialogs.create()
                    .owner(tabPane)
                    .title("Datei nicht vorhanden")
                    .message("Die angeforderte Datei ist nicht vorhanden und kann deshalb nicht geöffnet werden.")
                    .showError();
            return;
        }
        tab.setText(resource.getFileName());

        ScrollPane pane;
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/image_view.fxml"), null, new JavaFXBuilderFactory(),
                    type -> BeanFactory.getInstance().getBean(type));
            loader.setController(this);
            pane = loader.load();

            ImageResource imageResource = (ImageResource) resource;
            Image image = imageResource.getAsNativeFormat();
            imageView.setImage(image);
            imageView.setFitHeight(-1);
            imageView.setFitWidth(-1);

            String sizeInKB = NumberUtils.formatDouble(Math.round(resource.getSize() / 1024.0 * 100) / 100.0);
            imagePropertiesLabel.setText(((Double) image.getWidth()).intValue() + "×" + ((Double) image.getHeight()).intValue() + " px | "
                    + sizeInKB + " KB | " + imageResource.getImageInfo().getBitsPerPixel() + " bpp");

            tab.setContent(pane);
            tab.setUserData(resource);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        }
        catch (IOException e)
        {
            Dialogs.create()
                    .owner(tabPane)
                    .title("Bild anzeigen")
                    .masthead(null)
                    .message("Fehler beim Öffnen eines Bildes.")
                    .showException(e);
            logger.error(e);
        }
    }

    public void reset()
    {


    }
}
