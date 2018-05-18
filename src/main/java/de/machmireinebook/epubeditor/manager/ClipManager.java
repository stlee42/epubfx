package de.machmireinebook.epubeditor.manager;

import javax.inject.Singleton;

import de.machmireinebook.epubeditor.clips.Clip;
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
