package de.machmireinebook.epubeditor.epublib.epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.bookprocessor.HtmlCleanerBookProcessor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Resources;
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

    private static final Logger log = Logger.getLogger(EpubReader.class);
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

    public Book readEpub(InputStream in) throws IOException
    {
        return readEpub(in, Constants.CHARACTER_ENCODING);
    }

    public Book readEpub(ZipInputStream in) throws IOException
    {
        return readEpub(in, Constants.CHARACTER_ENCODING);
    }

    public Book readEpub(ZipFile zipfile) throws IOException
    {
        Book book = readEpub(zipfile, Constants.CHARACTER_ENCODING);
        book.setPhysicalFileName(Paths.get(zipfile.getName()));
        return book;
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


    /**
     * Reads this EPUB without loading any resources into memory.
     *
     * @param fileName the file to load
     * @param encoding the encoding for XHTML files
     * @return this Book without loading all resources into memory.
     * @throws java.io.IOException
     */
    public Book readEpubLazy(ZipFile zipFile, String encoding) throws IOException
    {
        Book book = readEpubLazy(zipFile, encoding, Arrays.asList(MediaType.mediatypes));
        book.setPhysicalFileName(Paths.get(zipFile.getName()));
        return book;
    }

    public Book readEpub(ZipInputStream in, String encoding) throws IOException
    {
        return readEpub(ResourcesLoader.loadResources(in, encoding));
    }

    public Book readEpub(ZipFile in, String encoding) throws IOException
    {
        Book book = readEpub(ResourcesLoader.loadResources(in, encoding));
        book.setPhysicalFileName(Paths.get(in.getName()));
        return book;
    }

    /**
     * Reads this EPUB without loading all resources into memory.
     *
     * @param fileName        the file to load
     * @param encoding        the encoding for XHTML files
     * @param lazyLoadedTypes a list of the MediaType to load lazily
     * @return this Book without loading all resources into memory.
     * @throws java.io.IOException
     */
    public Book readEpubLazy(ZipFile zipFile, String encoding, List<MediaType> lazyLoadedTypes) throws IOException
    {
        Resources resources = ResourcesLoader.loadResources(zipFile, encoding, lazyLoadedTypes);
        return readEpub(resources);
    }

    public Book readEpub(Resources resources) throws IOException
    {
        return readEpub(resources, new Book());
    }

    public Book readEpub(Resources resources, Book book) throws IOException
    {
        if (book == null)
        {
            book = new Book();
        }
        handleMimeType(book, resources);
        String packageResourceHref = getPackageResourceHref(resources);
        Resource packageResource = processPackageResource(packageResourceHref, book, resources);
        book.setOpfResource(packageResource);
        Resource ncxResource = processNcxResource(book);
        book.setNcxResource(ncxResource);
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
            PackageDocumentReader.read(packageResource, book, resources);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
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
            log.error(e.getMessage(), e);
        }
        if (StringUtils.isBlank(result))
        {
            result = defaultResult;
        }
        return result;
    }

    private void handleMimeType(Book result, Resources resources)
    {
        resources.remove("mimetype");
    }
}
