package de.machmireinebook.epubeditor.epublib.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Guide;
import de.machmireinebook.epubeditor.epublib.domain.epub2.GuideReference;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub3.LandmarkReference;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Landmarks;
import de.machmireinebook.epubeditor.epublib.domain.epub3.ManifestItemPropertiesValue;
import de.machmireinebook.epubeditor.epublib.epub2.NCXDocument;
import de.machmireinebook.epubeditor.epublib.epub2.PackageDocumentWriter;
import de.machmireinebook.epubeditor.epublib.epub3.Epub3PackageDocumentWriter;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.Resources;
import de.machmireinebook.epubeditor.epublib.resource.TextResource;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.toc.TableOfContents;
import de.machmireinebook.epubeditor.jdom2.AttributeElementFilter;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;


/**
 * Representation of a Book.
 */
@Named
public class Book implements Serializable
{
    private static final Logger logger = Logger.getLogger(Book.class);
    private static final long serialVersionUID = 2068355170895770100L;

    private Resources resources = new Resources();
    private EpubMetadata metadata;
    private Spine spine = new Spine();
    private TableOfContents tableOfContents = new TableOfContents();
    private Guide guide = new Guide();
    private Landmarks landmarks = new Landmarks();
    private ObjectProperty<Resource<?>> opfResource = new SimpleObjectProperty<>();
    private Resource<?> ncxResource;
    private ImageResource coverImage;

    // versionProperty
    private final ObjectProperty<EpubVersion> versionProperty = new SimpleObjectProperty<>(this, "version");
    private boolean isFixedLayout = false;
    private int fixedLayoutWidth;
    private int fixedLayoutHeight;
    private Resource<Document> epub3NavResource;
    private Resource<?> appleDisplayOptions;

    private BooleanProperty bookIsChanged = new SimpleBooleanProperty(false);
    private ObjectProperty<Path> physicalFileNameProperty = new SimpleObjectProperty<>(this, "physicalFileName");

    public Book()
    {
        setVersion(EpubVersion.VERSION_2);
    }

    public static Book createMinimalBook()
    {
        Book book = new Book();
        book.setVersion(EpubVersion.VERSION_2);

        book.setMetadata(new Metadata());

        Resource<Document> ncxResource = NCXDocument.createNCXResource(book);
        book.setNcxResource(ncxResource);
        book.getSpine().setTocResource(ncxResource);
        book.addResource(ncxResource, false);

        Resource<Document> opfResource = PackageDocumentWriter.createOPFResource(book);
        book.setOpfResource(opfResource);

        Resource<Document> textRes = book.addResourceFromTemplate("/epub/template.xhtml", "Text/text-0001.xhtml", false);
        book.addSection("Start", textRes);

        book.addResourceFromTemplate("/epub/standard-small.css", "Styles/standard.css", false);

        return book;
    }

    public static Book createMinimalEpub3Book()
    {
        Book book = new Book();
        book.setVersion(EpubVersion.VERSION_3_2);

        book.setMetadata(new de.machmireinebook.epubeditor.epublib.domain.epub3.Metadata());

        Resource<?> navResource = book.addResourceFromTemplate("/epub/nav.xhtml", "Text/nav.xhtml", false);
        navResource.addPropertiesValue(ManifestItemPropertiesValue.nav);
        book.setEpub3NavResource(navResource);
        book.getSpine().addResource(navResource);
        book.addResource(navResource, false);

        //for compatibility create a toc ncx too
        Resource<?> ncxResource = NCXDocument.createNCXResource(book);
        book.setNcxResource(ncxResource);
        book.addResource(ncxResource, false);

        Resource<Document> textRes = book.addResourceFromTemplate("/epub/template-epub3.html", "Text/text-0001.xhtml", false);
        book.addSection("Start", textRes);

        book.addResourceFromTemplate("/epub/standard-small.css", "Styles/standard.css", false);

        Resource<?> opfResource = Epub3PackageDocumentWriter.createOPFResource(book);
        book.setOpfResource(opfResource);

        return book;
    }

    public Resource<?> addResourceFromTemplate(String templateFileName, String href) {
        return addResourceFromTemplate(templateFileName, href, true);
    }

