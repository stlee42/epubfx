package de.machmireinebook.epubeditor.editor;

import java.io.IOException;
import java.io.InputStream;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:16
 */
public class XHTMLCodeEditor extends AbstractCodeEditor
{
    public static final Logger logger = Logger.getLogger(XHTMLCodeEditor.class);

    public interface TagInspector
    {
        boolean isTagFound(EditorToken token);
    }

    public static class BlockTagInspector implements TagInspector
    {
        public boolean isTagFound(EditorToken token)
        {
            String type = token.getType();
            if ("tag".equals(type))
            {
                String content = token.getContent();
                if ("h1".equals(content) || "h2".equals(content) || "h3".equals(content) || "h4".equals(content)
                        || "h5".equals(content) || "h6".equals(content) || "p".equals(content) || "div".equals(content))
                {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * a template for editing code - this can be changed to any template derived from the
     * supported modes at http://codemirror.net to allow syntax highlighted editing of
     * a wide variety of languages.
     */
    private static String editingTemplate;

    static
    {
        logger.info("reading template file for xhtml editor");
        InputStream is = XHTMLCodeEditor.class.getResourceAsStream("/modes/xhtml.html");
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
        }
    }

    /**
     * Create a new code editor.
     *
     * @param editingCode the initial code to be edited in the code editor.
     */
    public XHTMLCodeEditor()
    {
        super();
    }

    @Override
    public String getEditingTemplate()
    {
        return editingTemplate;
    }

    @Override
    public MediaType getMediaType()
    {
        return MediaType.XHTML;
    }

    public XMLTagPair findSurroundingTags(TagInspector inspector)
    {
        XMLTagPair pair;
        EditorPosition beginPos = getEditorCursorPosition();
        int lineLength = getLineLength(beginPos.getLine());
        EditorToken startToken;
        if (beginPos.getColumn() > 0 && beginPos.getColumn() < lineLength)
        {
            beginPos.setColumn(beginPos.getColumn());
            startToken = getTokenAt(beginPos);
            logger.info("start token " + startToken.getContent());
        }
        else
        {
            startToken = new EditorToken();
        }

        EditorPosition openTagBegin = new EditorPosition();
        EditorPosition openTagEnd = new EditorPosition();
        EditorPosition closeTagBegin = new EditorPosition();
        EditorPosition closeTagEnd = new EditorPosition();
        String tagName = "";

        if (startToken.getType() == null || (">".equals(startToken.getContent()) && "tag bracket".equals(startToken.getType())))
        {
            logger.info("cursor steht in normalem text token");
            //nach vorne suchen
            int currentLine = beginPos.getLine();
            EditorPosition currentPos = new EditorPosition(currentLine, startToken.getStart() - 1);
            EditorToken currentToken = getTokenAt(currentPos);

            boolean found = false;
            do  //�ffnendes Tag suchen
            {
                logger.debug("suchen �ffnendes Tag, currrent token " + currentToken.getContent());
                if (inspector.isTagFound(currentToken))
                {
                    //�ffnendes BlockTag gefunden
                    logger.info("�ffnendes BlockTag gefunden " + currentToken.getContent());
                    found = true;
                    tagName = currentToken.getContent();
                    openTagBegin = new EditorPosition(currentLine, currentToken.getStart());
                    openTagEnd = new EditorPosition(currentLine, currentToken.getEnd());
                }
                else
                {
                    if (currentToken.getStart() == 0 || currentToken.getStart() > currentPos.getColumn())
                    {
                        //an den Beginn der Zeile gekommen
                        currentPos = new EditorPosition(--currentLine, Integer.MAX_VALUE);
                        if (currentLine < 0)
                        {
                            return null;
                        }
                        currentToken = getTokenAt(currentPos);
                    }
                    else
                    {
                        currentPos = new EditorPosition(currentLine, currentToken.getStart() - 1);
                        currentToken = getTokenAt(currentPos);
                    }
                }
            }
            while (!found);

            //nach hinten suchen
            currentPos = new EditorPosition(currentLine, startToken.getEnd() + 1);
            currentToken = getTokenAt(currentPos);
            currentLine = beginPos.getLine();

            found = false;
            do  //schlie�endes Tag suchen
            {
                logger.debug("suchen schlie�endes Tag, currrent token " + currentToken.getContent());
                if (inspector.isTagFound(currentToken))
                {
                    //BlockTag gleichen Namens gefunden, ist das aber auch ein passendes schlie�endes
                    if (tagName.equals(currentToken.getContent()))
                    {
                        found = true;
                        if (isTokenClosingTag(currentLine, currentToken))
                        {
                            logger.info("schlie�endes Tag gefunden " + currentToken.getContent());
                            closeTagBegin = new EditorPosition(currentLine, currentToken.getStart());
                            closeTagEnd = new EditorPosition(currentLine, currentToken.getEnd());
                        }
                        else //�ffnendes BlockTag gefunden (d.h. ung�ltiges HTML, wir brechen ab und ersetzen nur das �ffnende Tag
                        {
                            logger.info("n�chstes �ffnende Tag gefunden " + currentToken.getContent() + ", breche ab");
                            closeTagBegin = null;
                            closeTagEnd = null;
                        }
                    }
                }
                else
                {
                    if (currentPos.getColumn() > currentToken.getEnd())
                    {
                        //an das Ende der Zeile gekommen
                        currentPos = new EditorPosition(++currentLine, -1);
                        currentToken = getTokenAt(currentPos);
                    }
                    else
                    {
                        currentPos = new EditorPosition(currentLine, currentToken.getEnd() + 1);
                        currentToken = getTokenAt(currentPos);
                    }
                }
            }
            while (!found);

        }
        pair = new XMLTagPair(openTagBegin, openTagEnd, closeTagBegin, closeTagEnd);
        pair.setTagName(tagName);
        return pair;
    }

    private boolean isTokenClosingTag(int line, EditorToken token)
    {
        EditorPosition pos = new EditorPosition(line, token.getStart() - 1);
        EditorToken tokenBefore = getTokenAt(pos);
        return "</".equals(tokenBefore.getContent()) && "tag bracket".equals(tokenBefore.getType());
    }
}
