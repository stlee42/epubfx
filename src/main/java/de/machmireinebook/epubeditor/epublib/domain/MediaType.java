package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * MediaType is used to tell the type of content a resource is.
 * <p>
 * Examples of mediatypes are image/gif, text/css and application/xhtml+xml
 * <p>
 * All allowed mediaTypes are maintained bye the MediaTypeService.
 *
 * @author paul
 * @see de.machmireinebook.epubeditor.epublib.service.MediatypeService
 */
public class MediaType implements Serializable
{

    public static final MediaType XHTML = new MediaType("application/xhtml+xml", ".xhtml", new String[]{".htm", ".html", ".xhtml"}, "text", XHTMLResourceFactory.getInstance());
    public static final MediaType XML = new MediaType("application/xml", ".xml", XMLResourceFactory.getInstance());
    public static final MediaType OPF = new MediaType("application/oebps-package+xml", ".opf", XMLResourceFactory.getInstance());
    public static final MediaType EPUB = new MediaType("application/epub+zip", ".epub", XMLResourceFactory.getInstance());
    public static final MediaType NCX = new MediaType("application/x-dtbncx+xml", ".ncx", XMLResourceFactory.getInstance());

    public static final MediaType JAVASCRIPT = new MediaType("text/javascript", ".js", "script", JavascriptResourceFactory.getInstance());
    public static final MediaType CSS = new MediaType("text/css", ".css", "style", CSSResourceFactory.getInstance());

    // images
    public static final MediaType JPG = new MediaType("image/jpeg", ".jpg", new String[]{".jpg", ".jpeg"}, "image", ImageResourceFactory.getInstance());
    public static final MediaType PNG = new MediaType("image/png", ".png", ImageResourceFactory.getInstance());
    public static final MediaType GIF = new MediaType("image/gif", ".gif", ImageResourceFactory.getInstance());

    public static final MediaType SVG = new MediaType("image/svg+xml", ".svg", ImageResourceFactory.getInstance());

    // fonts
    public static final MediaType TTF = new MediaType("application/x-truetype-font", ".ttf", DefaultResourceFactory.getInstance());
    public static final MediaType OPENTYPE = new MediaType("application/vnd.ms-opentype", ".otf", DefaultResourceFactory.getInstance());
    public static final MediaType WOFF = new MediaType("application/font-woff", ".woff", DefaultResourceFactory.getInstance());

    // audio
    public static final MediaType MP3 = new MediaType("audio/mpeg", ".mp3", DefaultResourceFactory.getInstance());
    public static final MediaType MP4 = new MediaType("audio/mp4", ".mp4", DefaultResourceFactory.getInstance());
    public static final MediaType OGG = new MediaType("audio/ogg", ".ogg", DefaultResourceFactory.getInstance());

    public static final MediaType SMIL = new MediaType("application/smil+xml", ".smil", DefaultResourceFactory.getInstance());
    public static final MediaType XPGT = new MediaType("application/adobe-page-template+xml", ".xpgt", DefaultResourceFactory.getInstance());
    public static final MediaType PLS = new MediaType("application/pls+xml", ".pls", DefaultResourceFactory.getInstance());

    public static MediaType[] mediatypes = new MediaType[]{
            XHTML, EPUB, JPG, PNG, GIF, CSS, SVG, TTF, NCX, XPGT, OPENTYPE, WOFF, SMIL, PLS, JAVASCRIPT, MP3, MP4, OGG, OPF, XML
    };

    public static Map<String, MediaType> mediaTypesByName = new HashMap<>();

    static
    {
        for (MediaType mediatype : mediatypes)
        {
            mediaTypesByName.put(mediatype.getName(), mediatype);
        }
    }

    public boolean isBitmapImage()
    {
        return this.equals(JPG) || this.equals(PNG) || this.equals(GIF);
    }

    public boolean isXML()
    {
        return this.equals(XHTML) || this.equals(NCX) || this.equals(SVG) || this.equals(OPF);
    }

    public boolean isFont()
    {
        return this.equals(TTF) || this.equals(OPENTYPE) || this.equals(WOFF);
    }

    /**
     * Gets the MediaType based on the file extension.
     * Null of no matching extension found.
     *
     * @param filename
     * @return the MediaType based on the file extension.
     */
    public static MediaType getByFileName(String filename)
    {
        for (MediaType mediatype : mediatypes)
        {
            for (String extension : mediatype.getExtensions())
            {
                if (StringUtils.endsWithIgnoreCase(filename, extension))
                {
                    return mediatype;
                }
            }
        }
        return null;
    }

    public static MediaType getByName(String mediaTypeName)
    {
        return mediaTypesByName.get(mediaTypeName);
    }

    /**
     *
     */
    private static final long serialVersionUID = -7256091153727506788L;
    private String name;
    private String defaultExtension;
    private Collection<String> extensions;
    private String fileNamePrefix;
    private ResourceFactory resourceFactory;

    public MediaType(String name, String defaultExtension, ResourceFactory resourceFactory)
    {
        this(name, defaultExtension, new String[]{defaultExtension}, resourceFactory);
    }

    public MediaType(String name, String defaultExtension,
                     String[] extensions, ResourceFactory resourceFactory)
    {
        this(name, defaultExtension, Arrays.asList(extensions), resourceFactory);
    }

    public int hashCode()
    {
        if (name == null)
        {
            return 0;
        }
        return name.hashCode();
    }

    public MediaType(String name, String defaultExtension,
                     Collection<String> extensions, ResourceFactory resourceFactory)
    {
        super();
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = extensions;
        this.resourceFactory = resourceFactory;
    }

    public MediaType(String name, String defaultExtension,
                     String[] extensions, String fileNamePrefix, ResourceFactory resourceFactory)
    {
        super();
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = Arrays.asList(extensions);
        this.fileNamePrefix = fileNamePrefix;
        this.resourceFactory = resourceFactory;
    }

    public MediaType(String name, String defaultExtension, String fileNamePrefix, ResourceFactory resourceFactory)
    {
        super();
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = Arrays.asList(defaultExtension);
        this.fileNamePrefix = fileNamePrefix;
        this.resourceFactory = resourceFactory;
    }

    public String getName()
    {
        return name;
    }


    public String getDefaultExtension()
    {
        return defaultExtension;
    }


    public Collection<String> getExtensions()
    {
        return extensions;
    }

    public boolean equals(Object otherMediaType)
    {
        if (!(otherMediaType instanceof MediaType))
        {
            return false;
        }
        return name.equals(((MediaType) otherMediaType).getName());
    }

    public String toString()
    {
        return name;
    }

    public String getFileNamePrefix()
    {
        return fileNamePrefix;
    }

    public ResourceFactory getResourceFactory()
    {
        return resourceFactory;
    }


}
