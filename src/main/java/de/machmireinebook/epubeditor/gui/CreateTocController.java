package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.toc.TocGenerator;
import de.machmireinebook.epubeditor.javafx.cells.WrappableTextCellFactory;

import org.apache.commons.lang3.StringUtils;

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
    @Inject
    private TocGenerator tocGenerator;

    private ObjectProperty<Book> currentBook = new SimpleObjectProperty<>(this, "currentBook");
    private Stage stage;

    private static CreateTocController instance;

    public static CreateTocController getInstance()
    {
        return instance;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnShowing(event -> {
            List<TocEntry> tocEntries = tocGenerator.generateTocEntriesFromText();
            ObservableList<TocEntry> tableViewItems = tableView.getItems();
            for (TocEntry tocEntry : tocEntries)
            {
                addTocEntryToTableView(tocEntry, tableViewItems, 0);
            }
        });
    }

    private void addTocEntryToTableView(TocEntry tocEntry, ObservableList<TocEntry> tableViewItems, int level)
    {
        tocEntry.setTitle(StringUtils.leftPad(tocEntry.getTitle(), tocEntry.getTitle().length() + level * 4, " "));
        tableViewItems.add(tocEntry);
        for (TocEntry childEntry : tocEntry.getChildren())
        {
            addTocEntryToTableView(childEntry, tableViewItems, level + 1);
        }
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
        TableColumn<TocEntry, String> tc = (TableColumn<TocEntry, String>) tableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("title"));
        tc.setCellFactory(new WrappableTextCellFactory<>());
        tc.setSortable(true);

        TableColumn<TocEntry, String> tc2 = (TableColumn<TocEntry, String>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("level"));
        tc2.setCellFactory(new WrappableTextCellFactory<>());
        tc2.setSortable(true);

        tocGenerator.bookProperty().bind(currentBook);

        instance = this;
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
        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }
}
