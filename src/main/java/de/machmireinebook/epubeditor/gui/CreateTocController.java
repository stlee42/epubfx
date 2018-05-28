package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.property.BooleanProperty;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.toc.ChoosableTocEntry;
import de.machmireinebook.epubeditor.epublib.toc.TocGenerator;
import de.machmireinebook.epubeditor.javafx.cells.WrappableTextCellFactory;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.manager.EditorTabManager;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * Created by Michail Jungierek, Acando GmbH on 17.05.2018
 */
public class CreateTocController implements StandardController
{
    private static final Logger logger = Logger.getLogger(CreateTocController.class);

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
        tc.setCellValueFactory(new PropertyValueFactory<>("displayTitle"));
        tc.setCellFactory(param -> {
            TextFieldTableCell cell = new TextFieldTableCell<>(new DefaultStringConverter());
            cell.editingProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue){
                    logger.info("editing started with value " + cell.getItem());
                    ChoosableTocEntry tocEntry = tableView.getSelectionModel().getSelectedItem();
                    cell.setItem(tocEntry.getTitle());
                }
                else
                {
                    ChoosableTocEntry tocEntry = tableView.getSelectionModel().getSelectedItem();
                    tocEntry.setTitle(cell.getItem().toString());
                    cell.setItem(tocEntry.getDisplayTitle());
                }
            });
            return cell;
        });
//        tc.setOnEditStart(event -> logger.info("setOnEditStart " + event.getSource().getText()));
        tc.setOnEditCommit(event -> {
            String value = event.getNewValue();
            ChoosableTocEntry tocEntry = event.getRowValue();
            tocEntry.setTitle(value);
            /*TextFieldTableCell cell = event.getTableColumn();
            cell.setItem(tocEntry.getDisplayTitle());*/
        });
        tc.setSortable(false);

        TableColumn<ChoosableTocEntry, String> tc2 = (TableColumn<ChoosableTocEntry, String>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("level"));
        tc2.setCellFactory(new WrappableTextCellFactory<>());
        tc2.setSortable(false);

        TableColumn<ChoosableTocEntry, Boolean> tc3 = (TableColumn<ChoosableTocEntry, Boolean>) tableView.getColumns().get(2);
        tc3.setEditable(true);
        tc3.setCellValueFactory(c -> {
            BooleanProperty property = new SimpleBooleanProperty(c.getValue().getChoosed());
            property.addListener((observable, oldValue, newValue) -> {
                c.getValue().setChoosed(newValue);
                setTableViewItems();
            });
            return property;
        });
        tc3.setCellFactory(cell -> new CheckBoxTableCell<>());
        tc3.setSortable(false);

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
        ObservableList<ChoosableTocEntry> tableViewItems = tableView.getItems();
        tableViewItems.clear();
        for (ChoosableTocEntry tocEntry : allTocEntries)
        {
            addTocEntryToTableView(tocEntry, tableViewItems, 0);
        }
    }

    private void addTocEntryToTableView(ChoosableTocEntry tocEntry, ObservableList<ChoosableTocEntry> tableViewItems, int level)
    {
        int levelIncrement = 0;
        if (!showTocItemsCheckBox.isSelected() || tocEntry.getChoosed())
        {
            tocEntry.setDisplayLevel(level);
            tableViewItems.add(tocEntry);
            levelIncrement = 1;
        }
        for (TocEntry childEntry : tocEntry.getChildren())
        {
            addTocEntryToTableView((ChoosableTocEntry)childEntry, tableViewItems, level + levelIncrement);
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

    public void onOkAction()
    {
        try
        {
            TocGenerator.TocGeneratorResult result;
            List<TocEntry<? extends TocEntry>> tocEntriesToUseInToc = new ArrayList<>();

            for (ChoosableTocEntry tocEntry : allTocEntries)
            {
                addTocEntryToGeneratorResult(tocEntry, tocEntriesToUseInToc);
            }

            if (currentBook.get().isEpub3())
            {
                result = tocGenerator.generateNav(tocEntriesToUseInToc);
            }
            else
            {
                result = tocGenerator.generateNcx(tocEntriesToUseInToc);
            }

            Map<Resource, Document> resourcesToRewrite = result.getResourcesToRewrite();
            for (Resource resource : resourcesToRewrite.keySet())
            {
                resource.setData(XHTMLUtils.outputXHTMLDocument(resourcesToRewrite.get(resource)));
                editorTabManager.refreshEditorCode(resource);
            }

            bookBrowserManager.refreshBookBrowser();
            editorTabManager.refreshEditorCode(result.getTocResource());
        }
        catch (IOException | JDOMException e)
        {
            logger.error("", e);
        }
        stage.close();
    }

    private void addTocEntryToGeneratorResult(ChoosableTocEntry originalEntry, List<TocEntry<? extends TocEntry>> parent)
    {
        boolean elevateChildren = false;
        ChoosableTocEntry tocEntry = null;
        if (originalEntry.getChoosed())
        {
            tocEntry = originalEntry.clone();
            tocEntry.getChildren().clear(); //will be filled with choosed toc entries and not with all current childs
            parent.add(tocEntry);
        }
        else
        {
            elevateChildren = true;
        }

        for (TocEntry childEntry : originalEntry.getChildren())
        {
            if (elevateChildren)
            {
                addTocEntryToGeneratorResult((ChoosableTocEntry)childEntry, parent);
            }
            else
            {
                addTocEntryToGeneratorResult((ChoosableTocEntry)childEntry, tocEntry.getChildren());
            }
        }
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }
}
