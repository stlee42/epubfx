package de.machmireinebook.epubeditor.epublib.toc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.AbstractFilter;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.domain.epub3.EpubType;
import de.machmireinebook.epubeditor.epublib.domain.epub3.LandmarkReference;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Landmarks;
import de.machmireinebook.epubeditor.epublib.epub2.NCXDocument;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResource;
import de.machmireinebook.epubeditor.jdom2.AttributeElementFilter;
import de.machmireinebook.epubeditor.manager.TemplateManager;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;
import de.machmireinebook.epubeditor.preferences.TocPosition;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import static de.machmireinebook.epubeditor.epublib.Constants.IGNORE_IN_TOC_CLASS_NAMES;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_EPUB;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_XHTML;

/**
 * Created by Michail Jungierek
 */
@Named
public class TocGenerator
{
    private static final Logger logger = Logger.getLogger(TocGenerator.class);

    @Inject
    private TemplateManager templateManager;
    @Inject
    private PreferencesManager preferencesManager;

    public static class TocGeneratorResult
    {
        private final Resource<Document> tocResource;
        private final Map<Resource<Document>, Document> resourcesToRewrite;

        public TocGeneratorResult(Resource<Document> tocResource, Map<Resource<Document>, Document> resourcesToRewrite)
        {
            this.tocResource = tocResource;
            this.resourcesToRewrite = resourcesToRewrite;
        }

        public Resource<Document> getTocResource()
        {
            return tocResource;
        }

        public Map<Resource<Document>, Document> getResourcesToRewrite()
        {
            return resourcesToRewrite;
        }
    }

    // bookProperty
    private final ObjectProperty<Book> bookProperty = new SimpleObjectProperty<>(this, "book");

    public final ObjectProperty<Book> bookProperty()
    {
        return bookProperty;
    }

    public final Book getBook()
    {
        return bookProperty.get();
    }

    public final void setBook(Book value)
    {
        bookProperty.set(value);
    }


    private static class PossibleTocEntryFilter extends AbstractFilter<Element>
    {
        private static final List<String> ELEMENT_NAMES = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6");

        @Override
        public Element filter(Object content)
        {
            if (content instanceof Element)
            {
                Element el = (Element) content;
                if (ELEMENT_NAMES.contains(el.getName()))
                {
                    return el;
                }
            }
            return null;
        }
    }

    public List<EditableTocEntry> generateTocEntriesFromText()
    {
        List<EditableTocEntry> tocEntries = new ArrayList<>();
        Book book = bookProperty.get();
        List<Resource<?>> contentResources = book.getReadableContents();
        EditableTocEntry currentHighestEntry = null;
        int currentHighestLevel = 999;

        for (Resource<?> resource : contentResources)
        {
            if (resource.getMediaType().equals(MediaType.XHTML))
            {
                int tocEntriesinResource = 0;
                XHTMLResource xhtmlResource = (XHTMLResource) resource;
                Document document = xhtmlResource.asNativeFormat();
                IteratorIterable<Element> possibleTocEntryElements = document.getDescendants(new PossibleTocEntryFilter());
                for (Element possibleTocEntryElement : possibleTocEntryElements)
                {
                    EditableTocEntry tocEntry = new EditableTocEntry();
                    tocEntry.setLevel(possibleTocEntryElement.getName());
                    tocEntry.setReference(xhtmlResource.getHref());
                    if (StringUtils.isNotEmpty(possibleTocEntryElement.getAttributeValue("title")))
                    {
                        tocEntry.setTitle(possibleTocEntryElement.getAttributeValue("title"));
                    }
                    else
                    {
                        tocEntry.setTitle(possibleTocEntryElement.getValue());
                    }
                    tocEntry.setResource(xhtmlResource);
                    tocEntry.setDocument(document);
                    tocEntry.setCorrespondingElement(possibleTocEntryElement);

                    if (possibleTocEntryElement.getAttribute("id") != null)
                    {
                        tocEntry.setFragmentId(possibleTocEntryElement.getAttribute("id").getValue());
                    }
                    if (possibleTocEntryElement.getAttribute("name") != null)
                    {
                        tocEntry.setFragmentId(possibleTocEntryElement.getAttribute("name").getValue());
                    }
                    else if (tocEntriesinResource > 0) //more then one toc entry in this resource without id, write a new id on this element
                    {
                        String tocId = "toc-id-" + tocEntriesinResource;
                        possibleTocEntryElement.setAttribute("id", tocId);
                        tocEntry.setFragmentId(tocId);
                    }
                    tocEntriesinResource++;


                    tocEntry.setChoosed(possibleTocEntryElement.getAttributeValue("class") == null
                            || !IGNORE_IN_TOC_CLASS_NAMES.contains(possibleTocEntryElement.getAttributeValue("class")));

                    int levelPossibleEntry = getLevel(possibleTocEntryElement.getName());

                    if (currentHighestEntry == null || levelPossibleEntry <= currentHighestLevel)
                    {
                        tocEntries.add(tocEntry);
                        currentHighestEntry = tocEntry;
                        currentHighestLevel = levelPossibleEntry;
                    }
                    else
                    {
                        currentHighestEntry.addChildSection(tocEntry);
                    }
                }
            }
        }
        return tocEntries;
    }

