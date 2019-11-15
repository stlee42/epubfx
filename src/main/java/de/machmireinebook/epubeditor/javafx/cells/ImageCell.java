package de.machmireinebook.epubeditor.javafx.cells;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 19.11.13
 * Time: 21:20
 */
public class ImageCell<S> extends TableCell<S, Image>
{
    private static final Logger logger = Logger.getLogger(ImageCell.class);

    private Double width;
    private Double height;

    public ImageCell(Double width, Double height)
    {
        this.width = width;
        this.height = height;
    }

    @Override
    public void updateItem(Image item, boolean empty)
    {
        getStyleClass().add("center");
        ImageView imageview = new ImageView();
        if (height != null)
        {
            imageview.setFitHeight(height);
        }
        else
        {
            imageview.setPreserveRatio(true);
        }
        if (width != null)
        {
            if (item != null && width > item.getWidth()) {
                imageview.setFitWidth(item.getWidth());
            } else {
                imageview.setFitWidth(width);
            }             
        }
        else
        {
            imageview.setPreserveRatio(true);
        }
        imageview.setImage(item);

        super.updateItem(item, false);
        setText(null);
        setGraphic(empty ? null : imageview);
    }
}