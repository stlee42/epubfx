package de.machmireinebook.epubeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import de.machmireinebook.epubeditor.cdi.BeanFactory;
import de.machmireinebook.epubeditor.cdi.ClipManagerProducer;
import de.machmireinebook.epubeditor.cdi.EpubEditorConfigurationProducer;
import de.machmireinebook.epubeditor.domain.Clip;
import de.machmireinebook.epubeditor.gui.EpubEditorMainController;
import de.machmireinebook.epubeditor.jdom2.XHTMLOutputProcessor;
import de.machmireinebook.epubeditor.manager.ClipManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * User: mjungierek
 * Date: 15.11.13
 * Time: 22:28
 */
public class EpubEditorConfiguration
{
    public static final Logger logger = Logger.getLogger(EpubEditorConfiguration.class);

    private static EpubEditorConfiguration instance;

    //color
    String color = "#0093FF";

    private Stage mainWindow;
    private Document configurationDocument;
    private List<OpenWithApplication> xhtmlOpenWithApplications = new ArrayList<>();
    private List<OpenWithApplication> imageOpenWithApplications = new ArrayList<>();
    private List<OpenWithApplication> cssOpenWithApplications = new ArrayList<>();
    private List<OpenWithApplication> fontOpenWithApplications = new ArrayList<>();

    private ObservableList<Path> recentFiles = FXCollections.observableList(SetUniqueList.setUniqueList(new ArrayList<>()));

    public static final int RECENT_FILE_NUMBER = 3;

    @Inject @ClipManagerProducer
    private ClipManager clipManager;

    public static class OpenWithApplication
    {
        private String displayName;
        private String fileName;

        public OpenWithApplication(String displayName, String fileName)
        {
            this.displayName = displayName;
            this.fileName = fileName;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public String getFileName()
        {
            return fileName;
        }
    }

    @Produces @EpubEditorConfigurationProducer
    public static EpubEditorConfiguration getInstance()
    {
        if (instance == null)
        {
            instance = BeanFactory.getInstance().getBean(EpubEditorConfiguration.class);
        }
        return instance;
    }

    private EpubEditorConfiguration()
    {
    }

    public static void initLogger()
    {
        //log4j
        Layout layout = new PatternLayout("%d{HH:mm:ss} %-5p %c %x - %m\n");
        Appender appender = new WriterAppender(layout, System.out);
        BasicConfigurator.configure(appender);
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("de.machmireinebook").setLevel(Level.DEBUG);
    }

