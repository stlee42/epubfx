package de.machmireinebook.epubeditor.epublib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.domain.DefaultResourceFactory;
import de.machmireinebook.epubeditor.epublib.domain.ImageResource;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

/**
 * Various resource utility methods
 * 
 * @author paul
 *
 */
public class ResourceUtil
{
	public static final Logger logger = Logger.getLogger(ResourceUtil.class);

	public static Resource createResource(File file) throws IOException {
		if (file == null) {
			return null;
		}
		MediaType mediaType = MediaType.getByFileName(file.getName());
		byte[] data = IOUtils.toByteArray(new FileInputStream(file));
        Resource result;
        if (mediaType.isBitmapImage())
        {
            return new ImageResource(data, mediaType);
        }
        else
        {
            result = new Resource(data, mediaType);
        }
		return result;
	}
	
	
	/**
	 * Creates a resource with as contents a html page with the given title.
	 * 
	 * @param title
	 * @param href
	 * @return a resource with as contents a html page with the given title.
	 */
	public static Resource createResource(String title, String href) {
		String content = "<html><head><title>" + title + "</title></head><body><h1>" + title + "</h1></body></html>";
		return new Resource(null, content.getBytes(), href, MediaType.XHTML, Constants.CHARACTER_ENCODING);
	}

	/**
	 * Creates a resource out of the given zipEntry and zipInputStream.
	 * 
	 * @param zipEntry
	 * @param zipInputStream
	 * @return a resource created out of the given zipEntry and zipInputStream.
	 * @throws java.io.IOException
	 */
	public static Resource createResource(ZipEntry zipEntry, ZipInputStream zipInputStream) throws IOException {
        MediaType mediaType = MediaType.getByFileName(zipEntry.getName());
		if (mediaType != null)
		{
			return mediaType.getResourceFactory().createResource(IOUtils.toByteArray(zipInputStream), zipEntry.getName());
		}
		else
		{
			logger.info("reading resource " + zipEntry.getName() + " with unknown mediatype, using default resource factory");
			return DefaultResourceFactory.getInstance().createResource(IOUtils.toByteArray(zipInputStream), zipEntry.getName());
		}

	}

    public static Resource createResource(ZipEntry zipEntry, InputStream zipInputStream) throws IOException
    {
        MediaType mediaType = MediaType.getByFileName(zipEntry.getName());
        return mediaType.getResourceFactory().createResource(IOUtils.toByteArray(zipInputStream), zipEntry.getName());
    }

	/**
	 * Converts a given string from given input character encoding to the requested output character encoding.
	 * 
	 * @param inputEncoding
	 * @param outputEncoding
	 * @param input
	 * @return the string from given input character encoding converted to the requested output character encoding.
	 * @throws java.io.UnsupportedEncodingException
	 */
	public static byte[] recode(String inputEncoding, String outputEncoding, byte[] input) throws UnsupportedEncodingException {
		return new String(input, inputEncoding).getBytes(outputEncoding);
	}
	
	/**
	 * Gets the contents of the Resource as an InputSource in a null-safe manner.
	 * 
	 */
	public static InputSource getInputSource(Resource resource) throws IOException {
		if (resource == null) {
			return null;
		}
		Reader reader = resource.asReader();
		if (reader == null) {
			return null;
		}
		InputSource inputSource = new InputSource(reader);
		return inputSource;
	}
	
	
	/**
	 * Reads parses the xml therein and returns the result as a Document
	 */
	public static Document getAsDocument(Resource resource) throws IOException, JDOMException
    {
		return getAsDocument(resource, new SAXBuilder());
	}
	
	
	/**
	 * Reads the given resources inputstream, parses the xml therein and returns the result as a Document
	 * 
	 * @param resource
	 * @param documentBuilder
	 * @return the document created from the given resource
	 * @throws java.io.UnsupportedEncodingException
	 * @throws org.xml.sax.SAXException
	 * @throws java.io.IOException
	 * @throws javax.xml.parsers.ParserConfigurationException
	 */
	public static Document getAsDocument(Resource resource, SAXBuilder documentBuilder) throws IOException, JDOMException
    {
		InputSource inputSource = getInputSource(resource);
		if (inputSource == null) {
			return null;
		}
		Document result = documentBuilder.build(inputSource);
		return result;
	}

}
