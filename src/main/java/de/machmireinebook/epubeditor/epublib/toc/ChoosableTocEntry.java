package de.machmireinebook.epubeditor.epublib.toc;

import de.machmireinebook.epubeditor.epublib.domain.TocEntry;

/**
 * Created by Michail Jungierek, Acando GmbH on 23.05.2018
 */
public class ChoosableTocEntry extends TocEntry<ChoosableTocEntry>
{
    private boolean choosed;

    public boolean getChoosed()
    {
        return choosed;
    }

    public void setChoosed(boolean choosed)
    {
        this.choosed = choosed;
    }
}
