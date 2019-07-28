package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * User: Michail Jungierek
 * Date: 28.07.2019
 * Time: 18:17
 */
public enum Prefix {
    rendition("rendition", "http://www.idpf.org/vocab/rendition/#"),
    schema("schema", "http://schema.org/"),
    ibooks("ibooks", "http://vocabulary.itunes.apple.com/rdf/ibooks/vocabulary-extensions-1.0/");

    private String prefix;
    private String uri;

    Prefix(String prefix, String uri)
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

    public static Prefix getByPrefix(String prefix)
    {
        for (Prefix value : values())
        {
            if (value.getPrefix().equals(prefix))
            {
                return value;
            }
        }
        return null;
    }

    public String asAttributeValue() {
        return prefix + ": " + uri;
    }

    public static String allAsAttributeValue() {
        StringBuilder result = new StringBuilder();
        for (Prefix value : values()) {
            result.append(value.getPrefix()).append(": ").append(value.getUri()).append(" ");
        }
        return result.toString().trim();
    }
}