    public List<EditableTocEntry> generateTocEntriesFromToc()
    {
        List<EditableTocEntry> tocEntries = new ArrayList<>();
        Book book = bookProperty.get();

        TableOfContents toc = book.getTableOfContents();
        for (TocEntry tocEntry : toc.getTocReferences())
        {
            if (tocEntry instanceof EditableTocEntry)
            {
                tocEntries.add((EditableTocEntry) tocEntry);
            }
            else
            {
                EditableTocEntry editableTocEntry = new EditableTocEntry(tocEntry);
                setEditingValues(editableTocEntry);
                tocEntries.add(editableTocEntry);
            }
        }

        return tocEntries;
    }

    private void setEditingValues(EditableTocEntry editableTocEntry)
    {
        editableTocEntry.setChoosed(true);
        Document document = editableTocEntry.getResource().asNativeFormat();
        editableTocEntry.setDocument(document);
        //fill the needed information for editing the toc entries
        //it's not possible to map a toc entry to an element in document in all cases, because the toc could be edited manually
        //and a toc entry must not map to an xml element in resource
        // but if the toc entry has an fragment id then a xhtml element with this id or name should be in code
        if (editableTocEntry.hasFragmentId())
        {
            String fragmentId = editableTocEntry.getFragmentId();
            AttributeElementFilter idFilter = new AttributeElementFilter("id", fragmentId);
            AttributeElementFilter nameFilter = new AttributeElementFilter("name", fragmentId);
            IteratorIterable<? extends Content> elementsWithFragment = document.getDescendants(idFilter.or(nameFilter));
            if (elementsWithFragment.hasNext())
            {
                editableTocEntry.setCorrespondingElement((Element)elementsWithFragment.next());
            }
        }

        //set needed values for all children too
        for (TocEntry child : editableTocEntry.getChildren())
        {
            setEditingValues((EditableTocEntry)child);
        }
    }

    public static int getLevel(String tagName)
    {
        int level = -1;
        switch (tagName)
        {
            case "h1":
                level = 1;
                break;
            case "h2":
                level = 2;
                break;
            case "h3":
                level = 3;
                break;
            case "h4":
                level = 4;
                break;
            case "h5":
                level = 5;
                break;
            case "h6":
                level = 6;
                break;
            case "p":
                level = 7;
                break;
        }
        return level;
    }

