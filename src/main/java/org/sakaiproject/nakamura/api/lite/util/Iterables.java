package org.sakaiproject.nakamura.api.lite.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Iterables {

    public static <T> Iterable<T> of(final T[] array) {
        return new Iterable<T>() {

            public Iterator<T> iterator() {
                return new Iterator<T>() {

                    int p = 0;

                    public boolean hasNext() {
                        return array != null && p < array.length;
                    }

                    public T next() {
                        try {
                            return array[p++];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new NoSuchElementException();
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

}
