package de.machmireinebook.epubeditor.xhtml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.EpubVersion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


public class XhtmlFileSplitter {
    private static final Logger logger = Logger.getLogger(XhtmlFileSplitter.class);

    private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENTOPEN>(<\\h*)(\\w+:?\\w*)([^<>]*)(\\h*/?>))" +
            "|(?<ELEMENTCLOSE>(</?\\h*)(\\w+:?\\w*)([^<>]*)(\\h*>))" +
            "|(?<ENTITY>(&(.*?);))" +
            "|(?<COMMENT><!--[^<>]+-->)");
    private static final int GROUP_ELEMENT_NAME = 3;
    private static final int GROUP_ATTRIBUTES_SECTION = 4;

    private List<CompletionElement> completions = new ArrayList<>();
    private EpubVersion epubVersion;

    public XhtmlFileSplitter(EpubVersion epubVersion) {
        this.epubVersion = epubVersion;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class CompletionElement {
        private String elementName;
        private String attributes;
    }

    public String completeFrontPart(String frontPart) {
        completions.clear();
        Optional<CompletionElement> currentCompletionOptional = getNextCompletion(frontPart);
        while (currentCompletionOptional.isPresent()) {
            frontPart = frontPart + "</" + currentCompletionOptional.get().getElementName() + ">";
            completions.add(currentCompletionOptional.get());
            currentCompletionOptional = getNextCompletion(frontPart);
        }
        String frontPartFormatted = "";
        try {
            Document jdomDocument = XHTMLUtils.parseXHTMLDocument(frontPart);
            frontPartFormatted = XHTMLUtils.outputXHTMLDocumentAsString(jdomDocument, true, epubVersion);
        }
        catch (JDOMException | IOException e) {
            logger.error("", e);
        }
        return frontPartFormatted;
    }

    private Optional<CompletionElement> getNextCompletion(String text) {
        Matcher matcher = XML_TAG.matcher(text);
        Stack<CompletionElement> openTagsStack = new Stack<>();
        while (matcher.find()) {
            if(matcher.group("ELEMENTOPEN") != null && !matcher.group(GROUP_ATTRIBUTES_SECTION).endsWith("/")) {
                String elementOpen = matcher.group(GROUP_ELEMENT_NAME);
                String attributes = matcher.group(GROUP_ATTRIBUTES_SECTION);
                openTagsStack.push(new CompletionElement(elementOpen, attributes));
            } else if(matcher.group("ELEMENTCLOSE") != null) {
                openTagsStack.pop();
            }
        }
        if (!openTagsStack.empty()) {
            return Optional.of(openTagsStack.pop());
        } else {
            return Optional.empty();
        }
    }

    public String completeBackPart(String backPart,  List<Content> originalHeadContent) {
        StringBuilder dataBuilder = new StringBuilder(backPart);
        for (CompletionElement completion : completions) {
            dataBuilder.insert(0, "\n<" + completion.getElementName() + " " + completion.getAttributes() + ">");
        }
        String backPartCompleted = dataBuilder.toString();
        try {
            try {
                Document jdomDocument = XHTMLUtils.parseXHTMLDocument(backPartCompleted);
                Element root = jdomDocument.getRootElement();

                Element headElement = new Element("head");
                root.addContent(0, headElement);
                for (Content content : originalHeadContent)
                {
                    headElement.addContent(content);
                }

                root.setNamespace(Constants.NAMESPACE_XHTML);
                root.addNamespaceDeclaration(Constants.NAMESPACE_XHTML);
                root.addNamespaceDeclaration(Constants.NAMESPACE_EPUB);
                IteratorIterable<Element> elements = root.getDescendants(Filters.element());
                for (Element element : elements)
                {
                    if (element.getNamespace() == null || element.getNamespace() == Namespace.NO_NAMESPACE) //kein oder der leere NS zum XHTML namespace machen
                    {
                        element.setNamespace(Constants.NAMESPACE_XHTML);
                    }
                }
                if (epubVersion.isEpub2()) {
                    jdomDocument.setDocType(Constants.DOCTYPE_XHTML.clone());
                } else {
                    jdomDocument.setDocType(Constants.DOCTYPE_HTML.clone());
                }
                backPartCompleted = XHTMLUtils.outputXHTMLDocumentAsString(jdomDocument, true, epubVersion);
            }
            catch (JDOMException e) {
                logger.error("", e);
            }
        }
        catch (IOException e) {
            logger.error("", e);
            throw new XhtmlOutputException(e.getMessage());
        }

        return backPartCompleted;
    }

}
