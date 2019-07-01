package de.machmireinebook.epubeditor.epublib.domain.epub3;

import java.io.Serializable;

/**
 *
 */
public class MetadataDate extends DublinCoreMetadataElement implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 7533866830395120136L;

    public MetadataDate(String dateString) {
        super(null, null, dateString, null);
    }

    public MetadataDate(String id, String scheme, String value)
    {
        super(id, scheme, value, null);
    }

	private static String checkDate(String dateString) {
		if (dateString == null) {
			throw new IllegalArgumentException("Cannot create a date from a blank string");
		}
		return dateString;
	}
}

