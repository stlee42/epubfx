package de.machmireinebook.epubeditor.clips;

import javax.inject.Singleton;

import javafx.scene.control.TreeItem;

/**
 * User: mjungierek
 * Date: 03.01.2015
 * Time: 00:24
 */
@Singleton
public class ClipManager
{
    private TreeItem<Clip> clipsRoot = new TreeItem<>();

    public ClipManager()
    {
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
