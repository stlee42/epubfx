package de.machmireinebook.epubeditor.epublib.toc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.log4j.Logger;

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
import de.machmireinebook.epubeditor.manager.TemplateManager;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;
import de.machmireinebook.epubeditor.preferences.TocPosition;

import static de.machmireinebook.epubeditor.epublib.Constants.CLASS_SIGIL_NOT_IN_TOC;
import static de.machmireinebook.epubeditor.epublib.Constants.IGNORE_IN_TOC;

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
                XHTMLResource xhtmlResource = (XHTMLResource) resource;
                Document document = xhtmlResource.asNativeFormat();
                IteratorIterable<Element> possibleTocEntries = document.getDescendants(new PossibleTocEntryFilter());
                for (Element possibleTocEntry : possibleTocEntries)
                {
                    ChoosableTocEntry tocEntry = new ChoosableTocEntry();
                    tocEntry.setLevel(possibleTocEntry.getName());
                    tocEntry.setReference(xhtmlResource.getHref());
                    tocEntry.setTitle(possibleTocEntry.getValue());
                    tocEntry.setResource(xhtmlResource);

                    if (possibleTocEntry.getAttributeValue("class") != null
                            && (possibleTocEntry.getAttributeValue("class").contains(CLASS_SIGIL_NOT_IN_TOC) ||
                                possibleTocEntry.getAttributeValue("class").contains(IGNORE_IN_TOC)))
                    {
                        tocEntry.setChoosed(false);
                    }
                    else
                    {
                        tocEntry.setChoosed(true);
                    }

                    int levelPossibleEntry = getLevel(possibleTocEntry.getName());

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

    public void generateNav(List<TocEntry> tocEntries)
    {
        //first add entries to books toc
        Book book = getBook();
        book.getTableOfContents().setTocReferences(tocEntries);
        try
        {
            Document navDoc = templateManager.getNavTemplate();
            Element root = navDoc.getRootElement();
            //for now we presume that nav template is how we expect this
            Element bodyElement = root.getChild("body", Namespace.XML_NAMESPACE);
            if (bodyElement != null)
            {
                List<Element> navElements = root.getChildren("nav", Namespace.XML_NAMESPACE);
                for (Element navElement : navElements)
                {
                    if (navElement.getAttributeValue("id").equals("nav"))
                    {
                        generateToc(tocEntries, navElement);
                    }
                    else if (navElement.getAttributeValue("id").equals("landmark"))
                    {
                        generateLandmarks(tocEntries, navElement);
                    }
                }
            }
            XHTMLResource resource = new XHTMLResource(navDoc, "../Text/nav.xhtml");
            if (preferencesManager.getTocPosition().equals(TocPosition.AFTER_COVER) && book.getCoverPage() != null)
            {
                book.getSpine().getResourceIndex(book.getCoverPage());
            }
            //add to spine and set as toc resource beacuse its xhtml too
            book.addSpineResource(resource);
            book.getSpine().setTocResource(resource);
        }
        catch (IOException | JDOMException e)
        {
            logger.error("can't generate nav", e);
        }
    }

    private void generateToc(List<TocEntry> tocEntries, Element navElement)
    {
        Element h1Element = navElement.getChild("h1");
        String tocHeadline = preferencesManager.getHeadlineToc();
        h1Element.setText(tocHeadline);
        Element olElement = new Element("ol", Namespace.XML_NAMESPACE);
        navElement.addContent(olElement);
    }

    private void generateLandmarks(List<TocEntry> tocEntries, Element navElement)
    {

    }

    public void generateNcx(List<TocEntry> tocEntries)
    {

    }


}
