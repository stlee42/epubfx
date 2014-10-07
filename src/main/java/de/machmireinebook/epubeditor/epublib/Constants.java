package de.machmireinebook.epubeditor.epublib;


import org.jdom2.DocType;
import org.jdom2.Namespace;

public interface Constants {
	String CHARACTER_ENCODING = "UTF-8";
    DocType DOCTYPE_XHTML = new DocType("html", "-//W3C//DTD XHTML 1.1//EN", "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
    Namespace NAMESPACE_XHTML = Namespace.getNamespace("http://www.w3.org/1999/xhtml");
    Namespace NAMESPACE_XHTML_WITH_PREFIX = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");
	String EPUBLIB_GENERATOR_NAME = "epub4mmee version 1.0";
	String FRAGMENT_SEPARATOR_CHAR = "#";
	String DEFAULT_TOC_ID = "toc";
}
