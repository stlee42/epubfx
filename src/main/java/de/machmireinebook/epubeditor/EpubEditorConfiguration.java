package de.machmireinebook.epubeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SplitPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.machmireinebook.epubeditor.clips.ClipManager;
import de.machmireinebook.epubeditor.gui.MainController;
import de.machmireinebook.epubeditor.javafx.StashableSplitPane;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;
import de.machmireinebook.epubeditor.preferences.StageSizer;

/**
 * User: mjungierek
 * Date: 15.11.13
 * Time: 22:28
 */
@Singleton
public class EpubEditorConfiguration
{
    private static final Logger logger = Logger.getLogger(EpubEditorConfiguration.class);

    private Stage mainWindow;
    private Document configurationDocument;
    private List<OpenWithApplication> xhtmlOpenWithApplications = new ArrayList<>();
    private List<OpenWithApplication> imageOpenWithApplications = new ArrayList<>();
    private List<OpenWithApplication> cssOpenWithApplications = new ArrayList<>();
    private List<OpenWithApplication> fontOpenWithApplications = new ArrayList<>();
    private StageSizer stageSizer = new StageSizer();

    /**
     * Use a SetUniqueList and not a set, because sets have no guaranteed order, but this is needed for recent list
     */
    private ObservableList<Path> recentFiles = FXCollections.observableList(SetUniqueList.setUniqueList(new ArrayList<>()));

    public static final int RECENT_FILE_NUMBER = 10;
    public static final String LOCATION_CLASS_PREFIX = "epubfx-line-";

    //tag names in xml for main sections
    public static final String PREFERENCES_ELEMENT_NAME = "preferences";
    public static final String CLIPS_ELEMENT_NAME = "clips";

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
        MainController epubEditorMainController =  BeanFactory.getInstance().getBean(MainController.class);
        StashableSplitPane leftDivider = epubEditorMainController.getLeftDivider();
        StashableSplitPane rightDivider = epubEditorMainController.getRightDivider();
        StashableSplitPane centerDivider = epubEditorMainController.getCenterDivider();

        ClipManager clipManager =  BeanFactory.getInstance().getBean(ClipManager.class);
        PreferencesManager preferencesManager =  BeanFactory.getInstance().getBean(PreferencesManager.class);
        //saved configuration
        String alluserProfileFolder = System.getenv().get("ALLUSERSPROFILE");

        stageSizer.setStage(mainWindow);

        InputStream fis;
        try
        {
            if (StringUtils.isNotEmpty(alluserProfileFolder)) {
                String confFilePath = alluserProfileFolder + "/epubfx";
                Files.createDirectories(Paths.get(confFilePath));
                fis = new FileInputStream(confFilePath + "/application.xml");
            } else {
                fis = EpubEditorConfiguration.class.getResourceAsStream("/application.xml");
            }
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
                        //noinspection ResultOfMethodCallIgnored
                        recentFilesElement.getChildren("path")
                                .stream()
                                .map(element -> Paths.get(element.getText()))
                                .collect(Collectors.collectingAndThen(Collectors.toList(), (Function<List<Path>, List<Path>>) pathList -> {
                                    //trigger listeners only one
                                    recentFiles.addAll(pathList);
                                    return recentFiles;
                                }));
                    }

