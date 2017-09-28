package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import jidefx.scene.control.searchable.TableViewSearchable;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 18.11.13
 * Time: 13:10
 */
public class ChooserWindowController implements Initializable
{
    @FXML
    private GridPane chooserWindowGridPane;
    @FXML
    private Label chooserWindowLabel;
    @FXML
    private TableView chosserWindowTableView;
    @FXML
    private Button chooserWindowOkButton;
    @FXML
    private Button chooserWindowCancelButton;

    private Stage chooserWindow;
    private static final Logger logger = Logger.getLogger(ChooserWindowController.class);

    private static ChooserWindowController instance;

    public static ChooserWindowController getInstance()
    {
        return instance;
    }

    public class CancelChooserWindowHandler implements EventHandler<ActionEvent>
    {
        @Override
        public void handle(ActionEvent actionEvent)
        {
            chooserWindow.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        logger.info("init chooser window");
        chooserWindowCancelButton.setOnAction(new CancelChooserWindowHandler());
        TableViewSearchable searchableProvisions = new TableViewSearchable(chosserWindowTableView);
        searchableProvisions.setCaseSensitive(false);

        instance = this;
    }

    public Button getChooserWindowCancelButton()
    {
        return chooserWindowCancelButton;
    }

    public Label getChooserWindowLabel()
    {
        return chooserWindowLabel;
    }

    public Button getChooserWindowOkButton()
    {
        return chooserWindowOkButton;
    }

    public TableView getChosserWindowTableView()
    {
        return chosserWindowTableView;
    }

    public GridPane getChooserWindowGridPane()
    {
        return chooserWindowGridPane;
    }

    public Stage getChooserWindow()
    {
        return chooserWindow;
    }

    public void setChooserWindow(Stage chooserWindow)
    {
        this.chooserWindow = chooserWindow;
    }
}
