package de.machmireinebook.epubeditor.epublib.domain.epub3;

import java.util.ArrayList;
import java.util.List;

/**
 * User: mjungierek
 * Date: 28.09.2017
 * Time: 20:43
 */
public class DublinCoreMetadataElement
{
    private String id;
    private String value;
    private String language;
    private OpfDirAttribute dir;
    private List<MetadataProperty> refinements = new ArrayList<>();

    public DublinCoreMetadataElement(String value)
    {
        this(null, value, null);
    }

    public DublinCoreMetadataElement(String id, String value)
    {
        this(id, value, null);
    }

    public DublinCoreMetadataElement(String id, String value, String language)
    {
        this.id = id;
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

    public OpfDirAttribute getDir() {
        return dir;
    }

    public void setDir(OpfDirAttribute dir) {
        this.dir = dir;
    }

    public List<MetadataProperty> getRefinements()
    {
        return refinements;
    }
}