    public TocGeneratorResult generateNav(List<TocEntry> tocEntries) throws IOException, JDOMException
    {
        //first add entries to books toc
        Book book = getBook();
        book.setBookIsChanged(true);
        book.getTableOfContents().setTocReferences(tocEntries);
        Resource<Document> navResource = book.getEpub3NavResource();

        Map<Resource<Document>, Document> resourcesToRewrite = new HashMap<>();

        Document navDoc = templateManager.getNavTemplate();  //ever recreate the nav by template
        Element originalHeadElement = null; //but try to use the original head because the title, styles etc.
        if (navResource == null) {
            //set here with empty data, will at the end replaced by the created document
            navResource = new XHTMLResource(new byte[]{}, "Text/nav.xhtml");
            if (preferencesManager.getTocPosition().equals(TocPosition.AFTER_COVER) && book.getCoverPage() != null) {
                int index = book.getSpine().getResourceIndex(book.getCoverPage());
                book.addSpineResource(navResource, index);
            } else {//if no cover or other setting put new toc at the end

                //add to spine because its xhtml too
                book.addSpineResource(navResource);
            }
        } else {
            Document originalNavDoc = navResource.asNativeFormat();
            Element originalRoot = originalNavDoc.getRootElement();
            if (originalRoot != null) {
                originalHeadElement = originalRoot.getChild("head", NAMESPACE_XHTML);
                originalHeadElement.detach();
            }
        }

        TocGeneratorResult result = new TocGeneratorResult(navResource, resourcesToRewrite);
        //for now we presume that nav template is in most parts how we expect it
        Element root = navDoc.getRootElement();

        Attribute langAttribute = root.getAttribute("lang", Namespace.XML_NAMESPACE);
        setLanguage(book, langAttribute, root);

        Element headElement = root.getChild("head", NAMESPACE_XHTML);
        if (originalHeadElement != null) {
            root.removeContent(headElement);
            root.setContent(0, originalHeadElement);
        } else {
            if (headElement != null)
            {
                Element titleElement = headElement.getChild("title", NAMESPACE_XHTML);
                if (titleElement == null)
                {
                    titleElement = new Element("title", NAMESPACE_XHTML);
                    headElement.addContent(titleElement);
                }
                titleElement.setText(book.getTitle());
            }
        }

        Element bodyElement = root.getChild("body", NAMESPACE_XHTML);
        if (bodyElement != null)
        {
            //language attribute
            langAttribute = bodyElement.getAttribute("lang", Namespace.XML_NAMESPACE);
            setLanguage(book, langAttribute, bodyElement);

            //the different navs
            List<Element> navElements = bodyElement.getChildren("nav", NAMESPACE_XHTML);
            for (Element navElement : navElements)
            {
                navElement.getChildren().clear();
                if (navElement.getAttributeValue("type", NAMESPACE_EPUB).equals(EpubType.toc.getSpecificationName()))
                {
                    generateToc(tocEntries, navElement, result);
                }
                else if (navElement.getAttributeValue("type", NAMESPACE_EPUB).equals(EpubType.landmarks.getSpecificationName()))
                {
                    generateLandmarks(navResource, navElement);
                }
            }
        }
        navResource.setData(XHTMLUtils.outputXHTMLDocument(navDoc, true, book.getVersion()));
        return result;
    }

    private void setLanguage(Book book, Attribute langAttribute, Element element) {
        if (langAttribute == null)
        {
            if (book.getMetadata().getLanguage() != null) {
                element.setAttribute("lang", book.getMetadata().getLanguage(), Namespace.XML_NAMESPACE);
            } else {
                element.setAttribute("lang", preferencesManager.getLanguageSpellSelection().getStorageContent(), Namespace.XML_NAMESPACE);
            }
        }
        else
        {
            if (book.getMetadata().getLanguage() != null) {
                langAttribute.setValue(book.getMetadata().getLanguage());
            } else {
                langAttribute.setValue(preferencesManager.getLanguageSpellSelection().getStorageContent());
            }
        }
    }

    private void generateToc(List<TocEntry> tocEntries, Element navElement, TocGeneratorResult tocGeneratorResult)
    {
        Element h1Element = navElement.getChild("h1", NAMESPACE_XHTML);
        if (h1Element == null)
        {
            h1Element = new Element("h1", NAMESPACE_XHTML);
            navElement.addContent(h1Element);
        }
        Book book = bookProperty.get();
        TableOfContents toc = book.getTableOfContents();
        String tocHeadline;
        if (StringUtils.isNotEmpty(toc.getTocTitle())) {
            tocHeadline = toc.getTocTitle();
        } else {
            tocHeadline = preferencesManager.getHeadlineToc();
            toc.setTocTitle(tocHeadline);
        }
        h1Element.setText(tocHeadline);

        generateNavOrderedList(tocEntries, navElement, tocGeneratorResult);
    }

