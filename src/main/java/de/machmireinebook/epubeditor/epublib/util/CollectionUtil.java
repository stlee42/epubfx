package de.machmireinebook.epubeditor.epublib.util;

import java.util.Enumeration;
import java.util.Iterator;

public class CollectionUtil {

	/**
	 * Wraps an Enumeration around an Iterator
	 * @author paul.siegmann
	 *
	 * @param <T>
	 */
	private static class IteratorEnumerationAdapter<T> implements Enumeration<T> {
		private Iterator<T> iterator;

		public IteratorEnumerationAdapter(Iterator<T> iter) {
			this.iterator = iter;
		}
		
		@Override
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

		@Override
		public T nextElement() {
			return iterator.next();
		}
	}
	
	/**
	 * Creates an Enumeration out of the given Iterator.
	 * @param <T>
	 * @param it
	 * @return an Enumeration created out of the given Iterator.
	 */
	public static <T> Enumeration<T> createEnumerationFromIterator(Iterator<T> it) {
		return new IteratorEnumerationAdapter<T>(it);
	}
}
