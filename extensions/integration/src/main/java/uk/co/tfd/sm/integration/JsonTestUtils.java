package uk.co.tfd.sm.integration;

import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonTestUtils {
	public static void checkProperty(JsonObject json, String propertyName,
			String propertyValue) {
		JsonElement testProp = json.get(propertyName);
		Assert.assertNotNull(testProp);
		Assert.assertEquals(propertyValue, testProp.getAsString());
	}
	public static void checkProperty(JsonObject json, String propertyName,
			Boolean[] booleans) {
		JsonArray jsonArray = toJsonArray(json.get(propertyName));
		Assert.assertEquals(booleans.length, jsonArray.size());
		for ( int i = 0; i < booleans.length; i++ ) {
			Assert.assertEquals(booleans[i], jsonArray.get(i).getAsBoolean());
		}
	}

	public static void checkProperty(JsonObject json, String propertyName,
			boolean propertyValue) {
		JsonElement testProp = json.get(propertyName);
		Assert.assertNotNull(testProp);
		Assert.assertEquals(propertyValue, testProp.getAsBoolean());
	}

	public static void checkProperty(JsonObject json, String propertyName, int[] values) {
		JsonArray jsonArray = toJsonArray(json.get(propertyName));
		Assert.assertEquals(values.length, jsonArray.size());
		for ( int i = 0; i < values.length; i++ ) {
			Assert.assertEquals(values[i], jsonArray.get(i).getAsInt());
		}
	}

	public static void checkProperty(JsonObject json, String propertyName, String[] values) {
		JsonArray jsonArray = toJsonArray(json.get(propertyName));
		Assert.assertEquals(values.length, jsonArray.size());
		for ( int i = 0; i < values.length; i++ ) {
			Assert.assertEquals(values[i], jsonArray.get(i).getAsString());
		}
	}
	public static void checkProperty(JsonObject json, String propertyName,
			Double[] values) {
		JsonArray jsonArray = toJsonArray(json.get(propertyName));
		Assert.assertEquals(values.length, jsonArray.size());
		for ( int i = 0; i < values.length; i++ ) {
			Assert.assertEquals((Double)values[i], (Double)jsonArray.get(i).getAsDouble());
		}
	}
	public static void checkProperty(JsonObject json, String propertyName,
			double propertyValue) {
		JsonElement testProp = json.get(propertyName);
		Assert.assertNotNull(testProp);
		Assert.assertEquals((Double)propertyValue, (Double)testProp.getAsDouble());
	}
	
	public static void checkProperty(JsonObject json, String propertyName,
			Integer[] values) {
		JsonArray jsonArray = toJsonArray(json.get(propertyName));
		Assert.assertEquals(values.length, jsonArray.size());
		for ( int i = 0; i < values.length; i++ ) {
			Assert.assertEquals(values[i], (Integer)jsonArray.get(i).getAsInt());
		}
	}

	public static void checkProperty(JsonObject json, String propertyName,
			Integer propertyValue) {
		JsonElement testProp = json.get(propertyName);
		Assert.assertNotNull(testProp);
		Assert.assertEquals((Integer)propertyValue, (Integer)testProp.getAsInt());
	}

	public static void checkProperty(JsonObject json, String propertyName,
			Long[] values) {
		JsonArray jsonArray = toJsonArray(json.get(propertyName));
		Assert.assertEquals(values.length, jsonArray.size());
		for ( int i = 0; i < values.length; i++ ) {
			Assert.assertEquals(values[i], (Long)jsonArray.get(i).getAsLong());
		}
	}

	public static void checkProperty(JsonObject json, String propertyName,
			Long propertyValue) {
		JsonElement testProp = json.get(propertyName);
		Assert.assertNotNull(testProp);
		Assert.assertEquals((Long)propertyValue, (Long)testProp.getAsLong());
	}

	public static Set<String> toResponseSet(JsonElement jsonElement) {
		Set<String> result = Sets.newHashSet();
		JsonArray responseArray = toJsonArray(jsonElement);
		for (int i = 0; i < responseArray.size(); i++) {
			JsonElement je = responseArray.get(i);
			result.add(je.getAsString());
		}
		return result;
	}

	public static JsonArray toJsonArray(JsonElement jsonElement) {
		Assert.assertNotNull(jsonElement);
		Assert.assertTrue(jsonElement.isJsonArray());
		return jsonElement.getAsJsonArray();
	}

	public static JsonObject toJsonObject(JsonElement jsonElement) {
		Assert.assertNotNull(jsonElement);
		Assert.assertTrue(jsonElement.isJsonObject());
		return jsonElement.getAsJsonObject();
	}


}