                    Element layoutElement = root.getChild("layout");
                    if (layoutElement != null)
                    {
                        Element mainWindoElement = layoutElement.getChild("main-window");
                        if (mainWindoElement != null)
                        {
                            stageSizer.setMaximized(BooleanUtils.toBoolean(mainWindoElement.getAttributeValue("is-fullscreen")));
                            stageSizer.setWidth(Double.valueOf(mainWindoElement.getAttributeValue("width")));
                            stageSizer.setHeight(Double.valueOf(mainWindoElement.getAttributeValue("height")));
                            stageSizer.setX(Double.valueOf(mainWindoElement.getAttributeValue("x")));
                            stageSizer.setY(Double.valueOf(mainWindoElement.getAttributeValue("y")));
                        }

                        Element visibilityElement = layoutElement.getChild("visibility");
                        boolean showBookBrowser = false;
                        boolean showClips = false;
                        boolean showToc = false;
                        boolean showValidationResults = false;
                        boolean showPreview = false;

                        if (visibilityElement != null)
                        {
                            showBookBrowser = BooleanUtils.toBoolean(visibilityElement.getAttributeValue("book-browser"));
                            epubEditorMainController.getShowBookBrowserToggleButton().selectedProperty().set(showBookBrowser);
                            leftDivider.setVisibility(0, showBookBrowser);

                            showClips = BooleanUtils.toBoolean(visibilityElement.getAttributeValue("clips-list"));
                            epubEditorMainController.getShowClipsToggleButton().selectedProperty().set(showClips);
                            leftDivider.setVisibility(1, showClips);

                            showPreview = BooleanUtils.toBoolean(visibilityElement.getAttributeValue("preview"));
                            epubEditorMainController.getShowPreviewToggleButton().selectedProperty().set(showPreview);
                            rightDivider.setVisibility(0, showPreview);

                            showToc = BooleanUtils.toBoolean(visibilityElement.getAttributeValue("toc"));
                            epubEditorMainController.getShowTocToggleButton().selectedProperty().set(showToc);
                            rightDivider.setVisibility(1, showToc);

                            showValidationResults = BooleanUtils.toBoolean(visibilityElement.getAttributeValue("validation-results"));
                            epubEditorMainController.getShowValidationResultsToggleButton().selectedProperty().set(showValidationResults);
                            centerDivider.setVisibility(1, showValidationResults);
                        }

                        Element dividersElement = layoutElement.getChild("dividers");
                        if (dividersElement != null)
                        {
                            Element mainDividerElement = dividersElement.getChild("main-divider");
                            if (mainDividerElement != null)
                            {
                                logger.debug("setting main divider");

                                List<SplitPane.Divider> dividers = epubEditorMainController.getMainDivider().getDividers();
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
                                List<SplitPane.Divider> dividers = leftDivider.getDividers();
                                if (!"none".equals(leftDividerElement.getAttributeValue("divider-1")) && showBookBrowser && showClips)
                                {
                                    SplitPane.Divider divider = dividers.get(0);
                                    divider.setPosition(Double.parseDouble(leftDividerElement.getAttributeValue("divider-1")));
                                }
                            }

                            Element rightDividerElement = dividersElement.getChild("right-divider");
                            if (rightDividerElement != null)
                            {
                                List<SplitPane.Divider> dividers = epubEditorMainController.getRightDivider().getDividers();
                                if (!"none".equals(rightDividerElement.getAttributeValue("divider-1")) && showPreview && showToc)
                                {
                                    dividers.get(0).setPosition(Double.parseDouble(rightDividerElement.getAttributeValue("divider-1")));
                                }
                            }

                            Element centerDividerElement = dividersElement.getChild("center-divider");
                            if (centerDividerElement != null)
                            {
                                List<SplitPane.Divider> dividers = epubEditorMainController.getCenterDivider().getDividers();
                                if (!"none".equals(centerDividerElement.getAttributeValue("divider-1")) && showValidationResults)
                                {
                                    dividers.get(0).setPosition(Double.parseDouble(centerDividerElement.getAttributeValue("divider-1")));
                                }
                            }
                        }

                    }

                    Element clipsElement = root.getChild(CLIPS_ELEMENT_NAME);
                    List<Element> children = clipsElement.getChildren();
                    clipManager.readClips(children);

