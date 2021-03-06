package de.machmireinebook.epubeditor.epublib.domain.epub2;

import java.io.Serializable;

import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.TitledResourceReference;


/**
 * These are references to elements of the book's guide.
 */
public class GuideReference extends TitledResourceReference implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -316179702440631834L;

    public enum Semantics
    {
        /**
         * the book cover(s), jacket information, etc.
         */
        COVER ("cover", "Cover"),
        /**
         * Human-readable table of contents.
         * Not to be confused the epub file table of contents
         */
        TOC("toc", "Inhaltsverzeichnis"),
        /**
         * back-of-book style index
         */
        INDEX("index", "Index"),
        GLOSSARY("glossary", "Glossar"),
        ACKNOWLEDGEMENTS("acknowledgements", "Danksagung"),
        BIBLIOGRAPHY("bibliography", "Bibliografie"),
        COLOPHON("colophon", "Kolophon"),
        COPYRIGHT_PAGE("copyright-page", "Impressum"),
        DEDICATION("dedication", "Widmung"),

        /**
         *  an epigraph is a phrase, quotation, or poem that is set at the beginning of a document or component.
         *  source: http://en.wikipedia.org/wiki/Epigraph_%28literature%29
         */
        EPIGRAPH("epigraph", "Epigraph"),
        FOREWORD("foreword", "Vorwort"),
        /**
         * list of illustrations
         */
        LOI("loi", "Bilderverzeichnis"),

        /**
         * list of tables
         */
        LOT("lot", "Tabellenverzeichnis"),
        NOTES("notes", "Anmerkungen"),
        PREFACE("preface", "Einleitung"),
        /**
         * A page of content (e.g. "Chapter 1")
         */
        TEXT("text", "Text"),
        /**
         * human-readable page with title, author, publisher, and other metadata
         */
        TITLE_PAGE("title-page", "Titelseite");

        private String name;
        private String description;

        Semantics(String name, String description)
        {
            this.name = name;
            this.description = description;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public static Semantics getByName(String name)
        {
            Semantics result = null;
            for (Semantics semantics : values())
            {
                if (semantics.name.equalsIgnoreCase(name))
                {
                    result = semantics;
                }
            }
            return result;
        }
    }

	private Semantics type;
	
	public GuideReference(Resource resource) {
		this(resource, null);
	}
	
	public GuideReference(Resource resource, String title) {
		super(resource, title);
	}
	
	public GuideReference(Resource resource, String type, String title) {
		this(resource, type, title, null);
	}

    public GuideReference(Resource resource, Semantics type, String title) {
        super(resource, title);
        this.type = type;
    }

	public GuideReference(Resource resource, String type, String title, String fragmentId) {
        super(resource, title, fragmentId);
		this.type = Semantics.getByName(type);
	}

	public Semantics getType() {
		return type;
	}

	public void setType(Semantics type) {
		this.type = type;
	}
}
