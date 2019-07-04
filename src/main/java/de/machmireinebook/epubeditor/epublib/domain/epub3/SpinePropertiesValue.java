package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * @author Michail Jungierek, CGI
 */
public enum SpinePropertiesValue
{
    rendition_flow_auto("rendition:flow-auto"),
    rendition_flow_paginated("rendition:flow-paginated"),
    rendition_flow_scrolled_continuous("rendition:flow-scrolled-continuous"),
    rendition_flow_scrolled_doc("rendition:flow-scrolled-doc"),
    rendition_spread_auto("rendition:spread-auto"),
    rendition_spread_both("rendition:spread-both"),
    rendition_spread_landscape("rendition:spread-landscape"),
    rendition_spread_none("rendition:spread-none"),
    rendition_spread_portrait("rendition:spread-portrait"),
    rendition_page_spread_center("rendition:page-spread-center"),
    rendition_page_spread_right("rendition:page-spread-right"),
    rendition_page_spread_left("rendition:page-spread-left")
    ;

    private String value;

    SpinePropertiesValue(String value)
    {
        this.value = value;
    }

    public static SpinePropertiesValue getByValue(String value)
    {
        SpinePropertiesValue result = null;
        for (SpinePropertiesValue propertiesValue : values())
        {
            if (propertiesValue.value.equals(value))
            {
                result = propertiesValue;
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
