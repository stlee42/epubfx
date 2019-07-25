package de.machmireinebook.epubeditor.validation;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import org.apache.log4j.Logger;

import org.controlsfx.dialog.ExceptionDialog;

import com.adobe.epubcheck.api.EpubCheck;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.manager.EditorTabManager;

/**
 * User: Michail Jungierek
 * Date: 16.07.2019
 * Time: 20:53
 */
@Named
public class ValidationManager {
    private static final Logger logger = Logger.getLogger(ValidationManager.class);

    @Inject
    private EpubEditorConfiguration epubEditorConfiguration;
    @Inject
    private EditorTabManager editorTabManager;

    private TableView<ValidationMessage> tableView;
    private final ObjectProperty<Book> bookProperty = new SimpleObjectProperty<>(this, "book");

    private class ValidateEpubService extends Service<EpubCheckReport>
    {
        private Path epubFileName;

        ValidateEpubService(Path epubFileName)
        {
            this.epubFileName = epubFileName;
        }

        @Override
        protected Task<EpubCheckReport> createTask() {
            return new Task<>() {
                @Override
                protected EpubCheckReport call()
                {
                    return validateEpub(epubFileName.toFile());
                }
            };
        }
    }

    public void setTableView(TableView<ValidationMessage> tableView) {
        this.tableView = tableView;

        TableColumn<ValidationMessage, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(75);
        TableColumn<ValidationMessage, String> resCol = new TableColumn<>("File");
        resCol.setCellValueFactory(new PropertyValueFactory<>("resource"));
        resCol.setPrefWidth(150);
        TableColumn<ValidationMessage, String> lineCol = new TableColumn<>("Line");
        lineCol.setCellValueFactory(new PropertyValueFactory<>("line"));
        lineCol.setPrefWidth(50);
        TableColumn<ValidationMessage, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageCol.setPrefWidth(350);

        tableView.getColumns().setAll(Arrays.asList(typeCol, resCol, lineCol, messageCol));

        tableView.setPadding(new Insets(0, 0, 0, 0));
        tableView.setStyle("-fx-padding:0");

        tableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                ValidationMessage message = tableView.getSelectionModel().getSelectedItem();
                logger.info("double clicked on message " + message.getMessage() + " for resource " + message.getResource());
                Resource resource = getBook().getResources().getByHref(message.getResource());
                editorTabManager.openFileInEditor(resource);
            }
        });
    }

    public void startValidationEpub(Path epubFile)
    {
        ValidateEpubService service = new ValidateEpubService(epubFile);
        tableView.getItems().clear();
        Node placeholder = tableView.getPlaceholder();
        service.setOnSucceeded(event -> {
            EpubCheckReport report = service.getValue();
            List<ValidationMessage> messages = report.getMessages();
            if (messages.isEmpty())
            {
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.initOwner(tableView.getScene().getWindow());
                alert.setTitle("Result Epub Check");
                alert.setHeaderText("Result of the Epub Check");
                alert.getDialogPane().setGraphic(FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CHECK));
                alert.setContentText("The ebook is valid");
                alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
                alert.showAndWait();
                return;
            }
            tableView.getItems().addAll(FXCollections.observableList(messages));
        });
        service.setOnFailed(event -> {
            ExceptionDialog exceptionDialog = new ExceptionDialog(event.getSource().getException());
            exceptionDialog.initOwner(epubEditorConfiguration.getMainWindow());
            exceptionDialog.setTitle("Validation");
            exceptionDialog.setHeaderText(null);
            exceptionDialog.setContentText("Unknown error while validation ebook");
            exceptionDialog.showAndWait();
        });
        service.restart();
    }

    public EpubCheckReport validateEpub(File epubFile)
    {
        EpubCheckReport report = new EpubCheckReport();
        report.setReportingLevel(0);
        try
        {
            EpubCheck check = new EpubCheck(epubFile, report);
            check.validate();
        }
        catch (Exception e)
        {
            logger.error("", e);
            throw e;
        }
        return report;
    }

    public final ObjectProperty<Book> bookProperty() {
        return bookProperty;
    }
    public final Book getBook() {
        return bookProperty.get();
    }
}
