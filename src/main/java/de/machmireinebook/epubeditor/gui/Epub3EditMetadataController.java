package de.machmireinebook.epubeditor.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreTag;
import de.machmireinebook.epubeditor.epublib.domain.Relator;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Author;
import de.machmireinebook.epubeditor.epublib.domain.epub3.DublinCoreMetadataElement;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataProperty;

import jidefx.scene.control.searchable.TableViewSearchable;

/**
 * User: mjungierek
 * Date: 27.07.2014
 * Time: 18:40
 */
public class Epub3EditMetadataController implements Initializable
{
    private static final Logger logger = Logger.getLogger(Epub3EditMetadataController.class);

    @FXML
    private TextField saveAsAuthorTextField;
    @FXML
    private TableView<Author> otherContributorsTableView;
    @FXML
    private TableView<MetadataListItem> metadateTableView;
    @FXML
    private ComboBox<Locale> languageComboBox;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextField authorTextField;

    private static Epub3EditMetadataController instance;
    private Stage stage;
    private Book book;

    public class MetadataListItem
    {
        private String type;
        private String value;

        private String id;
        private String refines;
        private String scheme;
        private String language;

        public MetadataListItem(String type, String value)
        {
            this.type = type;
            this.value = value;
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

        public String getRefines()
        {
            return refines;
        }

        public void setRefines(String refines)
        {
            this.refines = refines;
        }

        public String getScheme()
        {
            return scheme;
        }

        public void setScheme(String scheme)
        {
            this.scheme = scheme;
        }

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getLanguage()
        {
            return language;
        }

        public void setLanguage(String language)
        {
            this.language = language;
        }
    }

    public enum MetadataTemplate
    {
        COVERAGE(DublinCoreTag.coverage, "coverage", null),
        CUSTOM_DATE(DublinCoreTag.date, "Date (custom)", "customidentifier"),
        CREATION_DATE(DublinCoreTag.date, "Date: Creation", "creation"),
        MODIFICATION_DATE(DublinCoreTag.date, "Date: Modification", "modification"),
        PUBLICATION_DATE(DublinCoreTag.date, "Date: Publication", "publication"),
        DESCRIPTION(DublinCoreTag.description, "Description", null),
        CUSTOM_IDENTIFIER(DublinCoreTag.description, "Identifier (custom)", "customidentifier"),
        DOI_IDENTIFIER(DublinCoreTag.description, "Identifier: DOI", "DOI"),
        ISBN_IDENTIFIER(DublinCoreTag.description, "Identifier: ISBN", "ISBN"),
        ISSN_IDENTIFIER(DublinCoreTag.description, "Identifier: ISSN", "ISSN"),
        LANGUAGE(DublinCoreTag.language, "Language", null),
        PUBLISHER(DublinCoreTag.publisher, "Publisher", null),
        RELATION(DublinCoreTag.relation, "Relation", null),
        RIGHTS(DublinCoreTag.rights, "Rights", null),
        SOURCE(DublinCoreTag.source, "Source", null),
        SUBJECT(DublinCoreTag.subject, "Subject", null),
        TITLE(DublinCoreTag.title, "Title", null),
        TYPE(DublinCoreTag.type, "Type", null),
        ;

        private String description;
        private DublinCoreTag dcTag;
        private String scheme;

        MetadataTemplate(DublinCoreTag dcTag, String description, String scheme)
        {
            this.dcTag = dcTag;
            this.description = description;
            this.scheme = scheme;
        }

        public DublinCoreTag getDcTag()
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
        logger.info("initialize");
        new TableViewSearchable<>(otherContributorsTableView);
        otherContributorsTableView.setEditable(true);
        TableColumn<Author, String> tc = (TableColumn<Author, String>) otherContributorsTableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<Author, String> tc2 = (TableColumn<Author, String>) otherContributorsTableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("name"));
        tc2.setEditable(true);
        tc2.setCellFactory(TextFieldTableCell.forTableColumn());
        tc2.setOnEditCommit(event -> {
            String value = event.getNewValue();
            Author author = event.getTableView().getItems().get(event.getTablePosition().getRow());
            author.setName(value);
        });

        TableColumn<Author, String> tc3 = (TableColumn<Author, String>) otherContributorsTableView.getColumns().get(2);
        tc3.setCellValueFactory(new PropertyValueFactory<>("fileAs"));

        new TableViewSearchable<>(metadateTableView);
        metadateTableView.setEditable(true);

        TableColumn<MetadataListItem, String> tc4 = (TableColumn<MetadataListItem, String>) metadateTableView.getColumns().get(0);
        tc4.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<MetadataListItem, String> tc5 = (TableColumn<MetadataListItem, String>) metadateTableView.getColumns().get(1);
        tc5.setCellValueFactory(new PropertyValueFactory<>("value"));
        tc5.setEditable(true);
        tc5.setCellFactory(TextFieldTableCell.forTableColumn());
        tc5.setOnEditCommit(event -> {
            String value = event.getNewValue();
            MetadataListItem element = event.getTableView().getItems().get(event.getTablePosition().getRow());
            element.setValue(value);
        });

