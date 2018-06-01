package de.machmireinebook.epubeditor.epublib.toc;

import javafx.util.StringConverter;

import org.jdom2.Document;
import org.jdom2.Element;

import de.machmireinebook.epubeditor.epublib.domain.TocEntry;

/**
 * Created by Michail Jungierek, Acando GmbH on 23.05.2018
 */
public class ChoosableTocEntry extends TocEntry<ChoosableTocEntry> implements Cloneable
{
    private boolean choosed;
    private boolean titleChanged = false;
    private Document document;
    private Element correspondingElement;

    public ChoosableTocEntry()
    {
    }

    public ChoosableTocEntry(TocEntry<? extends TocEntry> tocEntry)
    {
        this.setTitle(tocEntry.getTitle());
        this.setFragmentId(tocEntry.getFragmentId());
        for (TocEntry<? extends TocEntry> child : tocEntry.getChildren())
        {
            ChoosableTocEntry choosableChild = new ChoosableTocEntry(child);
            this.getChildren().add(choosableChild);
        }
        this.setReference(tocEntry.getReference());
        this.setResource(tocEntry.getResource());
        this.setLevel(tocEntry.getLevel());
    }



    public boolean getChoosed()
    {
        return choosed;
    }

    public void setChoosed(boolean choosed)
    {
        this.choosed = choosed;
    }

    public Document getDocument()
    {
        return document;
    }

    public void setDocument(Document document)
    {
        this.document = document;
    }

    public static class ChoosableTocEntryStringConverter extends StringConverter<ChoosableTocEntry>
    {
        @Override
        public String toString(ChoosableTocEntry object)
        {
            return object.getTitle();
        }

        @Override
        public ChoosableTocEntry fromString(String string)
        {
            return null;
        }
    }

    @Override
    public ChoosableTocEntry clone()
    {
        ChoosableTocEntry newTocEntry = (ChoosableTocEntry) super.clone();
        newTocEntry.setDocument(document.clone());
        return newTocEntry;
    }

    public boolean isTitleChanged()
    {
        return titleChanged;
    }

    public void setTitleChanged(boolean titleChanged)
    {
        this.titleChanged = titleChanged;
    }

    public Element getCorrespondingElement()
    {
        return correspondingElement;
    }

    public void setCorrespondingElement(Element correspondingElement)
    {
        this.correspondingElement = correspondingElement;
    }
}