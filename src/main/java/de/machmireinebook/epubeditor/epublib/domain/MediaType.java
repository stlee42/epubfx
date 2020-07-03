package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import de.machmireinebook.epubeditor.epublib.resource.CSSResourceFactory;
import de.machmireinebook.epubeditor.epublib.resource.FontResourceFactory;
import de.machmireinebook.epubeditor.epublib.resource.ImageResourceFactory;
import de.machmireinebook.epubeditor.epublib.resource.JavascriptResourceFactory;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.ResourceFactory;
import de.machmireinebook.epubeditor.epublib.resource.XHTMLResourceFactory;
import de.machmireinebook.epubeditor.epublib.resource.XMLResourceFactory;

/**
 * MediaType is used to tell the type of content a resource is.
 */
public enum MediaType implements Serializable
{

    XHTML("application/xhtml+xml", ".xhtml", new String[]{".htm", ".html", ".xhtml"}, "text", XHTMLResourceFactory.getInstance()),
    XML("application/xml", ".xml", XMLResourceFactory.getInstance()),
    OPF ("application/oebps-package+xml", ".opf", XMLResourceFactory.getInstance()),
    EPUB ("application/epub+zip", ".epub", XMLResourceFactory.getInstance()),
    NCX ("application/x-dtbncx+xml", ".ncx", XMLResourceFactory.getInstance()),

    JAVASCRIPT ("text/javascript", ".js", "script", JavascriptResourceFactory.getInstance()),
    JAVASCRIPT_SINCE_3_1 ("text/javascript", ".js", "script", JavascriptResourceFactory.getInstance(), 3.1F),
    CSS("text/css", ".css", "style", CSSResourceFactory.getInstance()),

    MIMETYPE ("text/plain", "mimetype", "", DefaultResourceFactory.getInstance()),

    // images
    JPG ("image/jpeg", ".jpg", new String[]{".jpg", ".jpeg"}, "image", ImageResourceFactory.getInstance()),
    PNG ("image/png", ".png", ImageResourceFactory.getInstance()),
    GIF ("image/gif", ".gif", ImageResourceFactory.getInstance()),

    SVG ("image/svg+xml", ".svg", ImageResourceFactory.getInstance()),

    // fonts
    TTF ("application/x-font-ttf", ".ttf", FontResourceFactory.getInstance()),
    TTF_1 ("application/x-font-truetype", ".ttf", FontResourceFactory.getInstance()),
    TTF_2 ("application/x-truetype-font", ".ttf", FontResourceFactory.getInstance()),
    TTF_SINCE_3_2 ("font/ttf", ".ttf", FontResourceFactory.getInstance(), 3.2F),
    TTF_2_SINCE_3_2 ("application/font-sfnt", ".ttf", FontResourceFactory.getInstance(), 3.2F),
    TTF_RFC_8081 ("font/ttf", ".ttf", FontResourceFactory.getInstance()),
    OPENTYPE_UNTIL_3 ("application/vnd.ms-opentype", ".otf", FontResourceFactory.getInstance()),
    OPENTYPE_SINCE_3_1 ("application/font-sfnt", ".otf", FontResourceFactory.getInstance(), 3.1F),
    OPENTYPE_SIGIL ("font/otf", ".otf", FontResourceFactory.getInstance()),
    OPENTYPE_RFC_8081("font/sfnt", ".otf", FontResourceFactory.getInstance(), 3.1F),
    WOFF ("application/font-woff", ".woff", FontResourceFactory.getInstance()),
    WOFF_RFC_8081 ("font/woff", ".woff", FontResourceFactory.getInstance()),
    WOFF2 ("font/woff2", ".woff2", FontResourceFactory.getInstance(), 3.1F),

    // audio
    MP3 ("audio/mpeg", ".mp3", DefaultResourceFactory.getInstance()),
    MP4 ("audio/mp4", ".mp4", DefaultResourceFactory.getInstance()),
    OGG ("audio/ogg", ".ogg", DefaultResourceFactory.getInstance()),