    public void readConfiguration()
    {
        //saved configuration
        InputStream fis = EpubEditorConfiguration.class.getResourceAsStream("/application.xml");
        try
        {
            configurationDocument = new SAXBuilder().build(fis);
            if (configurationDocument != null)
            {
                Element root = configurationDocument.getRootElement();
                if (root != null)
                {
                    Element openWithElement = root.getChild("open-with");
                    if (openWithElement != null)
                    {
                        Element xhtmlElement = openWithElement.getChild("xhtml");
                        addApplications(xhtmlElement, xhtmlOpenWithApplications);
                        Element imagesElement = openWithElement.getChild("image");
                        addApplications(imagesElement, imageOpenWithApplications);
                        Element cssElement = openWithElement.getChild("css");
                        addApplications(cssElement, cssOpenWithApplications);
                        Element fontElement = openWithElement.getChild("font");
                        addApplications(fontElement, fontOpenWithApplications);
                    }

                    Element recentFilesElement = root.getChild("recent-files");
                    if (recentFilesElement != null)
                    {
                        List<Element> pathElements = recentFilesElement.getChildren("path");
                        for (Element pathElement : pathElements)
                        {
                            Path path = Paths.get(pathElement.getText());
                            recentFiles.add(path);
                        }
                    }

                    Element layoutElement = root.getChild("layout");
                    if (layoutElement != null)
                    {
                        EpubEditorMainController mainController = EpubEditorMainController.getInstance();

                        Element mainWindoElement = layoutElement.getChild("main-window");
                        if (mainWindoElement != null)
                        {
                            Boolean isFullscreen = BooleanUtils.toBoolean(mainWindoElement.getAttributeValue("is-fullscreen"));
                            Double width = Double.valueOf(mainWindoElement.getAttributeValue("width"));
                            Double height = Double.valueOf(mainWindoElement.getAttributeValue("height"));
                            Double x = Double.valueOf(mainWindoElement.getAttributeValue("x"));
                            Double y = Double.valueOf(mainWindoElement.getAttributeValue("y"));

                            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                            if (isFullscreen)
                            {
                                mainWindow.setFullScreen(true);
                                mainWindow.setWidth(primaryScreenBounds.getWidth());
                                mainWindow.setHeight(primaryScreenBounds.getHeight());
                                mainWindow.setX(0);
                                mainWindow.setY(0);
                            }
                            else if (x >= 0.0 && y >= 0.0 && x < primaryScreenBounds.getWidth() - 100 && y < primaryScreenBounds.getHeight() - 100) // verhindern dass Fenster ausserhalb des sichtbaren Bereichs geöffnet wird
                            {
                                mainWindow.setFullScreen(false);
                                mainWindow.setWidth(width);
                                mainWindow.setHeight(height);
                                mainWindow.setX(x);
                                mainWindow.setY(y);
                            }
                        }

                        Element dividersElement = layoutElement.getChild("dividers");
                        if (dividersElement != null)
                        {
                            Element mainDividerElement = dividersElement.getChild("main-divider");
                            if (mainDividerElement != null)
                            {
                                logger.debug("setting main divider");
                                List<SplitPane.Divider> dividers = mainController.getMainDivider().getDividers();
                                if (!"none".equals(mainDividerElement.getAttributeValue("divider-1")))
                                {
                                    double value = Double.parseDouble(mainDividerElement.getAttributeValue("divider-1"));
                                    dividers.get(0).setPosition(value);
                                }
                                if (!"none".equals(mainDividerElement.getAttributeValue("divider-2")))
                                {
                                    double value = Double.parseDouble(mainDividerElement.getAttributeValue("divider-2"));
                                    dividers.get(1).setPosition(value);
                                }
                            }

                            Element leftDividerElement = dividersElement.getChild("left-divider");
                            if (leftDividerElement != null)
                            {
                                List<SplitPane.Divider> dividers = mainController.getLeftDivider().getDividers();
                                if (!"none".equals(leftDividerElement.getAttributeValue("divider-1")))
                                {
                                    dividers.get(0).setPosition(Double.parseDouble(leftDividerElement.getAttributeValue("divider-1")));
                                }
                            }

                            Element rightDividerElement = dividersElement.getChild("right-divider");
                            if (rightDividerElement != null)
                            {
                                List<SplitPane.Divider> dividers = mainController.getRightDivider().getDividers();
                                if (!"none".equals(rightDividerElement.getAttributeValue("divider-1")))
                                {
                                    dividers.get(0).setPosition(Double.parseDouble(rightDividerElement.getAttributeValue("divider-1")));
                                }
                            }

                            Element centerDividerElement = dividersElement.getChild("center-divider");
                            if (centerDividerElement != null)
                            {
                                List<SplitPane.Divider> dividers = mainController.getCenterDivider().getDividers();
                                if (!"none".equals(centerDividerElement.getAttributeValue("divider-1")))
                                {
                                    dividers.get(0).setPosition(Double.parseDouble(centerDividerElement.getAttributeValue("divider-1")));
                                }
                            }
                        }

                        Element visibilityElement = layoutElement.getChild("visibility");
                        if (visibilityElement != null)
                        {
                            mainController.getShowBookBrowserMenuItem().selectedProperty().set(BooleanUtils.toBoolean(visibilityElement.getAttributeValue("book-browser")));
                            mainController.getShowTocMenuItem().selectedProperty().set(BooleanUtils.toBoolean(visibilityElement.getAttributeValue("toc")));
                            mainController.getShowValidationResultsMenuItem().selectedProperty().set(BooleanUtils.toBoolean(visibilityElement.getAttributeValue("validation-results")));
                            mainController.getClipsMenuItem().selectedProperty().set(BooleanUtils.toBoolean(visibilityElement.getAttributeValue("clips-list")));
                            mainController.getShowPreviewMenuItem().selectedProperty().set(BooleanUtils.toBoolean(visibilityElement.getAttributeValue("preview")));
                        }
                    }


                    Element clipsElement = root.getChild("clips");
                    List<Element> children = clipsElement.getChildren();
                    TreeItem<Clip> clipsRoot =  clipManager.getClipsRoot();
                    readChildren(children, clipsRoot);
                }
            }
        }
        catch (JDOMException | IOException e)
        {
            logger.error("", e);
            //im fehlerfall leeres Document erzeugen
            configurationDocument = new Document(new Element("configuration"));
        }
    }

