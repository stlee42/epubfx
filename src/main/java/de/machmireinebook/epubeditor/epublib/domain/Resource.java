package de.machmireinebook.epubeditor.epublib.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.Path;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.ToStringConvertible;
import de.machmireinebook.epubeditor.epublib.filesystem.EpubFileSystem;
import de.machmireinebook.epubeditor.epublib.util.commons.io.XmlStreamReader;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Represents a resource that is part of the epub.
 * A resource can be a html file, image, xml, etc.
 * 
 * @author paul
 *
 */
public class Resource<T> implements Serializable, ToStringConvertible
{
    private static final Logger logger = Logger.getLogger(Resource.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1043946707835004037L;
	private String id;
	private String title;
	private StringProperty href = new SimpleStringProperty();
	protected String originalHref;
    private String properties;
	private ObjectProperty<MediaType> mediaType = new SimpleObjectProperty<>();
	private String inputEncoding = Constants.CHARACTER_ENCODING;
	protected byte[] data;

    public Resource()
    {
    }

    /**
	 * Creates an empty Resource with the given href.
	 * 
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 */
    public Resource(String href) {
		this(null, new byte[0], href, MediaType.getByFileName(href));
	}
	
	/**
	 * Creates a Resource with the given data and MediaType.
	 * The href will be automatically generated.
	 * 
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * @param data The Resource's contents
	 * @param mediaType The MediaType of the Resource
	 */
	public Resource(byte[] data, MediaType mediaType) {
		this(null, data, null, mediaType);
	}
	
	/**
	 * Creates a resource with the given data at the specified href.
	 * The MediaType will be determined based on the href extension.
	 * 
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 */
	public Resource(byte[] data, String href) {
		this(null, data, href, MediaType.getByFileName(href), Constants.CHARACTER_ENCODING);
	}
	
	/**
	 * Creates a resource with the data from the given Reader at the specified href.
	 * The MediaType will be determined based on the href extension.
	 *
	 * @param in The Resource's contents
	 * @param href The location of the resource within the epub. Example: "cover.jpg".
	 */
	public Resource(Reader in, String href) throws IOException {
		this(null, IOUtils.toByteArray(in, Constants.CHARACTER_ENCODING), href, MediaType.getByFileName(href), Constants.CHARACTER_ENCODING);
	}
	
	/**
	 * Creates a resource with the data from the given InputStream at the specified href.
	 * The MediaType will be determined based on the href extension.
	 *
	 * @see nl.siegmann.epublib.service.MediatypeService#determineMediaType(String)
	 * 
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * It is recommended to us the {@link #Resource(java.io.Reader, String)} method for creating textual
	 * (html/css/etc) resources to prevent encoding problems.
	 * Use this method only for binary Resources like images, fonts, etc.
	 * 
	 * 
	 * @param in The Resource's contents
	 * @param href The location of the resource within the epub. Example: "cover.jpg".
	 */
	public Resource(InputStream in, String href) throws IOException {
		this(null, IOUtils.toByteArray(in), href, MediaType.getByFileName(href));
	}
	
	/**
	 * Creates a resource with the given id, data, mediatype at the specified href.
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 * 
	 * @param id The id of the Resource. Internal use only. Will be auto-generated if it has a null-value.
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 * @param mediaType The resources MediaType
	 */
	public Resource(String id, byte[] data, String href, MediaType mediaType) {
		this(id, data, href, mediaType, Constants.CHARACTER_ENCODING);
	}

    public Resource(InputStream in, String href, MediaType mediaType) throws IOException
    {
        this(null, IOUtils.toByteArray(in), href, mediaType, Constants.CHARACTER_ENCODING);
    }

    public Resource(byte[] data, String href, MediaType mediaType)
    {
        this(null, data, href, mediaType, Constants.CHARACTER_ENCODING);
    }
	/**
	 * Creates a resource with the given id, data, mediatype at the specified href.
	 * If the data is of a text type (html/css/etc) then it will use the given inputEncoding.
	 * 
	 * @param id The id of the Resource. Internal use only. Will be auto-generated if it has a null-value.
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 * @param mediaType The resources MediaType
	 * @param inputEncoding If the data is of a text type (html/css/etc) then it will use the given inputEncoding.
	 */
	public Resource(String id, byte[] data, String href, MediaType mediaType, String inputEncoding) {
		this.id = id;
		this.href.set(href);
		this.originalHref = href;
		this.mediaType.setValue(mediaType);
		this.inputEncoding = inputEncoding;
		this.data = data;
	}

    /**
	 * Gets the contents of the Resource as an InputStream.
	 * 
	 * @return The contents of the Resource.
	 */
	public InputStream getInputStream()
	{
		return new ByteArrayInputStream(getData());
	}
	
	/**
	 * The contents of the resource as a byte[]
	 * 
	 * @return The contents of the resource
	 */
	public byte[] getData()
    {
		return data;
	}

    public T asNativeFormat()
    {
        return null;
    }

	/**
	 * Tells this resource to release its cached data.
	 * 
	 * If this resource was not lazy-loaded, this is a no-op.
	 */
	public void close() {
	}

	/**
	 * Sets the data of the Resource.
	 * If the data is a of a different type then the original data then make sure to change the MediaType.
	 * 
	 * @param data
	 */
	public void setData(byte[] data)
	{
		this.data = data;
	}

	/**
	 * Returns the size of this resource in bytes.
	 * 
	 * @return the size.
	 */
	public long getSize() {
		return data.length;
	}
	
	/**
	 * If the title is found by scanning the underlying html document then it is cached here.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the Resource's id: Make sure it is unique and a valid identifier.
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * The resources Id.
	 * 
	 * Must be both unique within all the resources of this book and a valid identifier.
	 * @return The resources Id.
	 */
	public String getId() {
		return id;
	}

    /**
	 * The location of the resource within the contents folder of the epub file.
	 * 
	 * Example:<br/>
	 * images/cover.jpg<br/>
	 * content/chapter1.xhtml<br/>
	 * 
	 * @return The location of the resource within the contents folder of the epub file.
	 */
    public String getHref()
    {
        return href.get();
    }

    public StringProperty hrefProperty()
    {
        return href;
    }

    public Path getHrefAsPath()
    {
        int index = StringUtils.lastIndexOf(getHref(), "/");
        String path = "";
        if (index > -1)
        {
            path = getHref().substring(0, index);
        }
        return EpubFileSystem.INSTANCE.getPath("/" + path);
    }

    public String getFileName()
    {
        int index = StringUtils.lastIndexOf(getHref(), "/");
        String fileName = getHref();
        if (index > -1)
        {
            fileName = getHref().substring(index + 1);
        }
        return fileName;
    }

    /**
	 * Sets the Resource's href.
	 * 
	 * @param href
	 */
    public void setHref(String href)
    {
        this.href.set(href);
    }


    public String relativize(Resource other)
    {
        String result = "";
        if (other != null)
        {
            Path resultPath = getHrefAsPath().relativize(other.getHrefAsPath());
            result = resultPath.toString() + "/" + other.getFileName();
        }
        return result;
    }

	public String getOriginalHref()
	{
		return originalHref;
	}

	public void setOriginalHref(String originalHref)
	{
		this.originalHref = originalHref;
	}

	/**
	 * The character encoding of the resource.
	 * Is allowed to be null for non-text resources like images.
	 * 
	 * @return The character encoding of the resource.
	 */
	public String getInputEncoding() {
		return inputEncoding;
	}
	
	/**
	 * Sets the Resource's input character encoding.
	 * 
	 * @param encoding
	 */
	public void setInputEncoding(String encoding) {
		this.inputEncoding = encoding;
	}
	
	/**
	 * Gets the contents of the Resource as Reader.
	 * 
	 * Does all sorts of smart things (courtesy of apache commons io XMLStreamREader) to handle encodings, byte order markers, etc.
	 * 
	 * @return the contents of the Resource as Reader.
	 * @throws java.io.IOException
	 */
	public Reader asReader() throws IOException {
		return new XmlStreamReader(new ByteArrayInputStream(getData()), getInputEncoding());
	}
	
	/**
	 * Gets the hashCode of the Resource's href.
	 * 
	 */
	public int hashCode() {
		return getHref().hashCode();
	}
	
	/**
	 * Checks to see of the given resourceObject is a resource and whether its href is equal to this one.
	 * 
	 * @return whether the given resourceObject is a resource and whether its href is equal to this one.
	 */
	public boolean equals(Object resourceObject) {
		if (! (resourceObject instanceof Resource)) {
			return false;
		}
		return getHref().equals(((Resource) resourceObject).getHref());
	}

	/**
	 * This resource's mediaType.
	 *
	 * @return This resource's mediaType.
	 */
	public MediaType getMediaType()
	{
		return mediaType.get();
	}

	public ObjectProperty<MediaType> mediaTypeProperty()
	{
		return mediaType;
	}

	public void setMediaType(MediaType mediaType)
	{
		this.mediaType.set(mediaType);
	}

	public void setTitle(String title) {
		this.title = title;
	}

    public String getProperties()
    {
        return properties;
    }

    public void setProperties(String properties)
    {
        this.properties = properties;
    }

    public String toString()
    {
        String result;
        if (StringUtils.isNotEmpty(title))
        {
            result = title;
        }
        else
        {
            result = getFileName();
        }
		return result;
	}


    @Override
    public String convertToString()
    {
        return getFileName();
    }

    @Override
    public void convertFromString(String string)
    {
        int index = StringUtils.lastIndexOf(getHref(), "/");
        String fileName = getHref();
        if (index > -1)
        {
            fileName = getHref().substring(index + 1);
        }
        setHref(getHref().replace(fileName, string));
    }
}
