package de.machmireinebook.epubeditor.manager;

import javax.enterprise.inject.Produces;

import de.machmireinebook.epubeditor.cdi.ClipManagerProducer;
import de.machmireinebook.epubeditor.domain.Clip;

import javafx.scene.control.TreeItem;

/**
 * User: mjungierek
 * Date: 03.01.2015
 * Time: 00:24
 */
public class ClipManager
{
    private TreeItem<Clip> clipsRoot = new TreeItem<>();
    private static final ClipManager instance = new ClipManager();

    private ClipManager()
    {
    }

    @Produces @ClipManagerProducer
    public static ClipManager getInstance()
    {
        return instance;
    }

    public void setClipsRoot(TreeItem<Clip> clipsRoot)
    {
        this.clipsRoot = clipsRoot;
    }

    public TreeItem<Clip> getClipsRoot()
    {
        return clipsRoot;
    }
}
