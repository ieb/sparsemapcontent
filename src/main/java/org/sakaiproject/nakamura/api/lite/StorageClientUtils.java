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
package org.sakaiproject.nakamura.api.lite;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.util.Type1UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StorageClientUtils {

    public final static String UTF8 = "UTF-8";
    /** how are numbers encoded, base ? */
    public final static int ENCODING_BASE = 10;
    public final static String SECURE_HASH_DIGEST = "SHA-512";
    public static final char[] URL_SAFE_ENCODING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
            .toCharArray();
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageClientUtils.class);

    public static String toString(Object object) {
        try {
            if (object == null) {
                return null;
            } else if (object instanceof byte[]) {
                return new String((byte[]) object, UTF8);
            } else if (object instanceof String) {
                return (String) object;
            } else {
                LOGGER.warn("Converting " + object.getClass() + " to String via toString");
                return String.valueOf(object);
            }
        } catch (UnsupportedEncodingException e) {
            return null; // no utf8.. get real!
        }
    }

    public static Object toStore(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof byte[]) {
            return (byte[]) object;
        } else if (object instanceof String) {
            return ((String) object);
        } else if (object instanceof Long) {
            return Long.toString((Long) object, ENCODING_BASE);
        } else if (object instanceof Integer) {
            return Integer.toString((Integer) object, ENCODING_BASE);
        } else {
            LOGGER.warn("Converting " + object.getClass() + " to byte[] via string");
            return String.valueOf(object);
        }
    }

    public static byte[] toBytes(Object value) {
        Object o = toStore(value);
        if (o instanceof byte[]) {
            return (byte[]) o;
        } else {
            try {
                return ((String) o).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return null; // no utf8.. get real!
            }
        }
    }

    public static boolean isRoot(String objectPath) {
        return (objectPath == null) || "/".equals(objectPath) || "".equals(objectPath)
                || (objectPath.indexOf("/") < 0);
    }

    public static String getParentObjectPath(String objectPath) {
        if ("/".equals(objectPath)) {
            return "/";
        }
        int i = objectPath.lastIndexOf('/');
        if (i == objectPath.length() - 1) {
            i = objectPath.substring(0, i).lastIndexOf('/');
        }
        String res = objectPath;
        if (i > 0) {
            res = objectPath.substring(0, i);
        } else if (i == 0) {
            return "/";
        }
        return res;
    }

    public static String getObjectName(String objectPath) {
        if ("/".equals(objectPath)) {
            return "/";
        }
        int i = objectPath.lastIndexOf('/');
        int j = objectPath.length();
        if (i == objectPath.length() - 1) {
            j--;
            i = objectPath.substring(0, i).lastIndexOf('/');
        }
        String res = objectPath;
        if (i >= 0) {
            res = objectPath.substring(i + 1, j);
        }
        return res;

    }

    public static String insecureHash(String naked) {
        try {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e1) {
                try {
                    md = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e2) {
                    LOGGER.error("You have no Message Digest Algorightms intalled in this JVM, secure Hashes are not availalbe, encoding bytes :"
                            + e2.getMessage());
                    return encode(StringUtils.leftPad(naked, 10, '_').getBytes(UTF8),
                            URL_SAFE_ENCODING);
                }
            }
            byte[] bytes = md.digest(naked.getBytes(UTF8));
            return encode(bytes, URL_SAFE_ENCODING);
        } catch (UnsupportedEncodingException e3) {
            LOGGER.error("no UTF-8 Envoding, get a real JVM, nothing will work here. NPE to come");
            return null;
        }
    }

    public static String secureHash(String password) {
        try {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance(SECURE_HASH_DIGEST);
            } catch (NoSuchAlgorithmException e) {
                try {
                    md = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e1) {
                    try {
                        md = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException e2) {
                        LOGGER.error("You have no Message Digest Algorightms intalled in this JVM, secure Hashes are not availalbe, encoding bytes :"
                                + e2.getMessage());
                        return encode(StringUtils.leftPad(password, 10, '_').getBytes(UTF8),
                                URL_SAFE_ENCODING);
                    }
                }
            }
            byte[] bytes = md.digest(password.getBytes(UTF8));
            return encode(bytes, URL_SAFE_ENCODING);
        } catch (UnsupportedEncodingException e3) {
            LOGGER.error("no UTF-8 Envoding, get a real JVM, nothing will work here. NPE to come");
            return null;
        }
    }

    /**
     * Generate an encoded array of chars using as few chars as possible
     * 
     * @param hash
     *            the hash to encode
     * @param encode
     *            a char array of encodings any length you lik but probably but
     *            the shorter it is the longer the result. Dont be dumb and use
     *            an encoding size of < 2.
     * @return
     */
    public static String encode(byte[] hash, char[] encode) {
        StringBuilder sb = new StringBuilder((hash.length * 15) / 10);
        int x = (int) (hash[0] + 128);
        int xt = 0;
        int i = 0;
        while (i < hash.length) {
            if (x < encode.length) {
                i++;
                if (i < hash.length) {
                    if (x == 0) {
                        x = (int) (hash[i] + 128);
                    } else {
                        x = (x + 1) * (int) (hash[i] + 128);
                    }
                } else {
                    sb.append(encode[x]);
                    break;
                }
            }
            xt = x % encode.length;
            x = x / encode.length;
            sb.append(encode[xt]);
        }

        return sb.toString();
    }

    /**
     * Converts to an Immutable map, with keys that are in the filter not
     * transdered. Nested maps are also transfered.
     * 
     * @param <K>
     * @param <V>
     * @param source
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> getFilterMap(Map<K, V> source, Set<K> filter) {
        Builder<K, V> filteredMap = new ImmutableMap.Builder<K, V>();
        for (Entry<K, V> e : source.entrySet()) {
            if (!filter.contains(e.getKey())) {
                Object o = e.getValue();
                if (o instanceof Map) {
                    filteredMap.put(e.getKey(), (V) getFilterMap((Map<K, V>) e.getValue(), filter));
                } else {
                    filteredMap.put(e.getKey(), e.getValue());
                }
            }
        }
        return filteredMap.build();
    }

    /**
     * Converts a map into Map or byte[] values with String keys. No control
     * over depth of nesting. Keys in the filter set are not transfered
     * Resulting map is mutable.
     * 
     * @param source
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getFilteredAndEcodedMap(Map<String, Object> source,
            Set<String> filter) {
        Map<String, Object> filteredMap = Maps.newHashMap();
        for (Entry<String, Object> e : source.entrySet()) {
            if (!filter.contains(e.getKey())) {
                Object o = e.getValue();
                if (o instanceof Map) {
                    filteredMap.put(e.getKey(),
                            getFilteredAndEcodedMap((Map<String, Object>) e.getValue(), filter));
                } else {
                    filteredMap.put(e.getKey(), toStore(e.getValue()));
                }
            }
        }
        return filteredMap;
    }

    public static String getUuid() {
        return StorageClientUtils.encode(Type1UUID.next(), StorageClientUtils.URL_SAFE_ENCODING);
    }

    public static int toInt(Object object) {
        if (object instanceof Integer) {
            return ((Integer) object).intValue();
        } else if (object == null) {
            return 0;
        }
        return Integer.parseInt(toString(object), ENCODING_BASE);
    }

    public static long toLong(Object object) {
        if (object instanceof Long) {
            return ((Long) object).longValue();
        }
        return Long.parseLong(toString(object), ENCODING_BASE);
    }

    public static String newPath(String path, String child) {
        if (!path.endsWith("/")) {
            if (!child.startsWith("/")) {
                return path + "/" + child;
            } else {
                return path + child;

            }
        } else {
            if (!child.startsWith("/")) {
                return path + child;
            } else {
                return path + child.substring(1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSetting(Object setting, T defaultValue) {
        if (setting != null) {
            return (T) setting;
        }
        return defaultValue;
    }

    public static String shardPath(String id) {
        String hash = insecureHash(id);
        return hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" + hash.substring(4, 6) + "/"
                + hash;
    }

    public static String arrayEscape(String string) {
        string = string.replaceAll("/", "//");
        string = string.replaceAll(",", "/,");
        return string;
    }

    public static String arrayUnEscape(String string) {
        string = string.replaceAll("/,", ",");
        string = string.replaceAll("//", "/");
        return string;
    }

}