    SMIL ("application/smil+xml", ".smil", DefaultResourceFactory.getInstance()),
    XPGT ("application/adobe-page-template+xml", ".xpgt", DefaultResourceFactory.getInstance()),
    PLS ("application/pls+xml", ".pls", DefaultResourceFactory.getInstance()),

    UNKNWON_MEDIATYPE("", "", DefaultResourceFactory.getInstance());


    public boolean isXML()
    {
        return this.equals(XHTML) || this.equals(NCX) || this.equals(SVG) || this.equals(OPF);
    }

    public boolean isFont()
    {
        return isTTFFont() || isOpenTypeFont() ||  isWoffFont();
    }

    public boolean isTTFFont() {
        return this.equals(TTF) || this.equals(TTF_1) ||  this.equals(TTF_2) || this.equals(TTF_RFC_8081) || this.equals(TTF_SINCE_3_2) || this.equals(TTF_2_SINCE_3_2);
    }

    public boolean isOpenTypeFont() {
        return this.equals(OPENTYPE_UNTIL_3) || this.equals(OPENTYPE_SINCE_3_1) || this.equals(OPENTYPE_RFC_8081) || this.equals(OPENTYPE_SIGIL);
    }

    public boolean isWoffFont()
    {
        return this.equals(WOFF) || this.equals(WOFF_RFC_8081) || this.equals(WOFF2);
    }

    public boolean isImage()
    {
        return this.equals(JPG) || this.equals(PNG) || this.equals(GIF) || this.equals(SVG);
    }

    public boolean isBitmapImage()
    {
        return this.equals(JPG) || this.equals(PNG) || this.equals(GIF);
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
    private ResourceFactory<? extends Resource<?>, ?> resourceFactory;
    private Float sinceVersion;

    MediaType(String name, String defaultExtension, ResourceFactory<? extends Resource<?>, ?> resourceFactory)
    {
        this(name, defaultExtension, new String[]{defaultExtension}, resourceFactory);
    }

    MediaType(String name, String defaultExtension, ResourceFactory<? extends Resource<?>, ?> resourceFactory, float sinceVersion)
    {
        this(name, defaultExtension, resourceFactory);
        this.sinceVersion = sinceVersion;
    }

    MediaType(String name, String defaultExtension,
                     String[] extensions, ResourceFactory<? extends Resource<?>, ?> resourceFactory)
    {
        this(name, defaultExtension, Arrays.asList(extensions), resourceFactory);
    }

    MediaType(String name, String defaultExtension,
                     Collection<String> extensions, ResourceFactory<? extends Resource<?>, ?> resourceFactory)
    {
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = extensions;
        this.resourceFactory = resourceFactory;
    }

    MediaType(String name, String defaultExtension,
                     String[] extensions, String fileNamePrefix, ResourceFactory<? extends Resource<?>, ?> resourceFactory)
    {
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = Arrays.asList(extensions);
        this.fileNamePrefix = fileNamePrefix;
        this.resourceFactory = resourceFactory;
    }

    MediaType(String name, String defaultExtension, String fileNamePrefix, ResourceFactory<? extends Resource<?>, ?> resourceFactory)
    {
        this.name = name;
        this.defaultExtension = defaultExtension;
        this.extensions = Collections.singletonList(defaultExtension);
        this.fileNamePrefix = fileNamePrefix;
        this.resourceFactory = resourceFactory;
    }

    MediaType(String name, String defaultExtension, String fileNamePrefix, ResourceFactory<? extends Resource<?>, ?> resourceFactory, float sinceVersion)
    {
        this(name, defaultExtension, fileNamePrefix, resourceFactory);
        this.sinceVersion = sinceVersion;
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

    public ResourceFactory<? extends Resource<?>, ?> getResourceFactory()
    {
        return resourceFactory;
    }

    public Float getSinceVersion()
    {
        return sinceVersion;
    }
}
