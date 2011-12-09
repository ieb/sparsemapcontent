package uk.co.tfd.sm.util.http;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

public class RequestUtilsTest {

	@Test
	public void testGetFileName() {
		Assert.assertEquals("testname.bin",RequestUtils.getFileName("testname.bin"));
		Assert.assertEquals("testname",RequestUtils.getFileName("testname@stream1"));
		Assert.assertEquals("testname",RequestUtils.getFileName("testname@stream1@xxx"));
		Assert.assertNull(RequestUtils.getFileName(null));
	}

	@Test
	public void testGetStreamName() {
		Assert.assertEquals(null,RequestUtils.getStreamName("testname"));
		Assert.assertEquals("stream1",RequestUtils.getStreamName("testname@stream1"));
		Assert.assertEquals("stream1",RequestUtils.getStreamName("testname@stream1@xxx"));
		Assert.assertNull(RequestUtils.getStreamName(null));
		Assert.assertNull(RequestUtils.getStreamName(""));
		Assert.assertNull(RequestUtils.getStreamName("filedefault"));
		Assert.assertEquals("x",RequestUtils.getStreamName("filedefault@x"));
		Assert.assertEquals("x",RequestUtils.getStreamName("filedefault@x@sdfsdf"));
		Assert.assertEquals("x....",RequestUtils.getStreamName("filedefault@x....@sdfsdf"));
	}
	
	@Test
	public void testPropertyName() {
		Assert.assertNull(RequestUtils.propertyName(null));
		Assert.assertNull(RequestUtils.propertyName(""));
		Assert.assertEquals("propname",RequestUtils.propertyName("propname"));
		Assert.assertEquals("propname",RequestUtils.propertyName("propname@x"));
		Assert.assertEquals("propname",RequestUtils.propertyName("propname[]@x@sdfsdf"));
		Assert.assertEquals("propname",RequestUtils.propertyName("propname@x....@sdfsdf"));
	}
	
	@Test
	public void testPropertyDelete() {
		Assert.assertEquals(ParameterType.REMOVE, ParameterType.typeOfRequestParameter("something@Delete"));
		Assert.assertEquals(ParameterType.ADD, ParameterType.typeOfRequestParameter("something@NotDelete"));
		Assert.assertEquals(ParameterType.ADD, ParameterType.typeOfRequestParameter("something@"));
		Assert.assertEquals(ParameterType.ADD, ParameterType.typeOfRequestParameter("something"));
		Assert.assertEquals(ParameterType.SPECIAL, ParameterType.typeOfRequestParameter("something:"));
		Assert.assertEquals(ParameterType.OPERATION, ParameterType.typeOfRequestParameter(":something"));

	}

	@Test
	public void testToValue() {
		Assert.assertEquals("S",RequestUtils.toValue("test", "S"));
		Assert.assertEquals("S",RequestUtils.toValue("test", new String[]{"S"}));
		Assert.assertArrayEquals(new String[]{"S"},(String[])RequestUtils.toValue("test[]", new String[]{"S"}));
		Assert.assertArrayEquals(new String[]{"S","B"},(String[])RequestUtils.toValue("test", new String[]{"S","B"}));
		Assert.assertArrayEquals(new String[]{"S","B"},(String[])RequestUtils.toValue("test[]", new String[]{"S","B"}));
		Assert.assertEquals("S",RequestUtils.toValue("test@String", "S"));
		Assert.assertEquals("S",RequestUtils.toValue("test@String", new String[]{"S"}));
		Assert.assertArrayEquals(new String[]{"S"},(String[])RequestUtils.toValue("test[]@String", new String[]{"S"}));
		Assert.assertArrayEquals(new String[]{"S","B"},(String[])RequestUtils.toValue("test@String", new String[]{"S","B"}));
		Assert.assertArrayEquals(new String[]{"S","B"},(String[])RequestUtils.toValue("test[]@String", new String[]{"S","B"}));
		
		Assert.assertEquals(1,RequestUtils.toValue("test@Integer", "1"));
		Assert.assertEquals(2,RequestUtils.toValue("test@Integer", new String[]{"2"}));
		Assert.assertArrayEquals(new Integer[]{3},(Integer[])RequestUtils.toValue("test[]@Integer", new String[]{"3"}));
		Assert.assertArrayEquals(new Integer[]{4,5},(Integer[])RequestUtils.toValue("test@Integer", new Integer[]{4,5}));
		Assert.assertArrayEquals(new Integer[]{7,6},(Integer[])RequestUtils.toValue("test[]@Integer", new String[]{"7","6"}));

		Assert.assertEquals(1L,RequestUtils.toValue("test@Long", "1"));
		Assert.assertEquals(2L,RequestUtils.toValue("test@Long", new String[]{"2"}));
		Assert.assertArrayEquals(new Long[]{3L},(Long[])RequestUtils.toValue("test[]@Long", new String[]{"3"}));
		Assert.assertArrayEquals(new Long[]{4L,5L},(Long[])RequestUtils.toValue("test@Long", new String[]{"4","5"}));
		Assert.assertArrayEquals(new Long[]{7L,6L},(Long[])RequestUtils.toValue("test[]@Long", new Long[]{7L,6L}));

		Assert.assertEquals(true,RequestUtils.toValue("test@Boolean", "true"));
		Assert.assertEquals(false,RequestUtils.toValue("test@Boolean", new String[]{"F"}));
		Assert.assertArrayEquals(new Boolean[]{true},(Boolean[])RequestUtils.toValue("test[]@Boolean", new String[]{"true"}));
		Assert.assertArrayEquals(new Boolean[]{true, false},(Boolean[])RequestUtils.toValue("test@Boolean", new Boolean[]{true,false}));
		Assert.assertArrayEquals(new Boolean[]{false, true},(Boolean[])RequestUtils.toValue("test[]@Boolean", new String[]{"0","True"}));

		Assert.assertEquals(1.1,RequestUtils.toValue("test@Double", "1.1"));
		Assert.assertEquals(2.2,RequestUtils.toValue("test@Double", new String[]{"2.2"}));
		Assert.assertArrayEquals(new Double[]{3.3},(Double[])RequestUtils.toValue("test[]@Double", new String[]{"3.3"}));
		Assert.assertArrayEquals(new Double[]{4.4, 5.5},(Double[])RequestUtils.toValue("test@Double", new Double[]{4.4,5.5}));
		Assert.assertArrayEquals(new Double[]{7.7, 6.6},(Double[])RequestUtils.toValue("test[]@Double", new String[]{"7.7","6.6"}));

		
		Calendar c = (Calendar) RequestUtils.toValue("test@Calendar", 0L);
		Assert.assertEquals(0L,c.getTimeInMillis());
		c = (Calendar) RequestUtils.toValue("test@Calendar", "1997-07-14T17:23:11+01:30");
		Assert.assertEquals("GMT+01:30", c.getTimeZone().getDisplayName());
		Assert.assertEquals(1997, c.get(Calendar.YEAR));
		Assert.assertEquals(6, c.get(Calendar.MONTH)); // 0 - 11
		Assert.assertEquals(14, c.get(Calendar.DATE));
		Assert.assertEquals(17, c.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(23, c.get(Calendar.MINUTE));
		Assert.assertEquals(11, c.get(Calendar.SECOND));
	}

}
