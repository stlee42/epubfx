package de.machmireinebook.epubeditor.epublib.resource;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReader;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 19:12
 */
public class CSSResource extends Resource<CascadingStyleSheet> implements TextResource
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
        String cssString = asString();
        return CSSReader.readFromString(cssString, ECSSVersion.CSS30);
    }

    @Override
    public String asString() {
        try {
            return new String(getData(), getInputEncoding());
        }
        catch (UnsupportedEncodingException e) {
            //should not happens
            return null;
        }
    }
}
