package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * User: mjungierek
 * Date: 28.09.2017
 * Time: 00:07
 */
public class Epub3MetadataProperty
{
    /*
    <meta property="dcterms:modified">2013-10-26T17:27:34Z</meta>
    <meta refines="#creator1" scheme="marc:relators" property="role">aut</meta>
   <meta refines="#creator1" property="file-as">Kisselbach, Hans-Günter</meta>      */

    private String property;
    private String refines;
    private String scheme;

    private String value;

    public String getProperty()
    {
        return property;
    }

    public void setProperty(String qName)
    {
        this.property = qName;
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
