package de.machmireinebook.epubeditor.media;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.util.ResourceFilenameComparator;
import de.machmireinebook.epubeditor.gui.AbstractStandardController;
import de.machmireinebook.epubeditor.gui.ExceptionDialog;
import de.machmireinebook.epubeditor.gui.MainController;
import de.machmireinebook.epubeditor.gui.StandardController;
import de.machmireinebook.epubeditor.javafx.cells.ImageCellFactory;
import de.machmireinebook.epubeditor.editor.EditorTabManager;
import de.machmireinebook.epubeditor.util.EpubFxNumberUtils;
import de.machmireinebook.epubeditor.xhtml.XmlUtils;

/**
 * User: mjungierek
 * Date: 18.08.2014
 * Time: 21:14
 */
public class InsertMediaController extends AbstractStandardController
{
    private static final Logger logger = Logger.getLogger(InsertMediaController.class);
    @FXML
    private ChoiceBox alignmentChoiceBox;
    @FXML
    private CheckBox addBorderCheckBox;
    @FXML
    private TextField borderStyleTextField;
    @FXML
    private RadioButton noWidthHeightRadioButton;
    @FXML
    private ComboBox<String> mediaTypeComboBox;
    @FXML
    private TextField widthPixelTextField;
    @FXML
    private TextField heightPixelTextField;
    @FXML
    private RadioButton flexibleWidthRadioButton;
    @FXML
    private TextField percentWidthTextField;
    @FXML
    private CheckBox maxPhysicalWidthCheckBox;
    @FXML
    private CheckBox withCaptionCheckBox;
    @FXML
    private TextField captionTextField;
    @FXML
    private RadioButton fixWidthHeightRadioButton;
    @FXML
    private ToggleGroup widthHeightGroup;
    @FXML
    private TableView<ImageResource> tableView;
    @FXML
    private ImageView imageView;
    @FXML
    private Label imageValuesLabel;

    private static final int ALIGNMENT_LEFT = 0;
    private static final int ALIGNMENT_CENTER = 1;
    private static final int ALIGNMENT_RIGHT = 2;

    private static StandardController instance;

