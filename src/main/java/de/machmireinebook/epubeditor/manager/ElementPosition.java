package de.machmireinebook.epubeditor.manager;

/**
 * User: mjungierek
 * Date: 27.08.2014
 * Time: 21:25
 */
public class ElementPosition
{
    private int position;
    private String nodeName;
    private String namespaceUri;

    public ElementPosition(String nodeName, int position)
    {
        this.nodeName = nodeName;
        this.position = position;
    }

    public ElementPosition(String nodeName, int position, String namespaceUri)
    {
        this.nodeName = nodeName;
        this.position = position;
        this.namespaceUri = namespaceUri;
    }

    public int getPosition()
    {
        return position;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public String getNamespaceUri()
    {
        return namespaceUri;
    }

    @Override
    public String toString()
    {
        return "ElementPosition{" +
                "position=" + position +
                ", nodeName='" + nodeName + '\'' +
                ", namespaceUri='" + namespaceUri + '\'' +
                '}';
    }
}
