package de.machmireinebook.epubeditor.jdom2;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.DocType;
import org.jdom2.Element;
import org.jdom2.EntityRef;
import org.jdom2.Namespace;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;
import org.jdom2.output.Format;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.Walker;
import org.jdom2.util.NamespaceStack;

/**
 * User: mjungierek
 * Date: 04.08.2014
 * Time: 19:17
 */
public class XHTMLOutputProcessor extends AbstractXMLOutputProcessor
{
    public static final Logger logger = Logger.getLogger(XHTMLOutputProcessor.class);

    private static final List<String> preserveElements = Arrays.asList("p", "h1", "h2", "h3", "h4", "h5", "h6", "th", "td", "a");
    private static final List<String> removeBreaksInsideTextElements = Arrays.asList("p", "h1", "h2", "h3", "h4", "h5", "h6", "th", "td", "a");
    private static final List<String> emptyLineAfterElements = Arrays.asList("p", "h1", "h2", "h3", "h4", "h5", "h6", "div", "blockquote", "table", "tr");


    protected void printElement(final Writer out, final FormatStack fstack,
                                final NamespaceStack nstack, final Element element) throws IOException
    {

        nstack.push(element);
        try
        {
            final List<Content> content = element.getContent();

            // Print the beginning of the tag plus attributes and any
            // necessary namespace declarations
            write(out, "<");

            write(out, element.getQualifiedName());

            // Print the element's namespace, if appropriate
            for (final Namespace ns : nstack.addedForward())
            {
                printNamespace(out, fstack, ns);
            }

            // Print out attributes
            if (element.hasAttributes())
            {
                for (final Attribute attribute : element.getAttributes())
                {
                    printAttribute(out, fstack, attribute);
                }
            }

            if (content.isEmpty())
            {
                // Case content is empty
                if (fstack.isExpandEmptyElements())
                {
                    write(out, "></");
                    write(out, element.getQualifiedName());
                    write(out, ">");
                }
                else
                {
                    write(out, " />");
                    if ("br".equals(element.getQualifiedName()))
                    {
                        write(out, "\n");
                    }
                }
                // nothing more to do.
                return;
            }

            // OK, we have real content to push.
            fstack.push();
            try
            {
                fstack.setEscapeOutput(false);
                // Check for xml:space and adjust format settings
                final String space = element.getAttributeValue("space",
                        Namespace.XML_NAMESPACE);

                if ("default".equals(space))
                {
                    fstack.setTextMode(fstack.getDefaultMode());
                }
                else if ("preserve".equals(space))
                {
                    fstack.setTextMode(Format.TextMode.PRESERVE);
                }

                if (preserveElements.contains(element.getQualifiedName()))
                {
                    fstack.setLevelEOL(null);
                    fstack.setLevelIndent(null);
                    fstack.setTextMode(Format.TextMode.PRESERVE);
                }

                // note we ensure the FStack is right before creating the walker
                Walker walker = buildWalker(fstack, content, true);

                if (!walker.hasNext())
                {
                    // the walker has formatted out whatever content we had
                    if (fstack.isExpandEmptyElements())
                    {
                        write(out, "></");
                        write(out, element.getQualifiedName());
                        write(out, ">");
                    }
                    else
                    {
                        write(out, " />");
                    }
                    // nothing more to do.
                    return;
                }
                // we have some content.
                write(out, ">");
                if (!walker.isAllText())
                {
                    // we need to newline/indent
                    textRaw(out, fstack.getPadBetween());
                }

                if (removeBreaksInsideTextElements.contains(element.getQualifiedName()))
                {
                    printContentRemoveBreaks(out, fstack, nstack, walker);
                }
                else
                {
                    printContent(out, fstack, nstack, walker);
                }

                if (!walker.isAllText())
                {
                    // we need to newline/indent
                    textRaw(out, fstack.getPadLast());
                }
                write(out, "</");
                write(out, element.getQualifiedName());
                write(out, ">");

                if (emptyLineAfterElements.contains(element.getQualifiedName()))
                {
                    boolean isLast = false;
                    Element parent = element.getParentElement();
                    if (parent != null)
                    {
                        List<Element> siblings = parent.getChildren();
                        if (siblings.get(siblings.size() - 1).equals(element))
                        {
                            isLast = true;
                        }
                    }
                    if (!isLast)
                    {
                        write(out, "\n");
                    }
                }
            }
            finally
            {
                fstack.pop();
            }
        }
        finally
        {
            nstack.pop();
        }
    }

    protected void printContentRemoveBreaks(final Writer out,
                                            final FormatStack fstack, final NamespaceStack nstack,
                                            final Walker walker)
            throws IOException
    {
        int indexInParent = 0;
        while (walker.hasNext())
        {
            Content c = walker.next();
            if (c == null)
            {
                // it is a text value of some sort.
                String t = walker.text();
                if (walker.isCDATA())
                {
                    textCDATA(out, t);
                }
                else
                {
                    t = t.trim(); //hier kann getrimmt werden, da der komplette Inhalt aus Text besteht und vorn und hinten keine Leerzeichen �brigbleiben sollen
                    textRawRemoveBreaks(out, t);
                }
            }
            else
            {
                switch (c.getCType())
                {
                    case CDATA:
                        printCDATA(out, fstack, (CDATA) c);
                        break;
                    case Comment:
                        printComment(out, fstack, (Comment) c);
                        break;
                    case DocType:
                        printDocType(out, fstack, (DocType) c);
                        break;
                    case Element:
                        printElement(out, fstack, nstack, (Element) c);
                        break;
                    case EntityRef:
                        printEntityRef(out, fstack, (EntityRef) c);
                        break;
                    case ProcessingInstruction:
                        printProcessingInstruction(out, fstack,
                                (ProcessingInstruction) c);
                        break;
                    case Text:
                        String text = ((Text) c).getText();
                        if (indexInParent == 0) //den ersten Text innerhalb des Elements vorne trimmen
                        {
                            text = StringUtils.stripStart(text, " ");
                        }
                        else if (!walker.hasNext()) //den letzten Text innerhalb des Elements hinten trimmen
                        {
                            text = StringUtils.stripEnd(text, " ");
                        }
                        printTextRemoveBreaks(out, fstack, text);
                        break;
                }
            }
            indexInParent++;
        }
    }

    protected void printTextRemoveBreaks(final Writer out, final FormatStack fstack,
                                         final String text) throws IOException
    {
        if (fstack.getEscapeOutput())
        {
            textRawRemoveBreaks(out, Format.escapeText(fstack.getEscapeStrategy(),
                    fstack.getLineSeparator(), text));

            return;
        }
        textRawRemoveBreaks(out, text);
    }


    protected void textRawRemoveBreaks(final Writer out, final String str) throws IOException
    {
        if (str == null)
        {
            return;
        }
        String replaced = str.replaceAll("\\s{2,}", " ");
        write(out, replaced);
    }

}