    private void readChildren(List<Element> children, TreeItem<Clip> parentTreeItem)
    {
        for (Element child : children)
        {
            if (child.getName().equals("clip"))
            {
                String name = child.getChildText("name");
                String content = child.getChildText("content");
                Clip clip = new Clip(name, content);
                TreeItem<Clip> treeItem = new TreeItem<>(clip);
                parentTreeItem.getChildren().add(treeItem);
            }
            else if (child.getName().equals("group"))
            {
                String name = child.getAttributeValue("name");
                Clip clip = new Clip(name, true);
                TreeItem<Clip> treeItem = new TreeItem<>(clip);
                parentTreeItem.getChildren().add(treeItem);
                List<Element> subChildren = child.getChildren();
                readChildren(subChildren, treeItem);
            }
        }
    }

    private void addApplications(Element element, List<OpenWithApplication> applications)
    {
        if (element != null)
        {
            List<Element> applicationElements = element.getChildren("application");
            for (Element applicationElement : applicationElements)
            {
                String displayName = applicationElement.getAttributeValue("display-name");
                String fileName = applicationElement.getText();
                applications.add(new OpenWithApplication(displayName, fileName));
            }
        }
    }

    public Stage getMainWindow()
    {
        return mainWindow;
    }

    public void setMainWindow(Stage mainWindow)
    {
        this.mainWindow = mainWindow;
    }

    public List<OpenWithApplication> getXhtmlOpenWithApplications()
    {
        return xhtmlOpenWithApplications;
    }

    public List<OpenWithApplication> getImageOpenWithApplications()
    {
        return imageOpenWithApplications;
    }

    public List<OpenWithApplication> getCssOpenWithApplications()
    {
        return cssOpenWithApplications;
    }

    public List<OpenWithApplication> getFontOpenWithApplications()
    {
        return fontOpenWithApplications;
    }