    @Inject
    private EditorTabManager editorManager;
    @Inject
    private MainController mainController;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        super.initialize(location, resources);

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<ImageResource, String> tc = (TableColumn<ImageResource, String>) tableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("href"));
        tc.setSortable(true);

        TableColumn<ImageResource, Image> tc2 = (TableColumn<ImageResource, Image>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("image"));
        tc2.setCellFactory(new ImageCellFactory<>(160d, null));
        tc2.setSortable(false);

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            refreshImageView(newValue);
            if (newValue != null)
            {
                widthPixelTextField.setText(EpubFxNumberUtils.formatAsInteger(newValue.getWidth()));
                heightPixelTextField.setText(EpubFxNumberUtils.formatAsInteger(newValue.getHeight()));
            }
        });
        tableView.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY))
            {
                if (event.getClickCount() == 2)
                {
                    ImageResource resource = tableView.getSelectionModel().getSelectedItem();
                    insertMediaFile(resource);
                    captionTextField.setText("");
                    stage.close();
                }
            }
        });
        mediaTypeComboBox.getSelectionModel().select(1);
        alignmentChoiceBox.getSelectionModel().select(1);

        captionTextField.disableProperty().bind(Bindings.not(withCaptionCheckBox.selectedProperty()));
        widthPixelTextField.disableProperty().bind(Bindings.not(fixWidthHeightRadioButton.selectedProperty()));
        heightPixelTextField.disableProperty().bind(Bindings.not(fixWidthHeightRadioButton.selectedProperty()));
        percentWidthTextField.disableProperty().bind(Bindings.not(flexibleWidthRadioButton.selectedProperty()));
        maxPhysicalWidthCheckBox.disableProperty().bind(Bindings.not(flexibleWidthRadioButton.selectedProperty()));
        borderStyleTextField.disableProperty().bind(Bindings.not(addBorderCheckBox.selectedProperty()));

        instance = this;
    }

    public static StandardController getInstance()
    {
        return instance;
    }

    public void onOkAction(ActionEvent actionEvent)
    {
        ImageResource resource = tableView.getSelectionModel().getSelectedItem();
        if (resource != null) {
            insertMediaFile(resource);
        }
        captionTextField.setText("");
        stage.close();
    }

    private void insertMediaFile(ImageResource resource)
    {
        logger.info("insert media");
        try
        {
            int alignment = alignmentChoiceBox.getSelectionModel().getSelectedIndex();
            String snippet;
            boolean isEpub3 = currentBookProperty.get().isEpub3();
            if (withCaptionCheckBox.isSelected())
            {
                if (isEpub3)
                {
                    snippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/image-figure.html"), StandardCharsets.UTF_8);
                }
                else
                {
                    snippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/image-div.html"), StandardCharsets.UTF_8);
                }
                String text = captionTextField.getText();
                String altText = "";
                if (StringUtils.isNotEmpty(text)) {
                    altText = XmlUtils.removeTags(text);
                    altText = StringUtils.replace(altText, "&amp;", "");
                    altText = StringUtils.replace(altText, "&", "");
                    altText = StringUtils.replace(altText, "\"", "");
                    altText = StringUtils.replace(altText, "\'", "");
                }
                snippet = StringUtils.replace(snippet, "${alt}", altText);
                snippet = StringUtils.replace(snippet, "${caption}", text);
            }
            else
            {
                if (isEpub3)
                {
                    snippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/image-figure-without-caption.html"), StandardCharsets.UTF_8);
                }
                else
                {
                    snippet = IOUtils.toString(getClass().getResourceAsStream("/epub/snippets/image-single.html"), StandardCharsets.UTF_8);
                }
            }
            snippet = StringUtils.replace(snippet, "${url}", editorManager.getCurrentXHTMLResource().relativize(resource));

            String style = "";
            if (addBorderCheckBox.isSelected())
            {
                style += "border:" + borderStyleTextField.getText() + "; ";
            }
            if (fixWidthHeightRadioButton.isSelected())
            {
                style += "width:" + resource.getWidth() +"px; height:" + resource.getHeight() + "px;";
            }
            else if(flexibleWidthRadioButton.isSelected())
            {
                //max-width will be ignored by amazon, because this the physical width of the image set as width of the figure,
                // otherwise amazon creates funny effects
                style += "max-width:" + percentWidthTextField.getText() +"%; ";
                if (maxPhysicalWidthCheckBox.isSelected())
                {
                    style += "width:" + EpubFxNumberUtils.formatAsInteger(resource.getWidth()) +"px; ";
                }
            }
            String containerStyle;
            if (alignment == ALIGNMENT_LEFT) {
                containerStyle = "float:left";
            } else if (alignment == ALIGNMENT_RIGHT) {
                containerStyle = "float:right";
            } else {
                containerStyle = style + "margin-right: auto; margin-left:auto";
                style = "width: 100%";
            }

            if (StringUtils.isNotEmpty(style)) {
                style = "style=\"" + style.trim() + "\" ";
            }
            if (StringUtils.isNotEmpty(containerStyle)) {
                containerStyle = "style=\"" + containerStyle.trim() + "\" ";
            }

            snippet = StringUtils.replace(snippet, "${style}", style);
            snippet = StringUtils.replace(snippet, "${container-style}", containerStyle);

            CodeEditor editor = editorManager.getCurrentEditor();
            Integer cursorPosition = editor.getAbsoluteCursorPosition();
            editor.insertAt(cursorPosition, snippet);
            editorManager.refreshPreview();
        }
        catch (IOException e)
        {
            logger.error("", e);
            ExceptionDialog.showAndWait(e, stage,  "Insert not possible", "Unknown error while inserting image.");

        }
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }

    public void otherFileButtonAction(ActionEvent actionEvent)
    {
        mainController.addExistingFiles();
        refresh();
    }

    public void refresh()
    {
        List<ImageResource> imageResources = new ArrayList<>();
        List<Resource> resources = currentBookProperty.get().getResources().getResourcesByMediaTypes(new MediaType[]{
                MediaType.GIF,
                MediaType.PNG,
                MediaType.SVG,
                MediaType.JPG});
        for (Resource resource : resources)
        {
            imageResources.add((ImageResource)resource);
        }
        imageResources.sort(new ResourceFilenameComparator());
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

    public void setStage(Stage stage)
    {
        super.setStage(stage);
        stage.setOnShowing(event -> {
            refresh();
        });
    }

}
