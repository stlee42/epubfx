package de.machmireinebook.epubeditor.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.collections.FXCollections;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.editor.EditorTabManager;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.javafx.cells.ImageCellFactory;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;

/**
 * User: mjungierek
 * Date: 27.07.2014
 * Time: 15:30
 */
public class AddCoverController implements Initializable
{
    private static final Logger logger = Logger.getLogger(AddCoverController.class);

    public static final String DEFAULT_COVER_IMAGE_ID = "cover-image";
    public static final String DEFAULT_COVER_IMAGE_HREF = "Images/cover.jpg";
    public static final String DEFAULT_COVER_PAGE_ID = "cover";
    public static final String DEFAULT_COVER_PAGE_HREF = "Text/cover.xhtml";

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
        List<Resource<?>> resources = book.getResources().getResourcesByMediaTypes(new MediaType[]{
                MediaType.GIF,
                MediaType.PNG,
                MediaType.SVG,
                MediaType.JPG});
        for (Resource<?> resource : resources)
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

    public void onOkAction()
    {
        insertCover();
    }

    private void insertCover()
    {
        ImageResource image = tableView.getSelectionModel().getSelectedItem();
        book.setCoverImage(image);
        Optional<Resource> coverPageOptional = createOrReplaceCoverPage();
        coverPageOptional.ifPresentOrElse(coverPage -> {
            bookBrowserManager.refreshOpf();
            bookBrowserManager.refreshBookBrowser();
            bookBrowserManager.selectTextItem(coverPage);
            if (editorTabManager.isTabAlreadyOpen(coverPage)) {
                editorTabManager.refreshEditorCode(coverPage);
                editorTabManager.refreshPreview();
            } else {
                editorTabManager.openFileInEditor(coverPage);
            }
            book.setBookIsChanged(true);
            stage.close();
        }, () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Coverpage not generated");
            alert.getDialogPane().setHeader(null);
            alert.getDialogPane().setHeaderText(null);
            alert.setContentText("Can't find generated cover page, something goes wrong");
            alert.showAndWait();
        });
    }

    public void onCancelAction()
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

    public void otherFileButtonAction()
    {
        mainController.addExistingFiles();
        refresh();
    }

    public Optional<Resource> createOrReplaceCoverPage()
    {
        if (book.getCoverPage() == null && book.getCoverImage() == null)
        {
            return Optional.empty();
        }
        Resource coverPage = book.getCoverPage();
        ImageResource coverImage = book.getCoverImage();
        if (coverPage == null)
        {
            if (coverImage != null) {
                if (StringUtils.isBlank(coverImage.getHref()))
                {
                    coverImage.setHref(DEFAULT_COVER_IMAGE_HREF);
                }
                String coverPageHtml = createCoverpageHtml(coverImage.getHref(), coverImage.getWidth(), coverImage.getHeight());
                if (StringUtils.isEmpty(coverPageHtml)) {
                    logger.warn("generated coverPageHtml is empty");
                    return Optional.empty();
                }
                coverPage = MediaType.XHTML.getResourceFactory().createResource(coverPageHtml.getBytes(), DEFAULT_COVER_PAGE_HREF);
                fixCoverResourceId(book, coverPage, DEFAULT_COVER_PAGE_ID);
                book.getSpine().addResource(coverPage, 0);
            } else {
                return Optional.empty();
            }
        }
        else
        {
            if (StringUtils.isBlank(coverImage.getHref()))
            {
                coverImage.setHref(DEFAULT_COVER_IMAGE_HREF);
            }
            String coverPageHtml = createCoverpageHtml(coverImage.getHref(), coverImage.getWidth(), coverImage.getHeight());
            coverPage.setData(coverPageHtml.getBytes());
            fixCoverResourceId(book, coverPage, DEFAULT_COVER_PAGE_ID);
        }

        book.setCoverImage(coverImage);
        book.setCoverPage(coverPage);
        setCoverResourceIds(book);
        return Optional.of(coverPage);
    }

    private String createCoverpageHtml(String imageHref, double width, double height)
    {
        String templateFileName;
        if (book.isEpub3()) {
            templateFileName = "/epub/cover-epub3.xhtml";
        } else {
            templateFileName = "/epub/cover-epub2.xhtml";
        }
        File file = new File(Book.class.getResource(templateFileName).getFile());
        String content = null;
        try (InputStream is = new FileInputStream(file)) {
            content = IOUtils.toString(is, StandardCharsets.UTF_8);
            content = content.replaceAll("\\$\\{width}", String.valueOf(width));
            content = content.replaceAll("\\$\\{height}", String.valueOf(height));
            content = content.replaceAll("\\$\\{imageHref}", imageHref);
        }
        catch (IOException e) {
            logger.error("", e);
            return content;
        }
        return content;
    }

    private void setCoverResourceIds(Book book)
    {
        if (book.getCoverImage() != null)
        {
            fixCoverResourceId(book, book.getCoverImage(), DEFAULT_COVER_IMAGE_ID);
        }
        if (book.getCoverPage() != null)
        {
            fixCoverResourceId(book, book.getCoverPage(), DEFAULT_COVER_PAGE_ID);
        }
    }


    private void fixCoverResourceId(Book book, Resource resource, String defaultId)
    {
        if (StringUtils.isBlank(resource.getId()))
        {
            resource.setId(defaultId);
        }
        book.getResources().fixResourceId(resource);
    }
}
