package de.machmireinebook.epubeditor.epublib.epub;


/**
 * Functionality shared by the PackageDocumentReader and the PackageDocumentWriter
 *  
 * @author paul
 *
 */
public class PackageDocumentBase
{
    public enum DCTag
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

        DCTag(java.lang.String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

    protected interface DCAttributes
    {
        String scheme = "scheme";
        String id = "id";
    }

    protected interface OPFTags
    {
        String metadata = "metadata";
        String meta = "meta";
        String manifest = "manifest";
        String packageTag = "package";
        String itemref = "itemref";
        String spine = "spine";
        String reference = "reference";
        String guide = "guide";
        String item = "item";
    }

    protected interface OPFAttributes
    {
        String uniqueIdentifier = "unique-identifier";
        String idref = "idref";
        String name = "name";
        String content = "content";
        String type = "type";
        String href = "href";
        String event = "event";
        String file_as = "file-as";
        String id = "id";
        String lang = "lang";
        String linear = "linear";
        String media_type = "media-type";
        String property = "property";
        String properties = "properties";
        String refines = "refines";
        String role = "role";
        String scheme = "scheme";
        String title = "title";
        String toc = "toc";
        String version = "version";
    }

    protected interface OPFValues
    {
        String meta_cover = "cover";
        String reference_cover = "cover";
        String no = "no";
        String generator = "generator";
    }

    protected interface Epub3ManifestPropertiesValues
    {
        String cover_image = "cover-image";
        String mathml = "mathml";
        String scripted = "scripted";
        String svg = "svg";
        String remote_resources = "remote-resources";
        String non_epub_xml = "switch";
        String nav = "nav";
    }

    protected interface Epub3NavTypes
    {
        String toc = "toc";
        String page_list = "page-list";
        String landmarks = "landmarks";
    }
}
