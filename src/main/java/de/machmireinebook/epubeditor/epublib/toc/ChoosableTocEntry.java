package de.machmireinebook.epubeditor.epublib.toc;

import javafx.util.StringConverter;

import org.apache.commons.lang3.StringUtils;

import org.jdom2.Document;

import de.machmireinebook.epubeditor.epublib.domain.TocEntry;

/**
 * Created by Michail Jungierek, Acando GmbH on 23.05.2018
 */
public class ChoosableTocEntry extends TocEntry<ChoosableTocEntry> implements Cloneable
{
    private boolean choosed;
    private Document document;
    private int displayLevel;

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

    public String getDisplayTitle()
    {
        return StringUtils.leftPad(getTitle(), getTitle().length() + displayLevel * 4, " ");
    }

    @Override
    public ChoosableTocEntry clone()
    {
        ChoosableTocEntry newTocEntry = (ChoosableTocEntry) super.clone();
        newTocEntry.setDocument(document.clone());
        return newTocEntry;
    }

    public int getDisplayLevel()
    {
        return displayLevel;
    }

    public void setDisplayLevel(int displayLevel)
    {
        this.displayLevel = displayLevel;
    }
}