package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.machmireinebook.epubeditor.epublib.bookprocessor.CoverpageBookProcessor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.javafx.cells.ImageCellFactory;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jidefx.scene.control.searchable.TableViewSearchable;

/**
 * User: mjungierek
 * Date: 27.07.2014
 * Time: 15:30
 */
public class AddCoverController implements Initializable
{
    @FXML
    private ImageView imageView;
    @FXML
    private Label imageValuesLabel;
    @FXML
    private TableView<ImageResource> tableView;

    private static AddCoverController instance;
    private Stage stage;
    private Book book;
    private BookBrowserManager bookBrowserManager;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        TableViewSearchable<ImageResource> searchable = new TableViewSearchable<>(tableView);
        searchable.setCaseSensitive(false);

        TableColumn<ImageResource, String> tc = (TableColumn<ImageResource, String>) tableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("href"));
        tc.setSortable(true);

        TableColumn<ImageResource, Image> tc2 = (TableColumn<ImageResource, Image>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("cover"));
        tc2.setCellFactory(new ImageCellFactory<>(null, 100d));
        tc2.setSortable(false);

        tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ImageResource>()
        {
            @Override
            public void changed(ObservableValue<? extends ImageResource> observable, ImageResource oldValue, ImageResource newValue)
            {
                refreshImageView(newValue);
            }
        });
        tableView.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (event.getButton().equals(MouseButton.PRIMARY))
                {
                    if (event.getClickCount() == 2)
                    {
                        insertCover();
                    }
                }
            }
        });
        instance = this;
    }

    private void refresh()
    {
        List<ImageResource> imageResources = new ArrayList<>();
        List<Resource> resources = book.getResources().getResourcesByMediaTypes(new MediaType[]{
                MediaType.GIF,
                MediaType.PNG,
                MediaType.SVG,
                MediaType.JPG});
        for (Resource resource : resources)
        {
            imageResources.add((ImageResource)resource);
        }
        tableView.setItems(FXCollections.observableList(imageResources));
        tableView.getSelectionModel().select(0);
    }

    private void refreshImageView(ImageResource resource)
    {
        Image image = resource.asNativeFormat();
        imageView.setImage(image);
        imageValuesLabel.setText(image.getWidth() + "×" + image.getHeight() + " | " + resource.getSize());
    }


    public static AddCoverController getInstance()
    {
        return instance;
    }

    public void onOkAction(ActionEvent actionEvent)
    {
        insertCover();
    }

    private void insertCover()
    {
        ImageResource image = tableView.getSelectionModel().getSelectedItem();
        book.setCoverImage(image);
        new CoverpageBookProcessor().processBook(book);
        Resource coverPage = book.getGuide().getCoverPage();
        book.getSpine().addResource(coverPage, 0);
        bookBrowserManager.refreshBookBrowser();
        bookBrowserManager.selectTextItem(coverPage);
        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnShown(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent event)
            {
                refresh();
            }
        });
    }

    public void setBook(Book book)
    {
        this.book = book;
    }

    public void setBookBrowserManager(BookBrowserManager bookBrowserManager)
    {
        this.bookBrowserManager = bookBrowserManager;
    }
}
