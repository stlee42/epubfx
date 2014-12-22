package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import de.machmireinebook.commons.javafx.control.searchable.TableViewSearchable;
import de.machmireinebook.epubeditor.epublib.domain.Author;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.Relator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import static de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase.DCTag;

/**
 * User: mjungierek
 * Date: 27.07.2014
 * Time: 18:40
 */
public class EditMetadataController implements Initializable
{
    public static final Logger logger = Logger.getLogger(EditMetadataController.class);

    @FXML
    private TextField saveAsAuthorTextField;
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

    public enum MetadataTemplate
    {
        COVERAGE(DCTag.coverage, "coverage", null),
        CUSTOM_DATE(DCTag.date, "Date (custom)", "customidentifier"),
        CREATION_DATE(DCTag.date, "Date: Creation", "creation"),
        MODIFICATION_DATE(DCTag.date, "Date: Modification", "modificatin"),
        PUBLICATION_DATE(DCTag.date, "Date: Publication", "publication"),
        DESCRIPTION(DCTag.description, "Description", null),
        CUSTOM_IDENTIFIER(DCTag.description, "Identifier (custom)", "customidentifier"),
        DOI_IDENTIFIER(DCTag.description, "Identifier: DOI", "DOI"),
        ISBN_IDENTIFIER(DCTag.description, "Identifier: ISBN", "ISBN"),
        ISSN_IDENTIFIER(DCTag.description, "Identifier: ISSN", "ISSN"),
        LANGUAGE(DCTag.language, "Language", null),
        PUBLISHER(DCTag.publisher, "Publisher", null),
        RELATION(DCTag.relation, "Relation", null),
        RIGHTS(DCTag.rights, "Rights", null),
        SOURCE(DCTag.source, "Source", null),
        SUBJECT(DCTag.subject, "Subject", null),
        TITLE(DCTag.title, "Title", null),
        TYPE(DCTag.type, "Type", null),
        ;

        private String description;
        private DCTag dcTag;
        private String scheme;

        MetadataTemplate(DCTag dcTag, String description, String scheme)
        {
            this.dcTag = dcTag;
            this.description = description;
            this.scheme = scheme;
        }

        public DCTag getDcTag()
        {
            return dcTag;
        }

        public String getDescription()
        {
            return description;
        }

        public String getScheme()
        {
            return scheme;
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

    @SuppressWarnings("unchecked")
    public void addOtherContributorAction(ActionEvent actionEvent)
    {
        Stage chooserWindow = UIHelper.createChooserWindow();

        ChooserWindowController chooserWindowController = ChooserWindowController.getInstance();
        TableView<Relator> tableView = chooserWindowController.getChosserWindowTableView();
        chooserWindowController.getChooserWindowOkButton().setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                Relator relator = tableView.getSelectionModel().getSelectedItem();
                Author contributor = new Author("");
                contributor.setRelator(relator);
                otherContributorsTableView.getItems().add(contributor);
                chooserWindowController.getChooserWindow().close();
            }
        });
        tableView.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                MouseButton mb = event.getButton();
                int clicks = event.getClickCount();
                if ((mb.equals(MouseButton.PRIMARY) && clicks == 2) || mb.equals(MouseButton.MIDDLE))
                {
                    Relator relator = tableView.getSelectionModel().getSelectedItem();
                    Author contributor = new Author("");
                    contributor.setRelator(relator);
                    otherContributorsTableView.getItems().add(contributor);
                    chooserWindowController.getChooserWindow().close();
                }
            }
        });


        tableView.getColumns().clear();
        TableColumn tc = new TableColumn();
        tc.setPrefWidth(150);
        tableView.getColumns().add(tc);
        tc.setCellValueFactory(new PropertyValueFactory<>("name"));

        tc = new TableColumn();
        tc.setPrefWidth(300);
        tableView.getColumns().add(tc);
        tc.setCellValueFactory(new PropertyValueFactory<>("description"));

        chooserWindowController.getChooserWindowLabel().setText("Typ des Beteiligten auswählen");

        tableView.setItems(FXCollections.observableArrayList(Relator.values()));

        chooserWindow.show();
    }

    public void deleteOtherContributorAction(ActionEvent actionEvent)
    {
    }

    @SuppressWarnings("unchecked")
    public void addMetadateAction(ActionEvent actionEvent)
    {
        Stage chooserWindow = UIHelper.createChooserWindow();

        ChooserWindowController chooserWindowController = ChooserWindowController.getInstance();
        TableView<MetadataTemplate> tableView = chooserWindowController.getChosserWindowTableView();
        chooserWindowController.getChooserWindowOkButton().setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                MetadataTemplate template = tableView.getSelectionModel().getSelectedItem();
                MetadataElement metadataElement = new MetadataElement(template.getDcTag().getName(), "", template.getScheme());
                metadateTableView.getItems().add(metadataElement);
                chooserWindowController.getChooserWindow().close();
            }
        });
        tableView.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                MouseButton mb = event.getButton();
                int clicks = event.getClickCount();
                if ((mb.equals(MouseButton.PRIMARY) && clicks == 2) || mb.equals(MouseButton.MIDDLE))
                {
                    MetadataTemplate template = tableView.getSelectionModel().getSelectedItem();
                    MetadataElement metadataElement = new MetadataElement(template.getDcTag().getName(), "", template.getScheme());
                    metadateTableView.getItems().add(metadataElement);
                    chooserWindowController.getChooserWindow().close();
                }
            }
        });


        tableView.getColumns().clear();
        TableColumn tc = new TableColumn();
        tc.setPrefWidth(350);
        tableView.getColumns().add(tc);
        tc.setCellValueFactory(new PropertyValueFactory<>("description"));

        chooserWindowController.getChooserWindowLabel().setText("Metadateneigenschaft auswählen");

        tableView.setItems(FXCollections.observableArrayList(MetadataTemplate.values()));

        chooserWindow.show();
    }

    public void deleteMetadataAction(ActionEvent actionEvent)
    {
    }

    public void okButtonAction(ActionEvent actionEvent)
    {
        Metadata metadata = book.getMetadata();
        //auhtor
        Author firstAuthor = new Author(authorTextField.getText());
        firstAuthor.setFileAs(saveAsAuthorTextField.getText());
        metadata.getAuthors().clear();
        metadata.getContributors().clear();
        metadata.getAuthors().add(firstAuthor);
        List<Author> otherContributors = otherContributorsTableView.getItems();
        for (Author otherContributor : otherContributors)
        {
            logger.info("other contributor " + otherContributor);
            if (otherContributor.getRelator().equals(Relator.AUTHOR))
            {
                metadata.getAuthors().add(otherContributor);
            }
            else
            {
                metadata.getContributors().add(otherContributor);
            }
        }
        //title
        metadata.getTitles().clear();
        metadata.addTitle(titleTextField.getText());

        //metadata
        metadata.getDates().clear();
        metadata.getRights().clear();
        metadata.getTitles().clear();
        metadata.getIdentifiers().clear();
        metadata.getSubjects().clear();
        metadata.getTypes().clear();
        metadata.getDescriptions().clear();
        metadata.getPublishers().clear();
        List<MetadataElement> metadataElements = metadateTableView.getItems();
        for (MetadataElement metadataElement : metadataElements)
        {

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

        Map<QName, String> others = metadata.getOtherProperties();
        for (QName qName : others.keySet())
        {
            MetadataElement element = new MetadataElement(qName.getLocalPart(), others.get(qName), "");
            elements.add(element);
        }

        metadateTableView.setItems(elements);
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
    }
}
