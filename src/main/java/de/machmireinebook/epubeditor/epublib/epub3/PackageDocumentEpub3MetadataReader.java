package de.machmireinebook.epubeditor.epublib.epub3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.machmireinebook.commons.jdom2.JDOM2Utils;
import de.machmireinebook.epubeditor.epublib.domain.Metadata;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;

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
    public static final Logger logger = Logger.getLogger(PackageDocumentEpub3MetadataReader.class);

    public static Metadata readMetadata(Element root)
    {
        Metadata result = new Metadata();
        Element metadataElement = root.getChild(PackageDocumentBase.OPFTags.metadata, NAMESPACE_OPF);
        if (metadataElement == null)
        {
            logger.error("Package does not contain element " + PackageDocumentBase.OPFTags.metadata);
            return result;
        }

        result.setTitles(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, PackageDocumentBase.DCTags.title));
        result.setPublishers(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, PackageDocumentBase.DCTags.publisher));
        result.setDescriptions(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, PackageDocumentBase.DCTags.description));
        result.setRights(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, PackageDocumentBase.DCTags.rights));
        result.setTypes(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, PackageDocumentBase.DCTags.type));
        result.setSubjects(JDOM2Utils.getChildrenText(metadataElement, NAMESPACE_DUBLIN_CORE, PackageDocumentBase.DCTags.subject));
/*        result.setIdentifiers(readIdentifiers(metadataElement));
        result.setAuthors(readCreators(metadataElement));
        result.setContributors(readContributors(metadataElement));
        result.setDates(readDates(metadataElement)); */
        result.setOtherProperties(readOtherProperties(metadataElement));
/*        result.setMetaAttributes(readMetaProperties(metadataElement));
        result.setLanguage(metadataElement.getChildText(PackageDocumentBase.DCTags.language, NAMESPACE_DUBLIN_CORE));*/

        return result;
    }

    /**
     * consumes meta tags that have a property attribute as defined in the standard. For example:
     * &lt;meta property="rendition:layout"&gt;pre-paginated&lt;/meta&gt;
     *
     * @param metadataElement
     * @return
     */
    private static Map<QName, String> readOtherProperties(Element metadataElement)
    {
        Map<QName, String> result = new HashMap<>();

        List<Element> metaTags = metadataElement.getChildren(OPFTags.meta, NAMESPACE_OPF);
        for (Element metaTag : metaTags)
        {
            String name = metaTag.getAttributeValue(OPFAttributes.property);
            String value = metaTag.getText();
            if (StringUtils.isNotEmpty(name))
            {
                result.put(new QName(name), value);
            }
        }

        return result;
    }

}
