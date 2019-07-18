package de.machmireinebook.epubeditor.epublib.domain;

/**
 *
 */
public enum OPFTag {
    metadata,
    meta,
    manifest,
    packageTag("package"),
    itemref,
    spine,
    reference,
    guide,
    item;

    private String name;

    OPFTag() {
        this.name = name();
    }
    OPFTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
