package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import org.apache.log4j.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import de.machmireinebook.epubeditor.editor.EditorTabManager;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.toc.EditableTocEntry;
import de.machmireinebook.epubeditor.epublib.toc.TocGenerator;
import de.machmireinebook.epubeditor.manager.BookBrowserManager;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import static de.machmireinebook.epubeditor.epublib.Constants.IGNORE_IN_TOC;

/**
 * Created by Michail Jungierek
 */
public class GenerateTocController implements StandardController
{
    private static final Logger logger = Logger.getLogger(GenerateTocController.class);

    @FXML
    private Label headingLevelLabel;
    @FXML
    private Label showTocItemsLabel;
    @FXML
    private Button addAboveButton;
    @FXML
    private Button addBelowButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button selectTargetButton;
    @FXML
    private TreeTableView<EditableTocEntry> treeTableView;
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

    private final ObjectProperty<Book> currentBook = new SimpleObjectProperty<>(this, "currentBook");
    private Stage stage;
    private final ObservableList<EditableTocEntry> allTocEntries = FXCollections.observableArrayList();
    private final Map<Resource<Document>, Document> resourcesToRewrite = new HashMap<>();
    private final BooleanProperty editModeProperty = new SimpleBooleanProperty(this, "editMode");
    private final IntegerProperty levelToShowProperty = new SimpleIntegerProperty(this, "levelToShow");

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
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeTableView.setShowRoot(false);

        setEditMode(false);

