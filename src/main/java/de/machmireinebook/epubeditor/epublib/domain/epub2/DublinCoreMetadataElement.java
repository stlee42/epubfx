package de.machmireinebook.epubeditor.epublib.domain.epub2;

/**
 * User: mjungierek
 * Date: 28.09.2017
 * Time: 20:43
 */
public class DublinCoreMetadataElement
{
    private String id;
    private String scheme;
    private String value;

    public DublinCoreMetadataElement(String value)
    {
        this(null, null, value);
    }

    public DublinCoreMetadataElement(String id, String scheme, String value)
    {
        this.id = id;
        this.scheme = scheme;
        this.value = value;
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
}