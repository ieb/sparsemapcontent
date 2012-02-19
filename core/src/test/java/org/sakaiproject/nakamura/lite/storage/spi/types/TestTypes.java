package org.sakaiproject.nakamura.lite.storage.spi.types;

import com.google.common.collect.Maps;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.lite.storage.spi.types.LongString;
import org.sakaiproject.nakamura.lite.storage.spi.types.StringType;
import org.sakaiproject.nakamura.lite.storage.spi.types.Type;
import org.sakaiproject.nakamura.lite.storage.spi.types.Types;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

public class TestTypes {
    
    
    private static final String SHORTSTRING = "AShortString";
    private static final String LONGSTRING = "Longer than "+SHORTSTRING;

    @Before
    public void before() {
        LongString.setBase("target/longstringstore");
    }

    @Test
    public void testTypes() {
        Map<Integer, Type<?>> typeById = Types.getTypeByIdMap();
        Assert.assertNotNull(typeById);
        Map<Class<?>, Type<?>> typeByClass = Types.getTypeMap();
        Assert.assertNotNull(typeByClass);  
    }
    
    @Test
    public void testWriteTypes() throws IOException {
        Map<String, Object> map = Maps.newHashMap();
        map.put("A", 1);
        map.put("B", Long.MAX_VALUE);
        map.put("C", "String");
        map.put("D", new BigDecimal("12345.12E23"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("BST"));
        cal.setTimeInMillis(System.currentTimeMillis());
        map.put("E", cal);
        map.put("F", (double)0.1);
        map.put("G", true);
        map.put("H", false);
        map.put("J", null);
        
        InputStream in = Types.storeMapToStream("testkey", map, "testcf");
        Map<String, Object> output = Maps.newHashMap();
        Types.loadFromStream("testkey", output, in, "testcf");
        
        Integer a = (Integer) output.get("A");
        Assert.assertNotNull(a);
        Assert.assertEquals(1, a.intValue());
        Long b = (Long) output.get("B");
        Assert.assertNotNull(b);
        Assert.assertEquals(Long.MAX_VALUE, b.longValue());
        String c = (String) output.get("C");
        Assert.assertNotNull(c);
        Assert.assertEquals("String", c);
        BigDecimal d = (BigDecimal) output.get("D");
        Assert.assertNotNull(d);
        Assert.assertEquals(new BigDecimal("12345.12E23"), d);
        Calendar e = (Calendar) output.get("E");
        Assert.assertNotNull(e);
        Assert.assertEquals(cal, e);
        Assert.assertEquals(cal.getTimeInMillis(), e.getTimeInMillis());
        Assert.assertEquals(cal.getTimeZone(), e.getTimeZone());
        Double f = (Double) output.get("F");
        Assert.assertNotNull(f);
        Assert.assertEquals((double)0.1, f.doubleValue(),0.0);
        Boolean g = (Boolean) output.get("G");
        Assert.assertNotNull(g);
        Assert.assertTrue(g.booleanValue());
        Boolean h = (Boolean) output.get("H");
        Assert.assertNotNull(h);
        Assert.assertFalse(h.booleanValue());
        Object j = output.get("J");
        Assert.assertNull(j);
        
        
        
    }

    @Test
    public void testWriteTypesWrongFamily() throws IOException {
        Map<String, Object> map = Maps.newHashMap();
        map.put("A", 1);
        map.put("B", Long.MAX_VALUE);
        map.put("C", "String");
        map.put("D", new BigDecimal("12345.12E23"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("BST"));
        cal.setTimeInMillis(System.currentTimeMillis());
        map.put("E", cal);
        map.put("F", (double)0.1);
        map.put("G", true);
        map.put("H", false);
        map.put("J", null);
        InputStream in = Types.storeMapToStream("testkey", map, "testcf");
        Map<String, Object> output = Maps.newHashMap();
        try {
            Types.loadFromStream("testkey", output, in, "not-testcf");
            org.junit.Assert.fail();
        } catch ( IOException e ) {
            // Ok
        }
    }

    @Test
    public void testWriteArrayTypes() throws IOException {
        Map<String, Object> map = Maps.newHashMap();
        map.put("A", new Integer[]{1,2});
        map.put("B", new Long[]{Long.MIN_VALUE,Long.MAX_VALUE});
        map.put("C", new String[]{"StringA","StringB"});
        map.put("D", new BigDecimal[]{new BigDecimal("12345.12E23"),new BigDecimal("12345.12E21")});
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("BST"));
        cal.setTimeInMillis(System.currentTimeMillis());
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("PST"));
        cal2.setTimeInMillis(System.currentTimeMillis());
        map.put("E", new Calendar[]{cal,cal2});
        map.put("F", new Double[]{0.1,0.2});
        map.put("G", new Boolean[]{true,false});
        map.put("H", new Boolean[]{false,true});
        map.put("I", new boolean[]{true, true});
        map.put("J", new long[]{5, 10});
        map.put("K", new int[] {1,2});
        map.put("L", new double[] {1.1, 3.14});

        InputStream in = Types.storeMapToStream("testkey", map, "testcf");
        Map<String, Object> output = Maps.newHashMap();
        Types.loadFromStream("testkey", output, in, "testcf");
        
        Integer[] a = (Integer[]) output.get("A");
        Assert.assertNotNull(a);
        Assert.assertEquals(2, a.length);
        Assert.assertEquals(1, a[0].intValue());
        Assert.assertEquals(2, a[1].intValue());
        Long[] b = (Long[]) output.get("B");
        Assert.assertNotNull(b);
        Assert.assertEquals(2, b.length);
        Assert.assertEquals(Long.MIN_VALUE, b[0].longValue());
        Assert.assertEquals(Long.MAX_VALUE, b[1].longValue());
        String[] c = (String[]) output.get("C");
        Assert.assertNotNull(c);
        Assert.assertEquals(2, c.length);
        Assert.assertEquals("StringA", c[0]);
        Assert.assertEquals("StringB", c[1]);
        BigDecimal[] d = (BigDecimal[]) output.get("D");
        Assert.assertNotNull(d);
        Assert.assertEquals(2, d.length);
        Assert.assertEquals(new BigDecimal("12345.12E23"), d[0]);
        Assert.assertEquals(new BigDecimal("12345.12E21"), d[1]);
        Calendar[] e = (Calendar[]) output.get("E");
        Assert.assertNotNull(e);
        Assert.assertEquals(2, e.length);
        Assert.assertEquals(cal, e[0]);
        Assert.assertEquals(cal.getTimeInMillis(), e[0].getTimeInMillis());
        Assert.assertEquals(cal.getTimeZone(), e[0].getTimeZone());
        Assert.assertEquals(cal2, e[1]);
        Assert.assertEquals(cal2.getTimeInMillis(), e[1].getTimeInMillis());
        Assert.assertEquals(cal2.getTimeZone(), e[1].getTimeZone());
        Double[] f = (Double[]) output.get("F");
        Assert.assertNotNull(f);
        Assert.assertEquals(2, f.length);
        Assert.assertEquals(0.1, f[0],0.0);
        Assert.assertEquals(0.2, f[1],0.0);
        Boolean[] g = (Boolean[]) output.get("G");
        Assert.assertNotNull(g);
        Assert.assertEquals(2, g.length);
        Assert.assertTrue(g[0]);
        Assert.assertFalse(g[1]);
        Boolean[] h = (Boolean[]) output.get("H");
        Assert.assertNotNull(h);
        Assert.assertEquals(2, h.length);
        Assert.assertFalse(h[0]);
        Assert.assertTrue(h[1]);
        Boolean[] i = (Boolean[]) output.get("I");
        Assert.assertNotNull(i);
        Assert.assertEquals(true, i[0]);
        Assert.assertEquals(true, i[1]);
        Long[] j = (Long[]) output.get("J");
        Assert.assertEquals(5, j[0].longValue());
        Assert.assertEquals(10, j[1].longValue());
        Integer[] k = (Integer[]) output.get("K");
        Assert.assertEquals(1, k[0].intValue());
        Assert.assertEquals(2, k[1].intValue());
        Double[] l = (Double[]) output.get("L");
        Assert.assertEquals(1.1, l[0], 0.01);
        Assert.assertEquals(3.14, l[1], 0.01);
    }
    @Test
    public void testWriteEmptyArrayTypes() throws IOException {
        Map<String, Object> map = Maps.newHashMap();
        map.put("A", new Integer[]{});
        map.put("B", new Long[]{});
        map.put("C", new String[]{});
        map.put("D", new BigDecimal[]{});
        map.put("E", new Calendar[]{});
        map.put("F", new Double[]{});
        map.put("G", new Boolean[]{});
        map.put("H", new Boolean[]{});
        
        InputStream in = Types.storeMapToStream("testkey", map,"testcf");
        Map<String, Object> output = Maps.newHashMap();
        Types.loadFromStream("testkey", output, in, "testcf");
        
        Integer[] a = (Integer[]) output.get("A");
        Assert.assertNotNull(a);
        Assert.assertEquals(0, a.length);
        Long[] b = (Long[]) output.get("B");
        Assert.assertNotNull(b);
        Assert.assertEquals(0, b.length);
        String[] c = (String[]) output.get("C");
        Assert.assertNotNull(c);
        Assert.assertEquals(0, c.length);
        BigDecimal[] d = (BigDecimal[]) output.get("D");
        Assert.assertNotNull(d);
        Assert.assertEquals(0, d.length);
        Calendar[] e = (Calendar[]) output.get("E");
        Assert.assertNotNull(e);
        Assert.assertEquals(0, e.length);
        Double[] f = (Double[]) output.get("F");
        Assert.assertNotNull(f);
        Assert.assertEquals(0, f.length);
        Boolean[] g = (Boolean[]) output.get("G");
        Assert.assertNotNull(g);
        Assert.assertEquals(0, g.length);
        Boolean[] h = (Boolean[]) output.get("H");
        Assert.assertNotNull(h);
        Assert.assertEquals(0, h.length);
        
        
        
    }
    
    
    @Test
    public void testStringToLongString() throws IOException {
        Map<String, Object> map = Maps.newHashMap();
        map.put("short", SHORTSTRING);
        map.put("long", LONGSTRING);
        map.put("shortarray", new String[]{ SHORTSTRING, SHORTSTRING, SHORTSTRING});
        map.put("longarray", new String[]{ SHORTSTRING, SHORTSTRING, LONGSTRING, LONGSTRING});
        
        StringType.setLengthLimit(LONGSTRING.length() -1);
        
        InputStream in = Types.storeMapToStream("testkey", map,"testcf");
        Map<String, Object> output = Maps.newHashMap();
        Types.loadFromStream("testkey", output, in, "testcf");
        
        Assert.assertEquals(String.class, output.get("short").getClass());
        Assert.assertEquals(LongString.class, output.get("long").getClass());
        Assert.assertEquals(String[].class, output.get("shortarray").getClass());
        Assert.assertEquals(LongString[].class, output.get("longarray").getClass());
        
        Assert.assertEquals(SHORTSTRING, output.get("short"));
        Assert.assertEquals(LONGSTRING, ((LongString)output.get("long")).toString());
        Assert.assertArrayEquals(new String[]{SHORTSTRING, SHORTSTRING, SHORTSTRING}, (String[])output.get("shortarray"));
        LongString[] longStringArray = (LongString[]) output.get("longarray");
        Assert.assertEquals(longStringArray.length, 4);
        Assert.assertEquals(SHORTSTRING, longStringArray[0].toString());
        Assert.assertEquals(SHORTSTRING, longStringArray[1].toString());
        Assert.assertEquals(LONGSTRING, longStringArray[2].toString());
        Assert.assertEquals(LONGSTRING, longStringArray[3].toString());
        
        
        StringType.setLengthLimit(0);

    }

}
