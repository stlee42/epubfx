package de.machmireinebook.epubeditor.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;

/**
 * User: mjungierek
 * Date: 24.07.2014
 * Time: 20:19
 */
public class ResourceHttpHandler implements HttpHandler
{
    public static final Logger logger = Logger.getLogger(ResourceHttpHandler.class);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        URI requestURI = httpExchange.getRequestURI();
        logger.info("getting request " + requestURI);

        String command = httpExchange.getRequestMethod();
        httpExchange.getRequestHeaders();
        InputStream body = httpExchange.getRequestBody();
        body.close();
        Headers headers = httpExchange.getResponseHeaders();
        if ("GET".equals(command))
        {
            InputStream is = ResourceHttpHandler.class.getResourceAsStream(requestURI.toString());
            if (is != null)
            {
                httpExchange.sendResponseHeaders(200, 0);
                logger.info("return code 200");
                OutputStream out = httpExchange.getResponseBody();
                IOUtils.copy(is, out);
                out.close();
                is.close();
            }
            else
            {
                logger.info("return code 404");
                httpExchange.sendResponseHeaders(404, -1);
            }
        }
        else
        {
            logger.info("return code 404");
            httpExchange.sendResponseHeaders(404, -1);
        }
    }
}