        TableColumn<MetadataListItem, String> tc6 = (TableColumn<MetadataListItem, String>) metadateTableView.getColumns().get(2);
        tc6.setCellValueFactory(new PropertyValueFactory<>("id"));
        tc6.setEditable(true);
        tc6.setCellFactory(TextFieldTableCell.forTableColumn());
        tc6.setOnEditCommit(event -> {
            String value = event.getNewValue();
            MetadataListItem element = event.getTableView().getItems().get(event.getTablePosition().getRow());
            element.setId(value);
        });

        TableColumn<MetadataListItem, String> tc7 = (TableColumn<MetadataListItem, String>) metadateTableView.getColumns().get(3);
        tc7.setCellValueFactory(new PropertyValueFactory<>("refines"));
        tc7.setEditable(true);
        tc7.setCellFactory(TextFieldTableCell.forTableColumn());
        tc7.setOnEditCommit(event -> {
            String value = event.getNewValue();
            MetadataListItem element = event.getTableView().getItems().get(event.getTablePosition().getRow());
            element.setRefines(value);
        });

        TableColumn<MetadataListItem, String> tc8 = (TableColumn<MetadataListItem, String>) metadateTableView.getColumns().get(4);
        tc8.setCellValueFactory(new PropertyValueFactory<>("scheme"));

        instance = this;
    }

