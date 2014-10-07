package de.machmireinebook.epubeditor.epublib.epub3;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Resources;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentBase;

import org.apache.log4j.Logger;
import org.jdom2.Element;

/**
 * User: mjungierek
 * Date: 26.07.2014
 * Time: 17:11
 */
public class Epub3NavigatonDocumentReader extends PackageDocumentBase
{
    public static final Logger logger = Logger.getLogger(Epub3NavigatonDocumentReader.class);

    public static Resource read(Element packageRootElement, Resources resources)
    {
        Element manifestElement = packageRootElement.getChild(PackageDocumentBase.OPFTags.manifest, NAMESPACE_OPF);
        List<Element> manifestElements = manifestElement.getChildren("item", NAMESPACE_OPF);
        Resource resource = null;

        for (Element element : manifestElements)
        {
            if ("nav".equals(element.getAttributeValue("properties")))
            {
                String href = element.getAttributeValue("href");
                String id = element.getAttributeValue("id");
                String mediaTypeName = element.getAttributeValue(OPFAttributes.media_type);

                try
                {
                    href = URLDecoder.decode(href, Constants.CHARACTER_ENCODING);
                }
                catch (UnsupportedEncodingException e)
                {
                    //never happens
                }
                resource = resources.getByHref(href); //drinn lassen damit die im Manifest dann noch gefunden werden,
                // da es ja auch als ein normales HTML-Doc verwendet werden kann
                if(resource == null)
                {
                    logger.error("resource with href '" + href + "' not found");
                    continue;
                }
                resource.setId(id);
                MediaType mediaType = MediaType.getByName(mediaTypeName);
                if(mediaType != null)
                {
                    resource.setMediaType(mediaType);
                }
            }
        }
        return resource;
    }
}
