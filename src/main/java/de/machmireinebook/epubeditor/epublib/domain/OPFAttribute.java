package de.machmireinebook.epubeditor.epublib.domain;

/**
 * @author Michail Jungierek, CGI
 */
public enum OPFAttribute {
    uniqueIdentifier("unique-identifier"),
    idref,
    name_attribute,
    content,
    type,
    href,
    event,
    file_as("file-as"),
    id,
    lang,
    linear,
    media_type("media-type"),
    property,
    properties,
    refines,
    role,
    scheme,
    title,
    toc,
    version;

    private String name;

    OPFAttribute() {
        this.name = name();
    }
    OPFAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