                    Element preferencesElement = root.getChild(PREFERENCES_ELEMENT_NAME);
                    if (preferencesElement == null) {
                        preferencesElement = new Element(PREFERENCES_ELEMENT_NAME);
                    }
                    preferencesManager.init(preferencesElement);
                }
            }
        }
        catch (JDOMException | IOException e)
        {
            logger.error("", e);
            //in case of error or no configuration file is found, create a new one with standard values
            configurationDocument = new Document(new Element("configuration"));
            stageSizer.setMaximized(true);
            //set to screensize
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            stageSizer.setWidth(screenBounds.getWidth());
            stageSizer.setHeight(screenBounds.getHeight());
            stageSizer.setX(0);
            stageSizer.setY(0);

            //as standard show book browser, editor (can't be hidden) and preview
            leftDivider.setVisibility(0, true);
            leftDivider.setVisibility(1, false);
            centerDivider.setVisibility(1, false);
            rightDivider.setVisibility(0, true);
            rightDivider.setVisibility(1, false);

            epubEditorMainController.getShowBookBrowserToggleButton().selectedProperty().set(true);
            epubEditorMainController.getShowClipsToggleButton().selectedProperty().set(false);

            epubEditorMainController.getShowPreviewToggleButton().selectedProperty().set(true);
            epubEditorMainController.getShowTocToggleButton().selectedProperty().set(false);

            epubEditorMainController.getShowValidationResultsToggleButton().selectedProperty().set(false);
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
        MainController epubEditorMainController =  BeanFactory.getInstance().getBean(MainController.class);
        ClipManager clipManager =  BeanFactory.getInstance().getBean(ClipManager.class);

        double width = stageSizer.getWidth().doubleValue();
        double height = stageSizer.getHeight().doubleValue();
        double x = stageSizer.getX().doubleValue();
        double y = stageSizer.getY().doubleValue();
        boolean isFullscreen = stageSizer.getMaximized();
        if (configurationDocument != null)
        {
            Element root = configurationDocument.getRootElement();
            if (root == null) {
                root = new Element("configuration");
                configurationDocument.setRootElement(root);
            }
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
            recentFilesElement.removeContent();
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
            visibilityElement.setAttribute("book-browser", BooleanUtils.toStringTrueFalse(epubEditorMainController.getShowBookBrowserToggleButton().isSelected()));
            visibilityElement.setAttribute("toc", BooleanUtils.toStringTrueFalse(epubEditorMainController.getShowTocToggleButton().isSelected()));
            visibilityElement.setAttribute("validation-results", BooleanUtils.toStringTrueFalse(epubEditorMainController.getShowValidationResultsToggleButton().isSelected()));
            visibilityElement.setAttribute("clips-list", BooleanUtils.toStringTrueFalse(epubEditorMainController.getShowClipsToggleButton().isSelected()));
            visibilityElement.setAttribute("preview", BooleanUtils.toStringTrueFalse(epubEditorMainController.getShowPreviewToggleButton().isSelected()));

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
            List<SplitPane.Divider> dividers = epubEditorMainController.getMainDivider().getDividers();
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
            dividers = epubEditorMainController.getLeftDivider().getDividers();
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
            dividers = epubEditorMainController.getRightDivider().getDividers();
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
            dividers = epubEditorMainController.getCenterDivider().getDividers();
            if (dividers.size() > 0)
            {
                centerDividerElement.setAttribute("divider-1", String.valueOf(dividers.get(0).getPosition()));
            }
            else
            {
                centerDividerElement.setAttribute("divider-1", "none");
            }

            Element clipsElement = root.getChild(CLIPS_ELEMENT_NAME);
            if (clipsElement == null)
            {
                clipsElement = new Element(CLIPS_ELEMENT_NAME);
                root.addContent(clipsElement);
            }

            //neu aus ClipManager aufbauen
            clipsElement.removeContent();
            clipManager.saveClips(clipsElement);

            //create new from element in preferences manager
            root.removeChild(PREFERENCES_ELEMENT_NAME);
            PreferencesManager preferencesManager =  BeanFactory.getInstance().getBean(PreferencesManager.class);
            Optional<Element> preferencesElementOptional = preferencesManager.getPreferencesElement();
            preferencesElementOptional.ifPresent(root::addContent);
        }
        String alluserProfileFolder = System.getenv().get("ALLUSERSPROFILE");
        OutputStream os;
        try
        {
            if (StringUtils.isNotEmpty(alluserProfileFolder)) {
                String confFilePath = alluserProfileFolder + "/epubfx";
                Files.createDirectories(Paths.get(confFilePath));
                os = new FileOutputStream(confFilePath + "/application.xml");
            } else {
                os = new FileOutputStream(new File(EpubEditorConfiguration.class.getResource("/application.xml").getFile()));
            }

            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            outputter.output(configurationDocument, os);
        }
        catch (IOException e)
        {
            logger.error("", e);
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
