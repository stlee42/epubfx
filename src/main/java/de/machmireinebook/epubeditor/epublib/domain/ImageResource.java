package de.machmireinebook.epubeditor.epublib.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import de.machmireinebook.epubeditor.util.ImageInfo;
import de.machmireinebook.epubeditor.util.NumberUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 20:45
 */
public class ImageResource extends Resource<Image>
{
    public static final Logger logger = Logger.getLogger(ImageResource.class);

    private double width;
    private double height;

    private Image image;
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private ImageInfo imageInfo;
    private BooleanProperty coverProperty;

    public ImageResource()
    {
        super();
    }

    public ImageResource(String href)
    {
        super(href);
    }

    public ImageResource(byte[] data, MediaType mediaType)
    {
        super(data, mediaType);
        calculateImageInfo();
    }

    public ImageResource(byte[] data, String href)
    {
        super(data, href);
        calculateImageInfo();
    }

    public ImageResource(Reader in, String href) throws IOException
    {
        super(in, href);
    }

    public ImageResource(InputStream in, String href) throws IOException
    {
        super(in, href);
    }

    public ImageResource(String id, byte[] data, String href, MediaType mediaType)
    {
        super(id, data, href, mediaType);
    }

    public ImageResource(String id, byte[] data, String href, MediaType mediaType, String inputEncoding)
    {
        super(id, data, href, mediaType, inputEncoding);
    }

    public ImageResource(byte[] data, String href, MediaType mediaType)
    {
        super(data, href, mediaType);
    }

    @Override
    public void setData(byte[] data)
    {
        super.setData(data);
        calculateImageInfo();
    }

    private void calculateImageInfo()
    {
        image = new Image(getInputStream());
        imageProperty.setValue(image);
        width = image.getWidth();
        height = image.getHeight();

        imageInfo = new ImageInfo();
        imageInfo.setInput(getInputStream()); // in can be InputStream or RandomAccessFile
        if (!imageInfo.check())
        {
            logger.error("Not a supported image file format.");
        }
    }

    @Override
    public Image asNativeFormat()
    {
        if (image == null)
        {
            image = new Image(getInputStream());
            imageProperty.setValue(image);
        }
        return image;
    }

    public double getWidth()
    {
        return width;
    }

    public void setWidth(double width)
    {
        this.width = width;
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    public ImageInfo getImageInfo()
    {
        return imageInfo;
    }

    public String getImageDescription()
    {
        String sizeInKB = NumberUtils.formatDouble(Math.round(getSize() / 1024.0 * 100) / 100.0);
        return ((Double) image.getWidth()).intValue() + "×" + ((Double) image.getHeight()).intValue() + " px | "
                + sizeInKB + " KB | " + imageInfo.getBitsPerPixel() + " bpp";
    }

    public ObjectProperty<Image> imageProperty()
    {
        return imageProperty;
    }

    public BooleanProperty coverProperty()
    {
        if (coverProperty == null)
        {
            coverProperty = new SimpleBooleanProperty();
        }
        return coverProperty;
    }
}
