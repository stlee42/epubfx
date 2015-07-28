package de.machmireinebook.epubeditor.jdom2;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;

/**
 * User: mjungierek
 * Date: 29.10.2014
 * Time: 20:08
 */
public class AtrributeElementFilter extends ElementFilter
{
    private String attributeName;
    private Namespace attributeNamespace = Namespace.NO_NAMESPACE;
    private String attributeValue;

    public AtrributeElementFilter(String elementName, String attributeName, String attributeValue)
    {
        super(elementName);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public AtrributeElementFilter(String attributeName, String attributeValue)
    {
        super();
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public AtrributeElementFilter(String attributeName, Namespace attributeNamespace, String attributeValue)
    {
        super();
        this.attributeName = attributeName;
        this.attributeNamespace = attributeNamespace;
        this.attributeValue = attributeValue;
    }

    @Override
    public Element filter(Object content)
    {
        Element element = super.filter(content);
        if (element != null)
        {
            if (attributeValue.equals(element.getAttributeValue(attributeName, attributeNamespace)))
            {
                return element;
            }
        }
        return null;
    }
}
