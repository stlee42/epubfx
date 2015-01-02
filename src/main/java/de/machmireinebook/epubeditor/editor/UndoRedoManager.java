package de.machmireinebook.epubeditor.editor;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 20.12.2014
 * Time: 21:07
 */
public class UndoRedoManager<T> extends ArrayDeque<T>
{
    public static final Logger logger = Logger.getLogger(UndoRedoManager.class);
    private Deque<T> redoList = new ArrayDeque<>();

    private BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private BooleanProperty canRedo = new SimpleBooleanProperty(false);

    private T firstVersion;

    public T undo()
    {
        if (firstVersion == null && isEmpty())
        {
            canUndo.setValue(false);
            return  null;
        }
        else if (firstVersion != null && isEmpty())
        {
            canUndo.setValue(false);
            return firstVersion;
        }
        else if (size() == 1)
        {
            canUndo.setValue(false);
            T lastVersion = removeFirst();
            redoList.addFirst(lastVersion);
            canRedo.setValue(true);
            return firstVersion;
        }
        else
        {
            canUndo.setValue(true);
            T lastVersion = removeFirst();
            redoList.addFirst(lastVersion);
            canRedo.setValue(true);
            return getFirst();
        }
    }

    public T redo()
    {
        if (firstVersion == null && redoList.isEmpty() )
        {
            canRedo.setValue(false);
            return null;
        }
        if (firstVersion != null && redoList.isEmpty() )
        {
            canRedo.setValue(false);
            return firstVersion;
        }
        else if (redoList.size() == 1)
        {
            T lastRedoVersion = redoList.removeFirst();
            canRedo.setValue(false);
            addFirst(lastRedoVersion);
            canUndo.setValue(true);
            return lastRedoVersion;
        }
        else
        {
            T lastRedoVersion = redoList.removeFirst();
            canRedo.setValue(true);
            addFirst(lastRedoVersion);
            canUndo.setValue(true);
            return lastRedoVersion;
        }
    }

    public void saveVersion(T version)
    {
        boolean add = false;
        if (firstVersion == null && isEmpty())   //init mit erster version
        {
            firstVersion = version;
            logger.debug("no undo version, save first version");
        }
        else if (firstVersion != null && isEmpty())   //erste undo version
        {
            //Version nur hinzufügen wenn sich diese von der first unterscheidet
            if (!version.equals(firstVersion))
            {
                logger.debug("has no undo versions and version is different then first version, save new version");
                add = true;
                canUndo.setValue(true);
            }
        }
        else //weitere versionen einfach in liste eintragen, wenn unterschied
        {
            T lastVersion = getFirst();
            //Version nur hinzufügen wenn sich diese unterscheiden
            if (!version.equals(lastVersion))
            {
                logger.info("has undo versions and version is different then before, save new version");
                add = true;
            }
        }
        if (add)
        {
            if (!redoList.isEmpty()) //offenbar wird an einer undo version weitergearbeitet, dann alles was im redo noch ist verwerfen
            {
                redoList.clear();
                canRedo.setValue(false);
            }
            addFirst(version);
        }
    }

    public boolean getCanRedo()
    {
        return canRedo.get();
    }

    public BooleanProperty canRedoProperty()
    {
        return canRedo;
    }

    public boolean getCanUndo()
    {
        return canUndo.get();
    }

    public BooleanProperty canUndoProperty()
    {
        return canUndo;
    }
}
