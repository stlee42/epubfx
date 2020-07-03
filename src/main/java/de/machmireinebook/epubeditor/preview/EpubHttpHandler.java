package de.machmireinebook.epubeditor.preview;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.resource.Resource;

/**
 * User: mjungierek
 * Date: 23.07.2014
 * Time: 20:25
 */
public class EpubHttpHandler implements HttpHandler
{
    public static final Logger logger = Logger.getLogger(EpubHttpHandler.class);
    private final ObjectProperty<Book> book = new SimpleObjectProperty<>();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        URI requestURI = httpExchange.getRequestURI();
        logger.info("getting request " + requestURI);

        String command = httpExchange.getRequestMethod();
        Headers headers = httpExchange.getResponseHeaders();
        if ("GET".equals(command) && book.get() != null)
        {
            String uri = requestURI.toString();
            uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
            uri = uri.replaceFirst("/", "");
            Resource<?> resource = book.get().getResources().getByHref(uri);
            if (resource != null)
            {
                MediaType mediaType = resource.getMediaType();
                headers.set("Content-Type", mediaType.getName() + "; charset=utf-8");
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK , 0);
                logger.info("return code 200");
                try (OutputStream out = httpExchange.getResponseBody()) {
                    out.write(resource.getWebViewPreparedData());
                } catch (IOException e) {
                    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
                }
            }
            else
            {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
                logger.info("return code 404");
            }
        }
        else
        {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
            logger.info("return code 404");
        }
    }

    public ObjectProperty<Book> bookProperty()
    {
        return book;
    }
}
