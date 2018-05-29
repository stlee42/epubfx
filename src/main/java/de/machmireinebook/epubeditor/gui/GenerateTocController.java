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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
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
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.manager.EditorTabManager;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * Created by Michail Jungierek, Acando GmbH on 17.05.2018
 */
public class GenerateTocController implements StandardController
{
    private static final Logger logger = Logger.getLogger(GenerateTocController.class);

    @FXML
    private TreeTableView<ChoosableTocEntry> treeTableView;
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

    private static GenerateTocController instance;

    public static GenerateTocController getInstance()
    {
        return instance;
    }



    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        treeTableView.setEditable(true);
        treeTableView.setShowRoot(false);

        TreeTableColumn<ChoosableTocEntry, String> tc = (TreeTableColumn<ChoosableTocEntry, String>) treeTableView.getColumns().get(0);
        tc.setEditable(true);
        tc.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
        tc.setCellFactory(param -> new TextFieldTreeTableCell<>(new DefaultStringConverter()));
        tc.setSortable(false);
        tc.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            ChoosableTocEntry tocEntry = event.getRowValue().getValue();
            tocEntry.setTitle(newValue);
        });

        TreeTableColumn<ChoosableTocEntry, String> tc2 = (TreeTableColumn<ChoosableTocEntry, String>) treeTableView.getColumns().get(1);
        tc2.setCellValueFactory(new TreeItemPropertyValueFactory<>("level"));
        tc2.setCellFactory(column -> {
            TextFieldTreeTableCell<ChoosableTocEntry, String> cell = new TextFieldTreeTableCell<>(new DefaultStringConverter());
            cell.getStyleClass().add("toc-level-cell");
            return cell;
        });
        tc2.setSortable(false);

        TreeTableColumn<ChoosableTocEntry, Boolean> tc3 = (TreeTableColumn<ChoosableTocEntry, Boolean>) treeTableView.getColumns().get(2);
        tc3.setEditable(true);
        tc3.setCellValueFactory(c -> {
            BooleanProperty property = new SimpleBooleanProperty(c.getValue().getValue().getChoosed());
            property.addListener((observable, oldValue, newValue) -> {
                c.getValue().getValue().setChoosed(newValue);
                setTableViewItems();
            });
            return property;
        });
        tc3.setCellFactory(column -> {
            CheckBoxTreeTableCell<ChoosableTocEntry, Boolean> cell = new CheckBoxTreeTableCell<>();
            cell.getStyleClass().add("toc-choosed-cell");
            return cell;
        });
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
        TreeItem<ChoosableTocEntry> tableViewRoot = treeTableView.getRoot();
        if (tableViewRoot == null)
        {
            tableViewRoot = new TreeItem<>();
            treeTableView.setRoot(tableViewRoot);
        }
        tableViewRoot.getChildren().clear();
        for (ChoosableTocEntry tocEntry : allTocEntries)
        {
            addTocEntryToTableView(tocEntry, tableViewRoot, 0);
        }
    }

    private void addTocEntryToTableView(ChoosableTocEntry tocEntry, TreeItem<ChoosableTocEntry> treeItem, int level)
    {
        int levelIncrement = 0;
        TreeItem<ChoosableTocEntry> newParent = treeItem;
        if (!showTocItemsCheckBox.isSelected() || tocEntry.getChoosed())
        {
            tocEntry.setDisplayLevel(level);
            newParent = new TreeItem<>(tocEntry);
            newParent.setExpanded(true);
            treeItem.getChildren().add(newParent);
            levelIncrement = 1;
        }
        for (TocEntry childEntry : tocEntry.getChildren())
        {
            addTocEntryToTableView((ChoosableTocEntry)childEntry, newParent, level + levelIncrement);
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
