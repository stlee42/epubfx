package de.machmireinebook.epubeditor.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipInputStream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.BookTemplate;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Epub2Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub3.RenditionLayout;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Epub3MetadataProperty;
import de.machmireinebook.epubeditor.epublib.epub.EpubReader;
import de.machmireinebook.epubeditor.javafx.cells.ImageCellFactory;
import de.machmireinebook.epubeditor.javafx.cells.WrappableTextCellFactory;

import jidefx.scene.control.searchable.TableViewSearchable;

import static de.machmireinebook.epubeditor.epublib.domain.BookTemplate.MINIMAL_EPUB_2_BOOK;

/**
 * User: mjungierek
 * Date: 21.08.2014
 * Time: 21:01
 */
public class NewEBookController implements StandardController
{
    private static final Logger logger = Logger.getLogger(NewEBookController.class);

    DirectoryStream.Filter<Path> filter = file -> (!Files.isDirectory(file) && file.getFileName().toString().endsWith(".epub"));

    public ListView<String> typeListView;
    public TableView<BookTemplate> tableView;
    private ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>();
    private Stage stage;
    private static NewEBookController instance;

    private ObservableList<BookTemplate> epub2Books = FXCollections.observableArrayList();
    private ObservableList<BookTemplate> epub3ReflowableBooks = FXCollections.observableArrayList();
    private ObservableList<BookTemplate> epub3PrepaginatedBooks = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        ObservableList<String> epubTypes = FXCollections.observableArrayList("EPUB 2", "EPUB 3 Reflowable Layout", "EPUB3 Fixed Layout");
        typeListView.setItems(epubTypes);
        typeListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {
            if ((int) newValue == 0)
            {
                tableView.setItems(epub2Books);
            }
            else if ((int) newValue == 1)
            {
                tableView.setItems(epub3ReflowableBooks);
            }
            else if ((int) newValue == 2)
            {
                tableView.setItems(epub3PrepaginatedBooks);
            }
        });
        typeListView.getSelectionModel().select(0);

        TableViewSearchable<BookTemplate> searchable = new TableViewSearchable<>(tableView);
        searchable.setCaseSensitive(false);

        TableColumn<BookTemplate, String> tc = (TableColumn<BookTemplate, String>) tableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("name"));
        tc.setCellFactory(new WrappableTextCellFactory<>());
        tc.setSortable(true);

        TableColumn<BookTemplate, String> tc2 = (TableColumn<BookTemplate, String>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("description"));
        tc2.setCellFactory(new WrappableTextCellFactory<>());
        tc2.setSortable(true);

        TableColumn<BookTemplate, Image> tc3 = (TableColumn<BookTemplate, Image>) tableView.getColumns().get(2);
        tc3.setCellValueFactory(new PropertyValueFactory<>("cover"));
        tc3.setCellFactory(new ImageCellFactory<>(null, 100d));
        tc3.setSortable(false);

        instance = this;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnShowing(event -> findTemplates());
    }

    private void findTemplates()
    {
        epub2Books.clear();
        epub2Books.add(MINIMAL_EPUB_2_BOOK);
        epub3ReflowableBooks.clear();
        epub3PrepaginatedBooks.clear();
        try
        {
            Path templatePath = new File(NewEBookController.class.getResource("/epub/templates/").getFile()).toPath();
            DirectoryStream<Path> stream = Files.newDirectoryStream(templatePath, filter);
            for (Path path : stream)
            {
                ZipInputStream zis = new ZipInputStream(new FileInputStream(path.toFile()));
                Book book = new EpubReader().readEpub(zis);
                if (book.getVersion() == EpubVersion.VERSION_2)
                {
                    Image cover = null;
                    if (book.getCoverImage() != null)
                    {
                        cover = book.getCoverImage().asNativeFormat();
                    }
                    Epub2Metadata metadata = (Epub2Metadata) book.getMetadata();
                    List<String> descriptions = metadata.getDescriptions();
                    String description = "";
                    if (descriptions.size() > 0)
                    {
                        description = descriptions.get(0);
                    }
                    epub2Books.add(new BookTemplate(book.getTitle(), cover, description, path, 2.0));
                }
                else if (book.getVersion() == EpubVersion.VERSION_3 || book.getVersion() == EpubVersion.VERSION_3_1)
                {
                    Image cover = null;
                    if (book.getCoverImage() != null)
                    {
                        cover = book.getCoverImage().asNativeFormat();
                    }
                    Metadata metadata = (Metadata) book.getMetadata();
                    List<String> descriptions = metadata.getDescriptions();
                    String description = "";
                    if (descriptions.size() > 0)
                    {
                        description = descriptions.get(0);
                    }
                    BookTemplate template = new BookTemplate(book.getTitle(), cover, description, path, 3.0);
                    List<Epub3MetadataProperty> otherProperties = metadata.getEpub3MetaProperties();
                    boolean found = false;
                    for (Epub3MetadataProperty otherMetadataProperty : otherProperties)
                    {
                        String property = otherMetadataProperty.getProperty();
                        if (RenditionLayout.qName.equals(property))
                        {
                            found = true;
                            if (RenditionLayout.PRE_PAGINATED.getValue().equals(otherMetadataProperty.getValue()))
                            {
                                epub3PrepaginatedBooks.add(template);
                                break;
                            }
                            else if (RenditionLayout.REFLOWABLE.getValue().equals(otherMetadataProperty.getValue()))
                            {
                                epub3ReflowableBooks.add(template);
                                break;
                            }
                        }
                    }
                    if (!found)
                    {
                        epub3ReflowableBooks.add(template);
                    }
                }
            }
        }
        catch (IOException e)
        {
            logger.error("", e);
        }

    }

    @Override
    public Stage getStage()
    {
        return stage;
    }

    @Override
    public ObjectProperty<Book> currentBookProperty()
    {
        return currentBookProperty;
    }

    public static NewEBookController getInstance()
    {
        return instance;
    }

    public void onOkAction(ActionEvent actionEvent) throws IOException
    {
        if (tableView.getSelectionModel().getSelectedItem().equals(MINIMAL_EPUB_2_BOOK))
        {
            Book minimalBook = Book.createMinimalBook();
            currentBookProperty.set(minimalBook);
            currentBookProperty.get().setBookIsChanged(true);
        }
        else
        {
            BookTemplate template = tableView.getSelectionModel().getSelectedItem();
            Book book = new EpubReader().readEpub(template.getPath().toFile());
            currentBookProperty.set(book);
            currentBookProperty.get().setBookIsChanged(true);
        }
        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }
}
