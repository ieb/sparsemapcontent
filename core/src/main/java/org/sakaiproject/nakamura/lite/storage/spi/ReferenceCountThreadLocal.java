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
package org.sakaiproject.nakamura.lite.storage.spi;

/**
 * This class need to bind weakly to a thread, so when the thread dies, the
 * object gets a unbind
 * 
 * @author ieb
 * 
 * @param <T>
 */
public class ReferenceCountThreadLocal<T> {

    public class Holder {

        private int ref;
        private T payload;

        public Holder(T payload) {
            this.payload = payload;
        }

        public T get() {
            return payload;
        }

        public int inc() {
            ref++;
            return ref;
        }

        public int dec() {
            ref--;
            return ref;
        }

    }

    private ThreadLocal<Holder> store = new ThreadLocal<Holder>();

    public T get() {
        Holder h = store.get();
        if (h == null) {
            return null;
        }
        h.inc();
        return h.get();
    }

    public void set(T client) {
        store.set(new Holder(client));

    }

    public T release() {
        Holder h = store.get();
        if (h == null) {
            return null;
        }
        if (h.dec() == 0) {
            store.set(null);
            return h.get();
        }
        return null;
    }

}
