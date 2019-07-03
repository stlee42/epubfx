package de.machmireinebook.epubeditor.epublib.epub3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreTag;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Author;
import de.machmireinebook.epubeditor.epublib.domain.epub3.DublinCoreMetadataElement;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataProperty;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataPropertyValue;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;

import org.apache.commons.lang.StringUtils;

import org.jdom2.Element;
import org.jdom2.Namespace;

import static de.machmireinebook.epubeditor.epublib.Constants.*;

public class PackageDocumentEpub3MetadataWriter extends PackageDocumentBase
{
    private Book book;
    private Element metadataElement;

    private List<MetadataProperty> alreadyWrittenMetaProperty = new ArrayList<>();

    public PackageDocumentEpub3MetadataWriter(Book book, Element root) {
        metadataElement = new Element(OPFTags.metadata, NAMESPACE_OPF);
        metadataElement.addNamespaceDeclaration(NAMESPACE_OPF_WITH_PREFIX);
        metadataElement.addNamespaceDeclaration(NAMESPACE_DUBLIN_CORE);
        root.addContent(metadataElement);
        this.book = book;
    }
    /**
     * Writes the book's metadata.
     */
    public void writeMetaData()
    {
        //https://www.oreilly.com/library/view/epub-3-best/9781449329129/ch01.html
        Metadata metadata = (Metadata) book.getMetadata();
        writeIdentifiers(metadata);
        writeDublinCoreMetadataElements(DublinCoreTag.title.getName(), metadata.getTitles());
        writeSimpleMetadataElements(DublinCoreTag.subject.getName(), metadata.getSubjects());
        writeSimpleMetadataElements(DublinCoreTag.description.getName(), metadata.getDescriptions());
        writeSimpleMetadataElements(DublinCoreTag.publisher.getName(), metadata.getPublishers());
        writeSimpleMetadataElements(DublinCoreTag.type.getName(), metadata.getTypes());
        writeSimpleMetadataElements(DublinCoreTag.rights.getName(), metadata.getRights());
        writeSimpleMetadataElements(DublinCoreTag.coverage.getName(), metadata.getCoverages());

        // write authors
        for (Author author : metadata.getAuthors())
        {
            writeDublinCoreMetadataElement(DublinCoreTag.creator.getName(), author);
            if(!author.getRefinements().isEmpty()) {
                for (MetadataProperty refinement : author.getRefinements())
                {
                    writeMetaElement(refinement);
                    alreadyWrittenMetaProperty.add(refinement);
                }
            }
            if (author.getFileAs() != null) {
                writeMetaElement(author.getFileAs());
                alreadyWrittenMetaProperty.add(author.getFileAs());
            }
            if (author.getRole() != null) {
                writeMetaElement(author.getRole());
                alreadyWrittenMetaProperty.add(author.getRole());
            }
        }

        // write contributors
        for (Author contributor : metadata.getContributors())
        {
            writeDublinCoreMetadataElement(DublinCoreTag.contributor.getName(), contributor);
            if(!contributor.getRefinements().isEmpty()) {
                for (MetadataProperty refinement : contributor.getRefinements())
                {
                    writeMetaElement(refinement);
                    alreadyWrittenMetaProperty.add(refinement);
                }
            }
        }

        // write dates
        MetadataDate date = metadata.getPublicationDate();
        if (date != null)
        {
            Element dateElement = new Element(DublinCoreTag.date.getName(), NAMESPACE_DUBLIN_CORE);
            dateElement.setText(date.getValue());
            metadataElement.addContent(dateElement);
        }

        MetadataProperty modificationDate = metadata.getModificationDate();
        if (modificationDate == null) {
            modificationDate = new MetadataProperty();
            modificationDate.setProperty(MetadataPropertyValue.dcterms_modified.getName());
            metadata.setModificationDate(modificationDate);
        }
        modificationDate.setValue(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()));
        writeMetaElement(modificationDate);

        //source
        DublinCoreMetadataElement source = metadata.getSource();
        writeDublinCoreMetadataElement(DublinCoreTag.source.getName(), source);

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

        writeMetaElements(metadata.getEpub3MetaProperties());

    }

    private void writeMetaElements(List<MetadataProperty> values)
    {
        for (MetadataProperty value : values)
        {
            if (StringUtils.isBlank(value.getValue()))
            {
                continue;
            }
            writeMetaElement(value);
        }
    }

    private void writeMetaElement(MetadataProperty value)
    {
        if (alreadyWrittenMetaProperty.contains(value)) {
            return;
        }
        Element metaElement = new Element("meta", NAMESPACE_OPF);
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

    private void writeDublinCoreMetadataElement(String tagName, DublinCoreMetadataElement dcMetadata) {
        if (dcMetadata == null) {
            return;
        }
        Element dcElement = new Element(tagName, NAMESPACE_DUBLIN_CORE);
        dcElement.setText(dcMetadata.getValue());
        if (StringUtils.isNotEmpty(dcMetadata.getId()))
        {
            dcElement.setAttribute(OPFAttributes.id, dcMetadata.getId());
        }
        if (StringUtils.isNotEmpty(dcMetadata.getLanguage()))
        {
            dcElement.setAttribute(OPFAttributes.lang, dcMetadata.getLanguage(), Namespace.XML_NAMESPACE);
        }
        metadataElement.addContent(dcElement);
    }

    /**
     * Writes out the complete list of Identifiers to the package document.
     * The first identifier for which the bookId is true is made the bookId identifier.
     * If no identifier has bookId == true then the first bookId identifier is written as the primary.
     */
    private void writeIdentifiers(Metadata metadata)
    {
        Identifier bookIdIdentifier = metadata.getBookIdIdentifier();
        if (bookIdIdentifier == null)
        {
            bookIdIdentifier = new Identifier();
            metadata.getIdentifiers().add(bookIdIdentifier);
        }

        Element identifierElement = new Element(DublinCoreTag.identifier.getName(), NAMESPACE_DUBLIN_CORE);
        identifierElement.setAttribute(DCAttributes.id, BOOK_ID_ID);
        identifierElement.setText(bookIdIdentifier.getValue());
        metadataElement.addContent(identifierElement);

        if(!bookIdIdentifier.getRefinements().isEmpty()) {
            for (MetadataProperty refinement : bookIdIdentifier.getRefinements())
            {
                writeMetaElement(refinement);
                alreadyWrittenMetaProperty.add(refinement);
            }
        }

        List<Identifier> identifiers = metadata.getEpub3Identifiers();
        for (Identifier identifier : identifiers)
        {
            if (identifier == bookIdIdentifier)
            {
                continue;
            }
            Element otherIdentifierElement = new Element(DublinCoreTag.identifier.getName(), NAMESPACE_DUBLIN_CORE);
            otherIdentifierElement.setText(identifier.getValue());
            metadataElement.addContent(otherIdentifierElement);

            if(!identifier.getRefinements().isEmpty()) {
                for (MetadataProperty refinement : identifier.getRefinements())
                {
                    writeMetaElement(refinement);
                    alreadyWrittenMetaProperty.add(refinement);
                }
            }
        }
    }
}
