package de.machmireinebook.epubeditor.epublib.toc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.AbstractFilter;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.TocEntry;
import de.machmireinebook.epubeditor.epublib.domain.XHTMLResource;

/**
 * Created by Michail Jungierek, Acando GmbH on 18.05.2018
 */
@Named
public class TocGenerator
{
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

    public List<TocEntry> generateTocEntriesFromText()
    {
        List<TocEntry> tocEntries = new ArrayList<>();
        Book book = bookProperty.get();
        List<Resource> contentResources = book.getReadableContents();
        TocEntry currentHighestEntry = null;
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
                    TocEntry tocEntry = new TocEntry();
                    tocEntry.setLevel(possibleTocEntry.getName());
                    tocEntry.setReference(xhtmlResource.getHref());
                    tocEntry.setTitle(possibleTocEntry.getValue());
                    tocEntry.setResource(xhtmlResource);
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

}
