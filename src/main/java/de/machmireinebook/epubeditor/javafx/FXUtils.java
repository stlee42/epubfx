package de.machmireinebook.epubeditor.javafx;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * User: mjungierek
 * Date: 23.02.14
 * Time: 14:46
 */
public class FXUtils
{
    public static ImageView getIcon(String path, int size)
    {
        Image icon = new Image(FXUtils.class.getResourceAsStream(path));
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        return imageView;
    }

    public static Image getImage(String path)
    {
        return new Image(FXUtils.class.getResourceAsStream(path));
    }
}
