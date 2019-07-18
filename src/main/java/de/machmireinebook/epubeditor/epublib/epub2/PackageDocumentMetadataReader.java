package de.machmireinebook.epubeditor.epublib.epub2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.machmireinebook.epubeditor.epublib.domain.DublinCoreAttributes;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreTag;
import de.machmireinebook.epubeditor.epublib.domain.OPFAttribute;
import de.machmireinebook.epubeditor.epublib.domain.OPFTag;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Author;
import de.machmireinebook.epubeditor.epublib.domain.epub2.DublinCoreMetadataElement;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub2.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataProperty;

import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_DUBLIN_CORE;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_OPF;

/**
 * Reads the package document metadata.
 * <p>
 * In its own separate class because the PackageDocumentReader became a bit large and unwieldy.
 *
 * @author paul
 */
class PackageDocumentMetadataReader
{

    private static final Logger logger = Logger.getLogger(PackageDocumentMetadataReader.class);

    private Element metadataElement;

    public Metadata readMetadata(Element root)
    {
        Metadata result = new Metadata();
        metadataElement = root.getChild(OPFTag.metadata.name(), NAMESPACE_OPF);
        if (metadataElement == null)
        {
            logger.error("Package does not contain element " + OPFTag.metadata);
            return result;
        }

        result.setTitles(readTitles());
        result.setLanguages(readLanguages());
        result.setPublishers(readDublinCoreMetadata(DublinCoreTag.publisher));
        result.setDescriptions(readDublinCoreMetadata(DublinCoreTag.description));
        result.setFormats(readDublinCoreMetadata(DublinCoreTag.format));
        result.setRights(readDublinCoreMetadata(DublinCoreTag.rights));
        result.setTypes(readDublinCoreMetadata(DublinCoreTag.type));
        result.setSubjects(readDublinCoreMetadata(DublinCoreTag.subject));
        result.setSources(readDublinCoreMetadata(DublinCoreTag.source));
        result.setCoverages(readDublinCoreMetadata(DublinCoreTag.coverage));
        result.setRelations(readDublinCoreMetadata(DublinCoreTag.relation));
        result.setIdentifiers(readIdentifiers());
        result.setAuthors(readCreators());
        result.setContributors(readContributors());
        result.setDates(readDates());
        result.setEpub2MetaAttributes(readEpub2MetaProperties());

        return result;
    }

    private List<DublinCoreMetadataElement> readTitles()
    {
        List<Element> dcElements = metadataElement.getChildren(DublinCoreTag.title.getName(), NAMESPACE_DUBLIN_CORE);
        if (dcElements.isEmpty())
        {
            logger.info("Package does not contain element " + DublinCoreTag.title);
            return new ArrayList<>();
        }
        List<DublinCoreMetadataElement> result = new ArrayList<>(dcElements.size());
        for (Element dcElement : dcElements)
        {
            String titleValue = dcElement.getText();
            if (StringUtils.isEmpty(titleValue))
            {
                continue;
            }
            String idName = dcElement.getAttributeValue(DublinCoreAttributes.id.name());
            String language = dcElement.getAttributeValue(OPFAttribute.lang.getName(), Namespace.XML_NAMESPACE);
            DublinCoreMetadataElement dublinCoreMetadataElement = new DublinCoreMetadataElement(idName, titleValue, language);
            result.add(dublinCoreMetadataElement);
        }
        return result;
    }

    private List<DublinCoreMetadataElement> readDublinCoreMetadata(DublinCoreTag dcTagName)
    {
        List<Element> dcElements = metadataElement.getChildren(dcTagName.getName(), NAMESPACE_DUBLIN_CORE);
        if (dcElements.isEmpty())
        {
            logger.info("Package does not contain element " + dcTagName);
            return new ArrayList<>();
        }
        List<DublinCoreMetadataElement> result = new ArrayList<>(dcElements.size());
        for (Element dcElement : dcElements)
        {
            String value = dcElement.getText();
            if (StringUtils.isEmpty(value))
            {
                continue;
            }
            String idName = dcElement.getAttributeValue(DublinCoreAttributes.id.name());
            String language = dcElement.getAttributeValue(OPFAttribute.lang.getName(), Namespace.XML_NAMESPACE);
            DublinCoreMetadataElement dublinCoreMetadataElement = new DublinCoreMetadataElement(idName, value, language);
            result.add(dublinCoreMetadataElement);
        }
        return result;
    }


    private List<MetadataProperty> readOtherProperties()
    {
        List<MetadataProperty> result = new ArrayList<>();

        List<Element> metaTags = metadataElement.getChildren(OPFTag.meta.name(), NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            MetadataProperty otherMetadataElement = new MetadataProperty();
            String name = metaTag.getAttributeValue(OPFAttribute.property.getName());
            if (StringUtils.isNotEmpty(name)) //epub 3 metadata, read in, but ignore in epub 2 context
            {
                otherMetadataElement.setProperty(name);
                String value = metaTag.getText();
                otherMetadataElement.setValue(value);
                String refines = metaTag.getAttributeValue(OPFAttribute.refines.getName());
                otherMetadataElement.setRefines(refines);
                String scheme = metaTag.getAttributeValue(OPFAttribute.scheme.getName());
                otherMetadataElement.setScheme(scheme);

                result.add(otherMetadataElement);
            }
        }

        return result;
    }

