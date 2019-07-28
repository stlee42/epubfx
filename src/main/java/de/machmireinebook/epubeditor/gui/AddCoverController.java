package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.epublib.bookprocessor.CoverpageBookProcessor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.epub3.LandmarkReference;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.javafx.cells.ImageCellFactory;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.manager.EditorTabManager;

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

    @Inject
    private BookBrowserManager bookBrowserManager;
    @Inject
    private MainController mainController;
    @Inject
    private EditorTabManager editorTabManager;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<ImageResource, String> tc = (TableColumn<ImageResource, String>) tableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("href"));
        tc.setSortable(true);

        TableColumn<ImageResource, Image> tc2 = (TableColumn<ImageResource, Image>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("image"));
        tc2.setCellFactory(new ImageCellFactory<>(null, 100d));
        tc2.setSortable(false);

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> refreshImageView(newValue));
        tableView.setOnMouseClicked(event ->
        {
            if (event.getButton().equals(MouseButton.PRIMARY))
            {
                if (event.getClickCount() == 2)
                {
                    insertCover();
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
        if (resource != null)
        {
            Image image = resource.asNativeFormat();
            imageView.setImage(image);
            imageValuesLabel.setText(resource.getImageDescription());
        }
        else
        {
            imageView.setImage(null);
            imageValuesLabel.setText("");
        }
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
        Resource coverPage = null;
        if (book.isEpub3()) {
            List<LandmarkReference> landmarks = book.getLandmarks().getLandmarkReferencesByType(LandmarkReference.Semantic.COVER);
            if (!landmarks.isEmpty()) {
                coverPage = landmarks.get(0).getResource();
            }
        } else {
            coverPage = book.getGuide().getCoverPage();
        }
        if (coverPage == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Coverpage not generated");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("Can't find generated cover page, something goes wrong");
            alert.showAndWait();
            return;
        }
        book.getSpine().addResource(coverPage, 0);
        bookBrowserManager.refreshOpf();
        bookBrowserManager.refreshBookBrowser();
        bookBrowserManager.selectTextItem(coverPage);
        editorTabManager.openFileInEditor(coverPage);
        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnShown(event -> refresh());
    }

    public void setBook(Book book)
    {
        this.book = book;
    }

    public void otherFileButtonAction(ActionEvent actionEvent)
    {
        mainController.addExistingFiles();
        refresh();
    }
}
