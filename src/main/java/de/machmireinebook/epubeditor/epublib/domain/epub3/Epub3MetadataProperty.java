package de.machmireinebook.epubeditor.epublib.domain.epub3;

import javax.xml.namespace.QName;

/**
 * User: mjungierek
 * Date: 28.09.2017
 * Time: 00:07
 */
public class Epub3MetadataProperty
{
    /*   <meta refines="#creator1" scheme="marc:relators" property="role">aut</meta>
   <meta refines="#creator1" property="file-as">Kisselbach, Hans-Günter</meta>      */

    /**
     * saved in attribute property
     */
    private QName qName;
    private String value;
    private String refines;
    private String scheme;

    public QName getQName()
    {
        return qName;
    }

    public void setQName(QName qName)
    {
        this.qName = qName;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getRefines()
    {
        return refines;
    }

    public void setRefines(String refines)
    {
        this.refines = refines;
    }

    public String getScheme()
    {
        return scheme;
    }

    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }
}
