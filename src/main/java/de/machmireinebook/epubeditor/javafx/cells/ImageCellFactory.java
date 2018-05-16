package de.machmireinebook.epubeditor.javafx.cells;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.util.Callback;

/**
 * User: mjungierek
 * Date: 19.11.13
 * Time: 21:22
 */
public class ImageCellFactory<S> implements Callback<TableColumn<S, Image>, TableCell<S, Image>>
{
    private Double width;
    private Double height;

    public ImageCellFactory(Double width, Double height)
    {
        this.width = width;
        this.height = height;
    }

    @Override
    public TableCell<S, Image> call(TableColumn tableColumn)
    {
        return new ImageCell<>(width, height);
    }
}
