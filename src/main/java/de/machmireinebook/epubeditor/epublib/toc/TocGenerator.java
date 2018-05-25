package de.machmireinebook.epubeditor.epublib.toc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.AbstractFilter;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.domain.XHTMLResource;
import de.machmireinebook.epubeditor.epublib.domain.epub3.EpubType;
import de.machmireinebook.epubeditor.manager.TemplateManager;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;
import de.machmireinebook.epubeditor.preferences.TocPosition;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;

import static de.machmireinebook.epubeditor.epublib.Constants.*;

/**
 * Created by Michail Jungierek, Acando GmbH on 18.05.2018
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
        private Resource tocResource;
        private Map<Resource, Document> resourcesToRewrite;

        public TocGeneratorResult(Resource tocResource, Map<Resource, Document> resourcesToRewrite)
        {
            this.tocResource = tocResource;
            this.resourcesToRewrite = resourcesToRewrite;
        }

        public Resource getTocResource()
        {
            return tocResource;
        }

        public Map<Resource, Document> getResourcesToRewrite()
        {
            return resourcesToRewrite;
        }
    }

    // bookProperty
    private final ObjectProperty<Book> bookProperty = new SimpleObjectProperty<>(this, "book");
    public final ObjectProperty<Book> bookProperty() {
       return bookProperty;
    }
    public final Book getBook() {
       return bookProperty.get();
    }
    public final void setBook(Book value) {
        bookProperty.set(value);
    }


    private static class PossibleTocEntryFilter extends AbstractFilter<Element>
    {
        private static final List<String> ELEMENT_NAMES = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6");

        @Override
        public Element filter(Object content)
        {
            if (content instanceof Element) {
                Element el = (Element) content;
                if (ELEMENT_NAMES.contains(el.getName()))
                {
                    return el;
                }
            }
            return null;
        }
    }

    public List<ChoosableTocEntry> generateTocEntriesFromText()
    {
        List<ChoosableTocEntry> tocEntries = new ArrayList<>();
        Book book = bookProperty.get();
        List<Resource> contentResources = book.getReadableContents();
        ChoosableTocEntry currentHighestEntry = null;
        int currentHighestLevel = 999;

        for (Resource resource : contentResources)
        {
            if (resource.getMediaType().equals(MediaType.XHTML))
            {
                int tocEntriesinResource = 0;
                XHTMLResource xhtmlResource = (XHTMLResource) resource;
                Document document = xhtmlResource.asNativeFormat();
                IteratorIterable<Element> possibleTocEntrieElements = document.getDescendants(new PossibleTocEntryFilter());
                for (Element possibleTocEntryElement : possibleTocEntrieElements)
                {
                    ChoosableTocEntry tocEntry = new ChoosableTocEntry();
                    tocEntry.setLevel(possibleTocEntryElement.getName());
                    tocEntry.setReference(xhtmlResource.getHref());
                    tocEntry.setTitle(possibleTocEntryElement.getValue());
                    tocEntry.setResource(xhtmlResource);
                    tocEntry.setDocument(document);

                    if (possibleTocEntryElement.getAttribute("id") != null)
                    {
                        tocEntry.setFragmentId(possibleTocEntryElement.getAttribute("id").getValue());
                    }
                    else if (tocEntriesinResource > 0) //more then one toc entry in this resource without id, write a new id on this element
                    {
                        String tocId = "toc-id-" + tocEntriesinResource;
                        possibleTocEntryElement.setAttribute("id", tocId);
                        tocEntry.setFragmentId(tocId);
                    }
                    tocEntriesinResource++;


                    if (possibleTocEntryElement.getAttributeValue("class") != null
                            && (possibleTocEntryElement.getAttributeValue("class").contains(CLASS_SIGIL_NOT_IN_TOC) ||
                                possibleTocEntryElement.getAttributeValue("class").contains(IGNORE_IN_TOC)))
                    {
                        tocEntry.setChoosed(false);
                    }
                    else
                    {
                        tocEntry.setChoosed(true);
                    }

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

    private int getLevel(String tagName)
    {
        int level = -1;
        switch (tagName)
        {
            case "h1" : level = 1;
                break;
            case "h2" : level = 2;
                break;
            case "h3" : level = 3;
                break;
            case "h4" : level = 4;
                break;
            case "h5" : level = 5;
                break;
            case "h6" : level = 6;
                break;
            case "p" : level = 7;
                break;
        }
        return level;
    }

    public TocGeneratorResult generateNav(List<TocEntry<? extends TocEntry>> tocEntries)
    {
        //first add entries to books toc
        Book book = getBook();
        book.setBookIsChanged(true);
        book.getTableOfContents().setTocReferences(tocEntries);
        Resource navResource = book.getSpine().getTocResource();

        Map<Resource, Document> resourcesToRewrite = new HashMap<>();

        try
        {
            Document navDoc;
            if(navResource == null)
            {
                navDoc = templateManager.getNavTemplate();
            }
            else
            {
                navDoc = (Document) navResource.asNativeFormat();
            }

            //for now we presume that nav template is in most parts how we expect it
            Element root = navDoc.getRootElement();

            Attribute langAttribute = root.getAttribute("lang", Namespace.XML_NAMESPACE);
            if (langAttribute == null)
            {
                root.setAttribute("lang", book.getMetadata().getLanguage(),  Namespace.XML_NAMESPACE);
            }
            else
            {
                langAttribute.setValue(book.getMetadata().getLanguage());
            }
            Element headElement = root.getChild("head", NAMESPACE_XHTML);
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

            Element bodyElement = root.getChild("body", NAMESPACE_XHTML);
            if (bodyElement != null)
            {
                //language attribute
                langAttribute = bodyElement.getAttribute("lang", Namespace.XML_NAMESPACE);
                if (langAttribute == null)
                {
                    bodyElement.setAttribute("lang", book.getMetadata().getLanguage(),  Namespace.XML_NAMESPACE);
                }
                else
                {
                    langAttribute.setValue(book.getMetadata().getLanguage());
                }

                //the different navs
                List<Element> navElements = bodyElement.getChildren("nav", NAMESPACE_XHTML);
                for (Element navElement : navElements)
                {
                    navElement.getChildren().clear();
                    if (navElement.getAttributeValue("type", NAMESPACE_EPUB).equals(EpubType.toc.getSepcificationName()))
                    {
                        generateToc(tocEntries, navElement, resourcesToRewrite);
                    }
                    else if (navElement.getAttributeValue("type", NAMESPACE_EPUB).equals(EpubType.landmarks.getSepcificationName()))
                    {
                        generateLandmarks(tocEntries, navElement);
                    }
                }
            }
            if (navResource == null)
            {
                XHTMLResource resource = new XHTMLResource(navDoc, "../Text/nav.xhtml");
                if (preferencesManager.getTocPosition().equals(TocPosition.AFTER_COVER) && book.getCoverPage() != null)
                {
                    int index = book.getSpine().getResourceIndex(book.getCoverPage());
                    book.addSpineResource(resource, index);
                }
                else //if no cover or other setting put new toc at the end
                {
                    //add to spine and set as toc resource beacuse its xhtml too
                    book.addSpineResource(resource);
                    book.getSpine().setTocResource(resource);
                }
            }
            else
            {
                navResource.setData(XHTMLUtils.outputXHTMLDocument(navDoc));
            }
        }
        catch (IOException | JDOMException e)
        {
            logger.error("can't generate nav", e);
        }
        return new TocGeneratorResult(navResource, resourcesToRewrite);
    }

    private void generateToc(List<TocEntry<? extends TocEntry>> tocEntries, Element navElement,Map<Resource, Document> resourcesToRewrite)
    {
        Element h1Element = navElement.getChild("h1", NAMESPACE_XHTML);
        if (h1Element == null)
        {
            h1Element = new Element("h1", NAMESPACE_XHTML);
            navElement.addContent(h1Element);
        }
        String tocHeadline = preferencesManager.getHeadlineToc();
        h1Element.setText(tocHeadline);

        generateNavOrderedList(tocEntries, navElement, resourcesToRewrite);
    }

    private void generateNavOrderedList(List<TocEntry<? extends TocEntry>> tocEntries, Element parentElement, Map<Resource, Document> resourcesToRewrite)
    {
        Element olElement = new Element("ol", NAMESPACE_XHTML);
        parentElement.addContent(olElement);
        for (TocEntry<? extends TocEntry> tocEntry : tocEntries)
        {
            Element liElement = new Element("li", NAMESPACE_XHTML);
            olElement.addContent(liElement);

            Element anchorElement = new Element("a", NAMESPACE_XHTML);
            anchorElement.setAttribute("href", tocEntry.getCompleteRelativeHref("../Text/nav.xhtml"));
            anchorElement.setText(tocEntry.getTitle());
            liElement.addContent(anchorElement);

            if (!tocEntry.getChildren().isEmpty())
            {
                generateNavOrderedList(tocEntry.getChildren(), liElement, resourcesToRewrite);
            }

            if (StringUtils.isNotEmpty(tocEntry.getFragmentId()) && tocEntry instanceof ChoosableTocEntry)
            {
                resourcesToRewrite.put(tocEntry.getResource(), ((ChoosableTocEntry)tocEntry).getDocument());
            }
        }
    }

    private void generateLandmarks(List<TocEntry<? extends TocEntry>> tocEntries, Element navElement)
    {

    }

    public TocGeneratorResult generateNcx(List<TocEntry<? extends TocEntry>> tocEntries)
    {
        return null;
    }


}
