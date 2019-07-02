package de.machmireinebook.epubeditor.epublib.epub;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
	 * Loads all entries from the ZipInputStream as Resources.
	 * 
	 * Loads the contents of all ZipEntries into memory.
	 * Is fast, but may lead to memory problems when reading large books on devices with small amounts of memory.
	 */
	public static Resources loadResources(ZipInputStream in, String defaultHtmlEncoding) throws IOException {
		Resources result = new Resources();
		for(ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in.getNextEntry()) {
			if(zipEntry.isDirectory()) {
				continue;
			}
			Resource resource = ResourceUtil.createResource(zipEntry, in);
			if (resource.getMediaType() == MediaType.XHTML) {
				resource.setInputEncoding(defaultHtmlEncoding);
			}
			result.add(resource);
		}
		return result;
	}
}
