package de.machmireinebook.epubeditor.jdom2;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 19:06
 */
public class JDOM2Utils
{
    private static final Logger logger = Logger.getLogger(JDOM2Utils.class);

    public static List<String> getChildrenText(Element rootElement, Namespace namespace, String childrenName)
    {
        List<String> texts = new ArrayList<>();
        List<Element> elements = rootElement.getChildren(childrenName, namespace);
        for (Element element : elements)
        {
            texts.add(element.getText());
        }
        return texts;
    }
}
