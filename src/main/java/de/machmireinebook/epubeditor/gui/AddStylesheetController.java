package de.machmireinebook.epubeditor.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.javafx.cells.WrappableTextCellFactory;
import de.machmireinebook.epubeditor.manager.EditorTabManager;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

/**
 * User: mjungierek
 * Date: 23.08.2014
 * Time: 21:05
 */
public class AddStylesheetController implements StandardController
{
    private static final Logger logger = Logger.getLogger(AddStylesheetController.class);
    public TableView<StylesheetResource> tableView;

    private Stage stage;
    private ObjectProperty<Book> currentBookProperty = new SimpleObjectProperty<>();
    private List<Resource> xhtmlResources = new ArrayList<>();
    private Map<Element, Resource> headElements = new HashMap<>();
    private ObservableList<StylesheetResource> stylesheetResources = FXCollections.observableArrayList();

    private static AddStylesheetController instance;
    private EditorTabManager editorManager;

    public void setEditorManager(EditorTabManager editorManager)
    {
        this.editorManager = editorManager;
    }

    public class StylesheetResource
    {
        private Resource stylesheet;
        private int totalCount;
        private int count = 0;

        private SimpleBooleanProperty included = new SimpleBooleanProperty();

        public StylesheetResource(Resource stylesheet)
        {
            this.stylesheet = stylesheet;
        }

        public Resource getStylesheet()
        {
            return stylesheet;
        }

        public void initIncluded()
        {
            included.set(count >= totalCount);
        }

        public void setTotalCount(int totalCount)
        {
            this.totalCount = totalCount;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof StylesheetResource))
            {
                return false;
            }

            StylesheetResource that = (StylesheetResource) o;

            if (!stylesheet.equals(that.stylesheet))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return stylesheet.hashCode();
        }

        public String getHref()
        {
            return stylesheet.getHref();
        }

        public void increase()
        {
            count++;
        }

        public boolean isIncluded()
        {
            return included.get();
        }

        public SimpleBooleanProperty includedProperty()
        {
            return included;
        }

        public void setIncluded(boolean included)
        {
            this.included.set(included);
        }

        public void resetCount()
        {
            count = 0;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        tableView.setEditable(true);
        TableColumn<StylesheetResource, ObservableValue<Boolean>> tc = (TableColumn<StylesheetResource, ObservableValue<Boolean>>) tableView.getColumns().get(0);
        tc.setCellValueFactory(new PropertyValueFactory<>("included"));
        Callback<Integer, ObservableValue<Boolean>> propertyCallback = param -> {
            if (param != null)
            {
                return stylesheetResources.get(param).includedProperty();
            }
            return null;
        };
        tc.setEditable(true);
        tc.setCellFactory(CheckBoxTableCell.forTableColumn(propertyCallback, false));
        tc.setSortable(true);

        TableColumn<StylesheetResource, String> tc2 = (TableColumn<StylesheetResource, String>) tableView.getColumns().get(1);
        tc2.setCellValueFactory(new PropertyValueFactory<>("href"));
        tc2.setCellFactory(new WrappableTextCellFactory<>());
        tc2.setSortable(true);

        currentBookProperty.addListener((observable, oldValue, newValue) ->
        {
            stylesheetResources.clear();
            if (newValue != null)
            {
                List<Resource> cssResources = newValue.getResources().getResourcesByMediaType(MediaType.CSS);
                for (Resource cssResource : cssResources)
                {
                    StylesheetResource stylesheetResource = new StylesheetResource(cssResource);
                    stylesheetResources.add(stylesheetResource);
                }

                newValue.getResources().getResourcesMap().addListener((MapChangeListener<String, Resource>) change -> {
                    stylesheetResources.clear();
                    List<Resource> cssResources1 = currentBookProperty.get().getResources().getResourcesByMediaType(MediaType.CSS);
                    for (Resource cssResource : cssResources1)
                    {
                        StylesheetResource stylesheetResource = new StylesheetResource(cssResource);
                        stylesheetResources.add(stylesheetResource);
                    }
                    tableView.setItems(stylesheetResources);
                });
            }
            tableView.setItems(stylesheetResources);
        });

        instance = this;
    }

    @Override
    public void setStage(Stage stage)
    {
        this.stage = stage;
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

    public static AddStylesheetController getInstance()
    {
        return instance;
    }

    public void setXHTMLResources(List<Resource> resources)
    {
        this.xhtmlResources = resources;
        headElements.clear();
        for (StylesheetResource stylesheetResource : stylesheetResources)
        {
            stylesheetResource.setTotalCount(resources.size());
            stylesheetResource.resetCount();
        }
        for (Resource resource : resources)
        {
            Document document = ((XHTMLResource)resource).asNativeFormat();
            Element root = document.getRootElement();
            if (root != null)
            {
                Element headElement = root.getChild("head", Constants.NAMESPACE_XHTML);
                headElements.put(headElement, resource);
                List<Element> toRemove = new ArrayList<>();
                if (headElement != null)
                {
                    List<Element> styleElements = headElement.getChildren("link", Constants.NAMESPACE_XHTML);
                    for (Element styleElement : styleElements)
                    {
                        if ("stylesheet".equals(styleElement.getAttributeValue("rel")))
                        {
                            toRemove.add(styleElement);
                            String href = styleElement.getAttributeValue("href");
                            Resource cssResource = currentBookProperty.get().getResources().getByResolvedHref(resource, href);
                            if (cssResource != null)
                            {
                                for (StylesheetResource stylesheetResource : stylesheetResources)
                                {
                                    if (stylesheetResource.getStylesheet().equals(cssResource))
                                    {
                                        stylesheetResource.increase();
                                    }
                                }
                            }
                            else
                            {
                                logger.warn("css resource with href " + href + " not found");
                            }
                        }
                    }
                    for (Element element : toRemove) //style elemente erstmal weg, bei ok fügen wir die wieder in der richtigen Reihenfolge ein
                    {
                        headElement.removeContent(element);
                    }
                }
            }
        }
        for (StylesheetResource stylesheetResource : stylesheetResources)
        {
            stylesheetResource.initIncluded();
        }
    }

    public void okAction(ActionEvent actionEvent) throws IOException
    {
        for (Element headElement : headElements.keySet())
        {
            for (StylesheetResource stylesheetResource : stylesheetResources)
            {
                Resource resource = headElements.get(headElement);
                if (stylesheetResource.isIncluded())
                {
                    Element styleElement = new Element("link", Constants.NAMESPACE_XHTML);
                    styleElement.setAttribute("href", resource.relativize(stylesheetResource.getStylesheet()));
                    styleElement.setAttribute("rel", "stylesheet");
                    styleElement.setAttribute("type", "text/css");
                    headElement.addContent(styleElement);
                }
                Document document = headElement.getDocument();
                byte[] bytes = XHTMLUtils.outputXHTMLDocument(document, true, currentBookProperty().get().getVersion());

                resource.setData(bytes);
                editorManager.refreshEditorCode(resource);
            }
        }
        editorManager.refreshPreview();
        stage.close();
    }

    public void cancelAction(ActionEvent actionEvent)
    {
        stage.close();

    }

    public void styleDownAction(ActionEvent actionEvent)
    {


    }

    public void styleUpAction(ActionEvent actionEvent)
    {


    }
}
