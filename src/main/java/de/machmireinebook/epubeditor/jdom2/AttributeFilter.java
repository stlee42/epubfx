package de.machmireinebook.epubeditor.jdom2;

import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;

/**
 * User: Michail Jungierek
 * Date: 18.08.2019
 * Time: 19:00
 */
public class AttributeFilter extends ElementFilter implements Filter<Element> {

    private String attributeName;
    /**
     * Select only the Elements with the supplied attribute name in any Namespace
     *
     * @param name   The name of the Element.
     */
    public AttributeFilter(String attributeName) {
        super();
        this.attributeName = attributeName;
    }

    @Override
    public Element filter(Object content)
    {
        Element element = super.filter(content);
        if (element != null) {
            if (element.getAttribute(attributeName) != null) {
                return element;
            }
        }
        return null;
    }


}
