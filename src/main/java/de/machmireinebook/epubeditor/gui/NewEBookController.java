package de.machmireinebook.epubeditor.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.zip.ZipInputStream;

import javax.xml.namespace.QName;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.BookTemplate;
import de.machmireinebook.epubeditor.epublib.domain.RenditionLayout;
import de.machmireinebook.epubeditor.epublib.epub.EpubReader;
import de.machmireinebook.epubeditor.javafx.cells.ImageCellFactory;
import de.machmireinebook.epubeditor.javafx.cells.WrappableTextCellFactory;
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
import jidefx.scene.control.searchable.TableViewSearchable;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 21.08.2014
 * Time: 21:01
 */
public class NewEBookController implements StandardController
{
    public static final Logger logger = Logger.getLogger(NewEBookController.class);

    DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>()
    {
        public boolean accept(Path file) throws IOException
        {
            return (!Files.isDirectory(file) && file.getFileName().toString().endsWith(".epub"));
        }
    };

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

        epub2Books.add(BookTemplate.MINIMAL_EPUB_2_BOOK);
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
        try
        {
            Path templatePath = new File(NewEBookController.class.getResource("/epub/templates/").getFile()).toPath();
            DirectoryStream<Path> stream = Files.newDirectoryStream(templatePath, filter);
            for (Path path : stream)
            {
                ZipInputStream zis = new ZipInputStream(new FileInputStream(path.toFile()));
                Book book = new EpubReader().readEpub(zis, "UTF-8");
                if (book.getVersion() == 2.0)
                {
                    Image cover = null;
                    if (book.getCoverImage() != null)
                    {
                        cover = book.getCoverImage().asNativeFormat();
                    }
                    List<String> descriptions = book.getMetadata().getDescriptions();
                    String description = "";
                    if (descriptions.size() > 0)
                    {
                        description = descriptions.get(0);
                    }
                    epub2Books.add(new BookTemplate(book.getTitle(), cover, description, path, 2.0));
                }
                else if (book.getVersion() == 3.0)
                {
                    Image cover = null;
                    if (book.getCoverImage() != null)
                    {
                        cover = book.getCoverImage().asNativeFormat();
                    }
                    List<String> descriptions = book.getMetadata().getDescriptions();
                    String description = "";
                    if (descriptions.size() > 0)
                    {
                        description = descriptions.get(0);
                    }
                    BookTemplate template = new BookTemplate(book.getTitle(), cover, description, path, 3.0);
                    Map<QName, String> otherProperties = book.getMetadata().getOtherProperties();
                    boolean found = false;
                    for (QName qName : otherProperties.keySet())
                    {
                        if (RenditionLayout.qName.equals(qName.getPrefix() + ":" + qName.getLocalPart()))
                        {
                            found = true;
                            if (RenditionLayout.PRE_PAGINATED.getValue().equals(otherProperties.get(qName)))
                            {
                                epub3PrepaginatedBooks.add(template);
                                break;
                            }
                            else if (RenditionLayout.REFLOWABLE.getValue().equals(otherProperties.get(qName)))
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

    public void onOkAction(ActionEvent actionEvent)
    {
        if (tableView.getSelectionModel().getSelectedIndex() == 0)
        {
            Book minimalBook = Book.createMinimalBook();
            currentBookProperty.set(minimalBook);
            currentBookProperty.get().setBookIsChanged(true);
        }
        else
        {
            //
        }
        stage.close();
    }

    public void onCancelAction(ActionEvent actionEvent)
    {
        stage.close();
    }
}
