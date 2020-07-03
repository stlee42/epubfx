package de.machmireinebook.epubeditor.preview;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * User: mjungierek
 * Date: 24.07.2014
 * Time: 20:19
 */
public class ResourceHttpHandler implements HttpHandler
{
    private static final Logger logger = Logger.getLogger(ResourceHttpHandler.class);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        URI requestURI = httpExchange.getRequestURI();
        logger.info("getting request " + requestURI);

        String command = httpExchange.getRequestMethod();
        InputStream body = httpExchange.getRequestBody();
        body.close();
        if ("GET".equals(command))
        {
            try (InputStream is = ResourceHttpHandler.class.getResourceAsStream(requestURI.toString()); OutputStream out = httpExchange.getResponseBody();) {
                if (is != null) {
                    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    logger.info("return code 200");
                    IOUtils.copy(is, out);
                } else {
                    logger.info("return code 404");
                    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
                }
            } catch (IOException e) {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
            }
        }
        else
        {
            logger.info("return code 404");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
        }
    }
}
