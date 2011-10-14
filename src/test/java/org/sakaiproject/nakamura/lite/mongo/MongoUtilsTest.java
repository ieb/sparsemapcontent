package org.sakaiproject.nakamura.lite.mongo;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.RemoveProperty;
import org.sakaiproject.nakamura.lite.storage.mongo.MongoClient;
import org.sakaiproject.nakamura.lite.storage.mongo.MongoUtils;
import org.sakaiproject.nakamura.lite.storage.mongo.Operators;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoUtilsTest extends TestCase {
	
	private static final String fieldName = "dots.and$_x";
	private static final String escaped = "dots" + MongoUtils.MONGO_FIELD_DOT_REPLACEMENT + "and"
									+ MongoUtils.MONGO_FIELD_DOLLAR_REPLACEMENT + "_x";
	
	@Test
	public void testCleanPropertiesPrunesNulls(){
		Map<String,Object> props = new HashMap<String, Object>();
		props.put("NULL", null);
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		assertTrue(cleaned.keySet().isEmpty());
	}
	
	@Test
	public void testCleanPropertiesHandlesRemoveProperty(){
		Map<String,Object> props = new HashMap<String, Object>();
		props.put("toRemove", new RemoveProperty());
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		DBObject unset = (DBObject)cleaned.get(Operators.UNSET);
		assertFalse(cleaned.containsField(Operators.SET));
		assertTrue(unset.containsField("toRemove"));
	}
	
	@Test
	public void testCleanPropertiesHandlesIds(){
		Map<String,Object> props = new HashMap<String, Object>();
		props.put(MongoClient.MONGO_INTERNAL_ID_FIELD, "ID");
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		assertTrue(cleaned.keySet().isEmpty());
	}

	@Test
	public void testCleanPropertiesConvertsCalendarsToDates(){
		Calendar cal = new GregorianCalendar();
		Date date = cal.getTime();
		Map<String,Object> props = new HashMap<String, Object>();
		props.put("cal", cal);
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		DBObject set = (DBObject)cleaned.get(Operators.SET);
		assertEquals(date, (Date)set.get("cal"));
	}
	
	@Test
	public void testConvertDBObjectToMapHandlesIds(){
		DBObject doc = new BasicDBObject();
		doc.put(MongoClient.MONGO_INTERNAL_SPARSE_UUID_FIELD, "ID");
		Map<String,Object> cleaned = MongoUtils.convertDBObjectToMap(doc);
		
		assertTrue(cleaned.containsKey(MongoClient.MONGO_INTERNAL_ID_FIELD));
		assertFalse(cleaned.containsKey(MongoClient.MONGO_INTERNAL_SPARSE_UUID_FIELD));
		assertEquals("ID", cleaned.get(MongoClient.MONGO_INTERNAL_ID_FIELD));
	}
	
	@Test
	public void testEscapeFieldName(){
		assertEquals(escaped, MongoUtils.escapeFieldName(fieldName));
		assertEquals("_id", MongoUtils.escapeFieldName("_id"));
		assertEquals("homer", MongoUtils.escapeFieldName("homer"));
	}
	
	@Test
	public void testUnescapeFieldName(){
		assertEquals(fieldName, MongoUtils.unescapeFieldName(escaped));
		assertEquals("_id", MongoUtils.unescapeFieldName("_id"));
		assertEquals("homer", MongoUtils.unescapeFieldName("homer"));
	}
}
