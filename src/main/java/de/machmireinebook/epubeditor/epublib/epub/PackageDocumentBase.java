package de.machmireinebook.epubeditor.epublib.epub;


import org.jdom2.Namespace;

/**
 * Functionality shared by the PackageDocumentReader and the PackageDocumentWriter
 *  
 * @author paul
 *
 */
public class PackageDocumentBase
{
    public static final String BOOK_ID_ID = "BookId";
    public static final Namespace NAMESPACE_OPF = Namespace.getNamespace("http://www.idpf.org/2007/opf");
    public static final Namespace NAMESPACE_OPF_WITH_PREFIX = Namespace.getNamespace("opf", "http://www.idpf.org/2007/opf");
    public static final Namespace NAMESPACE_DUBLIN_CORE = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
    public static final Namespace NAMESPACE_EPUB = Namespace.getNamespace("epub", "http://www.idpf.org/2007/ops");
    public static final String dateFormat = "yyyy-MM-dd";

    public static final String EPUB3_NAV_DOCUMENT_TAG_VALUE = "nav";

    protected interface DCTags
    {
        String title = "title";
        String creator = "creator";
        String subject = "subject";
        String description = "description";
        String publisher = "publisher";
        String contributor = "contributor";
        String date = "date";
        String type = "type";
        String format = "format";
        String identifier = "identifier";
        String source = "source";
        String language = "language";
        String relation = "relation";
        String coverage = "coverage";
        String rights = "rights";
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
        String linear = "linear";
        String event = "event";
        String role = "role";
        String file_as = "file-as";
        String id = "id";
        String media_type = "media-type";
        String title = "title";
        String toc = "toc";
        String version = "version";
        String scheme = "scheme";
        String property = "property";
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
        String cover = "cover";
        String landmarks = "landmarks";
        String titlepage = "titlepage";
        String bodymatter = "bodymatter";
        String index = "index";
    }
}