package de.machmireinebook.epubeditor.editor;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.gui.ExceptionDialog;

/**
 * User: Michail Jungierek
 * Date: 21.07.2019
 * Time: 15:09
 */
public class SpellcheckPopoverContentAnchorPane extends AnchorPane implements Initializable {
    private static final Logger logger = Logger.getLogger(SpellcheckPopoverContentAnchorPane.class);
    
    @FXML
    private Label examplesLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label suggestionsLabel;

    public SpellcheckPopoverContentAnchorPane()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/spellcheck-popover-content.fxml"), null, new JavaFXBuilderFactory());
        loader.setRoot(this);
        loader.setController(this);

        try
        {
            loader.load();
        }
        catch (IOException e)
        {
            ExceptionDialog.showAndWait(e, null, "spell check popover", "Error opening popover");
            logger.error("", e);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    public Label getExamplesLabel() {
        return examplesLabel;
    }

    public Label getMessageLabel() {
        return messageLabel;
    }

    public Label getSuggestionsLabel() {
        return suggestionsLabel;
    }
}
