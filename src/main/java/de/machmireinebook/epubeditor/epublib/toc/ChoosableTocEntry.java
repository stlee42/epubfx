package de.machmireinebook.epubeditor.epublib.toc;

import org.jdom2.Document;

import de.machmireinebook.epubeditor.epublib.domain.TocEntry;

/**
 * Created by Michail Jungierek, Acando GmbH on 23.05.2018
 */
public class ChoosableTocEntry extends TocEntry<ChoosableTocEntry>
{
    private boolean choosed;
    private Document document;

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
}
