package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * Created by Michail Jungierek
 *
 * See https://idpf.github.io/epub-vocabs/structure/
 *
 * without the deprecated types
 */
public enum EpubType
{
    //Document partitions
    cover,
    frontmatter,
    bodymatter,
    backmatter,
    //Document divisions
    volume,
    part,
    chapter,
    division,
    //Document sections and components
    foreword,
    preface,
    prologue,
    introduction,
    preamble,
    conclusion,
    epilogue,
    afterword,
    epigraph,
    //Document navigation
    toc,
    landmarks,
    loa,
    loi,
    lot,
    lov,
    //Document reference sections
    appendix,
    colophon,
    index,
    index_headnotes("index_headnotes"),
    index_legend("index_legend"),
    index_group("index-group"),
    index_entry_list(""),
    index_entry(""),
    index_term(""),
    index_editor_note(""),
    index_locator(""),
    index_locator_list(""),
    index_locator_range(""),
    index_xref_preferred(""),
    index_xref_related(""),
    index_term_category(""),
    index_term_categories(""),
    //Glossaries
    glossary,
    glossterm,
    glossdef,
    //Bibliographies
    bibliography,
    biblioentry,
    //Preliminary sections and components
    titlepage,
    halftitlepage,
    copyright_page(""),
    acknowledgments,
    imprint,
    imprimatur,
    contributors,
    other_credits(""),
    errata,
    dedication,
    revision_history(""),
    notice,
    tip,
    //Titles and headings
    halftitle,
    fulltitle,
    covertitle,
    title,
    subtitle,
    bridgehead,
    //Notes and annotations
    footnote,
    endnote,
    footnotes,
    endnotes,
    //Document text
    keyword,
    topic_sentence(""),
    concluding_sentence(""),
    //Pagination
    pagebreak,
    page_list(""),
    //Tables
    table,
    table_row,
    table_cell,
    //Lists
    list,
    list_item(""),
    //Figures
    figure;

    private String sepcificationName;
    EpubType()
    {
        this.sepcificationName = this.name();
    }

    EpubType(String name)
    {
        this.sepcificationName = name;
    }

    public String getSepcificationName()
    {
        return sepcificationName;
    }
}
