package de.machmireinebook.epubeditor.media;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.util.ResourceFilenameComparator;
import de.machmireinebook.epubeditor.gui.AbstractStandardController;
import de.machmireinebook.epubeditor.javafx.cells.ImageCellFactory;

/**
 * User: Michail Jungierek
 * Date: 12.09.2019
 * Time: 20:45
 */
public class FindUnusedMediaFilesController extends AbstractStandardController {
    public TableView<ImageResource> tableView;
    public ImageView imageView;
    public Label imageValuesLabel;

    private static FindUnusedMediaFilesController instance;

    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources)
    {
        super.initialize(location, resources);

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        Label placeholderLabel = new Label("No unused media files found");
        tableView.setPlaceholder(placeholderLabel);

        TableColumn<ImageResource, String> tc = (TableColumn<ImageResource, String>)tableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("href"));
        tc.setSortable(true);

        TableColumn<ImageResource, Image> tc2 = (TableColumn<ImageResource, Image>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("image"));
        tc2.setCellFactory(new ImageCellFactory<>(160d, null));
        tc2.setSortable(false);

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            refreshImageView(newValue);
        });
        instance = this;
    }

    public static FindUnusedMediaFilesController getInstance() {
        return instance;
    }

    public void setStage(Stage stage)
    {
        super.setStage(stage);
        stage.setOnShowing(event -> {
            refresh();
        });
    }

    private void refresh()
    {
        List<ImageResource> unusedResources = new ArrayList<>();
        List<Resource> resources = currentBookProperty.get().getResources().getResourcesByMediaTypes(new MediaType[]{
                MediaType.GIF,
                MediaType.PNG,
                MediaType.SVG,
                MediaType.JPG});
        List<Resource> xhtmlResources = currentBookProperty.get().getResources().getResourcesByMediaTypes(new MediaType[]{
                MediaType.XHTML,
                MediaType.CSS});
        for (Resource resource : resources)
        {
            if (notUsed((ImageResource)resource, xhtmlResources)) {
                unusedResources.add((ImageResource) resource);
            }
        }
        unusedResources.sort(new ResourceFilenameComparator());
        tableView.setItems(FXCollections.observableList(unusedResources));
        tableView.getSelectionModel().select(0);
    }

    private boolean notUsed(ImageResource imageResource, List<Resource> xhtmlResources) {
        for (Resource xhtmlResource : xhtmlResources) {
            String text = StringUtils.toEncodedString(xhtmlResource.getData(), StandardCharsets.UTF_8);
            if (text.contains(imageResource.convertToString())) {
                return false;
            }
        }
        return true;
    }

    public void onOkAction(ActionEvent actionEvent) {

        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent) {

        stage.close();
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
}
