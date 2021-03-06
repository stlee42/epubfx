package de.machmireinebook.epubeditor.epublib.domain.epub2;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * A Date used by the book's metadata.
 * 
 * Examples: creation-date, modification-date, etc
 * 
 * @author paul
 *
 */
public class MetadataDate extends DublinCoreMetadataElement implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7533866830395120136L;

	public enum Event {
		UNKNOWN(""),
		EMPTY(""),
		PUBLICATION("publication"),
		MODIFICATION("modification"),
		CREATION("creation");
		
		private final String value;

		Event(String v) {
			value = v;
		}

		public static Event fromValue(String v) {
			if (StringUtils.isEmpty(v)) {
				return EMPTY;
			}
			for (Event c : Event.values()) {
				if (c.value.equals(v)) {
					return c;
				}
			}
			return UNKNOWN;
		}
		
		public String toString() {
			return value;
		}
	}

	private Event event;
	private String unknownEventValue;

    public MetadataDate(String dateString, Event event) {
        super(null, null, dateString);
        this.event = event;
    }

    public MetadataDate(String id, String scheme, String value)
    {
        super(id, scheme, value);
        this.event = Event.PUBLICATION;
    }

	public MetadataDate(String dateString, String event) {
		this(checkDate(dateString), Event.fromValue(event));
		setValue(dateString);
	}

	private static String checkDate(String dateString) {
		if (dateString == null) {
			throw new IllegalArgumentException("Cannot create a date from a blank string");
		}
		return dateString;
	}
	public Event getEvent() {
		return event;
	}
	
	public void setEvent(Event event) {
		this.event = event;
	}

	public String toString() {
		if (event == null) {
			return getValue();
		}
		return event + ":" + getValue();
	}

	public String getUnknownEventValue() {
		return unknownEventValue;
	}

	public void setUnknownEventValue(String unknownEventValue) {
		this.unknownEventValue = unknownEventValue;
	}
}

