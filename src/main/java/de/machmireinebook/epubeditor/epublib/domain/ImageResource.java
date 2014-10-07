package de.machmireinebook.epubeditor.epublib.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javafx.beans.property.ObjectProperty;
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
    private ObjectProperty<Image> coverProperty;

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
        calculateWidthAndHeight();
    }

    public ImageResource(byte[] data, String href)
    {
        super(data, href);
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
        calculateWidthAndHeight();
    }

    private void calculateWidthAndHeight()
    {
        try
        {
            image = new Image(getInputStream());
            width = image.getWidth();
            height = image.getHeight();
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
    }

    @Override
    public Image getAsNativeFormat()
    {
        if (image == null)
        {
            try
            {
                image = new Image(getInputStream());
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
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

    public ObjectProperty<Image> coverProperty()
    {
        if (coverProperty == null)
        {
            coverProperty = new SimpleObjectProperty<>(image);
        }
        return coverProperty;
    }
}
