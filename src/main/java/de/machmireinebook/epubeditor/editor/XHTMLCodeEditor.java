package de.machmireinebook.epubeditor.editor;

import org.apache.log4j.Logger;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 21.07.2014
 * Time: 21:16
 */
public class XHTMLCodeEditor extends AbstractCodeEditor
{
    private static final Logger logger = Logger.getLogger(XHTMLCodeEditor.class);

    @FunctionalInterface
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
 /*       logger.info("reading template file for xhtml editor");
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
        }*/
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

    public MediaType getMediaType()
    {
        return MediaType.XHTML;
    }

    public XMLTagPair findSurroundingTags(TagInspector inspector)
    {
        XMLTagPair pair = null;
        EditorPosition beginPos = getEditorCursorPosition();
        int lineLength = getLineLength(beginPos.getLine());
        EditorToken startToken;

        EditorPosition openTagBegin = new EditorPosition();
        EditorPosition openTagEnd = new EditorPosition();
        EditorPosition closeTagBegin = new EditorPosition();
        EditorPosition closeTagEnd = new EditorPosition();
        EditorPosition lastBracketBegin = new EditorPosition();
        String tagName = "";

        if (beginPos.getColumn() > 0 && beginPos.getColumn() < lineLength)
        {
            beginPos.setColumn(beginPos.getColumn());
            startToken = getTokenAt(beginPos);
            logger.info("start token " + startToken.getContent());
            if (">".equals(startToken.getContent()) && "tag bracket".equals(startToken.getType()))
            {
                lastBracketBegin = new EditorPosition(beginPos.getLine(), startToken.getStart());
            }
        }
        else
        {
            startToken = new EditorToken();
        }


        if (startToken.getType() == null || (">".equals(startToken.getContent()) && "tag bracket".equals(startToken.getType())))
        {
            int currentLine = beginPos.getLine();
            logger.info("cursor steht in normalem text token");
            //nach vorne suchen
            EditorPosition currentPos = new EditorPosition(currentLine, startToken.getStart());
            EditorToken currentToken = getTokenAt(currentPos);

            boolean found = false;
            do  //öffnendes Tag suchen
            {
                logger.debug("suchen öffnendes Tag, currrent token " + currentToken.getContent());
                if (">".equals(currentToken.getContent()) && "tag bracket".equals(currentToken.getType()))
                {
                    lastBracketBegin = new EditorPosition(currentLine, currentToken.getStart());
                }
                if (inspector.isTagFound(currentToken))
                {
                    //öffnendes BlockTag gefunden
                    logger.info("öffnendes BlockTag gefunden " + currentToken.getContent());
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
                        currentPos = new EditorPosition(currentLine, currentToken.getStart());
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
            do  //schließendes Tag suchen
            {
                logger.debug("suchen schließendes Tag, currrent token " + currentToken.getContent());
                if (inspector.isTagFound(currentToken))
                {
                    //BlockTag gleichen Namens gefunden, ist das aber auch ein passendes schließendes
                    if (tagName.equals(currentToken.getContent()))
                    {
                        found = true;
                        if (isTokenClosingTag(currentLine, currentToken))
                        {
                            logger.info("schließendes Tag gefunden " + currentToken.getContent());
                            closeTagBegin = new EditorPosition(currentLine, currentToken.getStart());
                            closeTagEnd = new EditorPosition(currentLine, currentToken.getEnd());
                        }
                        else //öffnendes BlockTag gefunden (d.h. ungültiges HTML, wir brechen ab und ersetzen nur das öffnende Tag
                        {
                            logger.info("nächstes öffnende Tag gefunden " + currentToken.getContent() + ", breche ab");
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
        //pair = new XMLTagPair(openTagBegin, openTagEnd, closeTagBegin, closeTagEnd, lastBracketBegin);
        //pair.setTagName(tagName);
        return pair;
    }

    private boolean isTokenClosingTag(int line, EditorToken token)
    {
        EditorPosition pos = new EditorPosition(line, token.getStart() - 1);
        EditorToken tokenBefore = getTokenAt(pos);
        return "</".equals(tokenBefore.getContent()) && "tag bracket".equals(tokenBefore.getType());
    }
}
