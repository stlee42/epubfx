package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * @author Michail Jungierek, CGI
 */
public enum ManifestItemAttribute {
    id, href,
    media_type("media-type"),
    fallback,
    media_overlay("media-overlay"),
    properties;

    private String name;
    ManifestItemAttribute()
    {
        this.name = this.name();
    }

    ManifestItemAttribute(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