        TreeTableColumn<EditableTocEntry, String> tc = (TreeTableColumn<EditableTocEntry, String>) treeTableView.getColumns().get(0);
        tc.setEditable(true);
        tc.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
        tc.setCellFactory(param -> new TextFieldTreeTableCell<>(new DefaultStringConverter()));
        tc.setSortable(false);
        tc.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            EditableTocEntry tocEntry = event.getRowValue().getValue();
            tocEntry.setTitle(newValue);
            tocEntry.setTitleChanged(true);
            if (tocEntry.getCorrespondingElement() != null)
            {
                tocEntry.getCorrespondingElement().setAttribute("title", newValue);
            }
        });

        TreeTableColumn<EditableTocEntry, String> tc2 = (TreeTableColumn<EditableTocEntry, String>) treeTableView.getColumns().get(1);
        tc2.setCellValueFactory(new TreeItemPropertyValueFactory<>("level"));
        tc2.setCellFactory(column -> {
            TextFieldTreeTableCell<EditableTocEntry, String> cell = new TextFieldTreeTableCell<>(new DefaultStringConverter());
            cell.getStyleClass().add("toc-level-cell");
            return cell;
        });
        tc2.setSortable(false);
        tc2.visibleProperty().bind(editModeProperty.not());

        TreeTableColumn<EditableTocEntry, Boolean> tc3 = (TreeTableColumn<EditableTocEntry, Boolean>) treeTableView.getColumns().get(2);
        tc3.setEditable(true);
        tc3.setCellValueFactory(c -> {
            EditableTocEntry tocEntry = c.getValue().getValue();
            BooleanProperty property = new SimpleBooleanProperty(tocEntry.getChoosed());
            property.addListener((observable, oldValue, newValue) -> {
                tocEntry.setChoosed(newValue);
                if (tocEntry.getCorrespondingElement() != null)
                {
                    Attribute att = tocEntry.getCorrespondingElement().getAttribute("class");
                    if (!newValue)
                    {
                        if (att != null)
                        {
                            att.setValue(att.getValue() + " " + IGNORE_IN_TOC);
                        }
                        else
                        {
                            tocEntry.getCorrespondingElement().setAttribute("class", IGNORE_IN_TOC);
                        }
                        resourcesToRewrite.put(tocEntry.getResource(), tocEntry.getDocument());
                    }
                    else
                    {
                        if (att != null)
                        {
                            att.setValue(att.getValue().replace(IGNORE_IN_TOC, "").trim());
                        }
                        resourcesToRewrite.remove(tocEntry.getResource());
                    }
                }
                setTableViewItems();
            });
            return property;
        });
        tc3.setCellFactory(column -> {
            CheckBoxTreeTableCell<EditableTocEntry, Boolean> cell = new CheckBoxTreeTableCell<>();
            cell.getStyleClass().add("toc-choosed-cell");
            return cell;
        });
        tc3.setSortable(false);
        tc3.visibleProperty().bind(editModeProperty.not());

        TreeTableColumn<EditableTocEntry, String> tc4 = (TreeTableColumn<EditableTocEntry, String>) treeTableView.getColumns().get(3);
        tc4.setEditable(true);
        tc4.setCellValueFactory(new TreeItemPropertyValueFactory<>("completeHref"));
        tc4.setCellFactory(param -> new TextFieldTreeTableCell<>(new DefaultStringConverter()));
        tc4.setSortable(false);
        tc4.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            EditableTocEntry tocEntry = event.getRowValue().getValue();
            tocEntry.setCompleteHref(newValue);
        });
        tc4.visibleProperty().bind(editModeProperty);
        
        showTocItemsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> setTableViewItems());
        showTocItemsCheckBox.setSelected(true);
        showTocItemsCheckBox.visibleProperty().bind(editModeProperty.not());
        showTocItemsLabel.visibleProperty().bind(editModeProperty.not());

        headingLevelComboBox.visibleProperty().bind(editModeProperty.not());
        headingLevelLabel.visibleProperty().bind(editModeProperty.not());

        levelToShowProperty.bind(Bindings.createIntegerBinding(() -> {
                if (headingLevelComboBox.getSelectionModel().getSelectedIndex() < 3) {
                    return headingLevelComboBox.getSelectionModel().getSelectedIndex();
                } else {
                    return 99;
                }
            }, headingLevelComboBox.getSelectionModel().selectedIndexProperty()));
        headingLevelComboBox.getSelectionModel().selectLast();

        levelToShowProperty.addListener((observable, oldValue, newValue) -> setTableViewItems());

        editModeProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue)
            {
                AnchorPane.setBottomAnchor(treeTableView, 62.0);
            }
            else
            {
                AnchorPane.setBottomAnchor(treeTableView, 125.0);
            }
        });

        tocGenerator.bookProperty().bind(currentBook);

        instance = this;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnShowing(event -> {
            resourcesToRewrite.clear();
            allTocEntries.clear();
            if (isEditMode())
            {
                allTocEntries.addAll(tocGenerator.generateTocEntriesFromToc());
            }
            else
            {
                allTocEntries.addAll(tocGenerator.generateTocEntriesFromText());
            }
            setTableViewItems();
        });
    }

    private void setTableViewItems()
    {
        TreeItem<EditableTocEntry> tableViewRoot = treeTableView.getRoot();
        if (tableViewRoot == null)
        {
            tableViewRoot = new TreeItem<>();
            treeTableView.setRoot(tableViewRoot);
        }
        tableViewRoot.getChildren().clear();
        for (EditableTocEntry tocEntry : allTocEntries)
        {
            addTocEntryToTableView(tocEntry, tableViewRoot, 0);
        }
    }

    private void addTocEntryToTableView(TocEntry tocEntry, TreeItem<EditableTocEntry> treeItem, int level)
    {
        int levelIncrement = 0;
        TreeItem<EditableTocEntry> newParent = treeItem;
        if (tocEntry instanceof EditableTocEntry &&
                (!showTocItemsCheckBox.isSelected() || ((EditableTocEntry)tocEntry).getChoosed()) &&
                ((!isEditMode() && TocGenerator.getLevel(tocEntry.getLevel()) <= levelToShowProperty.get()) || isEditMode())) {
            newParent = new TreeItem<>((EditableTocEntry)tocEntry);
            newParent.setExpanded(true);
            treeItem.getChildren().add(newParent);
            levelIncrement = 1;
        }

        for (TocEntry childEntry : tocEntry.getChildren()) {
            addTocEntryToTableView(childEntry, newParent, level + levelIncrement);
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

    public final BooleanProperty editModeProperty() {
        return editModeProperty;
    }
    public final boolean isEditMode() {
        return editModeProperty.get();
    }
    public final void setEditMode(boolean value) {
        editModeProperty.set(value);
    }

    public void renameButtonAction()
    {
        ObservableList<TreeTablePosition<EditableTocEntry, ?>> cells = treeTableView.getSelectionModel().getSelectedCells();
        treeTableView.edit(treeTableView.getSelectionModel().getSelectedIndex(), cells.get(0).getTableColumn());
    }

    public void higherLevelButtonAction()
    {

    }

    public void deeperLevelButtonAction()
    {

    }

    public void onOkAction()
    {
        try
        {
            TocGenerator.TocGeneratorResult result;
            List<TocEntry> tocEntriesToUseInToc = new ArrayList<>();

            for (TocEntry tocEntry : allTocEntries)
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

            Map<Resource<Document>, Document> allResourcesToRewrite = result.getResourcesToRewrite();
            allResourcesToRewrite.putAll(resourcesToRewrite);

            for (Resource<Document> resource : allResourcesToRewrite.keySet())
            {
                resource.setData(XHTMLUtils.outputXHTMLDocument(allResourcesToRewrite.get(resource), true, currentBook.get().getVersion()));
                editorTabManager.refreshEditorCode(resource);
            }

            bookBrowserManager.refreshBookBrowser();
            editorTabManager.refreshEditorCode(result.getTocResource());
            editorTabManager.totalRefreshPreview();
            currentBook.get().setBookIsChanged(true);
        }
        catch (IOException | JDOMException e)
        {
            logger.error("", e);
        }
        stage.close();
    }

    private void addTocEntryToGeneratorResult(TocEntry originalEntry, List<TocEntry> parent)
    {
        boolean elevateChildren = false;
        EditableTocEntry tocEntry = null;
        if (originalEntry instanceof EditableTocEntry && ((EditableTocEntry)originalEntry).getChoosed())
        {
            tocEntry = ((EditableTocEntry)originalEntry).clone();
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
                addTocEntryToGeneratorResult(childEntry, parent);
            }
            else
            {
                addTocEntryToGeneratorResult(childEntry, tocEntry.getChildren());
            }
        }
    }

    public void onCancelAction()
    {
        stage.close();
    }

    public void addAboveButtonAction()
    {
        TreeItem<EditableTocEntry> treeItem = treeTableView.getSelectionModel().getSelectedItem();
        EditableTocEntry parentTocEntry = treeItem.getParent().getValue();
        List<TocEntry> tocEntryList = parentTocEntry.getChildren();
        int index = tocEntryList.indexOf(treeItem.getValue());
        EditableTocEntry choosableTocEntry = new EditableTocEntry();
        choosableTocEntry.setChoosed(true);
        choosableTocEntry.setLevel(treeItem.getValue().getLevel());
        tocEntryList.add(index, choosableTocEntry);
        setTableViewItems();
    }

    public void addBelowButtonAction()
    {
        TreeItem<EditableTocEntry> treeItem = treeTableView.getSelectionModel().getSelectedItem();
        EditableTocEntry parentTocEntry = treeItem.getParent().getValue();
        List<TocEntry> tocEntryList = parentTocEntry.getChildren();
        int index = tocEntryList.indexOf(treeItem.getValue());
        EditableTocEntry choosableTocEntry = new EditableTocEntry();
        choosableTocEntry.setChoosed(true);
        choosableTocEntry.setLevel(treeItem.getValue().getLevel());

        if(index >= tocEntryList.size())
        {
            tocEntryList.add(choosableTocEntry);
        }
        else
        {
            tocEntryList.add(index + 1, choosableTocEntry);
        }
        setTableViewItems();
    }

    public void deleteButtonAction()
    {
        TreeItem<EditableTocEntry> treeItem = treeTableView.getSelectionModel().getSelectedItem();
        EditableTocEntry parentTocEntry = treeItem.getParent().getValue();
        List<TocEntry> tocEntryList = parentTocEntry.getChildren();
        tocEntryList.remove(treeItem.getValue());
        setTableViewItems();
    }

    public void selectTargetButtonAction()
    {

    }
}