    /**
     * consumes meta tags that have a property attribute as defined in the standard. For example:
     * &lt;meta property="rendition:layout"&gt;pre-paginated&lt;/meta&gt;
     *
     * @return
     */
    private Map<String, String> readEpub2MetaProperties()
    {
        Map<String, String> result = new HashMap<>();

        List<Element> metaTags = metadataElement.getChildren(OPFTag.meta.name(), NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            String name = metaTag.getAttributeValue(OPFAttribute.name_attribute.getName());
            String value = metaTag.getAttributeValue(OPFAttribute.content.getName());
            result.put(name, value);
        }

        return result;
    }

    private String getBookIdId(Element root)
    {
        String result = root.getAttributeValue(OPFAttribute.uniqueIdentifier.getName());
        return result;
    }

    private List<Author> readCreators()
    {
        return readAuthors(DublinCoreTag.creator.getName());
    }

    private List<Author> readContributors()
    {
        return readAuthors(DublinCoreTag.contributor.getName());
    }

    private List<Author> readAuthors(String authorTag)
    {
        List<Element> elements = metadataElement.getChildren(authorTag, NAMESPACE_DUBLIN_CORE);
        List<Author> result = new ArrayList<>(elements.size());
        for (Element authorElement : elements)
        {
            Author author = createAuthor(authorElement);
            if (author != null)
            {
                result.add(author);
            }
        }
        return result;

    }

    private List<MetadataDate> readDates()
    {
        List<Element> elements = metadataElement.getChildren(DublinCoreTag.date.getName(), NAMESPACE_DUBLIN_CORE);
        List<MetadataDate> result = new ArrayList<>(elements.size());
        for (Element dateElement : elements)
        {
            MetadataDate date;
            try
            {
                String event = dateElement.getAttributeValue(OPFAttribute.event.getName(), NAMESPACE_OPF);
                date = new MetadataDate(dateElement.getText(), event);
                if (date.getEvent() == MetadataDate.Event.UNKNOWN) {
                    // the value of event was not in our standard list
                    // specification says: "The set of values for event are not defined by this specification"
                    date.setUnknownEventValue(event);
                }
                result.add(date);
            }
            catch (IllegalArgumentException e)
            {
                logger.error(e.getMessage());
            }
        }
        return result;

    }

    private Author createAuthor(Element authorElement)
    {
        String authorString = authorElement.getText();
        if (StringUtils.isBlank(authorString))
        {
            return null;
        }
        Author result;
        result = new Author(null, authorString);
        result.setRole(authorElement.getAttributeValue(OPFAttribute.role.getName(), NAMESPACE_OPF));
        return result;
    }


    private List<Identifier> readIdentifiers()
    {
        List<Element> identifierElements = metadataElement.getChildren(DublinCoreTag.identifier.getName(), NAMESPACE_DUBLIN_CORE);
        if (identifierElements.isEmpty())
        {
            logger.error("Package does not contain element " + DublinCoreTag.identifier.getName());
            return new ArrayList<>();
        }
        String bookIdId = getBookIdId(metadataElement.getParentElement());
        List<Identifier> result = new ArrayList<>(identifierElements.size());
        if (StringUtils.isNotEmpty(bookIdId))
        {
            for (Element identifierElement : identifierElements)
            {
                String schemeName = identifierElement.getAttributeValue(DublinCoreAttributes.scheme.name(), NAMESPACE_OPF);
                String identifierValue = identifierElement.getText();
                if (StringUtils.isBlank(identifierValue))
                {
                    continue;
                }
                Identifier identifier = new Identifier(null, schemeName, identifierValue);
                if (bookIdId.equals(identifierElement.getAttributeValue(DublinCoreAttributes.id.name())))
                {
                    identifier.setBookId(true);
                }
                result.add(identifier);
            }
        }
        return result;
    }

    private List<DublinCoreMetadataElement> readLanguages()
    {
        List<Element> langElements = metadataElement.getChildren(DublinCoreTag.language.getName(), NAMESPACE_DUBLIN_CORE);
        if (langElements.isEmpty())
        {
            logger.error("Package does not contain element " + DublinCoreTag.language.getName());
            return new ArrayList<>();
        }
        List<DublinCoreMetadataElement> result = new ArrayList<>();
        for (Element langElement : langElements)
        {
            String langValue = langElement.getText();
            if (StringUtils.isBlank(langValue))
            {
                continue;
            }
            String idName = langElement.getAttributeValue(DublinCoreAttributes.id.name());
            DublinCoreMetadataElement dublinCoreMetadataElement = new DublinCoreMetadataElement(idName, langValue);
            result.add(dublinCoreMetadataElement);
        }
        return result;
    }
}
