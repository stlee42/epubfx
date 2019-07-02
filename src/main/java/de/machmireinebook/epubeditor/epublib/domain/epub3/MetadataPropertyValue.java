package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * @author Michail Jungierek, CGI
 */
public enum MetadataPropertyValue
{
    alternate_script("alternate-script"),
    display_seq("display-seq"),
    file_as("file-as"),
    group_position("group-position"),
    identifier_type("identifier-type"),
    meta_auth("meta-auth"),
    role,
    dcterms_modified("dcterms:modified"),
    ;

    private String sepcificationName;
    MetadataPropertyValue()
    {
        this.sepcificationName = this.name();
    }

    MetadataPropertyValue(String name)
    {
        this.sepcificationName = name;
    }

    public String getSpecificationName()
    {
        return sepcificationName;
    }

}
