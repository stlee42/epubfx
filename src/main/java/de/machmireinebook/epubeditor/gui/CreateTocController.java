package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.AbstractFilter;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.domain.XHTMLResource;
import de.machmireinebook.epubeditor.javafx.cells.WrappableTextCellFactory;

/**
 * Created by Michail Jungierek, Acando GmbH on 17.05.2018
 */
public class CreateTocController implements StandardController
{
    @FXML
    private TableView<TocEntry> tableView;
    @FXML
    private Button renameButton;
    @FXML
    private Button higherLevelButton;
    @FXML
    private Button deeperLevelButton;
    @FXML
    private CheckBox showTocItemsCheckBox;
    @FXML
    private ComboBox headingLevelComboBox;

    private ObjectProperty<Book> currentBook = new SimpleObjectProperty<>(this, "currentBook");
    private Stage stage;

    private static CreateTocController instance;

    private static class PossibleTocEntryFilter extends AbstractFilter<Element>
    {
        private static final List<String> ELEMENT_NAMES = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6");

        @Override
        public Element filter(Object content)
        {
            if (content instanceof Element) {
                Element el = (Element) content;
                if (ELEMENT_NAMES.contains(el.getName()))
                {
                    return el;
                }
            }
            return null;
        }
    }

    public static CreateTocController getInstance()
    {
        return instance;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
    }

    @Override
    public Stage getStage()
    {
        return stage;
    }

    @Override
    public ObjectProperty<Book> currentBookProperty() {
        return currentBook;
    }
    public Book getCurrentBook() {
        return currentBook.get();
    }
    public void setCurrentBook(Book value) {
        currentBook.set(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        currentBook.addListener((observable, oldValue, newValue) -> {

            TableColumn<TocEntry, String> tc = (TableColumn<TocEntry, String>) tableView.getColumns().get(0);
            tc.setCellValueFactory(new PropertyValueFactory<>("title"));
            tc.setCellFactory(new WrappableTextCellFactory<>());
            tc.setSortable(true);

            TableColumn<TocEntry, String> tc2 = (TableColumn<TocEntry, String>) tableView.getColumns().get(1);
            tc2.setCellValueFactory(new PropertyValueFactory<>("level"));
            tc2.setCellFactory(new WrappableTextCellFactory<>());
            tc2.setSortable(true);

            List<TocEntry> tocEntries = generateTocEntriesFromText();
            tableView.setItems(FXCollections.observableList(tocEntries));
        });
        instance = this;
    }

    private List<TocEntry> generateTocEntriesFromText()
    {
        List<TocEntry> tocEntries = new ArrayList<>();
        List<Resource> contentResources = currentBook.get().getContents();
        for (Resource resource : contentResources)
        {
            if (resource.getMediaType().equals(MediaType.XHTML))
            {
                XHTMLResource xhtmlResource = (XHTMLResource) resource;
                Document document = xhtmlResource.asNativeFormat();
                IteratorIterable<Element> possibleTocEntries = document.getDescendants(new PossibleTocEntryFilter());
            }
        }
        return tocEntries;
    }

    public void renameButtonAction(ActionEvent actionEvent)
    {

    }

    public void higherLevelButtonAction(ActionEvent actionEvent)
    {

    }

    public void deeperLevelButtonAction(ActionEvent actionEvent)
    {

    }

    public void showTocItemsCheckBoxAction(ActionEvent actionEvent)
    {

    }

    public void headingLevelComboBoxAction(ActionEvent actionEvent)
    {

    }

    public void onOkAction(ActionEvent actionEvent)
    {

    }

    public void onCancelAction(ActionEvent actionEvent)
    {

    }
}
