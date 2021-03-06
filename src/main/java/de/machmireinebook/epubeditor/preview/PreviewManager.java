package de.machmireinebook.epubeditor.preview;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import de.machmireinebook.epubeditor.EpubEditorConfiguration;
import de.machmireinebook.epubeditor.editor.EditorTabManager;
import de.machmireinebook.epubeditor.editor.ElementPosition;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.preferences.PreferencesManager;

/**
 * User: mjungierek
 * Date: 22.07.2014
 * Time: 20:25
 */
@Singleton
public class PreviewManager
{
    private static final Logger logger = Logger.getLogger(PreviewManager.class);

    private WebView webview;
    private boolean isVisible;

    @Inject
    private EditorTabManager editorManager;
    @Inject
    private PreferencesManager preferencesManager;

    public PreviewManager()
    {
        isVisible = true;
    }

    @PostConstruct
    public void init() {
        editorManager.currentXHTMLResourceProperty().addListener((observable, oldValue, newValue) -> refreshWebView(newValue));
        editorManager.needsRefreshProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue) {
                logger.info("getting reload event from needs refresh property");
                webview.getEngine().reload();
                editorManager.needsRefreshProperty().setValue(false);
            }
        });

        editorManager.currentLineProperty().addListener((observable, oldValue, newValue) -> {
            //webview.getEngine().executeScript("editor.indexFromPos({line:" + (to + 1) +",ch: 0});");
            logger.info("getting event line is changed, new line: " + newValue);
            scrollTo(newValue.intValue());
        });
    }

    public void setWebview(WebView webview)
    {
        this.webview = webview;
        webview.setContextMenuEnabled(true);
        webview.setZoom(preferencesManager.getPreviewZoom() / 100d);
        preferencesManager.previewZoomProperty().addListener((observable, oldValue, newValue) -> {
            webview.setZoom(preferencesManager.getPreviewZoom() / 100d);
        });

        WebEngine engine = webview.getEngine();
        engine.setOnError(event -> logger.error(event.getMessage(), event.getException()));
        engine.setOnAlert(event -> logger.info(event.getData()));
    }



    private void refreshWebView(Resource resource)
    {
        if (isVisible && resource != null)
        {
            WebEngine engine = webview.getEngine();
            engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
            {
                if (newValue.equals(Worker.State.SUCCEEDED))
                {
                    Document document = engine.getDocument();
                    Element documentElement = document.getDocumentElement();
                    if (documentElement != null)
                    {
                        ((EventTarget) documentElement).addEventListener("click", evt ->
                        {
                            if (evt.getTarget() instanceof Element)
                            {
                                Deque<ElementPosition> positions = new ArrayDeque<>();
                                Element currentElement = ((Element)evt.getTarget());
                                if (currentElement.getParentNode() instanceof Element) {
                                    Element parent = (Element) currentElement.getParentNode();
                                    while (parent != null) {
                                        NodeList children;
                                        if (StringUtils.isNotEmpty(currentElement.getNamespaceURI())) {
                                            children = parent.getElementsByTagNameNS(currentElement.getNamespaceURI(), currentElement.getNodeName());
                                        } else {
                                            children = parent.getElementsByTagName(currentElement.getNodeName());
                                        }
                                        for (int i = 0; i < children.getLength(); i++) {
                                            if (children.item(i) == currentElement) {
                                                ElementPosition position = new ElementPosition(currentElement.getNodeName(), i, currentElement.getNamespaceURI());
                                                positions.push(position);
                                            }
                                        }
                                        currentElement = parent;
                                        if (currentElement.getParentNode() instanceof Element) {
                                            parent = (Element) currentElement.getParentNode();
                                        } else {
                                            parent = null;
                                        }
                                    }
                                    editorManager.scrollTo(positions);
                                }
                            }
                            else
                            {
                                logger.info("clicked class " + evt.getTarget());
                            }

                        }, false);
                    }
                }
            });
            engine.load("http://localhost:8777/" + resource.getHref());
        }
    }

    public void scrollTo(int line) {
        logger.info("scrolling to line " + line);
        if (line > 0) {
            String script = "var list = document.getElementsByClassName(\"" +
                    EpubEditorConfiguration.LOCATION_CLASS_PREFIX + line + "\"); if (list[0]){list[0].scrollIntoView({ left: 0, block: 'start', behavior: 'smooth' })}";
            logger.info("script to execute: " + script);
            webview.getEngine().executeScript(script);
        }
    }


    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    public void reset()
    {
        webview.getEngine().load("about:blank");
    }

    public void changePreviewWidth(double width) {

    }
}
