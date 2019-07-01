package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * User: mjungierek
 * Date: 22.08.2014
 * Time: 19:10
 */
public enum RenditionSpread
{
    LANDSCAPE("landscape"),
    PORTRAIT("portrait"),
    AUTO("auto");

    public static final String qName = "rendition:orientation";
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
