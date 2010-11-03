package org.sakaiproject.nakamura.api.lite.util;

import java.util.Iterator;

public class Iterables {

	public static <T> Iterable<T> of(final T[] array) {
		return new Iterable<T>() {

			public Iterator<T> iterator() {
				return new Iterator<T>() {

					int p = 0;
					public boolean hasNext() {
						return p < array.length;
					}

					public T next() {
						return array[p++];
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

}
