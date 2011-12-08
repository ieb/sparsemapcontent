package uk.co.tfd.sm.util.http;

import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.google.common.collect.Maps;

public class ParameterUtil {
	public static void testParameters(ModificationRequest m) {
		Map<String, Object> toAdd = m.getParameterSet(ParameterType.ADD);
		Map<String, Object> operation = m
				.getParameterSet(ParameterType.OPERATION);
		Map<String, Object> remove = m.getParameterSet(ParameterType.REMOVE);
		Map<String, Object> special = m.getParameterSet(ParameterType.SPECIAL);
		Assert.assertEquals(3, toAdd.size());
		Assert.assertEquals(1, operation.size());
		Assert.assertEquals(1, remove.size());
		Assert.assertEquals(2, special.size());
		Assert.assertEquals("testString", toAdd.get("test"));
		Assert.assertArrayEquals(new String[] { "testArray" },
				(Object[]) toAdd.get("testArray"));
		Assert.assertArrayEquals(new String[] { "testArraySet", "testArraySet",
				"testArraySet" }, (Object[]) toAdd.get("testArraySet"));
		Assert.assertArrayEquals(new String[] { "testArraySet", "testArraySet",
				"testArraySet" }, (Object[]) remove.get("testDelete"));
		Assert.assertArrayEquals(new String[] { "Operation4", "Operation21",
				"Operation2" }, (Object[]) operation.get("testOperation"));
		Assert.assertArrayEquals(new String[] { "Special3", "Special2",
				"Special1" }, (Object[]) special.get("testSpecial"));
		Assert.assertArrayEquals(new String[] { "Special21", "Special22",
				"Special3" }, (Object[]) special.get("testSpecial2"));
		List<String> feedback = m.getFeedback();
		Assert.assertArrayEquals(new String[] { "Added test",
				"Added testArray", "Added testArraySet", "Removed testDelete",
				"Added testOperation", "Added testSpecial",
				"Added testSpecial2" }, feedback.toArray());
	}

	public static Map<String, String[]> getParameters() {
		Map<String, String[]> parameters = Maps.newLinkedHashMap();
		parameters.put("test", new String[] { "testString" });
		parameters.put("testArray[]", new String[] { "testArray" });
		parameters.put("testArraySet", new String[] { "testArraySet",
				"testArraySet", "testArraySet" });
		parameters.put("testDelete@Delete", new String[] { "testArraySet",
				"testArraySet", "testArraySet" });
		parameters.put(":testOperation", new String[] { "Operation4",
				"Operation21", "Operation2" });
		parameters.put("testSpecial:", new String[] { "Special3", "Special2",
				"Special1" });
		parameters.put("testSpecial2:", new String[] { "Special21",
				"Special22", "Special3" });
		return parameters;
	}

	public static void testProperties(Map<String, Object> properties) {
		Assert.assertEquals("testString", properties.get("test"));
		Assert.assertArrayEquals(new String[] { "testArray" },
				(Object[]) properties.get("testArray"));
		Assert.assertArrayEquals(new String[] { "testArraySet", "testArraySet",
				"testArraySet" }, (Object[]) properties.get("testArraySet"));
	}

	public static void checkResponse(String outputData) {
		Assert.assertEquals("[\n  \"Added test\",\n  \"Added testArray\",\n  \"Added testArraySet\",\n  \"Removed testDelete\",\n  \"Added testOperation\",\n  \"Added testSpecial\",\n  \"Added testSpecial2\"\n]", outputData);
	}

}
