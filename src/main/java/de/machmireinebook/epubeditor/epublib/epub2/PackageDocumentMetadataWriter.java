package de.machmireinebook.epubeditor.epublib.epub2;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreAttributes;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreTag;
import de.machmireinebook.epubeditor.epublib.domain.OPFAttribute;
import de.machmireinebook.epubeditor.epublib.domain.OPFTag;
import de.machmireinebook.epubeditor.epublib.domain.OPFValue;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Author;
import de.machmireinebook.epubeditor.epublib.domain.epub2.DublinCoreMetadataElement;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub2.MetadataDate;

import static de.machmireinebook.epubeditor.epublib.Constants.*;

public class PackageDocumentMetadataWriter
{
    private Element metadataElement;
    /**
     * Writes the book's metadata.
     */
    public void writeMetaData(Book book, Element root)
    {
        metadataElement = new Element(OPFTag.metadata.name(), NAMESPACE_OPF);
        metadataElement.addNamespaceDeclaration(NAMESPACE_OPF_WITH_PREFIX);
        metadataElement.addNamespaceDeclaration(NAMESPACE_DUBLIN_CORE);
        root.addContent(metadataElement);

        Metadata metadata = (Metadata) book.getMetadata();
        writeIdentifiers(metadata);
        writeMetadataElementsWithIdAndLang(DublinCoreTag.title, metadata.getTitles());
        writeMetadataElementsWithIdAndLang(DublinCoreTag.subject, metadata.getSubjects());
        writeMetadataElementsWithIdAndLang(DublinCoreTag.description, metadata.getDescriptions());
        writeMetadataElementsWithIdAndLang(DublinCoreTag.publisher, metadata.getPublishers());
        writeMetadataElementsWithId(DublinCoreTag.type, metadata.getTypes());
        writeMetadataElementsWithIdAndLang(DublinCoreTag.rights, metadata.getRights());
        writeMetadataElementsWithIdAndLang(DublinCoreTag.source, metadata.getSources());
        writeMetadataElementsWithIdAndLang(DublinCoreTag.coverage, metadata.getCoverages());
        writeMetadataElementsWithIdAndLang(DublinCoreTag.relation, metadata.getRelations());
        writeMetadataElementsWithId(DublinCoreTag.format, metadata.getFormats());

        // write authors
        for (Author author : metadata.getAuthors())
        {
            Element creatorElement = new Element(DublinCoreTag.creator.getName(), NAMESPACE_DUBLIN_CORE);
            creatorElement.setAttribute(OPFAttribute.role.getName(), author.getRole().getCode(), NAMESPACE_OPF_WITH_PREFIX);
            if (StringUtils.isNotEmpty(author.getFileAs()))
            {
                creatorElement.setAttribute(OPFAttribute.file_as.getName(), author.getFileAs(), NAMESPACE_OPF_WITH_PREFIX);
            }
            creatorElement.setText(author.getName());
            metadataElement.addContent(creatorElement);
        }

        // write contributors
        for (Author author : metadata.getContributors())
        {
            Element contributorElement = new Element(DublinCoreTag.contributor.getName(), NAMESPACE_DUBLIN_CORE);
            contributorElement.setAttribute(OPFAttribute.role.getName(), author.getRole().getCode(), NAMESPACE_OPF_WITH_PREFIX);
            if (StringUtils.isNotEmpty(author.getFileAs()))
            {
                contributorElement.setAttribute(OPFAttribute.file_as.getName(), author.getFileAs(), NAMESPACE_OPF_WITH_PREFIX);
            }
            contributorElement.setText(author.getName());
            metadataElement.addContent(contributorElement);
        }

        // write dates
        for (MetadataDate date : metadata.getDates())
        {
            Element dateElement = new Element(DublinCoreTag.date.getName(), NAMESPACE_DUBLIN_CORE);
            if (date.getEvent() != null && date.getEvent() != MetadataDate.Event.EMPTY) //dont write the internal event EMPTY to opf
            {
                if (date.getEvent() == MetadataDate.Event.UNKNOWN) {
                    dateElement.setAttribute(OPFAttribute.event.getName(), date.getUnknownEventValue(), NAMESPACE_OPF_WITH_PREFIX);
                } else {
                    dateElement.setAttribute(OPFAttribute.event.getName(), date.getEvent().toString(), NAMESPACE_OPF_WITH_PREFIX);
                }
            }

            dateElement.setText(date.getValue());
            metadataElement.addContent(dateElement);
        }

        // write languages
        for (DublinCoreMetadataElement language : metadata.getLanguages()) {
            Element langElement = new Element(DublinCoreTag.language.getName(), NAMESPACE_DUBLIN_CORE);
            langElement.setText(metadata.getLanguage());
            if (StringUtils.isNotEmpty(language.getId())) {
                langElement.setAttribute(DublinCoreAttributes.id.name(), language.getId());
            }
            metadataElement.addContent(langElement);
        }

        // write coverimage
        if (book.getCoverImage() != null)
        { // write the cover image
            Element metaElement = new Element(OPFTag.meta.name(), NAMESPACE_OPF);
            metaElement.setAttribute(OPFAttribute.name_attribute.getName(), OPFValue.meta_cover.getName());
            metaElement.setAttribute(OPFAttribute.content.getName(), book.getCoverImage().getId());
            metadataElement.addContent(metaElement);
        }

        // write generator
        Element generatorElement = new Element(OPFTag.meta.getName(), NAMESPACE_OPF);
        generatorElement.setAttribute(OPFAttribute.name_attribute.getName(), OPFValue.generator.getName());
        generatorElement.setAttribute(OPFAttribute.content.getName(), Constants.EPUBLIB_GENERATOR_NAME);
        metadataElement.addContent(generatorElement);
    }

