package org.sakaiproject.nakamura.api.lite.util;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.util.ISO8601Date;

import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;


/**
 *
 */
public class ISO8601DateTest {

  private static final long PERIOD = 24L*24L*60L*60L*1000L+2000L;

  @Test
  public void testParseUTC() {
    long t = 0;
    // london may not be correct
    TimeZone london =  new SimpleTimeZone(0,
        "Europe/London",
        Calendar.MARCH, -1, Calendar.SUNDAY,
        3600000, SimpleTimeZone.UTC_TIME,
        Calendar.OCTOBER, -1, Calendar.SUNDAY,
        3600000, SimpleTimeZone.UTC_TIME,
        3600000);
    TimeZone paris =  new SimpleTimeZone(3600000,
        "Europe/Paris",
        Calendar.MARCH, -1, Calendar.SUNDAY,
        3600000, SimpleTimeZone.UTC_TIME,
        Calendar.OCTOBER, -1, Calendar.SUNDAY,
        3600000, SimpleTimeZone.UTC_TIME,
        3600000);
    TimeZone la = new SimpleTimeZone(-28800000,
        "America/Los_Angeles",
        Calendar.APRIL, 1, -Calendar.SUNDAY,
        7200000,
        Calendar.OCTOBER, -1, Calendar.SUNDAY,
        7200000,
        3600000);
    for (int i = 0; i < 1000; i++ ) {
      t += PERIOD;
      ISO8601Date g = new ISO8601Date();
      g.setTimeZone(london);
      g.setTimeInMillis(t);
      String tt = g.toString();
      ISO8601Date g2 = new ISO8601Date(tt);
      Assert.assertEquals(g.getTimeInMillis(), g2.getTimeInMillis());
      Assert.assertEquals(g.toString(), g2.toString());
    }
    for (int i = 0; i < 1000; i++ ) {
      t += PERIOD;
      ISO8601Date g = new ISO8601Date();
      g.setTimeZone(paris);
      g.setTimeInMillis(t);
      String tt = g.toString();
      ISO8601Date g2 = new ISO8601Date(tt);
      Assert.assertEquals(g.getTimeInMillis(), g2.getTimeInMillis());
      Assert.assertEquals(g.toString(), g2.toString());
    }
    for (int i = 0; i < 1000; i++ ) {
      t += PERIOD;
      ISO8601Date g = new ISO8601Date();
      g.setTimeZone(la);
      g.setTimeInMillis(t);
      String tt = g.toString();
      ISO8601Date g2 = new ISO8601Date(tt);
      Assert.assertEquals(g.getTimeInMillis(), g2.getTimeInMillis());
      Assert.assertEquals(g.toString(), g2.toString());
    }
  }
  
  @Test
  public void testDate() {
    ISO8601Date g = new ISO8601Date();
    g.set(2010, 11, 24);
    g.setDate(true);
    Assert.assertEquals("2010-12-24", g.toString());
  }
  
  @Test
  public void testBefore() {
      ISO8601Date g = new ISO8601Date();
      g.set(2011,11,23);
      Calendar c = Calendar.getInstance();
      c.set(2011, 11, 20);
      Assert.assertTrue(g.compareTo(c)>0);
      g.setDate(true);
      Assert.assertTrue(g.compareTo(c)>0);
      
      
  }
}
