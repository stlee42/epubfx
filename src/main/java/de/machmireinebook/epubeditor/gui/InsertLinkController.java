package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.jdom2.AttributeFilter;
import de.machmireinebook.epubeditor.editor.EditorTabManager;

/**
 * User: Michail Jungierek
 * Date: 13.07.2019
 * Time: 11:24
 */
public class InsertLinkController extends AbstractStandardController {
    @FXML
    private TextField targetTextField;
    @FXML
    private ListView<String> targetsInBookListView;

    private static InsertLinkController instance;

    @Inject
    private EditorTabManager editorTabManager;
    @Inject
    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        targetsInBookListView.setOnMouseClicked(event ->
        {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                String selectedText = targetsInBookListView.getSelectionModel().getSelectedItem();
                targetTextField.setText(selectedText);
                if (event.getClickCount() == 2) {
                    onOkAction();
                }
            }
        });
        instance = this;
    }

    @Override
    public void setStage(Stage stage)
    {
        super.setStage(stage);
        stage.setOnShown(event -> {
            targetTextField.requestFocus();
            refresh();
        });
    }

    private void refresh() {
        ObservableList<String> targets = FXCollections.observableArrayList();
        //first add the targets inside the current file
        XHTMLResource currentResource = editorTabManager.getCurrentXHTMLResource();
        targets.addAll(getTargetsInResource(currentResource, false));
        //now all other files from
        Book book = mainController.getCurrentBook();
        List<SpineReference> references = book.getSpine().getSpineReferences();
        for (SpineReference reference : references) {
            Resource resource = reference.getResource();
            if (resource == currentResource || resource.getMediaType() != MediaType.XHTML) {
                continue;
            }
            targets.add(resource.getFileName());
            targets.addAll(getTargetsInResource((XHTMLResource) resource, true));
        }
        targetsInBookListView.setItems(targets);
    }

    private ObservableList<String> getTargetsInResource(XHTMLResource resource, boolean withResourceName) {
        ObservableList<String> targets = FXCollections.observableArrayList();
        Document document = resource.asNativeFormat();
        Element root = document.getRootElement();
        if (root != null) {
            IteratorIterable<Element> elementsWithId = root.getDescendants(new AttributeFilter("id"));
            for (Element element : elementsWithId) {
                String id = element.getAttributeValue("id");
                if (withResourceName) {
                    targets.add(resource.getFileName() + "#" + id);
                } else {
                    targets.add("#" + id);
                }
            }
        }
        return targets;
    }

    public static StandardController getInstance()
    {
        return instance;
    }


    public void onOkAction() {
        if (editorTabManager.currentEditorIsXHTML()) {
            editorTabManager.surroundSelection("<a href=\"" + targetTextField.getText() + "\">", "</a>");
        }
        targetTextField.setText("");
        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent) {
        stage.close();
    }
}
