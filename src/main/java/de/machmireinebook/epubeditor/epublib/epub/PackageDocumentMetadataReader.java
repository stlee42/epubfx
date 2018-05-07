package de.machmireinebook.epubeditor.epublib.epub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.machmireinebook.epubeditor.epublib.domain.Author;
import de.machmireinebook.epubeditor.epublib.domain.Identifier;
import de.machmireinebook.epubeditor.epublib.domain.Metadata;
import de.machmireinebook.epubeditor.epublib.domain.MetadataDate;
import de.machmireinebook.epubeditor.epublib.domain.Epub3MetadataProperty;
import de.machmireinebook.epubeditor.jdom2.JDOM2Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Element;

/**
 * Reads the package document metadata.
 * <p>
 * In its own separate class because the PackageDocumentReader became a bit large and unwieldy.
 *
 * @author paul
 */
// package
class PackageDocumentMetadataReader extends PackageDocumentBase
{

    private static final Logger log = Logger.getLogger(PackageDocumentMetadataReader.class);

    public static Metadata readMetadata(Element root)
    {
        Metadata result = new Metadata();
        Element metadataElement = root.getChild(OPFTags.metadata, NAMESPACE_OPF);
        if (metadataElement == null)
        {
            log.error("Package does not contain element " + OPFTags.metadata);
            return result;
        }

        result.setTitles(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.title.getName()));
        result.setPublishers(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.publisher.getName()));
        result.setDescriptions(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.description.getName()));
        result.setRights(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.rights.getName()));
        result.setTypes(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.type.getName()));
        result.setSubjects(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.subject.getName()));
        result.setCoverages(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.coverage.getName()));
        result.setIdentifiers(readIdentifiers(metadataElement));
        result.setAuthors(readCreators(metadataElement));
        result.setContributors(readContributors(metadataElement));
        result.setDates(readDates(metadataElement));
        result.setEpub3MetaProperties(readOtherProperties(metadataElement));
        result.setEpub2MetaAttributes(readEpub2MetaProperties(metadataElement));
        result.setLanguage(metadataElement.getChildText(DCTag.language.getName(), NAMESPACE_DUBLIN_CORE));

        return result;
    }

    /**
     * consumes meta tags that have a property attribute as defined in the standard. For example:
     * &lt;meta property="rendition:layout"&gt;pre-paginated&lt;/meta&gt;
     *
     * @param metadataElement
     * @return
     */
    private static List<Epub3MetadataProperty> readOtherProperties(Element metadataElement)
    {
        List<Epub3MetadataProperty> result = new ArrayList<>();

        List<Element> metaTags = metadataElement.getChildren(OPFTags.meta, NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            Epub3MetadataProperty otherMetadataElement = new Epub3MetadataProperty();
            String name = metaTag.getAttributeValue(OPFAttributes.property);
            if (StringUtils.isNotEmpty(name)) //epub 3 metadata, read in, but ignore in epub 2 context
            {
                otherMetadataElement.setQName(new QName(name));
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
     * @param metadataElement
     * @return
     */
    private static Map<String, String> readEpub2MetaProperties(Element metadataElement)
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

    private static String getBookIdId(Element root)
    {
        String result = root.getAttributeValue(OPFAttributes.uniqueIdentifier);
        return result;
    }

    private static List<Author> readCreators(Element metadataElement)
    {
        return readAuthors(DCTag.creator.getName(), metadataElement);
    }

    private static List<Author> readContributors(Element metadataElement)
    {
        return readAuthors(DCTag.contributor.getName(), metadataElement);
    }

    private static List<Author> readAuthors(String authorTag, Element metadataElement)
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

    private static List<MetadataDate> readDates(Element metadataElement)
    {
        List<Element> elements = metadataElement.getChildren(DCTag.date.getName(), NAMESPACE_DUBLIN_CORE);
        List<MetadataDate> result = new ArrayList<>(elements.size());
        for (Element dateElement : elements)
        {
            MetadataDate date;
            try
            {
                date = new MetadataDate(dateElement.getText(), dateElement.getAttributeValue(OPFAttributes.event, NAMESPACE_OPF));
                result.add(date);
            }
            catch (IllegalArgumentException e)
            {
                log.error(e.getMessage());
            }
        }
        return result;

    }

    private static Author createAuthor(Element authorElement)
    {
        String authorString = authorElement.getText();
        if (StringUtils.isBlank(authorString))
        {
            return null;
        }
        Author result;
        result = new Author(authorString);
        result.setRole(authorElement.getAttributeValue(OPFAttributes.role, NAMESPACE_OPF));
        return result;
    }


    private static List<Identifier> readIdentifiers(Element metadataElement)
    {
        List<Element> identifierElements = metadataElement.getChildren(DCTag.identifier.getName(), NAMESPACE_DUBLIN_CORE);
        if (identifierElements.isEmpty())
        {
            log.error("Package does not contain element " + DCTag.identifier.getName());
            return new ArrayList<>();
        }
        String bookIdId = getBookIdId(metadataElement.getParentElement());
        List<Identifier> result = new ArrayList<>(identifierElements.size());
        if (StringUtils.isNotEmpty(bookIdId))
        {
            for (Element identifierElement : identifierElements)
            {
                String schemeName = identifierElement.getAttributeValue(DCAttributes.scheme, NAMESPACE_OPF);
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
}
