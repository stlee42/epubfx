package de.machmireinebook.epubeditor.epublib.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Scanner;
import java.util.regex.Pattern;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Various resource utility methods
 * 
 * @author paul
 *
 */
public class ToolsResourceUtil {
	
	private static Logger log = Logger.getLogger(ToolsResourceUtil.class);

	
	public static String getTitle(Resource resource) {
		if (resource == null) {
			return "";
		}
		if (resource.getMediaType() != MediaType.XHTML) {
			return resource.getHref();
		}
		String title = findTitleFromXhtml(resource);
		if (title == null) {
			title = "";
		}
		return title;
	}

	

	
	/**
	 * Retrieves whatever it finds between &lt;title&gt;...&lt;/title&gt; or &lt;h1-7&gt;...&lt;/h1-7&gt;.
	 * The first match is returned, even if it is a blank string.
	 * If it finds nothing null is returned.
	 * @param resource
	 * @return whatever it finds in the resource between &lt;title&gt;...&lt;/title&gt; or &lt;h1-7&gt;...&lt;/h1-7&gt;.
	 */
	public static String findTitleFromXhtml(Resource resource) {
		if (resource == null) {
			return "";
		}
		if (resource.getTitle() != null) {
			return resource.getTitle();
		}
		Pattern h_tag = Pattern.compile("^h\\d\\s*", Pattern.CASE_INSENSITIVE);
		String title = null;
		try {
			Reader content = resource.asReader();
			Scanner scanner = new Scanner(content);
			scanner.useDelimiter("<");
			while(scanner.hasNext()) {
				String text = scanner.next();
				int closePos = text.indexOf('>');
				String tag = text.substring(0, closePos);
				if (tag.equalsIgnoreCase("title")
					|| h_tag.matcher(tag).find()) {

					title = text.substring(closePos + 1).trim();
					title = StringEscapeUtils.unescapeHtml(title);
					break;
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		resource.setTitle(title);
		return title;
	}
}
