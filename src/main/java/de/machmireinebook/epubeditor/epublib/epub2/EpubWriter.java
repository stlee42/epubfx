package de.machmireinebook.epubeditor.epublib.epub2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.bookprocessor.HtmlCleanerBookProcessor;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.epub3.Epub3PackageDocumentWriter;
import de.machmireinebook.epubeditor.epublib.resource.Resource;

/**
 * Generates an epub file. Not thread-safe, single use object.
 *
 * @author paul
 */
public class EpubWriter
{
    private final static Logger logger = Logger.getLogger(EpubWriter.class);

    private BookProcessor bookProcessor;

    public EpubWriter()
    {
        this(new HtmlCleanerBookProcessor());
    }


    public EpubWriter(BookProcessor bookProcessor)
    {
        this.bookProcessor = bookProcessor;
    }


    public void write(Book book, OutputStream out) throws IOException
    {
        book = processBook(book);
        try (ZipOutputStream resultStream = new ZipOutputStream(out)) {
            writeMimeType(resultStream);
            writeContainer(resultStream);
            if (!book.isEpub3()) {
                initTOCResource(book);
            } 
            writeResources(book, resultStream);
            writePackageDocument(book, resultStream);
        }
    }

    private Book processBook(Book book)
    {
        if (bookProcessor != null)
        {
            book = bookProcessor.processBook(book);
        }
        return book;
    }

    private void initTOCResource(Book book)
    {
        Resource tocResource;
        try
        {
            tocResource = NCXDocument.createNCXResource(book);
            Resource currentTocResource = book.getSpine().getTocResource();
            if (currentTocResource != null)
            {
                book.getResources().remove(currentTocResource.getHref());
            }
            book.getSpine().setTocResource(tocResource);
            book.getResources().put(tocResource);
        }
        catch (Exception e)
        {
            logger.error("Error writing table of contents: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }


    private void writeResources(Book book, ZipOutputStream resultStream)
    {
        for (Resource resource : book.getResources().getAll())
        {
            writeResource(resource, resultStream);
        }
    }

    /**
     * Writes the resource to the resultStream.
     */
    private void writeResource(Resource resource, ZipOutputStream resultStream)
    {
        if (resource == null)
        {
            return;
        }
        try
        {
            resultStream.putNextEntry(new ZipEntry("OEBPS/" + resource.getHref()));
            InputStream inputStream = resource.getInputStream();
            IOUtils.copy(inputStream, resultStream);
            inputStream.close();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }


    private void writePackageDocument(Book book, ZipOutputStream resultStream) throws IOException
    {
        resultStream.putNextEntry(new ZipEntry("OEBPS/content.opf"));
        Document opfDocument;
        if (book.isEpub3()) {
            opfDocument = Epub3PackageDocumentWriter.write(book);
        } else {
            opfDocument = PackageDocumentWriter.write(book);
        }

        XMLOutputter outputter = new XMLOutputter();
        Format xmlFormat = Format.getPrettyFormat();
        outputter.setFormat(xmlFormat);
        String opfText = outputter.outputString(opfDocument);
        book.getOpfResource().setData(opfText.getBytes(Constants.CHARACTER_ENCODING));
		resultStream.write(book.getOpfResource().getData());
    }

    /**
     * Writes the META-INF/container.xml file.
     */
    private void writeContainer(ZipOutputStream resultStream) throws IOException
    {
        resultStream.putNextEntry(new ZipEntry("META-INF/container.xml"));
        Writer out = new OutputStreamWriter(resultStream);
        out.write("<?xml version=\"1.0\"?>\n");
        out.write("<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n");
        out.write("\t<rootfiles>\n");
        out.write("\t\t<rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n");
        out.write("\t</rootfiles>\n");
        out.write("</container>");
        out.flush();
    }

    /**
     * Stores the mimetype as an uncompressed file in the ZipOutputStream.
     */
    private void writeMimeType(ZipOutputStream resultStream) throws IOException
    {
        ZipEntry mimetypeZipEntry = new ZipEntry("mimetype");
        mimetypeZipEntry.setMethod(ZipEntry.STORED);
        byte[] mimetypeBytes = MediaType.EPUB.getName().getBytes();
        mimetypeZipEntry.setSize(mimetypeBytes.length);
        mimetypeZipEntry.setCrc(calculateCrc(mimetypeBytes));
        resultStream.putNextEntry(mimetypeZipEntry);
        resultStream.write(mimetypeBytes);
    }

    private long calculateCrc(byte[] data)
    {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }
}
