package de.machmireinebook.epubeditor.epublib.epub3;

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
import de.machmireinebook.epubeditor.epublib.domain.epub3.Author;
import de.machmireinebook.epubeditor.epublib.domain.epub3.DublinCoreMetadataElement;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataProperty;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataPropertyValue;
import de.machmireinebook.epubeditor.epublib.domain.epub3.OpfDirAttribute;

import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_DUBLIN_CORE;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_OPF;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 19:00
 */
public class PackageDocumentEpub3MetadataReader
{
    private static final Logger logger = Logger.getLogger(PackageDocumentEpub3MetadataReader.class);

    private Element metadataElement;
    private Map<String, DublinCoreMetadataElement> refinableElements = new HashMap<>();

    public Metadata readMetadata(Element root)
    {
        Metadata result = new Metadata();
        metadataElement = root.getChild(OPFTag.metadata.getName(), NAMESPACE_OPF);
        if (metadataElement == null)
        {
            logger.error("Package does not contain element " + OPFTag.metadata);
            return result;
        }

        result.setTitles(readTitles());
        result.setPublishers(readDublinCoreMetadata(DublinCoreTag.publisher));
        result.setDescriptions(readDublinCoreMetadata(DublinCoreTag.description));
        result.setRights(readDublinCoreMetadata(DublinCoreTag.rights));
        result.setCoverages(readDublinCoreMetadata(DublinCoreTag.coverage));
        result.setSources(readDublinCoreMetadata(DublinCoreTag.source));
        result.setTypes(readDublinCoreMetadata(DublinCoreTag.type));
        result.setFormats(readDublinCoreMetadata(DublinCoreTag.format));
        result.setSubjects(readDublinCoreMetadata(DublinCoreTag.subject));
        result.setRelations(readDublinCoreMetadata(DublinCoreTag.relation));
        result.setIdentifiers(readIdentifiers());
        result.setAuthors(readCreators());
        result.setContributors(readContributors());
        result.setPublicationDate(readPublicationDate());
        result.setLanguages(readLanguages());

        List<MetadataProperty> metadataProperties = readEpub3MetaProperties();
        //searching for modification date
        metadataProperties.stream()
                .filter(metadataProperty -> MetadataPropertyValue.dcterms_modified.getName().equals(metadataProperty.getProperty()))
                .findFirst()
                .ifPresent(metadataProperty -> {
                    result.setModificationDate(metadataProperty);
                    metadataProperties.remove(metadataProperty);});
        result.setEpub3MetaProperties(metadataProperties);
        result.setEpub2MetaAttributes(readEpub2MetaProperties());

        return result;
    }

    /**
     * consumes meta tags that have a property attribute as defined in the standard. For example:
     * &lt;meta property="rendition:layout"&gt;pre-paginated&lt;/meta&gt;
     */
    private List<MetadataProperty> readEpub3MetaProperties()
    {
        List<MetadataProperty> result = new ArrayList<>();

        List<Element> metaTags = metadataElement.getChildren(OPFTag.meta.getName(), NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            MetadataProperty otherMetadataElement = new MetadataProperty();
            String property = metaTag.getAttributeValue(OPFAttribute.property.getName());
            if (StringUtils.isNotEmpty(property)) //if property not set it's a epub 2 metadata, read it later
            {
                boolean putInList = true;
                otherMetadataElement.setProperty(property);

                String value = metaTag.getText();
                otherMetadataElement.setValue(value);

                String id = metaTag.getAttributeValue(OPFAttribute.id.getName());
                otherMetadataElement.setId(id);
                String language = metaTag.getAttributeValue(OPFAttribute.lang.getName(), Namespace.XML_NAMESPACE);
                otherMetadataElement.setLanguage(language);
                String refines = metaTag.getAttributeValue(OPFAttribute.refines.getName());
                otherMetadataElement.setRefines(refines);
                String scheme = metaTag.getAttributeValue(OPFAttribute.scheme.getName());
                otherMetadataElement.setScheme(scheme);

                if (StringUtils.isNotEmpty(refines)) {
                    DublinCoreMetadataElement dcElement = refinableElements.get(refines.substring(1));
                    if (dcElement != null) {
                        if (dcElement instanceof Author) {
                            Author author = (Author) dcElement;
                            if (property.equals(MetadataPropertyValue.role.getName())) {
                                author.setRole(otherMetadataElement);
                                putInList = false;
                            }
                            else if (property.equals(MetadataPropertyValue.file_as.getName())) {
                                author.setFileAs(otherMetadataElement);
                                putInList = false;
                            }
                        }
                        if (putInList) {
                            dcElement.getRefinements().add(otherMetadataElement);
                            putInList = false;
                        }
                    }
                }
                if (putInList) {
                    result.add(otherMetadataElement);
                }
            }
        }

        return result;
    }

