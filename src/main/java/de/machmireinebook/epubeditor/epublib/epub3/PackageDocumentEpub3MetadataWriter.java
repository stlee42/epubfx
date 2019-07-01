package de.machmireinebook.epubeditor.epublib.epub3;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Author;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreMetadataElement;
import de.machmireinebook.epubeditor.epublib.domain.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Epub3Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Epub3MetadataProperty;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataProperty;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;

import static de.machmireinebook.epubeditor.epublib.Constants.*;

public class PackageDocumentEpub3MetadataWriter extends PackageDocumentBase
{
    private Book book;
    private Element metadataElement;

    public PackageDocumentEpub3MetadataWriter(Book book, Element root) {
        metadataElement = new Element(OPFTags.metadata, NAMESPACE_OPF);
        metadataElement.addNamespaceDeclaration(NAMESPACE_OPF_WITH_PREFIX);
        metadataElement.addNamespaceDeclaration(NAMESPACE_DUBLIN_CORE);
        root.addContent(metadataElement);
    }
    /**
     * Writes the book's metadata.
     */
    public void writeMetaData()
    {
        //https://www.oreilly.com/library/view/epub-3-best/9781449329129/ch01.html
        Epub3Metadata metadata = (Epub3Metadata) book.getMetadata();
        writeIdentifiers(metadata);
        writeMetaElements(metadata.getEpub3MetaProperties());
        writeDublinCoreMetadataElements(DCTag.title.getName(), metadata.getTitles());
        writeSimpleMetadataElements(DCTag.subject.getName(), metadata.getSubjects());
        writeSimpleMetadataElements(DCTag.description.getName(), metadata.getDescriptions());
        writeSimpleMetadataElements(DCTag.publisher.getName(), metadata.getPublishers());
        writeSimpleMetadataElements(DCTag.type.getName(), metadata.getTypes());
        writeSimpleMetadataElements(DCTag.rights.getName(), metadata.getRights());
        writeSimpleMetadataElements(DCTag.coverage.getName(), metadata.getCoverages());

        // write authors
        for (Author author : metadata.getAuthors())
        {
            Element creatorElement = writeDublinCoreMetadataElement(DCTag.creator.getName(), author);
            creatorElement.setAttribute(OPFAttributes.role, author.getRelator().getCode(), NAMESPACE_OPF_WITH_PREFIX);
            if (StringUtils.isNotEmpty(author.getFileAs()))
            {
                creatorElement.setAttribute(OPFAttributes.id, author.getId());
                writeMetaElement(MetadataProperty.file_as.getSepcificationName(), "#" + author.getId(), author.getScheme(), author.getFileAs());
            }
            creatorElement.setText(author.getName());
            metadataElement.addContent(creatorElement);
        }

        // write contributors
        for (Author contributor : metadata.getContributors())
        {
            Element contributorElement = writeDublinCoreMetadataElement(DCTag.contributor.getName(), contributor);
            contributorElement.setAttribute(OPFAttributes.role, contributor.getRelator().getCode(), NAMESPACE_OPF_WITH_PREFIX);
            if (StringUtils.isNotEmpty(contributor.getFileAs()))
            {
                contributorElement.setAttribute(OPFAttributes.id, contributor.getId());
                writeMetaElement(MetadataProperty.file_as.getSepcificationName(), "#" + contributor.getId(), contributor.getScheme(), contributor.getFileAs());
            }
            contributorElement.setText(contributor.getName());
            metadataElement.addContent(contributorElement);
        }

        // write dates
        for (MetadataDate date : metadata.getDates())
        {
            Element dateElement = new Element(DCTag.date.getName(), NAMESPACE_DUBLIN_CORE);
            if (date.getEvent() != null)
            {
                dateElement.setAttribute(OPFAttributes.event, date.getEvent().toString(), NAMESPACE_OPF_WITH_PREFIX);
            }

            dateElement.setText(date.getValue());
            metadataElement.addContent(dateElement);
        }

        // write language
        if (StringUtils.isNotBlank(metadata.getLanguage()))
        {
            Element langElement = new Element("language", NAMESPACE_DUBLIN_CORE);
            langElement.setText(metadata.getLanguage());
            metadataElement.addContent(langElement);
        }

        // write coverimage
        if (book.getCoverImage() != null)
        { // write the cover image
            Element metaElement = new Element(OPFTags.meta, NAMESPACE_OPF);
            metaElement.setAttribute(OPFAttributes.name, OPFValues.meta_cover);
            metaElement.setAttribute(OPFAttributes.content, book.getCoverImage().getId());
            metadataElement.addContent(metaElement);
        }

        // write generator
        Element generatorElement = new Element(OPFTags.meta, NAMESPACE_OPF);
        generatorElement.setAttribute(OPFAttributes.name, OPFValues.generator);
        generatorElement.setAttribute(OPFAttributes.content, Constants.EPUBLIB_GENERATOR_NAME);
        metadataElement.addContent(generatorElement);
    }

    private void writeMetaElements(List<Epub3MetadataProperty> values)
    {
        for (Epub3MetadataProperty value : values)
        {
            if (StringUtils.isBlank(value.getValue()))
            {
                continue;
            }
            writeMetaElement(value);
        }
    }

