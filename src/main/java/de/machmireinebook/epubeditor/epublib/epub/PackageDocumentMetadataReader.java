package de.machmireinebook.epubeditor.epublib.epub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Element;

import de.machmireinebook.epubeditor.epublib.domain.DublinCoreAttributes;
import de.machmireinebook.epubeditor.epublib.domain.DublinCoreTag;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Author;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.epub2.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.epub2.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.epub3.MetadataProperty;
import de.machmireinebook.epubeditor.jdom2.JDOM2Utils;

import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_DUBLIN_CORE;
import static de.machmireinebook.epubeditor.epublib.Constants.NAMESPACE_OPF;

/**
 * Reads the package document metadata.
 * <p>
 * In its own separate class because the PackageDocumentReader became a bit large and unwieldy.
 *
 * @author paul
 */
class PackageDocumentMetadataReader extends PackageDocumentBase
{

    private static final Logger logger = Logger.getLogger(PackageDocumentMetadataReader.class);

    private Element metadataElement;

    public Metadata readMetadata(Element root)
    {
        Metadata result = new Metadata();
        metadataElement = root.getChild(OPFTags.metadata, NAMESPACE_OPF);
        if (metadataElement == null)
        {
            logger.error("Package does not contain element " + OPFTags.metadata);
            return result;
        }

        result.setTitles(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.title.getName()));
        result.setPublishers(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.publisher.getName()));
        result.setDescriptions(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.description.getName()));
        result.setRights(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.rights.getName()));
        result.setTypes(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.type.getName()));
        result.setSubjects(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.subject.getName()));
        result.setCoverages(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DublinCoreTag.coverage.getName()));
        result.setIdentifiers(readIdentifiers());
        result.setAuthors(readCreators());
        result.setContributors(readContributors());
        result.setDates(readDates());
        result.setEpub2MetaAttributes(readEpub2MetaProperties());
        result.setLanguages(readLanguages());

        return result;
    }

    private List<MetadataProperty> readOtherProperties()
    {
        List<MetadataProperty> result = new ArrayList<>();

        List<Element> metaTags = metadataElement.getChildren(OPFTags.meta, NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            MetadataProperty otherMetadataElement = new MetadataProperty();
            String name = metaTag.getAttributeValue(OPFAttributes.property);
            if (StringUtils.isNotEmpty(name)) //epub 3 metadata, read in, but ignore in epub 2 context
            {
                otherMetadataElement.setProperty(name);
                String value = metaTag.getText();
                otherMetadataElement.setValue(value);
                String refines = metaTag.getAttributeValue(OPFAttributes.refines);
                otherMetadataElement.setRefines(refines);
                String scheme = metaTag.getAttributeValue(OPFAttributes.scheme);
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

        List<Element> metaTags = metadataElement.getChildren(OPFTags.meta, NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            String name = metaTag.getAttributeValue(OPFAttributes.name);
            String value = metaTag.getAttributeValue(OPFAttributes.content);
            result.put(name, value);
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
                String event = dateElement.getAttributeValue(OPFAttributes.event, NAMESPACE_OPF);
                date = new MetadataDate(dateElement.getText(), event);
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
        result = new Author(null, null, authorString, null);
        result.setRole(authorElement.getAttributeValue(OPFAttributes.role, NAMESPACE_OPF));
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
                if (bookIdId.equals(identifierElement.getAttributeValue("id")))
                {
                    identifier.setBookId(true);
                }
                result.add(identifier);
            }
        }
        return result;
    }

    private List<String> readLanguages()
    {
        List<Element> langElements = metadataElement.getChildren(DublinCoreTag.language.getName(), NAMESPACE_DUBLIN_CORE);
        if (langElements.isEmpty())
        {
            logger.error("Package does not contain element " + DublinCoreTag.language.getName());
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (Element langElement : langElements)
        {
            String langValue = langElement.getText();
            if (StringUtils.isBlank(langValue))
            {
                continue;
            }
            result.add(langValue);
        }
        return result;
    }
}
