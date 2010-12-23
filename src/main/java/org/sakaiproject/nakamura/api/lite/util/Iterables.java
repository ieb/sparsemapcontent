/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.lite.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Utility class to ease use of iterators and iterables.
 */
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
