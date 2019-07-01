package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * @author Michail Jungierek, CGI
 */
public enum MetadataProperty
{
    alternate_script("alternate-script"),
    display_seq("display-seq"),
    file_as("file-as"),
    group_position("group-position"),
    identifier_type("identifier-type"),
    meta_auth("meta-auth"),
    role
    ;

    private String sepcificationName;
    MetadataProperty()
    {
        this.sepcificationName = this.name();
    }

    MetadataProperty(String name)
    {
        this.sepcificationName = name;
    }

    public String getSepcificationName()
    {
        return sepcificationName;
    }

}
