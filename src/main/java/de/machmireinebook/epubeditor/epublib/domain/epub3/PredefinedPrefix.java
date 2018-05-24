package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * Created by Michail Jungierek, Acando GmbH on 24.05.2018
 */
public enum PredefinedPrefix
{
    DCTERMS("dcterms", "http://purl.org/dc/terms/"),
    MARC("marc", "http://id.loc.gov/vocabulary/"),
    MEDIA("media", "http://www.idpf.org/epub/vocab/overlays/#"),
    ONIX("onix", "http://www.editeur.org/ONIX/book/codelists/current.html#"),
    XSD("xsd", "http://www.w3.org/2001/XMLSchema#");

    private String prefix;
    private String uri;

    PredefinedPrefix(String prefix, String uri)
    {
        this.prefix = prefix;
        this.uri = uri;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getUri()
    {
        return uri;
    }

    public static PredefinedPrefix getByPrefix(String prefix)
    {
        for (PredefinedPrefix predefinedPrefix : values())
        {
            if (predefinedPrefix.getPrefix().equals(prefix))
            {
                return predefinedPrefix;
            }
        }
        return null;
    }
}