    private void generateNavOrderedList(List<TocEntry> tocEntries, Element parentElement, TocGeneratorResult tocGeneratorResult)
    {
        Element olElement = new Element("ol", NAMESPACE_XHTML);
        parentElement.addContent(olElement);
        for (TocEntry tocEntry : tocEntries)
        {
            Element liElement = new Element("li", NAMESPACE_XHTML);
            olElement.addContent(liElement);


            Path navPath = tocGeneratorResult.getTocResource().getHrefAsPath();
            Path relativePath = navPath.relativize(tocEntry.getCompleteHrefAsPath());

            Element anchorElement = new Element("a", NAMESPACE_XHTML);
            anchorElement.setAttribute("href", relativePath.toString());
            anchorElement.setText(tocEntry.getTitle());
            liElement.addContent(anchorElement);

            if (tocEntry.hasChildren())
            {
                generateNavOrderedList(tocEntry.getChildren(), liElement, tocGeneratorResult);
            }

            //toc entry has a fragment id, then the resource has to rewrite that this id is available in resource
            //or the title was changed in process of toc generation
            if (tocEntry instanceof EditableTocEntry
                    && (StringUtils.isNotEmpty(tocEntry.getFragmentId()) || ((EditableTocEntry)tocEntry).isTitleChanged())) {
                tocGeneratorResult.getResourcesToRewrite().put(tocEntry.getResource(), ((EditableTocEntry)tocEntry).getDocument());
            }
        }
    }

    private void generateLandmarks(Resource<Document> navResource, Element navElement)
    {
        //insert per default the toc, and try to find outside of nav titlepage, copyright page and cover
        Book book = getBook();
        Landmarks landmarks = book.getLandmarks();

        Element titleElement = new Element("h1");
        if (StringUtils.isEmpty(landmarks.getTitle())) {
            landmarks.setTitle(preferencesManager.getHeadlineLandmarks());
        }

        titleElement.setText(landmarks.getTitle());
        navElement.addContent(titleElement);

        Element olElement = new Element("ol");
        navElement.addContent(olElement);

        if (landmarks.isEmpty()) {
            //insert the toc             
            //<a epub:type="toc" href="#toc">Inhaltsverzeichnis</a>
            LandmarkReference tocReference = new LandmarkReference(navResource, LandmarkReference.Semantic.TOC, preferencesManager.getHeadlineToc());
            landmarks.addReference(tocReference);

            Resource<Document> coverPage = book.getCoverPage();
            if (coverPage != null) {
                landmarks.addReference(new LandmarkReference(coverPage, LandmarkReference.Semantic.COVER, LandmarkReference.Semantic.COVER.getDescription()));
            }
        }

        for (LandmarkReference landmark : landmarks) {
            Element liElement = new Element("li");
            olElement.addContent(liElement);

            Element ahrefElement = new Element("a");
            liElement.addContent(ahrefElement);
            
            ahrefElement.setAttribute("type", landmark.getType().getName(), NAMESPACE_EPUB);

            Path navPath = navResource.getHrefAsPath();
            Path relativePath = navPath.relativize(landmark.getCompleteHrefAsPath());
            ahrefElement.setAttribute("href", relativePath.toString());
            ahrefElement.setText(landmark.getTitle());
        }
    }

    public TocGeneratorResult generateNcx(List<TocEntry> tocEntries)
    {
        Book book = getBook();
        book.setBookIsChanged(true);
        book.getTableOfContents().setTocReferences(tocEntries);

        Map<Resource<Document>, Document> resourcesToRewrite = new HashMap<>();

        Resource<Document> ncxResource = NCXDocument.createNCXResource(book.getMetadata().getIdentifiers(), book);
        book.setNcxResource(ncxResource);
        book.getSpine().setTocResource(ncxResource);
        //in epub3 it will not be recreated at saving the book as for epub2, because this overwrite the old one in resources
        if (book.getVersion().isEpub3()) {
            book.getResources().put(ncxResource);
        }
        book.refreshOpfResource();

        TocGeneratorResult result = new TocGeneratorResult(ncxResource, resourcesToRewrite);
        generateNcxResourcesToRewrite(tocEntries, result);
        return result;
    }

    private void generateNcxResourcesToRewrite(List<TocEntry> tocEntries, TocGeneratorResult tocGeneratorResult)
    {
        for (TocEntry tocEntry : tocEntries)
        {
            if (tocEntry.hasChildren())
            {
                generateNcxResourcesToRewrite(tocEntry.getChildren(), tocGeneratorResult);
            }

            if (StringUtils.isNotEmpty(tocEntry.getFragmentId()) && !getBook().getVersion().isEpub3()) //not needed if the ncx is created from nav
            {
                tocGeneratorResult.getResourcesToRewrite().put(tocEntry.getResource(), ((EditableTocEntry) tocEntry).getDocument());
            }
        }
    }

    public TocGenerator.TocGeneratorResult createNcxFromNav() {
        TableOfContents toc = bookProperty.get().getTableOfContents();
        TocGenerator.TocGeneratorResult result = generateNcx(toc.getTocReferences());
        return result;
    }


}
