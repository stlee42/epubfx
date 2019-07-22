package de.machmireinebook.epubeditor.clips;

import java.util.List;

import javax.inject.Singleton;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;

import org.jdom2.CDATA;
import org.jdom2.Element;

/**
 * User: mjungierek
 * Date: 03.01.2015
 * Time: 00:24
 */
@Singleton
public class ClipManager
{
    private static final String SPACE_ESCAPE_STRING = "__SPACE__";

    // clipsRootProperty
    private final ObjectProperty<TreeItem<Clip>> clipsRootProperty = new SimpleObjectProperty<>(this, "clipsRoot");
    private ListChangeListener<TreeItem<Clip>> treeChangedListener;

    public ClipManager()
    {
        clipsRootProperty.set(new TreeItem<>());
    }

    public final ObjectProperty<TreeItem<Clip>> clipsRootProperty() {
        return clipsRootProperty;
    }

    public void setClipsRoot(TreeItem<Clip> clipsRoot)
    {
        clipsRootProperty.set(clipsRoot);
    }

    public TreeItem<Clip> getClipsRoot()
    {
        return clipsRootProperty.get();
    }

    public void setOnClipsTreeChanged(ListChangeListener<TreeItem<Clip>> treeChangedListener) {
        this.treeChangedListener = treeChangedListener;
    }

    public void saveClips(Element parentElement) {
        saveClips(parentElement, getClipsRoot());
    }

    public TreeItem<Clip> createClipTreeItem(String name, String content) {
        Clip clip = new Clip(name, content);
        TreeItem<Clip> treeItem = new TreeItem<>(clip);
        treeItem.getChildren().addListener(treeChangedListener);
        return treeItem;
    }

    public TreeItem<Clip> createClipTreeItem(String name, boolean isGroup) {
        Clip clip = new Clip(name, isGroup);
        TreeItem<Clip> treeItem = new TreeItem<>(clip);
        treeItem.getChildren().addListener(treeChangedListener);
        return treeItem;
    }


    private void saveClips(Element parentElement, TreeItem<Clip> currentTreeItem) {
        List<TreeItem<Clip>> treeItems = currentTreeItem.getChildren();
        for (TreeItem<Clip> treeItem : treeItems)
        {
            if (treeItem.getValue().isGroup())
            {
                Element groupElement = new Element("group");
                groupElement.setAttribute("name", treeItem.getValue().getName());
                saveClips(groupElement, treeItem);
                parentElement.addContent(groupElement);
            }
            else
            {
                Element clipElement = new Element("clip");
                parentElement.addContent(clipElement);

                Element nameElement =  new Element("name");
                clipElement.addContent(nameElement);
                nameElement.setText(treeItem.getValue().getName());

                Element contentElement =  new Element("content");
                clipElement.addContent(contentElement);
                String value = treeItem.getValue().getContent();
                //replace any space with a unique string, because somewhere in the process of generating xml, strings
                //will be trimed (cdata doesn't prevent this) so that important spaces at begin and/or the end of the clips
                // are eliminated
                value = value.replaceAll(" ", SPACE_ESCAPE_STRING);
                contentElement.setContent(new CDATA(value));
            }
        }
    }

    public void readClips(List<Element> children) {
        readClips(children, getClipsRoot());
    }

    public void readClips(List<Element> children, TreeItem<Clip> currentTreeItem)
    {
        for (Element child : children)
        {
            if (child.getName().equals("clip"))
            {
                String name = child.getChildText("name");
                String content = child.getChildText("content");
                content = content.replaceAll(SPACE_ESCAPE_STRING, " ");
                Clip clip = new Clip(name, content);
                TreeItem<Clip> treeItem = new TreeItem<>(clip);
                treeItem.getChildren().addListener(treeChangedListener);
                currentTreeItem.getChildren().add(treeItem);
            }
            else if (child.getName().equals("group"))
            {
                String name = child.getAttributeValue("name");
                Clip clip = new Clip(name, true);
                TreeItem<Clip> treeItem = new TreeItem<>(clip);
                treeItem.getChildren().addListener(treeChangedListener);
                currentTreeItem.getChildren().add(treeItem);
                List<Element> subChildren = child.getChildren();
                readClips(subChildren, treeItem);
            }
        }
    }



}
