package de.machmireinebook.epubeditor.editor;

import java.util.ArrayList;
import java.util.List;

/**
 * User: mjungierek
 * Date: 20.12.2014
 * Time: 21:07
 */
public class UndoRedoManager<T> extends ArrayList<T>
{
    private List<T> redoList = new ArrayList<>();

    public T undo()
    {
        if (isEmpty())
        {
            return  null;
        }
        //wenn letzte Version erreicht ist, dann geben wir immer diese zurück
        else if (size() > 1)
        {
            redoList.add(0, remove(0));
        }
        return get(0);
    }

    public T redo()
    {
        if (isEmpty())
        {
            return  null;
        }
        //wenn nichts mehr im Redo dann geben wir einfach die aktuelle Version zurück
        else if (!redoList.isEmpty())
        {
            add(0, redoList.remove(0));
        }
        return get(0);
    }

    public void saveVersion(T version)
    {
        add(0, version);
    }

    public boolean hasRedoVersion()
    {
        return !redoList.isEmpty();
    }

    public boolean hasUndoVersion()
    {
        return !isEmpty();
    }
}
