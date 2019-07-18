package de.machmireinebook.epubeditor.epublib;


import org.jdom2.DocType;
import org.jdom2.Namespace;

public interface Constants {
	String CHARACTER_ENCODING = "UTF-8";
    DocType DOCTYPE_XHTML = new DocType("html", "-//W3C//DTD XHTML 1.1//EN", "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
    Namespace NAMESPACE_XHTML = Namespace.getNamespace("http://www.w3.org/1999/xhtml");
    Namespace NAMESPACE_XHTML_WITH_PREFIX = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");
	String EPUBLIB_GENERATOR_NAME = "SmoekerSchriever - epubFx Version 1.0";
	String FRAGMENT_SEPARATOR_CHAR = "#";
	String DEFAULT_TOC_ID = "toc";

	String DEFAULT_NCX_ID = "ncx";
	String DEFAULT_NCX_HREF = "toc.ncx";

	String BOOK_ID_ID = "BookId";
	Namespace NAMESPACE_OPF = Namespace.getNamespace("http://www.idpf.org/2007/opf");
	Namespace NAMESPACE_OPF_WITH_PREFIX = Namespace.getNamespace("opf", "http://www.idpf.org/2007/opf");
	Namespace NAMESPACE_DUBLIN_CORE = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
	Namespace NAMESPACE_EPUB = Namespace.getNamespace("epub", "http://www.idpf.org/2007/ops");
	String dateFormat = "yyyy-MM-dd";

	String CLASS_SIGIL_NOT_IN_TOC = "sigil_not_in_toc";
	String IGNORE_IN_TOC = "ignore-in-toc";
}
