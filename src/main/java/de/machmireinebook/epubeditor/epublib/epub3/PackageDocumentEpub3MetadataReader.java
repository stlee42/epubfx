package de.machmireinebook.epubeditor.epublib.epub3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Element;
import org.jdom2.Namespace;

import de.machmireinebook.epubeditor.epublib.domain.DublinCoreTag;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Author;
import de.machmireinebook.epubeditor.epublib.domain.epub3.DublinCoreMetadataElement;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.epub3.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataProperty;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataPropertyValue;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;
import de.machmireinebook.epubeditor.jdom2.JDOM2Utils;

import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_DUBLIN_CORE;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_OPF;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 19:00
 */
public class PackageDocumentEpub3MetadataReader extends PackageDocumentBase
{
    private static final Logger logger = Logger.getLogger(PackageDocumentEpub3MetadataReader.class);

    private Element metadataElement;
    private Map<String, DublinCoreMetadataElement> refinableElements = new HashMap<>();

    public Metadata readMetadata(Element root)
    {
        Metadata result = new Metadata();
        metadataElement = root.getChild(OPFTags.metadata, NAMESPACE_OPF);
        if (metadataElement == null)
        {
            logger.error("Package does not contain element " + OPFTags.metadata);
            return result;
        }

        result.setTitles(readTitles());
        result.setPublishers(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.publisher.getName()));
        result.setDescriptions(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.description.getName()));
        result.setRights(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.rights.getName()));
        String sourceValue = metadataElement.getChildText(DublinCoreTag.source.getName(), NAMESPACE_DUBLIN_CORE);
        if (StringUtils.isNotEmpty(sourceValue))
        {
            result.setSource(new DublinCoreMetadataElement(sourceValue));
        }
        result.setTypes(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.type.getName()));
        result.setSubjects(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.subject.getName()));
        result.setIdentifiers(readIdentifiers());
        result.setAuthors(readCreators());
        result.setContributors(readContributors());
        result.setPublicationDate(readPublicationDate());
        result.setLanguages(readLanguages());

        result.setEpub3MetaProperties(readEpub3MetaProperties());
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

        List<Element> metaTags = metadataElement.getChildren(OPFTags.meta, NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            MetadataProperty otherMetadataElement = new MetadataProperty();
            String property = metaTag.getAttributeValue(OPFAttributes.property);
            if (StringUtils.isNotEmpty(property)) //if property not set it's a epub 2 metadata, read it later
            {
                boolean putInList = true;
                otherMetadataElement.setProperty(property);

                String value = metaTag.getText();
                otherMetadataElement.setValue(value);

                String id = metaTag.getAttributeValue(OPFAttributes.id);
                otherMetadataElement.setId(id);
                String language = metaTag.getAttributeValue(OPFAttributes.lang, Namespace.XML_NAMESPACE);
                otherMetadataElement.setLanguage(language);
                String refines = metaTag.getAttributeValue(OPFAttributes.refines);
                otherMetadataElement.setRefines(refines);
                String scheme = metaTag.getAttributeValue(OPFAttributes.scheme);
                otherMetadataElement.setScheme(scheme);

                if (StringUtils.isNotEmpty(refines)) {
                    DublinCoreMetadataElement dcElement = refinableElements.get(refines.substring(1));
                    if (dcElement != null) {
                        if (dcElement instanceof Author) {
                            Author author = (Author) dcElement;
                            if (property.equals(MetadataPropertyValue.role.getSpecificationName())) {
                                author.setRole(otherMetadataElement);
                                putInList = false;
                            }
                            else if (property.equals(MetadataPropertyValue.file_as.getSpecificationName())) {
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

        List<Element> metaTags = metadataElement.getChildren(OPFTags.meta, NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            if (metaTag.getAttribute(OPFAttributes.property) != null)
            {
                continue; //is epub 3 metadata, will be read in another method
            }

            String name = metaTag.getAttributeValue(OPFAttributes.name);
            String value = metaTag.getAttributeValue(OPFAttributes.content);
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
                String idName = identifierElement.getAttributeValue(DCAttributes.id);
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
        String result = root.getAttributeValue(OPFAttributes.uniqueIdentifier);
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
            String idName = authorElement.getAttributeValue(DCAttributes.id);
            String language = authorElement.getAttributeValue(OPFAttributes.lang, Namespace.XML_NAMESPACE);
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
        return readDublinCoreMetadata(DublinCoreTag.title.getName());
    }

    private List<DublinCoreMetadataElement> readDublinCoreMetadata(String dcTagName)
    {
        List<Element> dcElements = metadataElement.getChildren(dcTagName, NAMESPACE_DUBLIN_CORE);
        if (dcElements.isEmpty())
        {
            logger.info("Package does not contain element " + dcTagName);
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
            String idName = dcElement.getAttributeValue(DCAttributes.id);
            String language = dcElement.getAttributeValue(OPFAttributes.lang, Namespace.XML_NAMESPACE);
            DublinCoreMetadataElement dublinCoreMetadataElement = new DublinCoreMetadataElement(idName, titleValue, language);
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
        String idName = dcDateElement.getAttributeValue(DCAttributes.id);
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
            String idName = langElement.getAttributeValue(DCAttributes.id);
            DublinCoreMetadataElement language = new DublinCoreMetadataElement(idName, langValue);
            result.add(language);
        }
        return result;
    }
}
