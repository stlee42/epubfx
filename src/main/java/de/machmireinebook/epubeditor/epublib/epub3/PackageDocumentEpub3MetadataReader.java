package de.machmireinebook.epubeditor.epublib.epub3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.machmireinebook.epubeditor.epublib.domain.Epub3MetadataProperty;
import de.machmireinebook.epubeditor.epublib.domain.Metadata;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;
import de.machmireinebook.epubeditor.jdom2.JDOM2Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Element;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 19:00
 */
public class PackageDocumentEpub3MetadataReader  extends PackageDocumentBase
{
    private static final Logger logger = Logger.getLogger(PackageDocumentEpub3MetadataReader.class);

    public static Metadata readMetadata(Element root)
    {
        Metadata result = new Metadata();
        Element metadataElement = root.getChild(OPFTags.metadata, NAMESPACE_OPF);
        if (metadataElement == null)
        {
            logger.error("Package does not contain element " + OPFTags.metadata);
            return result;
        }

        result.setTitles(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.title.getName()));
        result.setPublishers(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.publisher.getName()));
        result.setDescriptions(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.description.getName()));
        result.setRights(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.rights.getName()));
        result.setTypes(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.type.getName()));
        result.setSubjects(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, DCTag.subject.getName()));
/*        result.setIdentifiers(readIdentifiers(metadataElement));
        result.setAuthors(readCreators(metadataElement));
        result.setContributors(readContributors(metadataElement));
        result.setDates(readDates(metadataElement)); */
        result.setEpub3MetaProperties(readEpub3MetaProperties(metadataElement));
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
    private static List<Epub3MetadataProperty> readEpub3MetaProperties(Element metadataElement)
    {
        List<Epub3MetadataProperty> result = new ArrayList<>();

        List<Element> metaTags = metadataElement.getChildren(OPFTags.meta, NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            Epub3MetadataProperty otherMetadataElement = new Epub3MetadataProperty();
            String name = metaTag.getAttributeValue(OPFAttributes.property);
            if (StringUtils.isNotEmpty(name)) //ansonsten epub 2 metadaten, die wir uns merken ansonsten aber ignorieren
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

    private static Map<String, String> readEpub2MetaProperties(Element metadataElement)
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


}