    public void saveConfiguration()
    {
        double width = mainWindow.getWidth();
        double height = mainWindow.getHeight();
        double x = mainWindow.getX();
        double y = mainWindow.getY();
        boolean isFullscreen = mainWindow.isMaximized();
        if (configurationDocument != null)
        {
            Element root = configurationDocument.getRootElement();
            if (root != null)
            {
                //applikationen für open with speichern
                Element openWithElement = root.getChild("open-with");
                if (openWithElement == null)
                {
                    openWithElement = new Element("open-with");
                    root.addContent(openWithElement);
                }
                Element xhtmlElement = openWithElement.getChild("xhtml");
                if (xhtmlElement == null)
                {
                    xhtmlElement = new Element("xhtml");
                    openWithElement.addContent(xhtmlElement);
                }
                saveApplications(xhtmlElement, xhtmlOpenWithApplications);

                Element imagesElement = openWithElement.getChild("image");
                if (imagesElement == null)
                {
                    imagesElement = new Element("image");
                    openWithElement.addContent(imagesElement);
                }
                saveApplications(imagesElement, imageOpenWithApplications);

                Element cssElement = openWithElement.getChild("css");
                if (cssElement == null)
                {
                    cssElement = new Element("css");
                    openWithElement.addContent(cssElement);
                }
                saveApplications(cssElement, cssOpenWithApplications);

                Element fontElement = openWithElement.getChild("font");
                if (fontElement == null)
                {
                    fontElement = new Element("font");
                    openWithElement.addContent(fontElement);
                }
                saveApplications(fontElement, fontOpenWithApplications);

                //recent files
                Element recentFilesElement = root.getChild("recent-files");
                if (recentFilesElement == null)
                {
                    recentFilesElement = new Element("recent-files");
                    root.addContent(recentFilesElement);
                }
                for (Path recentFile : recentFiles)
                {
                    Element recentFileElement = new Element("path");
                    recentFileElement.setText(recentFile.toString());
                    recentFilesElement.addContent(recentFileElement);
                }

                //layout der oberfläche speichern
                Element layoutElement = root.getChild("layout");
                if (layoutElement == null)
                {
                    layoutElement = new Element("layout");
                    root.addContent(layoutElement);
                }
                //<main-window x="312.0" height="1040.0" isFullscreen="false"  width="1114.0" y="0.0"/>
                Element mainWindoElement = layoutElement.getChild("main-window");
                if (mainWindoElement == null)
                {
                    mainWindoElement = new Element("main-window");
                    layoutElement.addContent(mainWindoElement);
                }
                mainWindoElement.setAttribute("x", String.valueOf(x));
                mainWindoElement.setAttribute("y", String.valueOf(y));
                mainWindoElement.setAttribute("width", String.valueOf(width));
                mainWindoElement.setAttribute("height", String.valueOf(height));
                mainWindoElement.setAttribute("is-fullscreen", BooleanUtils.toStringTrueFalse(isFullscreen));

                //<visibility book-browser="true" toc="true" validation-results="true" clips-list="true" preview="true"/>
                Element visibilityElement = layoutElement.getChild("visibility");
                if (visibilityElement == null)
                {
                    visibilityElement = new Element("visibility");
                    layoutElement.addContent(visibilityElement);
                }
                EpubEditorMainController mainController = EpubEditorMainController.getInstance();
                visibilityElement.setAttribute("book-browser", BooleanUtils.toStringTrueFalse(mainController.getShowBookBrowserMenuItem().isSelected()));
                visibilityElement.setAttribute("toc", BooleanUtils.toStringTrueFalse(mainController.getShowTocMenuItem().isSelected()));
                visibilityElement.setAttribute("validation-results", BooleanUtils.toStringTrueFalse(mainController.getShowValidationResultsMenuItem().isSelected()));
                visibilityElement.setAttribute("clips-list", BooleanUtils.toStringTrueFalse(mainController.getClipsMenuItem().isSelected()));
                visibilityElement.setAttribute("preview", BooleanUtils.toStringTrueFalse(mainController.getShowPreviewMenuItem().isSelected()));

                Element dividersElement = layoutElement.getChild("dividers");
                if (dividersElement == null)
                {
                    dividersElement = new Element("dividers");
                    layoutElement.addContent(dividersElement);
                }

                Element mainDividerElement = dividersElement.getChild("main-divider");
                if (mainDividerElement == null)
                {
                    mainDividerElement = new Element("main-divider");
                    dividersElement.addContent(mainDividerElement);
                }
                List<SplitPane.Divider> dividers = mainController.getMainDivider().getDividers();
                if (dividers.size() > 0)
                {
                    mainDividerElement.setAttribute("divider-1", String.valueOf(dividers.get(0).getPosition()));
                }
                else
                {
                    mainDividerElement.setAttribute("divider-1", "none");
                }
                if (dividers.size() > 1)
                {
                    mainDividerElement.setAttribute("divider-2", String.valueOf(dividers.get(1).getPosition()));
                }
                else
                {
                    mainDividerElement.setAttribute("divider-2", "none");
                }

                Element leftDividerElement = dividersElement.getChild("left-divider");
                if (leftDividerElement == null)
                {
                    leftDividerElement = new Element("left-divider");
                    dividersElement.addContent(leftDividerElement);
                }
                dividers = mainController.getLeftDivider().getDividers();
                if (dividers.size() > 0)
                {
                    leftDividerElement.setAttribute("divider-1", String.valueOf(dividers.get(0).getPosition()));
                }
                else
                {
                    leftDividerElement.setAttribute("divider-1", "none");
                }

                Element rightDividerElement = dividersElement.getChild("right-divider");
                if (rightDividerElement == null)
                {
                    rightDividerElement = new Element("right-divider");
                    dividersElement.addContent(rightDividerElement);
                }
                dividers = mainController.getRightDivider().getDividers();
                if (dividers.size() > 0)
                {
                    rightDividerElement.setAttribute("divider-1", String.valueOf(dividers.get(0).getPosition()));
                }
                else
                {
                    rightDividerElement.setAttribute("divider-1", "none");
                }

                Element centerDividerElement = dividersElement.getChild("center-divider");
                if (centerDividerElement == null)
                {
                    centerDividerElement = new Element("center-divider");
                    dividersElement.addContent(centerDividerElement);
                }
                dividers = mainController.getCenterDivider().getDividers();
                if (dividers.size() > 0)
                {
                    centerDividerElement.setAttribute("divider-1", String.valueOf(dividers.get(0).getPosition()));
                }
                else
                {
                    centerDividerElement.setAttribute("divider-1", "none");
                }

                Element clipsElement = root.getChild("clips");
                if (clipsElement == null)
                {
                    clipsElement = new Element("clips");
                    root.addContent(clipsElement);
                }
                //neu aus ClipManager aufbauen
                clipsElement.removeContent();
                TreeItem<Clip> clipRootItem = clipManager.getClipsRoot();
                writeChildren(clipRootItem, clipsElement);
            }
        }
        try
        {
            OutputStream os = new FileOutputStream(new File(EpubEditorConfiguration.class.getResource("/application.xml").getFile()));
            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.setXMLOutputProcessor(new XHTMLOutputProcessor());
            outputter.output(configurationDocument, os);
        }
        catch (IOException e)
        {
            logger.error(e);
        }

    }

