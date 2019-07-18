package de.machmireinebook.epubeditor.epublib.domain;

/**
 * @author Michail Jungierek, CGI
 */
public enum OPFValue {
    meta_cover("cover"),
    reference_cover("cover"),
    no,
    generator;

    private String name;

    OPFValue() {
        this.name = name();
    }
    OPFValue(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
