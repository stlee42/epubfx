package de.machmireinebook.epubeditor.javafx.cells;

import javafx.scene.control.TableCell;
import javafx.scene.text.Text;

/**
 * User: mjungierek
 * Date: 21.08.2014
 * Time: 22:38
 */
public class WrappableTextCell<T>  extends TableCell<T, String>
{
    @Override
    public void updateItem(String item, boolean empty)
    {
        super.updateItem(item, empty);
        if (!isEmpty())
        {
            Text text = new Text(item);
            text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
            setGraphic(text);
        }
        else
        {
            setText(null);
            setGraphic(null);
        }
    }
}
