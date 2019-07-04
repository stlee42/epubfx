package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * User: mjungierek
 * Date: 22.08.2014
 * Time: 19:10
 */
public enum RenditionSpread
{
    none("none"),
    landscape("landscape"),
    portrait("portrait"), //deprecated
    both("both"),
    AUTO("auto");

    public static final String propertyName = "rendition:spread";
    private String value;

    RenditionSpread(String value)
    {
        this.value = value;
    }

    public static RenditionSpread getByValue(String value)
    {
        RenditionSpread result = null;
        for (RenditionSpread fixedLayoutOrientation : values())
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
