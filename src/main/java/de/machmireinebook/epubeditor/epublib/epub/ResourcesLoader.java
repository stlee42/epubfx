package de.machmireinebook.epubeditor.epublib.epub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import de.machmireinebook.epubeditor.epublib.domain.LazyResource;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Resources;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

/**
 * Loads Resources from inputStreams, ZipFiles, etc
 * 
 * @author paul
 *
 */
public class ResourcesLoader {
	/**
	 * Loads the entries of the zipFile as resources.
	 * 
	 * The MediaTypes that are in the lazyLoadedTypes will not get their contents loaded, but are stored as references to
	 * entries into the ZipFile and are loaded on demand by the Resource system.
	 * 
	 * @param zipFile
	 * @param defaultHtmlEncoding
	 * @param lazyLoadedTypes
	 * @return
	 * @throws java.io.IOException
	 */
	public static Resources loadResources(ZipFile zipFile, String defaultHtmlEncoding,
			List<MediaType> lazyLoadedTypes) throws IOException {		
				
		Resources result = new Resources();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while( entries.hasMoreElements() ) {
            ZipEntry zipEntry = entries.nextElement();

			if(zipEntry == null || zipEntry.isDirectory()) {
				continue;
			}
			
			String href = zipEntry.getName();
			
			Resource resource;
			
			if (shouldLoadLazy(href, lazyLoadedTypes)) {
				resource = new LazyResource(zipFile.getName(), zipEntry.getSize(), href);								
			} else {		
				resource = ResourceUtil.createResource(zipEntry, zipFile.getInputStream(zipEntry));
			}
			
			if(resource.getMediaType() == MediaType.XHTML) {
				resource.setInputEncoding(defaultHtmlEncoding);
			}
			result.add(resource);
		}
		
		return result;
	}
	
	/**
	 * Whether the given href will load a mediaType that is in the collection of lazilyLoadedMediaTypes.
	 * 
	 * @param href
	 * @param lazilyLoadedMediaTypes
	 * @return Whether the given href will load a mediaType that is in the collection of lazilyLoadedMediaTypes.
	 */
	private static boolean shouldLoadLazy(String href, Collection<MediaType> lazilyLoadedMediaTypes) {
		if (lazilyLoadedMediaTypes == null || lazilyLoadedMediaTypes.isEmpty()) {
			return false;
		}
		MediaType mediaType = MediaType.getByFileName(href);
		return lazilyLoadedMediaTypes.contains(mediaType);
	}


	public static Resources loadResources(InputStream in, String defaultHtmlEncoding) throws IOException {
		return loadResources(new ZipInputStream(in), defaultHtmlEncoding);
	}

	
	/**
	 * Loads all entries from the ZipInputStream as Resources.
	 * 
	 * Loads the contents of all ZipEntries into memory.
	 * Is fast, but may lead to memory problems when reading large books on devices with small amounts of memory.
	 * 
	 * @param in
	 * @param defaultHtmlEncoding
	 * @return
	 * @throws java.io.IOException
	 */
	public static Resources loadResources(ZipInputStream in, String defaultHtmlEncoding) throws IOException {
		Resources result = new Resources();
		for(ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in.getNextEntry()) {
			if(zipEntry.isDirectory()) {
				continue;
			}
			Resource resource = ResourceUtil.createResource(zipEntry, in);
			if(resource.getMediaType() == MediaType.XHTML) {
				resource.setInputEncoding(defaultHtmlEncoding);
			}
			result.add(resource);
		}
		return result;
	}


	/**
	 * Loads all entries from the ZipInputStream as Resources.
	 * 
	 * Loads the contents of all ZipEntries into memory.
	 * Is fast, but may lead to memory problems when reading large books on devices with small amounts of memory.
	 * 
	 * @param in
	 * @param defaultHtmlEncoding
	 * @return
	 * @throws java.io.IOException
	 */
    public static Resources loadResources(ZipFile zipFile, String defaultHtmlEncoding) throws IOException {
    	return loadResources(zipFile, defaultHtmlEncoding, Collections.<MediaType>emptyList());
    }

}