    public static Epub3EditMetadataController getInstance()
    {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public void addOtherContributorAction(ActionEvent actionEvent)
    {
        Stage chooserWindow = new UIHelper().createChooserWindow();

        ChooserWindowController chooserWindowController = ChooserWindowController.getInstance();
        TableView<Relator> tableView = chooserWindowController.getChosserWindowTableView();
        chooserWindowController.getChooserWindowOkButton().setOnAction(event -> {
            Relator relator = tableView.getSelectionModel().getSelectedItem();
            Author contributor = new Author(null, null,null);
            contributor.setRole(relator);
            otherContributorsTableView.getItems().add(contributor);
            chooserWindowController.getChooserWindow().close();
        });
        tableView.setOnMouseClicked(event -> {
            MouseButton mb = event.getButton();
            int clicks = event.getClickCount();
            if ((mb.equals(MouseButton.PRIMARY) && clicks == 2) || mb.equals(MouseButton.MIDDLE))
            {
                Relator relator = tableView.getSelectionModel().getSelectedItem();
                Author contributor = new Author(null, null, null);
                contributor.setRole(relator);
                otherContributorsTableView.getItems().add(contributor);
                chooserWindowController.getChooserWindow().close();
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
        Stage chooserWindow = new UIHelper().createChooserWindow();

        ChooserWindowController chooserWindowController = ChooserWindowController.getInstance();
        TableView<MetadataTemplate> tableView = chooserWindowController.getChosserWindowTableView();
        chooserWindowController.getChooserWindowOkButton().setOnAction(event -> {
            MetadataTemplate template = tableView.getSelectionModel().getSelectedItem();
            MetadataListItem metadataElement = new MetadataListItem(template.getDcTag().getName(), "");
            metadataElement.setScheme(template.getScheme());
            metadateTableView.getItems().add(metadataElement);
            chooserWindowController.getChooserWindow().close();
        });
        tableView.setOnMouseClicked(event -> {
            MouseButton mb = event.getButton();
            int clicks = event.getClickCount();
            if ((mb.equals(MouseButton.PRIMARY) && clicks == 2) || mb.equals(MouseButton.MIDDLE))
            {
                MetadataTemplate template = tableView.getSelectionModel().getSelectedItem();
                MetadataListItem metadataElement = new MetadataListItem(template.getDcTag().getName(), "");
                metadataElement.setScheme(template.getScheme());
                metadateTableView.getItems().add(metadataElement);
                chooserWindowController.getChooserWindow().close();
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
        Metadata metadata = (Metadata) book.getMetadata();
        //auhtor
        Author firstAuthor = new Author(null, authorTextField.getText(), null);
        firstAuthor.setFileAs(saveAsAuthorTextField.getText());
        metadata.getAuthors().clear();
        metadata.getContributors().clear();
        metadata.getAuthors().add(firstAuthor);
        List<Author> otherContributors = otherContributorsTableView.getItems();
        for (Author otherContributor : otherContributors)
        {
            logger.info("other contributor " + otherContributor);
            if (otherContributor.getRole() != null && otherContributor.getRole().getValue() != null
                    && otherContributor.getRole().getValue().equals(Relator.AUTHOR.getCode()))
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
        metadata.getRights().clear();
        metadata.getTitles().clear();
        metadata.getEpub3Identifiers().clear();
        metadata.getSubjects().clear();
        metadata.getTypes().clear();
        metadata.getDescriptions().clear();
        metadata.getPublishers().clear();
        List<MetadataListItem> metadataElements = metadateTableView.getItems();
        for (MetadataListItem metadataElement : metadataElements)
        {
            if (metadataElement.getType().equals("publication-date")) {
                metadata.getPublicationDate().setValue(metadataElement.getValue());
            }
            if (metadataElement.getType().equals("right")) {
                metadata.getRights().add(metadataElement.getValue());
            }
            if (metadataElement.getType().equals("title")) {
                DublinCoreMetadataElement titleDCElement = new DublinCoreMetadataElement(metadataElement.getValue());
                titleDCElement.setId(metadataElement.getId());
                titleDCElement.setLanguage(metadataElement.getLanguage());
                metadata.getTitles().add(titleDCElement);
            }
            if (metadataElement.getType().equals("identifier")) {
                Identifier idDCElement = new Identifier();
                idDCElement.setValue(metadataElement.getValue());
                metadata.getIdentifiers().add(idDCElement);
            }
            if (metadataElement.getType().equals("subject")) {
                metadata.getSubjects().add(metadataElement.getValue());
            }
            if (metadataElement.getType().equals("type")) {
                metadata.getTypes().add(metadataElement.getValue());
            }
            if (metadataElement.getType().equals("description")) {
                metadata.getDescriptions().add(metadataElement.getValue());
            }
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
        Metadata metadata = (Metadata) book.getMetadata();
        Author firstAuthor = metadata.getFirstAuthor();
        if (firstAuthor != null)
        {
            authorTextField.setText(firstAuthor.getName());
            saveAsAuthorTextField.setText(firstAuthor.getFileAs() != null ? firstAuthor.getFileAs().getValue() : null);
        }
        titleTextField.setText(metadata.getFirstTitle());

        List<Author> authorsAndContributors = new ArrayList<>();
        authorsAndContributors.addAll(metadata.getAuthorsWithoutFirst());
        authorsAndContributors.addAll(metadata.getContributors());
        otherContributorsTableView.setItems(FXCollections.observableList(authorsAndContributors));

        ObservableList<MetadataListItem> elements = FXCollections.observableList(new ArrayList<>());
        MetadataDate pubDate = metadata.getPublicationDate();
        if (pubDate != null)
        {
            MetadataListItem pubDateElement = new MetadataListItem("publication-date", pubDate.getValue());
            elements.add(pubDateElement);
        }

        List<String> rights = metadata.getRights();
        for (String right : rights)
        {
            MetadataListItem rightElement = new MetadataListItem("right", right);
            elements.add(rightElement);
        }

        List<DublinCoreMetadataElement> titles = metadata.getTitles();
        if (titles.size() > 0)
        {
            titles = titles.subList(1, titles.size());
            for (DublinCoreMetadataElement title : titles)
            {
                MetadataListItem titleElement = new MetadataListItem("title", title.getValue());
                titleElement.setId(title.getId());
                titleElement.setLanguage(title.getLanguage());
                elements.add(titleElement);
            }
        }

        List<Identifier> identifiers = metadata.getEpub3Identifiers();
        for (Identifier identifier : identifiers)
        {
            MetadataListItem idElement = new MetadataListItem("identifier", identifier.getValue());
            elements.add(idElement);
        }

        List<String> subjects = metadata.getSubjects();
        for (String subject : subjects)
        {
            MetadataListItem subjectElement = new MetadataListItem("subject", subject);
            elements.add(subjectElement);
        }

        List<String> types = metadata.getTypes();
        for (String type : types)
        {
            MetadataListItem typeElement = new MetadataListItem("type", type);
            elements.add(typeElement);
        }

        List<String> descriptions = metadata.getDescriptions();
        for (String description : descriptions)
        {
            MetadataListItem descElement = new MetadataListItem("description", description);
            elements.add(descElement);
        }

        List<String> publishers = metadata.getPublishers();
        for (String publisher : publishers)
        {
            MetadataListItem publisherElement = new MetadataListItem("publisher", publisher);
            elements.add(publisherElement);
        }

        List<MetadataProperty> others = metadata.getEpub3MetaProperties();
        for (MetadataProperty other : others)
        {
            MetadataListItem otherElement = new MetadataListItem(other.getProperty(), other.getValue());
            if (StringUtils.isNotEmpty(other.getScheme()))
            {
                otherElement.setScheme(other.getScheme());
            }
            if (StringUtils.isNotEmpty(other.getRefines()))
            {
                otherElement.setRefines(other.getRefines());
            }
            elements.add(otherElement);
        }

        metadateTableView.setItems(elements);
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
    }
}
