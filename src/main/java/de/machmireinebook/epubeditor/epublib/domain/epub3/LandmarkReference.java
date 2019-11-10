package de.machmireinebook.epubeditor.epublib.domain.epub3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.TitledResourceReference;


/**
 * These are references to elements of the landmarks in the nav resource
 */
public class LandmarkReference extends TitledResourceReference implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = -316179702440631834L;

    public enum Semantic
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
        ACKNOWLEDGEMENTS("acknowledgements", "Danksagung", "acknowledgments"),
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
        BODYMATTER("bodymatter", "Start reading"),
        /**
         * human-readable page with title, author, publisher, and other metadata
         */
        TITLE_PAGE("titlepage", "Titelseite");

        private String name;
        private String description;
        private List<String> alternativeNames = new ArrayList<>();

        Semantic(String name, String description, String... alternativeNames)
        {
            this.name = name;
            this.description = description;
            this.alternativeNames.addAll(Arrays.asList(alternativeNames));
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public static Semantic getByName(String name)
        {
            Semantic result = null;
            for (Semantic semantics : values())
            {
                if (semantics.name.equalsIgnoreCase(name) || semantics.alternativeNames.contains(name.toLowerCase())) {
                    result = semantics;
                    return result;
                }
            }
            return result;
        }
    }

	private Semantic type;

	public LandmarkReference(Resource resource) {
		this(resource, null);
	}

	public LandmarkReference(Resource resource, String title) {
		super(resource, title);
	}

	public LandmarkReference(Resource resource, String type, String title) {
		this(resource, type, title, null);
	}

    public LandmarkReference(Resource resource, Semantic type, String title) {
        super(resource, title);
        this.type = type;
    }

	public LandmarkReference(Resource resource, String type, String title, String fragmentId) {
        super(resource, title, fragmentId);
		this.type = Semantic.getByName(type);
	}

	public Semantic getType() {
		return type;
	}

	public void setType(Semantic type) {
		this.type = type;
	}
}
