package de.machmireinebook.epubeditor.editor;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:16
 */
public class XMLCodeEditor extends AbstractCodeEditor
{
    private static final Logger logger = Logger.getLogger(XMLCodeEditor.class);

    /**
     * a template for editing code - this can be changed to any template derived from the
     * supported modes at http://codemirror.net to allow syntax highlighted editing of
     * a wide variety of languages.
     */
    private static String editingTemplate;

    static
    {
/*        logger.info("reading template file for xml editor");
        InputStream is = XMLCodeEditor.class.getResourceAsStream("/modes/xml.html");
        try
        {
            editingTemplate = IOUtils.toString(is, "UTF-8");
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }*/
    }

    /**
     * Create a new code editor.
     *
     * @param editingCode the initial code to be edited in the code editor.
     */
    public XMLCodeEditor()
    {
        super();
    }

    @Override
    public String getEditingTemplate()
    {
        return editingTemplate;
    }

    public MediaType getMediaType()
    {
        return MediaType.XML;
    }
}