    private Map<String, String> readEpub2MetaProperties()
    {
        Map<String, String> result = new HashMap<>();

        List<Element> metaTags = metadataElement.getChildren(OPFTag.meta.getName(), NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            if (metaTag.getAttribute(OPFAttribute.property.getName()) != null)
            {
                continue; //is epub 3 metadata, will be read in another method
            }

            String name = metaTag.getAttributeValue(OPFAttribute.name_attribute.getName());
            String value = metaTag.getAttributeValue(OPFAttribute.content.getName());
            result.put(name, value);
        }

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
                String identifierValue = identifierElement.getText();
                if (StringUtils.isBlank(identifierValue))
                {
                    continue;
                }
                String idName = identifierElement.getAttributeValue(DublinCoreAttributes.id.name());
                Identifier identifier = new Identifier(idName, identifierValue);
                if (bookIdId.equals(idName))
                {
                    identifier.setBookId(true);
                }
                if (StringUtils.isNotEmpty(idName)) {
                    refinableElements.put(idName, identifier);
                }
                result.add(identifier);
            }
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
        List<Element> authorsElements = metadataElement.getChildren(authorTag, NAMESPACE_DUBLIN_CORE);
        if (authorsElements.isEmpty())
        {
            logger.info("Package does not contain element " + authorTag);
            return new ArrayList<>();
        }
        List<Author> result = new ArrayList<>(authorsElements.size());
        for (Element authorElement : authorsElements)
        {
            String authorValue = authorElement.getText();
            if (StringUtils.isEmpty(authorValue))
            {
                continue;
            }
            String idName = authorElement.getAttributeValue(DublinCoreAttributes.id.name());
            String language = authorElement.getAttributeValue(OPFAttribute.lang.getName(), Namespace.XML_NAMESPACE);
            Author author = new Author(idName, authorValue, language);
            if (StringUtils.isNotEmpty(idName)) {
                refinableElements.put(idName, author);
            }
            result.add(author);
        }
        return result;
    }

    private List<DublinCoreMetadataElement> readTitles()
    {
        return readDublinCoreMetadata(DublinCoreTag.title);
    }

    private List<DublinCoreMetadataElement> readDublinCoreMetadata(DublinCoreTag dcTag)
    {
        List<Element> dcElements = metadataElement.getChildren(dcTag.getName(), NAMESPACE_DUBLIN_CORE);
        if (dcElements.isEmpty())
        {
            logger.info("Package does not contain element " + dcTag);
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
            String dirValue = dcElement.getAttributeValue(OpfDirAttribute.attributeName);
            if (StringUtils.isNotEmpty(dirValue)) {
                dublinCoreMetadataElement.setDir(OpfDirAttribute.valueOf(dirValue));
            }
            if (StringUtils.isNotEmpty(idName)) {
                refinableElements.put(idName, dublinCoreMetadataElement);
            }
            result.add(dublinCoreMetadataElement);
        }
        return result;
    }

    private MetadataDate readPublicationDate()
    {
        Element dcDateElement = metadataElement.getChild(DublinCoreTag.date.getName(), NAMESPACE_DUBLIN_CORE);
        if (dcDateElement == null)
        {
            logger.info("Package does not contain element " + DublinCoreTag.date.getName());
            return null;
        }
        String value = dcDateElement.getText();
        String idName = dcDateElement.getAttributeValue(DublinCoreAttributes.id.name());
        return new MetadataDate(idName, value);
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
            DublinCoreMetadataElement language = new DublinCoreMetadataElement(idName, langValue);
            result.add(language);
        }
        return result;
    }
}
