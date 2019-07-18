package de.machmireinebook.epubeditor.xhtml;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import org.jdom2.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import de.machmireinebook.epubeditor.httpserver.EpubHttpHandler;
import de.machmireinebook.epubeditor.httpserver.ResourceHttpHandler;

/**
 * @author Michail Jungierek, CGI
 */
public class XhtmlTest {

    public static final Logger logger = Logger.getLogger(XhtmlTest.class);

    @BeforeAll
    public static void setupAll()  throws Exception {
        Layout layout = new PatternLayout("%d{HH:mm:ss} %-5p %c %x - %m\n");
        Appender appender = new WriterAppender(layout, System.out);
        BasicConfigurator.configure(appender);
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("de.machmireinebook").setLevel(Level.DEBUG);

        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8777), 1000);
        EpubHttpHandler epubHttpHandler = new EpubHttpHandler();
        server.createContext("/", epubHttpHandler);

        ResourceHttpHandler resourceHttpHandler = new ResourceHttpHandler();
        server.createContext("/codemirror", resourceHttpHandler);
        server.createContext("/dtd", resourceHttpHandler);
        server.createContext("/modes", resourceHttpHandler);
        server.createContext("/images", resourceHttpHandler);

        server.start();
    }

    @Test
    public void escapingTestWithSaxParsing() throws Exception {
        String template = IOUtils.toString(getClass().getResourceAsStream("/test-escaping.xhtml"), "UTF-8");
        Document document =  XHTMLUtils.parseXHTMLDocument(template);
        String output = XHTMLUtils.outputXHTMLDocumentAsString(document, true);
        logger.info("output: \n" + output);
    }

    @Test
    public void escapingTestWithCleaning() throws Exception {
        String template = IOUtils.toString(getClass().getResourceAsStream("/test-escaping.xhtml"), "UTF-8");
        String output = XHTMLUtils.repair(template);
        logger.info("output: \n" + output);
    }

    @Test
    public void unescapeEscapingTest() throws Exception {
        String template = IOUtils.toString(getClass().getResourceAsStream("/test-unescape.xhtml"), "UTF-8");
        String unescaped = XHTMLUtils.unescapedHtmlWithXmlExceptions(template);
        logger.info("output: \n" + unescaped);
    }
}
