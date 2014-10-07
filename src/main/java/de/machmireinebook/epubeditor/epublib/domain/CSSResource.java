package de.machmireinebook.epubeditor.epublib.domain;

import java.io.IOException;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReader;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 19:12
 */
public class CSSResource extends Resource<CascadingStyleSheet>
{
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
    public CascadingStyleSheet getAsNativeFormat()
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
