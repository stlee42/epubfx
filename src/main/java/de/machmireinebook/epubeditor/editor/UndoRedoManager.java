package de.machmireinebook.epubeditor.editor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * User: mjungierek
 * Date: 20.12.2014
 * Time: 21:07
 */
public class UndoRedoManager<T> extends ArrayDeque<T>
{
    private Deque<T> redoList = new ArrayDeque<>();

    public T undo()
    {
        if (isEmpty())
        {
            return  null;
        }
        //wenn letzte Version erreicht ist, dann geben wir immer diese zurück
        else if (size() > 1)
        {
            redoList.addFirst(removeFirst());
        }
        return getFirst();
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
            addFirst(redoList.removeFirst());
        }
        return getFirst();
    }

    public void saveVersion(T version)
    {
        boolean add = false;
        if (isEmpty())
        {
            add = true;
        }
        else
        {
            T lastVersion = getFirst();
            //Version nur hinzufügen wenn sich diese unterscheiden
            if (!version.equals(lastVersion))
            {
                add = true;
            }
        }
        if (add)
        {
            if (!redoList.isEmpty()) //offenbar wird an einer undo version weitergearbeitet, dann alles was im redo noch ist verwerfen
            {
                redoList.clear();
            }
            addFirst(version);
        }
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
