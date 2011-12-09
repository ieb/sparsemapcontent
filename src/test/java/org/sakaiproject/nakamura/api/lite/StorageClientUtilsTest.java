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
import com.google.common.collect.ImmutableSet;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.RemoveProperty;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StorageClientUtilsTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testInt() throws UnsupportedEncodingException {
        for (int i = -100000; i < 10000; i++) {
            Assert.assertEquals(i, StorageClientUtils.toInt(StorageClientUtils.toStore(i)));
        }
        for (int i = Integer.MIN_VALUE; i < Integer.MIN_VALUE + 10000; i++) {
            Assert.assertEquals(i, StorageClientUtils.toInt(StorageClientUtils.toStore(i)));
        }
        for (int i = Integer.MAX_VALUE - 10000; i < Integer.MAX_VALUE; i++) {
            Assert.assertEquals(i, StorageClientUtils.toInt(StorageClientUtils.toStore(i)));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testLong() throws UnsupportedEncodingException {
        for (long i = -100000; i < 10000; i++) {
            Assert.assertEquals(i, StorageClientUtils.toLong(StorageClientUtils.toStore(i)));
        }
        for (long i = Long.MIN_VALUE; i < Long.MIN_VALUE + 10000; i++) {
            Assert.assertEquals(i, StorageClientUtils.toLong(StorageClientUtils.toStore(i)));
        }
        for (long i = Long.MAX_VALUE - 10000; i < Long.MAX_VALUE; i++) {
            Assert.assertEquals(i, StorageClientUtils.toLong(StorageClientUtils.toStore(i)));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testToString() throws UnsupportedEncodingException {
        Assert.assertEquals(null, StorageClientUtils.toString(null));
        Assert.assertEquals("test", StorageClientUtils.toString("test"));
        Assert.assertEquals("test", StorageClientUtils.toString("test".getBytes("UTF8")));
        Assert.assertEquals("100", StorageClientUtils.toString(100));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testToBytes() {
        Assert.assertEquals(null, StorageClientUtils.toStore(null));
        Assert.assertEquals("test", StorageClientUtils.toStore("test"));
        Assert.assertEquals(100L,
                StorageClientUtils.toStore((long) 100));
        Assert.assertEquals(100,
                StorageClientUtils.toStore((int) 100));
        Object o = new Object();
        Assert.assertEquals(o, StorageClientUtils.toStore(o));
    }

    @Test
    public void testIsRoot() throws UnsupportedEncodingException {
        Assert.assertTrue(StorageClientUtils.isRoot(null));
        Assert.assertTrue(StorageClientUtils.isRoot(""));
        Assert.assertTrue(StorageClientUtils.isRoot("/"));
        Assert.assertFalse(StorageClientUtils.isRoot("/sdfds"));
    }

    @Test
    public void testGetParentObjectPath() {
        Assert.assertEquals("/", StorageClientUtils.getParentObjectPath("/"));
        Assert.assertEquals("/", StorageClientUtils.getParentObjectPath("/test"));
        Assert.assertEquals("/", StorageClientUtils.getParentObjectPath("/test/"));
        Assert.assertEquals("/test", StorageClientUtils.getParentObjectPath("/test/ing"));
        Assert.assertEquals("/test", StorageClientUtils.getParentObjectPath("/test/ing/"));
    }

    @Test
    public void testGetParentObjectName() {
        Assert.assertEquals("/", StorageClientUtils.getObjectName("/"));
        Assert.assertEquals("test", StorageClientUtils.getObjectName("/test"));
        Assert.assertEquals("test", StorageClientUtils.getObjectName("/test/"));
        Assert.assertEquals("ing", StorageClientUtils.getObjectName("/test/ing"));
        Assert.assertEquals("ing", StorageClientUtils.getObjectName("/test/ing/"));
    }

    @Test
    public void testHash() {
        Assert.assertNotNull(StorageClientUtils.secureHash("test"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetFilterMap() {
        Map<String, Object> t = ImmutableMap.of("a", (Object) "b", "c", "d", "y", "should have been removed");
        Map<String, Object> modifications = ImmutableMap.of("a", (Object) "b", "x", "New", "y", new RemoveProperty() );
        Map<String, Object> m = StorageClientUtils.getFilterMap(t, modifications, null, ImmutableSet.of("c"), false);
        Assert.assertEquals(2, m.size());
        Assert.assertEquals("b", m.get("a"));
        Assert.assertEquals("New", m.get("x"));
        Assert.assertFalse(m.containsKey("y"));
        m = StorageClientUtils.getFilterMap(t, modifications, null, ImmutableSet.of("c"), true);
        Assert.assertEquals(3, m.size());
        Assert.assertEquals("b", m.get("a"));
        Assert.assertEquals("New", m.get("x"));
        Assert.assertTrue(m.containsKey("y"));
        Map<String, Object> t2 = ImmutableMap.of("a", (Object) "b", "c", "d", "e", m);
        Map<String, Object> m2 = StorageClientUtils.getFilterMap(t2, null, null, ImmutableSet.of("c"), false);
        Assert.assertEquals(2, m2.size());
        Assert.assertEquals("b", m2.get("a"));
        m = (Map<String, Object>) m2.get("e");
        Assert.assertEquals(3, m.size());
        Assert.assertEquals("b", m.get("a"));
        Assert.assertEquals("New", m.get("x"));
        Assert.assertTrue(m.containsKey("y"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetFilteredAndEcodedMap() throws UnsupportedEncodingException {
        Map<String, Object> t = ImmutableMap.of("a", (Object) "b", "c", "d");
        Map<String, Object> m = StorageClientUtils.getFilteredAndEcodedMap(t, ImmutableSet.of("c"));
        Assert.assertEquals(1, m.size());
        Assert.assertEquals("b", m.get("a"));
        Map<String, Object> t2 = ImmutableMap.of("a", (Object) "b", "c", "d", "e", m);
        Map<String, Object> m2 = StorageClientUtils.getFilteredAndEcodedMap(t2,
                ImmutableSet.of("c"));
        Assert.assertEquals(2, m2.size());
        Assert.assertEquals("b", m2.get("a"));
        m = (Map<String, Object>) m2.get("e");
        Assert.assertEquals(1, m.size());
        Assert.assertEquals("b", m.get("a"));
    }
    
    
    @Test
    public void testEncode() {
        Set<String> check = new HashSet<String>();
        byte[] b = new byte[1];
        for ( int i = 0; i < 100000; i++ ) {
            b = incByteArray(b,0);
            String id = StorageClientUtils.encode(b);
            if ( check.contains(id) ) {
                Assert.fail(id);
            }
            check.add(id);
        }
    }
    

    private byte[] incByteArray(byte[] b, int i) {
        if ( i == b.length) {
            byte[] bn = new byte[b.length+1];
            System.arraycopy(b, 0, bn, 0, b.length);
            bn[i] = 0x01;
            b = bn;
        } else {
            b[i]++;
            if (b[i] == 0) {
                b = incByteArray(b, i + 1);
            }
        }
        return b;
    }

}
