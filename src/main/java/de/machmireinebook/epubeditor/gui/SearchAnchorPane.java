package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;

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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.editor.CodeEditor;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.manager.EditorTabManager;
import de.machmireinebook.epubeditor.manager.SearchManager;

/**
* User: mjungierek
* Date: 06.01.2015
* Time: 22:08
*/
@Singleton
public class SearchAnchorPane extends AnchorPane implements Initializable
{
    private static final Logger logger = Logger.getLogger(SearchAnchorPane.class);

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
    private SearchManager searchManager;
    @Inject
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
            ExceptionDialog.showAndWait(e, null, "Suchmaske", "Fehler beim Öffnen der Suchmaske");
            logger.error("", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        modusChoiceBox.getSelectionModel().select(0);
        searchRegionChoiceBox.getSelectionModel().select(0);
        dotAllCheckBox.disableProperty().bind(Bindings.equal(2, modusChoiceBox.getSelectionModel().selectedIndexProperty()));
        minimalMatchCheckBox.disableProperty().bind(Bindings.equal(2, modusChoiceBox.getSelectionModel().selectedIndexProperty()));
    }

    public void findNextAction()
    {
        CodeEditor editor = editorManager.getCurrentEditor();
        int cursorIndex = editor.getAbsoluteCursorPosition();
        SearchManager.SearchParams params = new SearchManager.SearchParams(dotAllCheckBox.selectedProperty().get(),
                minimalMatchCheckBox.selectedProperty().get(),
                SearchManager.SearchMode.values()[modusChoiceBox.getSelectionModel().selectedIndexProperty().get()],
                SearchManager.SearchRegion.values()[searchRegionChoiceBox.getSelectionModel().selectedIndexProperty().get()]);
        Optional<SearchManager.SearchResult> result = searchManager.findNext(searchStringTextField.getText(), editorManager.getCurrentSearchableResource(), cursorIndex, params);
        result.ifPresent((searchResult) ->
                {
                    int fromIndex = searchResult.getBegin();
                    int toIndex = searchResult.getEnd();
                    editor.select(fromIndex, toIndex);
                    editor.setAbsoluteCursorPosition(toIndex);
                }
        );
    }

    public void replaceNextAction()
    {
        CodeEditor editor = editorManager.getCurrentEditor();
        int cursorIndex = editor.getAbsoluteCursorPosition();
        SearchManager.SearchParams params = new SearchManager.SearchParams(dotAllCheckBox.selectedProperty().get(),
                minimalMatchCheckBox.selectedProperty().get(),
                SearchManager.SearchMode.values()[modusChoiceBox.getSelectionModel().selectedIndexProperty().get()],
                SearchManager.SearchRegion.values()[searchRegionChoiceBox.getSelectionModel().selectedIndexProperty().get()]);
        Optional<SearchManager.SearchResult> result = searchManager.findNext(searchStringTextField.getText(), editorManager.getCurrentSearchableResource(), cursorIndex, params);
        result.ifPresent((searchResult) ->
            {
                int fromIndex = searchResult.getBegin();
                int toIndex = searchResult.getEnd();
                editor.select(fromIndex, toIndex);

                String replaceString = replaceStringTextField.getText();
                editor.replaceSelection(replaceString);
                editor.select(fromIndex, fromIndex + replaceString.length());
                editor.scrollTo(fromIndex);
                editor.setAbsoluteCursorPosition(fromIndex + replaceString.length());
            }
        );
    }

    public void replaceAllAction()
    {
        CodeEditor editor = editorManager.getCurrentEditor();
        SearchManager.SearchParams params = new SearchManager.SearchParams(dotAllCheckBox.selectedProperty().get(),
                minimalMatchCheckBox.selectedProperty().get(),
                SearchManager.SearchMode.values()[modusChoiceBox.getSelectionModel().selectedIndexProperty().get()],
                SearchManager.SearchRegion.values()[searchRegionChoiceBox.getSelectionModel().selectedIndexProperty().get()]);
        Resource resource = editorManager.getCurrentSearchableResource();
        List<SearchManager.SearchResult> result = searchManager.findAll(searchStringTextField.getText(), resource, params);
        try
        {
            String code = new String(resource.getData(), resource.getInputEncoding());
            String replaceText = StringUtils.defaultIfEmpty(replaceStringTextField.getText(), "");
            for (SearchManager.SearchResult searchResult : result)
            {
                int fromIndex = searchResult.getBegin();
                int toIndex = searchResult.getEnd();
                String beginPart = code.substring(0, fromIndex);
                String endPart = code.substring(toIndex);
                code = beginPart + replaceText + endPart;
            }
            editor.setCode(code);
        }
        catch (UnsupportedEncodingException e)
        {
            //schould not happens
        }
    }

    public void setSearchString(String searchString)
    {
        searchStringTextField.setText(searchString);
        searchStringTextField.requestFocus();
    }

    public Button getCloseButton()
    {
        return closeButton;
    }
}
