package de.machmireinebook.epubeditor.epublib.domain;

import java.io.IOException;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReader;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 19:12
 */
public class CSSResource extends Resource<CascadingStyleSheet>
{
    private static final Logger logger = Logger.getLogger(CSSResource.class);
    
    public CSSResource()
    {
    }

    public CSSResource(String href)
    {
        super(href);
    }

    public CSSResource(byte[] data, String href)
    {
        super(data, href);
    }

    public CSSResource(String id, byte[] data, String href)
    {
        super(id, data, href, MediaType.CSS);
    }

    public CSSResource(byte[] data, String href, MediaType mediaType)
    {
        super(data, href, mediaType);
    }

    @Override
    public CascadingStyleSheet asNativeFormat()
    {
        CascadingStyleSheet css = null;
        try
        {
            String cssString = new String(getData(), getInputEncoding());
            css = CSSReader.readFromString(cssString, ECSSVersion.CSS30);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return css;
    }
}