    private void writeMetadataElementsWithIdAndLang(DublinCoreTag tag, List<DublinCoreMetadataElement> metadatas)
    {
        for (DublinCoreMetadataElement metadata : metadatas)
        {
            if (StringUtils.isBlank(metadata.getValue()))
            {
                continue;
            }
            Element dcElement = new Element(tag.getName(), NAMESPACE_DUBLIN_CORE);
            dcElement.setText(metadata.getValue());
            if (StringUtils.isNotEmpty(metadata.getId()))
            {
                dcElement.setAttribute(OPFAttribute.id.getName(), metadata.getId());
            }
            if (StringUtils.isNotEmpty(metadata.getLanguage()))
            {
                dcElement.setAttribute(OPFAttribute.lang.getName(), metadata.getLanguage(), Namespace.XML_NAMESPACE);
            }
            metadataElement.addContent(dcElement);
        }
    }

    private void writeMetadataElementsWithId(DublinCoreTag tag, List<DublinCoreMetadataElement> metadatas)
    {
        for (DublinCoreMetadataElement metadata : metadatas)
        {
            if (StringUtils.isBlank(metadata.getValue()))
            {
                continue;
            }
            Element dcElement = new Element(tag.getName(), NAMESPACE_DUBLIN_CORE);
            dcElement.setText(metadata.getValue());
            if (StringUtils.isNotEmpty(metadata.getId()))
            {
                dcElement.setAttribute(OPFAttribute.id.getName(), metadata.getId());
            }
            metadataElement.addContent(dcElement);
        }
    }

    /**
     * Writes out the complete list of Identifiers to the package document.
     * The first identifier for which the bookId is true is made the bookId identifier.
     * If no identifier has bookId == true then the first bookId identifier is written as the primary.
     * @
     */
    private void writeIdentifiers(Metadata metadata)
    {
        Identifier bookIdIdentifier = metadata.getBookIdIdentifier();
        if (bookIdIdentifier == null)
        {
            return;
        }

        List<Identifier> identifiers = metadata.getEpub2Identifiers();

        Element identifierElement = new Element(DublinCoreTag.identifier.getName(), NAMESPACE_DUBLIN_CORE);
        identifierElement.setAttribute(DublinCoreAttributes.id.name(), BOOK_ID_ID);
        if (bookIdIdentifier.getScheme() != null)
        {
            identifierElement.setAttribute(OPFAttribute.scheme.getName(), bookIdIdentifier.getScheme(), NAMESPACE_OPF_WITH_PREFIX);
        }
        identifierElement.setText(bookIdIdentifier.getValue());
        metadataElement.addContent(identifierElement);

        for (Identifier identifier : identifiers.subList(1, identifiers.size()))
        {
            if (identifier == bookIdIdentifier)
            {
                continue;
            }
            Element otherIdentifierElement = new Element(DublinCoreTag.identifier.getName(), NAMESPACE_DUBLIN_CORE);
            otherIdentifierElement.setAttribute(OPFAttribute.scheme.getName(), identifier.getScheme(), NAMESPACE_OPF_WITH_PREFIX);
            otherIdentifierElement.setText(identifier.getValue());
            metadataElement.addContent(otherIdentifierElement);
        }
    }

}
