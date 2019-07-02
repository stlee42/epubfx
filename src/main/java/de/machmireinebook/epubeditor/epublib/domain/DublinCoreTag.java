package de.machmireinebook.epubeditor.epublib.domain;

/**
 * @author Michail Jungierek, CGI
 */
public enum DublinCoreTag
{
    title("title"),
    creator("creator"),
    subject("subject"),
    description("description"),
    publisher("publisher"),
    contributor("contributor"),
    date("date"),
    type("type"),
    format("format"),
    identifier("identifier"),
    source("source"),
    language("language"),
    relation("relation"),
    coverage("coverage"),
    rights("rights");

    private String name;

    DublinCoreTag(java.lang.String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