    private void writeChildren(TreeItem<Clip> parentTreeItem, Element parentElement)
    {
        List<TreeItem<Clip>> treeItems = parentTreeItem.getChildren();
        for (TreeItem<Clip> treeItem : treeItems)
        {
            if (treeItem.getValue().isGroup())
            {
                Element groupElement = new Element("group");
                groupElement.setAttribute("name", treeItem.getValue().getName());
                writeChildren(treeItem, groupElement);
                parentElement.addContent(groupElement);
            }
            else
            {
                Element clipElement = new Element("clip");
                parentElement.addContent(clipElement);

                Element nameElement =  new Element("name");
                clipElement.addContent(nameElement);
                nameElement.setText(treeItem.getValue().getName());

                Element contentElement =  new Element("content");
                clipElement.addContent(contentElement);
                contentElement.setContent(new CDATA(treeItem.getValue().getContent()));
            }
        }
    }

    private void saveApplications(Element element, List<OpenWithApplication> applications)
    {
        if (element != null)
        {
            element.getChildren().clear();
            for (OpenWithApplication application : applications)
            {
                Element applicationElement = new Element("application");
                applicationElement.setAttribute("display-name", application.getDisplayName());
                applicationElement.setText(application.getFileName());
                element.addContent(applicationElement);
            }
        }
    }

    public ObservableList<Path> getRecentFiles()
    {
        return recentFiles;
    }
}