    private void writeMetaElement(Epub3MetadataProperty value)
    {
        Element metaElement = new Element("meta");
        metaElement.setText(value.getValue());
        if (value.getProperty() != null)
        {
            metaElement.setAttribute(OPFAttributes.property, value.getProperty());
        }
        if (StringUtils.isNotEmpty(value.getRefines()))
        {
            metaElement.setAttribute(OPFAttributes.refines, value.getRefines());
        }
        if (StringUtils.isNotEmpty(value.getRefines()))
        {
            metaElement.setAttribute(OPFAttributes.scheme, value.getScheme());
        }
        metadataElement.addContent(metaElement);
    }

    private void writeMetaElement(String property, String refines, String scheme, String text)
    {
        Element metaElement = new Element("meta");
        metaElement.setText(text);
        if (property != null)
        {
            metaElement.setAttribute(OPFAttributes.property, property);
        }
        if (StringUtils.isNotEmpty(refines))
        {
            metaElement.setAttribute(OPFAttributes.refines, refines);
        }
        if (StringUtils.isNotEmpty(scheme))
        {
            metaElement.setAttribute(OPFAttributes.scheme, scheme);
        }
        metadataElement.addContent(metaElement);
    }

    private void writeSimpleMetadataElements(String tagName, List<String> values)
    {
        for (String value : values)
        {
            if (StringUtils.isBlank(value))
            {
                continue;
            }
            Element dcElement = new Element(tagName, NAMESPACE_DUBLIN_CORE);
            dcElement.setText(value);
            metadataElement.addContent(dcElement);
        }
    }

    private void writeDublinCoreMetadataElements(String tagName, List<DublinCoreMetadataElement> values)
    {
        for (DublinCoreMetadataElement value : values)
        {
            if (StringUtils.isBlank(value.getValue()))
            {
                continue;
            }
            Element dcElement = new Element(tagName, NAMESPACE_DUBLIN_CORE);
            dcElement.setText(value.getValue());
            if (StringUtils.isNotEmpty(value.getScheme()))
            {
                dcElement.setAttribute(OPFAttributes.scheme, value.getScheme());
            }
            if (StringUtils.isNotEmpty(value.getId()))
            {
                dcElement.setAttribute(OPFAttributes.id, value.getId());
            }
            if (StringUtils.isNotEmpty(value.getLanguage()))
            {
                dcElement.setAttribute(OPFAttributes.lang, value.getLanguage(), Namespace.XML_NAMESPACE);
            }

            metadataElement.addContent(dcElement);
        }
    }

    private Element writeDublinCoreMetadataElement(String tagName, DublinCoreMetadataElement dcMetadata) {
        Element dcElement = new Element(tagName, NAMESPACE_DUBLIN_CORE);
        dcElement.setText(dcMetadata.getValue());
        if (StringUtils.isNotEmpty(dcMetadata.getScheme()))
        {
            dcElement.setAttribute(OPFAttributes.scheme, dcMetadata.getScheme());
        }
        if (StringUtils.isNotEmpty(dcMetadata.getId()))
        {
            dcElement.setAttribute(OPFAttributes.id, dcMetadata.getId());
        }
        if (StringUtils.isNotEmpty(dcMetadata.getLanguage()))
        {
            dcElement.setAttribute(OPFAttributes.lang, dcMetadata.getLanguage(), Namespace.XML_NAMESPACE);
        }

        return metadataElement.addContent(dcElement);
    }

    /**
     * Writes out the complete list of Identifiers to the package document.
     * The first identifier for which the bookId is true is made the bookId identifier.
     * If no identifier has bookId == true then the first bookId identifier is written as the primary.
     */
    private void writeIdentifiers(Epub3Metadata metadata)
    {
        Identifier bookIdIdentifier = metadata.getBookIdIdentifier();
        if (bookIdIdentifier == null)
        {
            return;
        }

        List<Identifier> identifiers = metadata.getIdentifiers();

        Element identifierElement = new Element(DCTag.identifier.getName(), NAMESPACE_DUBLIN_CORE);
        identifierElement.setAttribute(DCAttributes.id, BOOK_ID_ID);
        if (bookIdIdentifier.getScheme() != null)
        {
            identifierElement.setAttribute(OPFAttributes.scheme, bookIdIdentifier.getScheme(), NAMESPACE_OPF_WITH_PREFIX);
        }
        identifierElement.setText(bookIdIdentifier.getValue());
        metadataElement.addContent(identifierElement);

        for (Identifier identifier : identifiers.subList(1, identifiers.size()))
        {
            if (identifier == bookIdIdentifier)
            {
                continue;
            }
            Element otherIdentifierElement = new Element(DCTag.identifier.getName(), NAMESPACE_DUBLIN_CORE);
            otherIdentifierElement.setAttribute(OPFAttributes.scheme, identifier.getScheme(), NAMESPACE_OPF_WITH_PREFIX);
            otherIdentifierElement.setText(identifier.getValue());
            metadataElement.addContent(otherIdentifierElement);
        }
    }
}
