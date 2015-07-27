package de.machmireinebook.epubeditor.manager;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.inject.Named;

import de.machmireinebook.epubeditor.epublib.domain.Resource;

import com.sun.webkit.dom.ElementImpl;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

/**
 * User: mjungierek
 * Date: 22.07.2014
 * Time: 20:25
 */
@Named
public class PreviewManager
{
    public static final Logger logger = Logger.getLogger(PreviewManager.class);

    private WebView webview;
    private EditorTabManager editorManager;
    private boolean isVisible;

    public PreviewManager()
    {
        isVisible = true;
    }

    public void setWebview(WebView webview)
    {
        this.webview = webview;

        webview.getEngine().setOnError(event -> {
            logger.error(event.getMessage(), event.getException());
        });
        webview.getEngine().setOnAlert(event -> {
            logger.info(event.getData());
        });
    }

    public void setEditorManager(EditorTabManager editorManager)
    {
        editorManager.currentXHTMLResourceProperty().addListener((observable, oldValue, newValue) -> refreshWebView(newValue));
        editorManager.needsRefreshProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue)
            {
                logger.info("getting reload event from needs refresh property");
                webview.getEngine().reload();
            }
        });
        this.editorManager = editorManager;
    }

    private void refreshWebView(Resource resource)
    {
        if (isVisible)
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
                            if (evt.getTarget() instanceof ElementImpl)
                            {
                                Deque<ElementPosition> positions = new ArrayDeque<>();
                                ElementImpl currentElement = ((ElementImpl)evt.getTarget());
                                ElementImpl parent = (ElementImpl)currentElement.getParentElement();
                                while(parent != null)
                                {
                                    NodeList children;
                                    if (StringUtils.isNotEmpty(currentElement.getNamespaceURI()))
                                    {
                                        children = parent.getElementsByTagNameNS(currentElement.getNamespaceURI(), currentElement.getNodeName());
                                    }
                                    else
                                    {
                                        children = parent.getElementsByTagName(currentElement.getNodeName());
                                    }
                                    for (int i = 0; i < children.getLength(); i++)
                                    {
                                        if (children.item(i) == currentElement)
                                        {
                                            ElementPosition position = new ElementPosition(currentElement.getNodeName(), i, currentElement.getNamespaceURI());
                                            positions.push(position);
                                        }
                                    }
                                    currentElement = parent;
                                    parent = (ElementImpl)currentElement.getParentElement();
                                }
                                editorManager.scrollTo(positions);
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

    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    public void reset()
    {
        webview.getEngine().load("about:blank");
    }
}
