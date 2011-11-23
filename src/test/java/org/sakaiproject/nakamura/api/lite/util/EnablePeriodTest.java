package org.sakaiproject.nakamura.api.lite.util;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class EnablePeriodTest {
    
    @Test
    public void testNone() {
        Calendar[] none = new Calendar[]{null, null};
        Assert.assertArrayEquals(none, EnabledPeriod.getEnabledPeriod(null));
        Assert.assertArrayEquals(none, EnabledPeriod.getEnabledPeriod("sdfsdf"));
        Assert.assertArrayEquals(none, EnabledPeriod.getEnabledPeriod(",sdffds"));
        Assert.assertArrayEquals(none, EnabledPeriod.getEnabledPeriod(",sdffds,"));
        Assert.assertArrayEquals(none, EnabledPeriod.getEnabledPeriod("sdffds,"));
        
    }
    @Test
    public void testFrom() {
        Calendar[] from = new Calendar[]{new ISO8601Date("20110112"), null};
        Assert.assertArrayEquals(from, EnabledPeriod.getEnabledPeriod("2011-01-12,"));
        
    }
    @Test
    public void testTo() {
        Calendar[] from = new Calendar[]{null, new ISO8601Date("20110112")};
        Assert.assertArrayEquals(from, EnabledPeriod.getEnabledPeriod(",2011-01-12"));
        
    }
    @Test
    public void testBetween() {
        Calendar[] from = new Calendar[]{new ISO8601Date("20110124"), new ISO8601Date("20110128")};
        Assert.assertArrayEquals(from, EnabledPeriod.getEnabledPeriod("2011-01-24,2011-01-28"));        
    }
    
    @Test
    public void testPeriod() {
        // test this properly including a time zone differences that cross the date line.
        long testTime = 1321998791404L; // 2011-11-23T08:53 AEST  which makes it 2011-11-22T21:53:11Z
        long testTimeEnd = 1322086391404L; // 2011-11-24T09:13 AEST  which makes it 2011-11-23T22:13:11Z
        Assert.assertNull(EnabledPeriod.getEnableValue(-1, -1, false, TimeZone.getTimeZone("GMT")));
        Assert.assertEquals("2011-11-22T21:53:11Z,",EnabledPeriod.getEnableValue(testTime, -1, false, TimeZone.getTimeZone("GMT")));
        Assert.assertEquals("2011-11-22,",EnabledPeriod.getEnableValue(testTime, -1, true, TimeZone.getTimeZone("GMT")));
        Assert.assertEquals("2011-11-23T08:53:11+11:00,",EnabledPeriod.getEnableValue(testTime, -1, false, TimeZone.getTimeZone("Australia/Sydney")));
        Assert.assertEquals("2011-11-23,",EnabledPeriod.getEnableValue(testTime, -1, true, TimeZone.getTimeZone("Australia/Sydney")));
        
        Assert.assertEquals(",2011-11-23T22:13:11Z",EnabledPeriod.getEnableValue(-1, testTimeEnd, false, TimeZone.getTimeZone("GMT")));
        Assert.assertEquals(",2011-11-23",EnabledPeriod.getEnableValue( -1, testTimeEnd, true, TimeZone.getTimeZone("GMT")));
        Assert.assertEquals(",2011-11-24T09:13:11+11:00",EnabledPeriod.getEnableValue( -1,testTimeEnd, false, TimeZone.getTimeZone("Australia/Sydney")));
        Assert.assertEquals(",2011-11-24",EnabledPeriod.getEnableValue( -1, testTimeEnd, true, TimeZone.getTimeZone("Australia/Sydney")));
        
        Assert.assertEquals("2011-11-22T21:53:11Z,2011-11-23T22:13:11Z",EnabledPeriod.getEnableValue(testTime, testTimeEnd, false, TimeZone.getTimeZone("GMT")));
        Assert.assertEquals("2011-11-22,2011-11-23",EnabledPeriod.getEnableValue( testTime, testTimeEnd, true, TimeZone.getTimeZone("GMT")));
        Assert.assertEquals("2011-11-23T08:53:11+11:00,2011-11-24T09:13:11+11:00",EnabledPeriod.getEnableValue( testTime, testTimeEnd, false, TimeZone.getTimeZone("Australia/Sydney")));
        Assert.assertEquals("2011-11-23,2011-11-24",EnabledPeriod.getEnableValue( testTime, testTimeEnd, true, TimeZone.getTimeZone("Australia/Sydney")));

    }
    
    @Test
    public void testPeriodEnable() {
        long minus24h = System.currentTimeMillis()-48*60*60*1000;
        long minus1h = System.currentTimeMillis()-60*1000;
        long plus24h = System.currentTimeMillis()+48*60*60*1000;
        Assert.assertTrue(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(minus1h, plus24h, false, TimeZone.getTimeZone("GMT"))));
        Assert.assertTrue(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(minus24h, plus24h, true, TimeZone.getTimeZone("GMT"))));
        Assert.assertTrue(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(-1, plus24h, true, TimeZone.getTimeZone("GMT"))));
        Assert.assertTrue(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(minus24h, -1, true, TimeZone.getTimeZone("GMT"))));
        Assert.assertTrue(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(-1, -1, true, TimeZone.getTimeZone("GMT"))));
        
        
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(0, 10, false, TimeZone.getTimeZone("GMT"))));
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(plus24h, minus24h, false, TimeZone.getTimeZone("GMT"))));
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(-1, minus1h, false, TimeZone.getTimeZone("GMT"))));
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(plus24h, -1, false, TimeZone.getTimeZone("GMT"))));
        
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(0, 10, true, TimeZone.getTimeZone("GMT"))));
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(plus24h, minus24h, true, TimeZone.getTimeZone("GMT"))));
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(-1, minus24h, true, TimeZone.getTimeZone("GMT"))));
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(plus24h, -1, true, TimeZone.getTimeZone("GMT"))));
        Assert.assertFalse(EnabledPeriod.isInEnabledPeriod(EnabledPeriod.getEnableValue(-1, minus24h, true, TimeZone.getTimeZone("GMT"))));
    }

}
