package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.toc.ChoosableTocEntry;
import de.machmireinebook.epubeditor.epublib.toc.TocGenerator;
import de.machmireinebook.epubeditor.javafx.cells.WrappableTextCellFactory;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.manager.EditorTabManager;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Michail Jungierek, Acando GmbH on 17.05.2018
 */
public class CreateTocController implements StandardController
{
    @FXML
    private TableView<ChoosableTocEntry> tableView;
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
    @Inject
    private BookBrowserManager bookBrowserManager;
    @Inject
    private EditorTabManager editorTabManager;

    private ObjectProperty<Book> currentBook = new SimpleObjectProperty<>(this, "currentBook");
    private Stage stage;
    private ObservableList<ChoosableTocEntry> allTocEntries = FXCollections.observableArrayList();

    private static CreateTocController instance;

    public static CreateTocController getInstance()
    {
        return instance;
    }



    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        tableView.setEditable(true);

        TableColumn<ChoosableTocEntry, String> tc = (TableColumn<ChoosableTocEntry, String>) tableView.getColumns().get(0);
        tc.setEditable(true);
        tc.setCellValueFactory(new PropertyValueFactory<>("title"));
        tc.setCellFactory(new WrappableTextCellFactory<>());
        tc.setSortable(true);

        TableColumn<ChoosableTocEntry, String> tc2 = (TableColumn<ChoosableTocEntry, String>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("level"));
        tc2.setCellFactory(new WrappableTextCellFactory<>());
        tc2.setSortable(true);

        TableColumn<ChoosableTocEntry, Boolean> tc3 = (TableColumn<ChoosableTocEntry, Boolean>) tableView.getColumns().get(2);
        tc3.setEditable(true);
        tc3.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().getChoosed()));
        tc3.setCellFactory(cell -> new CheckBoxTableCell<>());
        tc3.setSortable(true);

        showTocItemsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> setTableViewItems());
        showTocItemsCheckBox.setSelected(true);

        tocGenerator.bookProperty().bind(currentBook);

        instance = this;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnShowing(event -> {
            allTocEntries.clear();
            allTocEntries.addAll(tocGenerator.generateTocEntriesFromText());
            setTableViewItems();
        });
    }

    private void setTableViewItems()
    {
        List<ChoosableTocEntry> usedTocEntries = allTocEntries.filtered(choosableTocEntry -> !showTocItemsCheckBox.isSelected() || choosableTocEntry.getChoosed());
        ObservableList<ChoosableTocEntry> tableViewItems = tableView.getItems();
        tableViewItems.clear();
        for (ChoosableTocEntry tocEntry : usedTocEntries)
        {
            addTocEntryToTableView(tocEntry, tableViewItems, 0);
        }
    }

    private void addTocEntryToTableView(ChoosableTocEntry tocEntry, ObservableList<ChoosableTocEntry> tableViewItems, int level)
    {
        tocEntry.setTitle(StringUtils.leftPad(tocEntry.getTitle(), tocEntry.getTitle().length() + level * 4, " "));
        tableViewItems.add(tocEntry);
        for (TocEntry childEntry : tocEntry.getChildren())
        {
            addTocEntryToTableView((ChoosableTocEntry)childEntry, tableViewItems, level + 1);
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
        Resource resource;
        if (currentBook.get().isEpub3())
        {
            resource = tocGenerator.generateNav(new ArrayList<>(tableView.getItems()));
        }
        else
        {
            resource = tocGenerator.generateNcx(new ArrayList<>(tableView.getItems()));
        }
        bookBrowserManager.refreshBookBrowser();
        editorTabManager.refreshEditorCode(resource);
        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }
}
