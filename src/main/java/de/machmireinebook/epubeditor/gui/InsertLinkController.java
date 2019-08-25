package de.machmireinebook.epubeditor.gui;

import java.net.URL;
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

import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.jdom2.AttributeFilter;
import de.machmireinebook.epubeditor.manager.EditorTabManager;

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

    private static StandardController instance;

    @Inject
    private EditorTabManager editorTabManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        targetsInBookListView.setOnMouseClicked(event ->
        {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 2) {
                    String selectedText = targetsInBookListView.getSelectionModel().getSelectedItem();
                    targetTextField.setText(selectedText);
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
        XHTMLResource resource = editorTabManager.getCurrentXHTMLResource();
        Document document = resource.asNativeFormat();
        Element root = document.getRootElement();
        if (root != null) {
            IteratorIterable<Element> elementsWithId = root.getDescendants(new AttributeFilter("id"));
            for (Element element : elementsWithId) {
                String id = element.getAttributeValue("id");
                targets.add("#" + id);
            }
        }
        targetsInBookListView.setItems(targets);
    }

    public static StandardController getInstance()
    {
        return instance;
    }


    public void onOkAction(ActionEvent actionEvent) {
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
