package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private ListView targetsInBookListView;
    @Inject
    private EditorTabManager editorTabManager;

    @Override
    public void setStage(Stage stage)
    {
        super.setStage(stage);
        targetTextField.requestFocus();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
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
