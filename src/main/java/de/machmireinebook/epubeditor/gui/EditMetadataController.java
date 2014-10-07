package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import de.machmireinebook.commons.javafx.control.searchable.TableViewSearchable;
import de.machmireinebook.epubeditor.epublib.domain.Author;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.MetadataDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.apache.commons.lang.StringUtils;

/**
 * User: mjungierek
 * Date: 27.07.2014
 * Time: 18:40
 */
public class EditMetadataController implements Initializable
{
    public TextField saveAsAuthorTextField;
    @FXML
    private TableView<Author> otherContributorsTableView;
    @FXML
    private TableView<MetadataElement> metadateTableView;
    @FXML
    private ComboBox<Locale> languageComboBox;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextField authorTextField;

    private static EditMetadataController instance;
    private Stage stage;
    private Book book;

    public class MetadataElement
    {
        private String type;
        private String value;
        private String subtype;

        public MetadataElement(String type, String value, String subtype)
        {
            this.type = type;
            this.value = value;
            this.subtype = subtype;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        public String getSubtype()
        {
            return subtype;
        }

        public void setSubtype(String subtype)
        {
            this.subtype = subtype;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        new TableViewSearchable<>(otherContributorsTableView);
        otherContributorsTableView.setEditable(true);
        TableColumn<Author, String> tc = (TableColumn<Author, String>) otherContributorsTableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("relator"));

        TableColumn<Author, String> tc2 = (TableColumn<Author, String>) otherContributorsTableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("name"));
        tc2.setEditable(true);
        tc2.setCellFactory(TextFieldTableCell.<Author>forTableColumn());
        tc2.setOnEditCommit(event -> {
            String value = event.getNewValue();
            Author author = event.getTableView().getItems().get(event.getTablePosition().getRow());
            author.setName(value);
        });

        TableColumn<Author, String> tc3 = (TableColumn<Author, String>) otherContributorsTableView.getColumns().get(2);
        tc3.setCellValueFactory(new PropertyValueFactory<>("fileAs"));

        new TableViewSearchable<>(metadateTableView);
        metadateTableView.setEditable(true);

        TableColumn<MetadataElement, String> tc4 = (TableColumn<MetadataElement, String>) metadateTableView.getColumns().get(0);
        tc4.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<MetadataElement, String> tc5 = (TableColumn<MetadataElement, String>) metadateTableView.getColumns().get(1);
        tc5.setCellValueFactory(new PropertyValueFactory<>("value"));
        tc5.setEditable(true);
        tc5.setCellFactory(TextFieldTableCell.<MetadataElement>forTableColumn());
        tc5.setOnEditCommit(event -> {
            String value = event.getNewValue();
            MetadataElement element = event.getTableView().getItems().get(event.getTablePosition().getRow());
            element.setValue(value);
        });

        TableColumn<MetadataElement, String> tc6 = (TableColumn<MetadataElement, String>) metadateTableView.getColumns().get(2);
        tc6.setCellValueFactory(new PropertyValueFactory<>("subtype"));

        instance = this;
    }

    public static EditMetadataController getInstance()
    {
        return instance;
    }

    public void addOtherContributorAction(ActionEvent actionEvent)
    {


    }

    public void deleteOtherContributorAction(ActionEvent actionEvent)
    {
    }

    public void addMetadateAction(ActionEvent actionEvent)
    {
    }

    public void deleteMetadataAction(ActionEvent actionEvent)
    {
    }

    public void okButtonAction(ActionEvent actionEvent)
    {
        Metadata metadata = book.getMetadata();
        Author firstAuthor = metadata.getFirstAuthor();
        if (firstAuthor != null)
        {
            firstAuthor.setName(authorTextField.getText());
            firstAuthor.setFileAs(saveAsAuthorTextField.getText());
        }
        else if (StringUtils.isNotEmpty(authorTextField.getText()))
        {
            firstAuthor = new Author(authorTextField.getText());
            firstAuthor.setFileAs(saveAsAuthorTextField.getText());
            metadata.getAuthors().add(firstAuthor);
        }
        stage.close();
    }

    public void cancelButtonAction(ActionEvent actionEvent)
    {
        stage.close();
    }

    public void setBook(Book book)
    {
        this.book = book;
        Metadata metadata = book.getMetadata();
        Author firstAuthor = metadata.getFirstAuthor();
        if (firstAuthor != null)
        {
            authorTextField.setText(firstAuthor.getName());
            saveAsAuthorTextField.setText(firstAuthor.getFileAs());
        }
        titleTextField.setText(metadata.getFirstTitle());

        List<Author> authorsAndContributors = new ArrayList<>();
        authorsAndContributors.addAll(metadata.getAuthorsWithoutFirst());
        authorsAndContributors.addAll(metadata.getContributors());
        otherContributorsTableView.setItems(FXCollections.observableList(authorsAndContributors));

        ObservableList<MetadataElement> elements = FXCollections.observableList(new ArrayList<>());
        List<MetadataDate> dates = metadata.getDates();
        for (MetadataDate date : dates)
        {
            MetadataElement element = new MetadataElement("date", date.getValue(), date.getEvent().toString());
            elements.add(element);
        }

        List<String> rights = metadata.getRights();
        for (String right : rights)
        {
            MetadataElement element = new MetadataElement("right", right, "");
            elements.add(element);
        }

        List<String> titles = metadata.getTitles();
        if (titles.size() > 0)
        {
            titles = titles.subList(1, titles.size());
            for (String title : titles)
            {
                MetadataElement element = new MetadataElement("title", title, "");
                elements.add(element);
            }
        }

        List<Identifier> identifiers = metadata.getIdentifiers();
        for (Identifier identifier : identifiers)
        {
            MetadataElement element = new MetadataElement("identifier", identifier.getValue(), identifier.getScheme());
            elements.add(element);
        }

        List<String> subjects = metadata.getSubjects();
        for (String subject : subjects)
        {
            MetadataElement element = new MetadataElement("subject", subject, "");
            elements.add(element);
        }

        List<String> types = metadata.getTypes();
        for (String type : types)
        {
            MetadataElement element = new MetadataElement("type", type, "");
            elements.add(element);
        }

        List<String> descriptions = metadata.getDescriptions();
        for (String description : descriptions)
        {
            MetadataElement element = new MetadataElement("description", description, "");
            elements.add(element);
        }

        List<String> publishers = metadata.getPublishers();
        for (String publisher : publishers)
        {
            MetadataElement element = new MetadataElement("publisher", publisher, "");
            elements.add(element);
        }

        metadateTableView.setItems(elements);
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
    }
}
