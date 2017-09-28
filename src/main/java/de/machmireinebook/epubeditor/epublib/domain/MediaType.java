package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
public enum MediaType implements Serializable
{

    XHTML("application/xhtml+xml", ".xhtml", new String[]{".htm", ".html", ".xhtml"}, "text", XHTMLResourceFactory.getInstance()),
    XML("application/xml", ".xml", XMLResourceFactory.getInstance()),
    OPF ("application/oebps-package+xml", ".opf", XMLResourceFactory.getInstance()),
    EPUB ("application/epub+zip", ".epub", XMLResourceFactory.getInstance()),
    NCX ("application/x-dtbncx+xml", ".ncx", XMLResourceFactory.getInstance()),

    JAVASCRIPT ("text/javascript", ".js", "script", JavascriptResourceFactory.getInstance()),
    CSS("text/css", ".css", "style", CSSResourceFactory.getInstance()),

    MIMETYPE ("text/plain", "mimetype", "", DefaultResourceFactory.getInstance()),

    // images
    JPG ("image/jpeg", ".jpg", new String[]{".jpg", ".jpeg"}, "image", ImageResourceFactory.getInstance()),
    PNG ("image/png", ".png", ImageResourceFactory.getInstance()),
    GIF ("image/gif", ".gif", ImageResourceFactory.getInstance()),

    SVG ("image/svg+xml", ".svg", ImageResourceFactory.getInstance()),

    // fonts
    TTF ("application/x-truetype-font", ".ttf", DefaultResourceFactory.getInstance()),
    OPENTYPE ("application/vnd.ms-opentype", ".otf", DefaultResourceFactory.getInstance()),
    WOFF ("application/font-woff", ".woff", DefaultResourceFactory.getInstance()),

    // audio
    MP3 ("audio/mpeg", ".mp3", DefaultResourceFactory.getInstance()),
    MP4 ("audio/mp4", ".mp4", DefaultResourceFactory.getInstance()),
    OGG ("audio/ogg", ".ogg", DefaultResourceFactory.getInstance()),

    SMIL ("application/smil+xml", ".smil", DefaultResourceFactory.getInstance()),
    XPGT ("application/adobe-page-template+xml", ".xpgt", DefaultResourceFactory.getInstance()),
    PLS ("application/pls+xml", ".pls", DefaultResourceFactory.getInstance()),

    UNKNWON_MEDIATYPE("", "", DefaultResourceFactory.getInstance());

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
        for (MediaType mediatype : values())
        {
            for (String extension : mediatype.getExtensions())
            {
                if (StringUtils.endsWithIgnoreCase(filename, extension))
                {
                    return mediatype;
                }
            }
        }
        return UNKNWON_MEDIATYPE;
    }

    public static MediaType getByName(String mediaTypeName)
    {
        for (MediaType mediatype : values())
        {
            if (mediatype.name.equalsIgnoreCase(mediaTypeName))
            {
                return mediatype;
            }
        }
        return UNKNWON_MEDIATYPE;

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

    private MediaType(String name, String defaultExtension, ResourceFactory resourceFactory)
    {
        this(name, defaultExtension, new String[]{defaultExtension}, resourceFactory);
    }

    private MediaType(String name, String defaultExtension,
                     String[] extensions, ResourceFactory resourceFactory)
    {
        this(name, defaultExtension, Arrays.asList(extensions), resourceFactory);
    }

    private MediaType(String name, String defaultExtension,
                     Collection<String> extensions, ResourceFactory resourceFactory)
    {
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = extensions;
        this.resourceFactory = resourceFactory;
    }

    private MediaType(String name, String defaultExtension,
                     String[] extensions, String fileNamePrefix, ResourceFactory resourceFactory)
    {
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = Arrays.asList(extensions);
        this.fileNamePrefix = fileNamePrefix;
        this.resourceFactory = resourceFactory;
    }

    private MediaType(String name, String defaultExtension, String fileNamePrefix, ResourceFactory resourceFactory)
    {
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = Collections.singletonList(defaultExtension);
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
