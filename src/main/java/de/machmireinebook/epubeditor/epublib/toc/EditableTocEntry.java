package de.machmireinebook.epubeditor.epublib.toc;

import javafx.util.StringConverter;

import org.apache.commons.lang3.StringUtils;

import org.jdom2.Document;
import org.jdom2.Element;

import de.machmireinebook.epubeditor.epublib.domain.TocEntry;

/**
 * Created by Michail Jungierek
 */
public class EditableTocEntry extends TocEntry<EditableTocEntry, Document> implements Cloneable
{
    private boolean choosed;
    private boolean titleChanged = false;
    private Document document;
    private Element correspondingElement;

    public EditableTocEntry()
    {
    }

    public EditableTocEntry(TocEntry<? extends TocEntry, ?> tocEntry)
    {
        this.setTitle(tocEntry.getTitle());
        for (TocEntry<? extends TocEntry, ?> child : tocEntry.getChildren())
        {
            EditableTocEntry choosableChild = new EditableTocEntry(child);
            this.getChildren().add(choosableChild);
        }
        this.setReference(tocEntry.getReference());
        this.setResource(tocEntry.getResource(), tocEntry.getFragmentId());
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

    public static class ChoosableTocEntryStringConverter extends StringConverter<EditableTocEntry>
    {
        @Override
        public String toString(EditableTocEntry object)
        {
            return object.getTitle();
        }

        @Override
        public EditableTocEntry fromString(String string)
        {
            return null;
        }
    }

    @Override
    public EditableTocEntry clone()
    {
        EditableTocEntry newTocEntry = (EditableTocEntry) super.clone();
        if (document != null)
        {
            newTocEntry.setDocument(document.clone());
        }
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

    public void setCompleteHref(String fullReference)
    {
        String[] splitted = StringUtils.split(fullReference);
        setReference(splitted[0]);
        if (splitted.length > 1)
        {
            setFragmentId(splitted[1]);
        }
    }
}