package de.machmireinebook.epubeditor.epublib.domain.epub3;

import java.util.ArrayList;
import java.util.List;

/**
 * User: mjungierek
 * Date: 28.09.2017
 * Time: 20:43
 */
public class Epub3DublinCoreMetadataElement
{
    private String id;
    private String scheme;
    private String value;
    private String language;
    private List<MetadataPropertyValue> refinements = new ArrayList<>();

    public Epub3DublinCoreMetadataElement(String value)
    {
        this(null, null, value, null);
    }

    public Epub3DublinCoreMetadataElement(String id, String scheme, String value, String language)
    {
        this.id = id;
        this.scheme = scheme;
        this.value = value;
        this.language = language;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getScheme()
    {
        return scheme;
    }

    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public List<MetadataPropertyValue> getRefinements()
    {
        return refinements;
    }
}