    public Resource<Document> addResourceFromTemplate(String templateFileName, String href, boolean refreshOpf)
    {
        File file = new File(Book.class.getResource(templateFileName).getFile());
        Resource<?> res = createResourceFromFile(file, href, MediaType.getByFileName(href));
        addResource(res, refreshOpf);
        if (MediaType.XHTML.equals(res.getMediaType()))
        {
            try
            {
                String content = new String(res.getData(), res.getInputEncoding());
                content = content.replace("${title}", getTitle());
                if (isEpub3() && isFixedLayout())
                {
                    content = content.replace("${width}", String.valueOf(getFixedLayoutWidth()));
                    content = content.replace("${height}", String.valueOf(getFixedLayoutHeight()));
                }
                res.setData(content.getBytes(res.getInputEncoding()));
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }
        return (Resource<Document>)res;
    }

    public Resource<?> addResourceFromFile(File file, String href, MediaType mediaType)
    {
        Resource<?> res = createResourceFromFile(file, href, mediaType);
        addResource(res);
        return res;
    }

    public Resource<?> addSpineResourceFromFile(File file, String href, MediaType mediaType)
    {
        Resource<?> res = createResourceFromFile(file, href, mediaType);
        res = XHTMLUtils.fromHtml(res, getVersion());
        addSpineResource(res);
        return res;
    }

    public Resource<?> createResourceFromFile(File file, String href, MediaType mediaType)
    {
        Resource<?> res = mediaType.getResourceFactory().createResource(href);
        logger.info("reading file " + file.getName() + " for adding as resource");
        byte[] content = null;
        InputStream is = null;
        try
        {
            is = new FileInputStream(file);
            content = IOUtils.toByteArray(is);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        finally
        {
            try
            {
                if (is != null)
                {
                    is.close();
                }
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }
        res.setData(content);
        res.setMediaType(mediaType);
        return res;
    }

    /**
     * Adds a resource to the book's set of resources, table of contents and if there is no resource with the id in the spine also adds it to the spine.
     *
     * @param title
     * @param resource
     * @return The table of contents
     */
    public void addSection(String title, Resource<Document> resource)
    {
        getResources().put(resource);
        tableOfContents.addTOCReference(new TocEntry(title, resource));
        if (spine.findFirstResourceById(resource.getId()) < 0)
        {
            spine.addSpineReference(new SpineReference(resource), null);
        }
    }

    public void addSpineResource(Resource<?> resource)
    {
        addSpineResource(resource, null);
    }

    public void addSpineResource(Resource<?> resource, Integer index)
    {
        resource.hrefProperty().addListener((observable, oldValue, newValue) -> renameResource(resource, oldValue, newValue));
        getResources().put(resource);
        if (spine.findFirstResourceById(resource.getId()) < 0) {
            spine.addSpineReference(new SpineReference(resource), index);
        }
        refreshOpfResource();
    }

    public void removeResource(Resource<?> resource)
    {
        getResources().remove(resource);
        if (resource.getMediaType().equals(MediaType.CSS))
        {
            String cssFileName = resource.getFileName();
            //aus allen XHTML-Dateien entfernen
            List<Resource<?>> xhtmlResources = getResources().getResourcesByMediaType(MediaType.XHTML);
            for (Resource<?> xhtmlResource : xhtmlResources)
            {
                Document document = ((XHTMLResource) xhtmlResource).asNativeFormat();
                if (document != null)
                {
                    Element root = document.getRootElement();
                    if (root != null)
                    {
                        Element headElement = root.getChild("head");
                        if (headElement != null)
                        {
                            List<Element> linkElements = headElement.getChildren("link");
                            Element toRemove = null;
                            for (Element linkElement : linkElements)
                            {
                                if ("stylesheet".equals(linkElement.getAttributeValue("rel"))
                                        && linkElement.getAttributeValue("href").contains(cssFileName))
                                {
                                    toRemove = linkElement;
                                    break;
                                }
                            }
                            if (toRemove != null)
                            {
                                headElement.removeContent(toRemove);
                            }
                        }
                    }
                }
            }
        }
        refreshOpfResource();
    }


    public void removeSpineResource(Resource resource)
    {
        getResources().remove(resource);
        int index = spine.findFirstResourceById(resource.getId());
        if (index >= 0)
        {
            spine.getSpineReferences().remove(index);
        }
        refreshOpfResource();
    }

    public void refreshOpfResource()
    {
        if (isEpub3()) {
            opfResource.get().setData(Epub3PackageDocumentWriter.createOPFContent(this));
        } else {
            opfResource.get().setData(PackageDocumentWriter.createOPFContent(this));
        }
    }

    /**
     * Refreshes NCX if a ncx resource exists in book, otherwise do nothing.
     */
    public void refreshNcxResource()
    {
        if (ncxResource != null)
        {
            Document ncxDocument = NCXDocument.write(this);
            XMLOutputter outputter = new XMLOutputter();
            Format xmlFormat = Format.getPrettyFormat();
            outputter.setFormat(xmlFormat);
            String text = outputter.outputString(ncxDocument);

            try
            {
                ncxResource.setData(text.getBytes(Constants.CHARACTER_ENCODING));
            }
            catch (UnsupportedEncodingException e)
            {
                //never happens
            }
        }
    }

    public void replaceHrefInXhtmlResources(String oldHref, String newHref) {
        for (Resource<?> resource : getResources().getResourcesByMediaType(MediaType.XHTML)) {
            String text = ((TextResource)resource).asString();
            text = text.replace(oldHref, newHref);
            try {
                resource.setData(text.getBytes(resource.getInputEncoding()));
                if (resource instanceof XHTMLResource) {
                    ((XHTMLResource)resource).prepareWebViewDocument(getVersion());
                }
                resource.triggerExternalChanged();
            }
            catch (UnsupportedEncodingException e) {
                logger.error(e);
            }
        }
    }

    public String getNextStandardFileName(MediaType mediaType)
    {
        int lastNumber = 0;
        for (Resource<?> resource : getResources().getResourcesByMediaType(mediaType))
        {
            String fileName = resource.getFileName();
            if (fileName.startsWith(mediaType.getFileNamePrefix()))
            {
                String[] splitted = fileName.split("-");
                if (splitted.length > 1)
                {
                    String numberPart = splitted[1];
                    numberPart = numberPart.replace(mediaType.getDefaultExtension(), "");
                    int number = Integer.parseInt(numberPart);
                    if (number > lastNumber)
                    {
                        lastNumber = number;
                    }
                }
            }
        }
        return mediaType.getFileNamePrefix() + "-" + StringUtils.leftPad(String.valueOf(lastNumber + 1), 4, "0")
                + mediaType.getDefaultExtension();
    }

    /**
     * The Book's metadata (titles, authors, etc)
     *
     * @return The Book's metadata (titles, authors, etc)
     */
    public EpubMetadata getMetadata()
    {
        return metadata;
    }

    public void setMetadata(EpubMetadata metadata)
    {
        this.metadata = metadata;
    }


    public void setResources(Resources resources)
    {
        resources.getResourcesMap().values().forEach(resource -> resource.hrefProperty().addListener((observable, oldValue, newValue) -> {
            renameResource(resource, oldValue, newValue);
        }));
        this.resources = resources;

    }


    public void addResource(Resource<?> resource)
    {
        addResource(resource, true);
    }

    public void addResource(Resource<?> resource, boolean refreshOpf)
    {
        resource.hrefProperty().addListener((observable, oldValue, newValue) -> renameResource(resource, oldValue, newValue));

        if (resource instanceof ImageResource)
        {
            ImageResource imageResource = (ImageResource) resource;
            imageResource.coverProperty().addListener((observable, oldValue, newValue) ->
            {
                if (newValue)
                {
                    this.coverImage = imageResource;
                    refreshOpfResource();
                }
                if (oldValue && coverImage == imageResource)  //wenn das alte nur ausgeschaltet wird, aber vorher kein neues gesetzt wurde
                {
                    this.coverImage = null;
                    refreshOpfResource();
                }
            });
        }
        resources.put(resource);
        if (refreshOpf)
        {
            refreshOpfResource();
        }
    }

    public Resource addCopyOfResource(Resource resource) {
        Resource newResource = (Resource) resource.clone();
        MediaType mediaType = newResource.getMediaType();
        String fileName = getNextStandardFileName(mediaType);
        String href;
        if (MediaType.CSS.equals(mediaType))
        {
            href = "Styles/" + fileName;
            newResource.setHref(href);
            addResource(newResource);
        }
        else if (MediaType.XHTML.equals(mediaType) || MediaType.XML.equals(mediaType))
        {
            href = "Text/" + fileName;
            newResource.setHref(href);
            addSpineResource(newResource);
        }
        else if (mediaType.isBitmapImage())
        {
            href = "Images/" + fileName;
            newResource.setHref(href);
            addResource(newResource);
        }
        else if (MediaType.JAVASCRIPT.equals(mediaType))
        {
            href = "Scripts/" + fileName;
            newResource.setHref(href);
            addResource(newResource);
        }
        else if (mediaType.isFont())
        {
            href = "Fonts/" + fileName;
            newResource.setHref(href);
            addResource(newResource);
        }
        else
        {
            href = "Misc/" + fileName;
            newResource.setHref(href);
            addResource(newResource);
        }
        return newResource;
    }

    /**
     * The collection of all images, chapters, sections, xhtml files, stylesheets, etc that make up the book.
     *
     * @return The collection of all images, chapters, sections, xhtml files, stylesheets, etc that make up the book.
     */
    public Resources getResources()
    {
        return resources;
    }


    /**
     * The sections of the book that should be shown if a user reads the book from start to finish.
     *
     * @return The Spine
     */
    public Spine getSpine()
    {
        return spine;
    }


    public void setSpine(Spine spine)
    {
        this.spine = spine;
    }

    /**
     * The Table of Contents of the book.
     *
     * @return The Table of Contents of the book.
     */
    public TableOfContents getTableOfContents()
    {
        return tableOfContents;
    }

    public void setTableOfContents(TableOfContents tableOfContents)
    {
        this.tableOfContents = tableOfContents;
    }

    /**
     * The book's cover page as a Resource.
     * An XHTML document containing a link to the cover image.
     *
     * @return The book's cover page as a Resource
     */
    public XHTMLResource getCoverPage()
    {
        return guide.getCoverPage();
    }

    public void setCoverPage(Resource<Document> coverPage)
    {
        if (coverPage == null)
        {
            return;
        }
        if (!resources.containsByHref(coverPage.getHref()))
        {
            resources.put(coverPage);
        }
        if (isEpub3()) {
            getLandmarks().addReference(new LandmarkReference(coverPage, LandmarkReference.Semantic.COVER, "Cover"));
        }
        //also in epub3 as compatibility
        guide.setCoverPage(coverPage);
    }

    /**
     * Gets the first non-blank title from the book's metadata.
     *
     * @return the first non-blank title from the book's metadata.
     */
    public String getTitle()
    {
        return getMetadata().getFirstTitle();
    }

    /**
     * The book's cover image.
     *
     * @return The book's cover image.
     */
    public ImageResource getCoverImage()
    {
        return coverImage;
    }

    public void setCoverImage(ImageResource coverImage)
    {
        if (coverImage == null)
        {
            return;
        }
        if (!resources.containsByHref(coverImage.getHref()))
        {
            resources.put(coverImage);
        }
        this.coverImage = coverImage;
    }

    /**
     * The guide; contains references to special sections of the book like colophon, glossary, etc.
     *
     * @return The guide; contains references to special sections of the book like colophon, glossary, etc.
     */
    public Guide getGuide()
    {
        return guide;
    }

    /**
     * All Resources of the Book that can be reached via the Spine, the TableOfContents or the Guide.
     * <p>
     * Consists of a list of "reachable" resources:
     * <ul>
     * <li>The coverpage</li>
     * <li>The resources of the Spine that are not already in the result</li>
     * <li>The resources of the Table of Contents that are not already in the result</li>
     * <li>The resources of the Guide that are not already in the result</li>
     * </ul>
     * To get all html files that make up the epub file use {@link #getResources()}
     *
     * @return All Resources of the Book that can be reached via the Spine, the TableOfContents or the Guide.
     */
    public List<Resource<?>> getContents()
    {
        Map<String, Resource<?>> result = new LinkedHashMap<>();
        addToContentsResult(getCoverPage(), result);

        for (SpineReference spineReference : getSpine().getSpineReferences())
        {
            addToContentsResult(spineReference.getResource(), result);
        }

        for (Resource<?> resource : getTableOfContents().getAllUniqueResources())
        {
            addToContentsResult(resource, result);
        }

        for (GuideReference guideReference : getGuide().getReferences())
        {
            addToContentsResult(guideReference.getResource(), result);
        }

        return new ArrayList<>(result.values());
    }

    /**
     * All Resources of the Book that can be reached via the Spine or the TableOfContents.
     * <p>
     * Consists of a list of "readable" resources, excludes the meta pages of the book like toc, cover page, impressum and so on:
     * <ul>
     * <li>The resources of the Spine that are not already in the result</li>
     * <li>The resources of the Table of Contents that are not already in the result</li>
     * </ul>
     * To get all html files that make up the epub file use {@link #getResources()}
     *
     * @return All Resources of the Book that can be reached via the Spine, the TableOfContents.
     */
    public List<Resource<?>> getReadableContents()
    {
        Map<String, Resource<?>> result = new LinkedHashMap<>();

        for (SpineReference spineReference : getSpine().getSpineReferences())
        {
            addToContentsResult(spineReference.getResource(), result);
        }

        for (Resource<?> resource : getTableOfContents().getAllUniqueResources())
        {
            addToContentsResult(resource, result);
        }

        if (isEpub3())
        {
            if (getEpub3NavResource() != null)
            {
                result.remove(getEpub3NavResource().getHref());
            }
            removeLandmarkEntries(result, LandmarkReference.Semantic.COPYRIGHT_PAGE);
            removeLandmarkEntries(result, LandmarkReference.Semantic.COVER);
            removeLandmarkEntries(result, LandmarkReference.Semantic.TITLE_PAGE);
        }
        else //EPUB 2
        {
            if (getGuide().getCoverPage() != null)
            {
                result.remove(getGuide().getCoverPage().getHref());
            }
            removeGuideEntries(result, GuideReference.Semantics.COPYRIGHT_PAGE);
            removeGuideEntries(result, GuideReference.Semantics.COVER);
            removeGuideEntries(result, GuideReference.Semantics.TITLE_PAGE);
            removeGuideEntries(result, GuideReference.Semantics.TOC);
        }

        return new ArrayList<>(result.values());
    }

    private void removeGuideEntries(Map<String, Resource<?>> result, GuideReference.Semantics semantic)
    {
        List<GuideReference> references = getGuide().getGuideReferencesByType(semantic);
        if (!references.isEmpty())
        {
            for (GuideReference reference : references)
            {
                result.remove(reference.getResource().getHref());
            }
        }
    }

    private void removeLandmarkEntries(Map<String, Resource<?>> result, LandmarkReference.Semantic semantic)
    {
        List<LandmarkReference> references = getLandmarks().getLandmarkReferencesByType(semantic);
        if (!references.isEmpty())
        {
            for (LandmarkReference reference : references)
            {
                result.remove(reference.getResource().getHref());
            }
        }
    }

    private static void addToContentsResult(Resource<?> resource, Map<String, Resource<?>> allReachableResources)
    {
        if (resource != null && (!allReachableResources.containsKey(resource.getHref())))
        {
            allReachableResources.put(resource.getHref(), resource);
        }
    }

    public void setNcxResource(Resource<?> ncxResource)
    {
        this.ncxResource = ncxResource;
    }

    public Resource<?> getNcxResource()
    {
        return ncxResource;
    }

    public final ObjectProperty<EpubVersion> versionProperty() {
        return versionProperty;
    }
    public final EpubVersion getVersion() {
        return versionProperty.get();
    }
    public final void setVersion(EpubVersion value) {
        versionProperty.set(value);
    }

    public boolean isFixedLayout()
    {
        return isEpub3() && isFixedLayout;
    }

    public boolean isEpub3()
    {
        return versionProperty.get().isEpub3();
    }

    public void setFixedLayout(boolean isFixedLayout)
    {
        this.isFixedLayout = isFixedLayout;
    }

    public Resource<Document> getEpub3NavResource()
    {
        return epub3NavResource;
    }

    public void setEpub3NavResource(Resource epub3NavResource)
    {
        this.epub3NavResource = epub3NavResource;
    }

    public Resource getAppleDisplayOptions()
    {
        return appleDisplayOptions;
    }

    public void setAppleDisplayOptions(Resource appleDisplayOptions)
    {
        this.appleDisplayOptions = appleDisplayOptions;
    }

    public boolean getBookIsChanged()
    {
        return bookIsChanged.get();
    }

    public BooleanProperty bookIsChangedProperty()
    {
        return bookIsChanged;
    }

    public void setBookIsChanged(boolean bookIsChanged)
    {
        this.bookIsChanged.set(bookIsChanged);
    }

    public Resource getOpfResource()
    {
        return opfResource.get();
    }

    public ObjectProperty<Resource<?>> opfResourceProperty()
    {
        return opfResource;
    }

    public void setOpfResource(Resource opfResource)
    {
        this.opfResource.set(opfResource);
    }

    public ObjectProperty<Path> physicalFileNameProperty() {
        return physicalFileNameProperty;
    }

    public Path getPhysicalFileName()
    {
        return physicalFileNameProperty.getValue();
    }

    public void setPhysicalFileName(Path physicalFileName)
    {
        this.physicalFileNameProperty.setValue(physicalFileName);
    }

    public int getFixedLayoutWidth()
    {
        return fixedLayoutWidth;
    }

    public void setFixedLayoutWidth(int fixedLayoutWidth)
    {
        this.fixedLayoutWidth = fixedLayoutWidth;
    }

    public int getFixedLayoutHeight()
    {
        return fixedLayoutHeight;
    }

    public void setFixedLayoutHeight(int fixedLayoutHeight)
    {
        this.fixedLayoutHeight = fixedLayoutHeight;
    }

    public void renameResource(Resource<?> resource, String oldValue, String newValue)
    {
        resources.remove(oldValue); //remove resource with old name, because the search key is the name of the resource
        resources.put(resource); //and put again with new name

        if (MediaType.CSS.equals(resource.getMediaType()))
        {
            //css umbenannt, erstmal alle XHTMLs durchsuchen
            List<Resource<?>> xhtmlResources = resources.getResourcesByMediaType(MediaType.XHTML);
            Path resourcePath = resource.getHrefAsPath();
            int index = StringUtils.lastIndexOf(oldValue, "/");
            String oldFileName = oldValue;
            if (index > -1)
            {
                oldFileName = oldValue.substring(index + 1);
            }

            index = StringUtils.lastIndexOf(newValue, "/");
            String newFileName = newValue;
            if (index > -1)
            {
                newFileName = newValue.substring(index + 1);
            }

            for (Resource<?> xhtmlResource : xhtmlResources)
            {
                Document document = ((XHTMLResource)xhtmlResource).asNativeFormat();
                Path relativePath = xhtmlResource.getHrefAsPath().relativize(resourcePath);
                AttributeElementFilter hrefFilter = new AttributeElementFilter("href", relativePath + "/" + oldFileName);
                IteratorIterable<Element> descendants = document.getDescendants(hrefFilter);
                for (Element descendant : descendants)
                {
                    logger.info("found element with attribute href in resource " + xhtmlResource);
                    descendant.setAttribute("href", relativePath + "/" + newFileName);
                }
                //nach noch mehr Elementen suchen
                //zB src-Attribut
                xhtmlResource.setData(XHTMLUtils.outputXHTMLDocument(document, true, getVersion()));
                ((XHTMLResource) xhtmlResource).prepareWebViewDocument(getVersion());
                xhtmlResource.triggerExternalChanged();
            }

            //weiter nach import etc. in anderen css dateien suchen
        }
        else if(MediaType.XHTML.equals(resource.getMediaType()))
        {
            //refresh ncx
            refreshNcxResource();

            //replacing href in other xhtml files
            replaceHrefInXhtmlResources(oldValue, newValue);
        }
        else if(resource.getMediaType().isBitmapImage())
        {
            //nach href und src
        }
        refreshOpfResource();
    }

    public Landmarks getLandmarks()
    {
        return landmarks;
    }

    /**
     * Changes or deletes some informations of the book, if it's used as template
     */
    public void applyTemplate() {
        getMetadata().generateNewUuid();
        refreshOpfResource();
        refreshNcxResource();
        setPhysicalFileName(null);
        setBookIsChanged(true);
    }
}

