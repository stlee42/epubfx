package de.machmireinebook.epubeditor.javafx.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * User: mjungierek
 * Date: 21.08.2014
 * Time: 22:38
 */
public class WrappableTextCellFactory<S> implements Callback<TableColumn<S, String>, TableCell<S, String>>
{
    @Override
    public TableCell<S, String> call(TableColumn<S, String> param)
    {
        return new WrappableTextCell<S> ();
    }
}
