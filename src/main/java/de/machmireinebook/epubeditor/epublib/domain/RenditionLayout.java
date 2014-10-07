package de.machmireinebook.epubeditor.epublib.domain;

/**
 * User: mjungierek
 * Date: 22.08.2014
 * Time: 19:10
 */
public enum RenditionLayout
{
    REFLOWABLE("reflowable"),
    PRE_PAGINATED("pre-paginated");

    public static final String qName = "rendition:layout";
    private String value;

    RenditionLayout(String value)
    {
        this.value = value;
    }

    public static RenditionLayout getByValue(String value)
    {
        RenditionLayout result = null;
        for (RenditionLayout fixedLayoutOrientation : values())
        {
            if (fixedLayoutOrientation.value.equals(value))
            {
                result = fixedLayoutOrientation;
                break;
            }
        }
        return result;
    }

    public String getValue()
    {
        return value;
    }
}
