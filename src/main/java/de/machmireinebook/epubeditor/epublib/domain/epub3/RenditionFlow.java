package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * User: mjungierek
 * Date: 22.08.2014
 * Time: 19:10
 */
public enum RenditionFlow
{
    paginated("paginated"),
    scrolled_continuous("scrolled-continuous"),
    scrolled_doc("scrolled-doc"),
    auto("auto");

    public static final String propertyName = "rendition:flow";
    private String value;

    RenditionFlow(String value)
    {
        this.value = value;
    }

    public static RenditionFlow getByValue(String value)
    {
        RenditionFlow result = null;
        for (RenditionFlow flow : values())
        {
            if (flow.value.equals(value))
            {
                result = flow;
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
