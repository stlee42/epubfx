package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import de.machmireinebook.commons.cdi.BeanFactory;
import de.machmireinebook.epubeditor.cdi.EditorTabManagerProducer;
import de.machmireinebook.epubeditor.cdi.SearchManagerProducer;
import de.machmireinebook.epubeditor.cdi.SearchPaneProducer;
import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.manager.EditorTabManager;
import de.machmireinebook.epubeditor.manager.SearchManager;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.dialog.Dialogs;

/**
* User: mjungierek
* Date: 06.01.2015
* Time: 22:08
*/
public class SearchAnchorPane extends AnchorPane implements Initializable
{
    private static SearchAnchorPane instance;
    @FXML
    private TextField replaceStringTextField;
    @FXML
    private TextField searchStringTextField;
    @FXML
    private Button closeButton;
    @FXML
    private Label searchMatchesLabel;
    @FXML
    private CheckBox dotAllCheckBox;
    @FXML
    private CheckBox minimalMatchCheckBox;
    @FXML
    private ChoiceBox modusChoiceBox;
    @FXML
    private ChoiceBox searchRegionChoiceBox;
    @Inject
    @SearchManagerProducer
    private SearchManager searchManager;
    @Inject
    @EditorTabManagerProducer
    private EditorTabManager editorManager;


    public SearchAnchorPane()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/search_pane.fxml"), null, new JavaFXBuilderFactory());
        loader.setRoot(this);
        loader.setController(this);

        try
        {
            loader.load();
        }
        catch (IOException e)
        {
            Dialogs.create()
                    .title("Suchmaske")
                    .masthead(null)
                    .message("Fehler beim Öffnen der Suchmaske")
                    .showException(e);
            SearchManager.logger.error("", e);
        }
    }

    @Produces
    @SearchPaneProducer
    public static SearchAnchorPane getInstance()
    {
        if (instance == null)
        {
            instance = BeanFactory.getInstance().getBean(SearchAnchorPane.class);
        }
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        closeButton.setOnAction(event -> {
            EpubEditorMainController.getInstance().closeSearchPaneAction(event);
        });
        modusChoiceBox.getSelectionModel().select(0);
        searchRegionChoiceBox.getSelectionModel().select(0);
        dotAllCheckBox.disableProperty().bind(Bindings.equal(2, modusChoiceBox.getSelectionModel().selectedIndexProperty()));
        minimalMatchCheckBox.disableProperty().bind(Bindings.equal(2, modusChoiceBox.getSelectionModel().selectedIndexProperty()));
    }

    public void findNextAction()
    {
        CodeEditor editor = editorManager.getCurrentEditor();
        int cursorIndex = editor.getEditorCursorIndex();
        SearchManager.SearchParams params = new SearchManager.SearchParams(dotAllCheckBox.selectedProperty().get(),
                minimalMatchCheckBox.selectedProperty().get(),
                SearchManager.SearchMode.values()[modusChoiceBox.getSelectionModel().selectedIndexProperty().get()],
                SearchManager.SearchRegion.values()[searchRegionChoiceBox.getSelectionModel().selectedIndexProperty().get()]);
        Optional<SearchManager.SearchResult> result = searchManager.findNext(searchStringTextField.getText(), editorManager.getCurrentSearchableResource(), cursorIndex, params);
        result.ifPresent(new Consumer<SearchManager.SearchResult>()
        {
            @Override
            public void accept(SearchManager.SearchResult searchResult)
            {
                int fromIndex = searchResult.getBegin();
                int toIndex = searchResult.getEnd();
                editor.select(fromIndex, toIndex);
            }
        });
    }
}
