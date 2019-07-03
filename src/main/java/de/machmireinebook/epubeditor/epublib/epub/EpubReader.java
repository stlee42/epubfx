package de.machmireinebook.epubeditor.epublib.epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.bookprocessor.HtmlCleanerBookProcessor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Resources;
import de.machmireinebook.epubeditor.epublib.epub3.Epub3PackageDocumentReader;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Reads an epub file.
 *
 * @author paul
 */
public class EpubReader
{

    private static final Logger logger = Logger.getLogger(EpubReader.class);
    private BookProcessor bookProcessor = new HtmlCleanerBookProcessor();

    public Book readEpub(File file) throws IOException
    {
        Book book;
        try (FileInputStream in = new FileInputStream(file))
        {
            book = readEpub(in, Constants.CHARACTER_ENCODING);
            book.setPhysicalFileName(file.toPath());
        }
        return book;
    }

    public Book readEpub(ZipInputStream in) throws IOException
    {
        return readEpub(in, Constants.CHARACTER_ENCODING);
    }

    /**
     * Read epub from inputstream
     *
     * @param in       the inputstream from which to read the epub
     * @param encoding the encoding to use for the html files within the epub
     * @return the Book as read from the inputstream
     * @throws java.io.IOException
     */
    public Book readEpub(InputStream in, String encoding) throws IOException
    {
        return readEpub(new ZipInputStream(in), encoding);
    }


    public Book readEpub(ZipInputStream in, String encoding) throws IOException
    {
        return readEpub(ResourcesLoader.loadResources(in, encoding));
    }

    public Book readEpub(Resources resources)
    {
        return readEpub(resources, new Book());
    }

    public Book readEpub(Resources resources, Book book)
    {
        if (book == null)
        {
            book = new Book();
        }
        handleMimeType(resources);
        String packageResourceHref = getPackageResourceHref(resources);
        Resource packageResource = processPackageResource(packageResourceHref, book, resources);
        book.setOpfResource(packageResource);
        if (!book.isEpub3()) {
            Resource ncxResource = processNcxResource(book);
            book.setNcxResource(ncxResource);
        }
        book = postProcessBook(book);
        return book;
    }

    private Book postProcessBook(Book book)
    {
        if (bookProcessor != null)
        {
            book = bookProcessor.processBook(book);
        }
        return book;
    }

    private Resource processNcxResource(Book book)
    {
        return NCXDocument.read(book);
    }

    private Resource processPackageResource(String packageResourceHref, Book book, Resources resources)
    {
        Resource packageResource = resources.remove(packageResourceHref);
        try
        {
            Document packageDocument = ResourceUtil.getAsDocument(packageResource);
            Element root = packageDocument.getRootElement();
            EpubVersion version = EpubVersion.getByString(root.getAttributeValue("version"));
            book.setVersion(version);
            if (version.isEpub3()) {
                Epub3PackageDocumentReader.read(packageResource, packageDocument, book, resources);
            } else {
                PackageDocumentReader.read(packageResource, packageDocument, book, resources);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        return packageResource;
    }

    private String getPackageResourceHref(Resources resources)
    {
        String defaultResult = "OEBPS/content.opf";
        String result = defaultResult;

        Namespace ns = Namespace.getNamespace("urn:oasis:names:tc:opendocument:xmlns:container");

        Resource containerResource = resources.remove("META-INF/container.xml");
        if (containerResource == null)
        {
            return result;
        }
        try
        {
            Document document = ResourceUtil.getAsDocument(containerResource);
            Element rootFileElement = document.getRootElement().getChild("rootfiles", ns).getChild("rootfile", ns);
            result = rootFileElement.getAttributeValue("full-path");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        if (StringUtils.isBlank(result))
        {
            result = defaultResult;
        }
        return result;
    }

    private void handleMimeType(Resources resources)
    {
        resources.remove("mimetype");
    }
}
