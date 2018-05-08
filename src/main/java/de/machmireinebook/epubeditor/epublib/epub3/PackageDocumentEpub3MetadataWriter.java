package de.machmireinebook.epubeditor.epublib.epub3;

import java.util.List;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Author;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreMetadataElement;
import de.machmireinebook.epubeditor.epublib.domain.Epub3Metadata;
import de.machmireinebook.epubeditor.epublib.domain.Epub3MetadataProperty;
import de.machmireinebook.epubeditor.epublib.domain.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.MetadataDate;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

public class PackageDocumentEpub3MetadataWriter extends PackageDocumentBase
{


    /**
     * Writes the book's metadata.
     *
     * @param book
     * @param serializer
     * @throws java.io.IOException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public static void writeMetaData(Book book, Element root)
    {
        Element metadataElement = new Element(OPFTags.metadata, NAMESPACE_OPF);
        metadataElement.addNamespaceDeclaration(NAMESPACE_OPF_WITH_PREFIX);
        metadataElement.addNamespaceDeclaration(NAMESPACE_DUBLIN_CORE);
        root.addContent(metadataElement);

        Epub3Metadata metadata = (Epub3Metadata) book.getMetadata();
        writeIdentifiers(metadata, metadataElement);
        writeDublicCoreMetadataElements(DCTag.title.getName(), metadata.getTitles(), metadataElement);
        writeSimpleMetadataElements(DCTag.subject.getName(), metadata.getSubjects(), metadataElement);
        writeSimpleMetadataElements(DCTag.description.getName(), metadata.getDescriptions(), metadataElement);
        writeSimpleMetadataElements(DCTag.publisher.getName(), metadata.getPublishers(), metadataElement);
        writeSimpleMetadataElements(DCTag.type.getName(), metadata.getTypes(), metadataElement);
        writeSimpleMetadataElements(DCTag.rights.getName(), metadata.getRights(), metadataElement);
        writeSimpleMetadataElements(DCTag.coverage.getName(), metadata.getCoverages(), metadataElement);

        // write authors
        for (Author author : metadata.getAuthors())
        {
            Element creatorElement = new Element(DCTag.creator.getName(), NAMESPACE_DUBLIN_CORE);
            creatorElement.setAttribute(OPFAttributes.role, author.getRelator().getCode(), NAMESPACE_OPF_WITH_PREFIX);
            if (StringUtils.isNotEmpty(author.getFileAs()))
            {
                creatorElement.setAttribute(OPFAttributes.file_as, author.getFileAs(), NAMESPACE_OPF_WITH_PREFIX);
            }
            creatorElement.setText(author.getName());
            metadataElement.addContent(creatorElement);
        }

        // write contributors
        for (Author author : metadata.getContributors())
        {
            Element contributorElement = new Element(DCTag.contributor.getName(), NAMESPACE_DUBLIN_CORE);
            contributorElement.setAttribute(OPFAttributes.role, author.getRelator().getCode(), NAMESPACE_OPF_WITH_PREFIX);
            if (StringUtils.isNotEmpty(author.getFileAs()))
            {
                contributorElement.setAttribute(OPFAttributes.file_as, author.getFileAs(), NAMESPACE_OPF_WITH_PREFIX);
            }
            contributorElement.setText(author.getName());
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

        // write other properties
        if (metadata.getEpub3MetaProperties() != null)
        {
            for (Epub3MetadataProperty otherProperty : metadata.getEpub3MetaProperties())
            {
                Element otherElement = new Element(OPFTags.meta, NAMESPACE_OPF);
                if (otherProperty.getQName() != null)
                {
                    otherElement.setAttribute(OPFAttributes.property, otherProperty.getQName().getLocalPart());
                }
                if (StringUtils.isNotEmpty(otherProperty.getRefines()))
                {
                    otherElement.setAttribute(OPFAttributes.refines, otherProperty.getRefines());
                }
                if (StringUtils.isNotEmpty(otherProperty.getRefines()))
                {
                    otherElement.setAttribute(OPFAttributes.scheme, otherProperty.getScheme());
                }
                otherElement.setText(otherProperty.getValue());
                metadataElement.addContent(otherElement);
            }
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

    private static void writeDublicCoreMetadataElements(String tagName, List<DublinCoreMetadataElement> values, Element metadataElement)
    {
        for (DublinCoreMetadataElement value : values)
        {
            if (StringUtils.isBlank(value.getValue()))
            {
                continue;
            }
            Element dcElement = new Element(tagName, NAMESPACE_DUBLIN_CORE);
            dcElement.setText(value.getValue());
            if (StringUtils.isNotEmpty(value.getId()))
            {
                dcElement.setAttribute(DCAttributes.id, value.getId());
            }
            if (StringUtils.isNotEmpty(value.getScheme()))
            {
                dcElement.setAttribute(DCAttributes.scheme, value.getScheme());
            }
            metadataElement.addContent(dcElement);
        }
    }

    private static void writeSimpleMetadataElements(String tagName, List<String> values, Element metadataElement)
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

    /**
     * Writes out the complete list of Identifiers to the package document.
     * The first identifier for which the bookId is true is made the bookId identifier.
     * If no identifier has bookId == true then the first bookId identifier is written as the primary.
     *
     * @param identifiers
     * @param serializer
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @
     */
    private static void writeIdentifiers(Epub3Metadata metadata, Element metadataElement)
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