package org.sakaiproject.nakamura.lite.mongo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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
	public void testConvertDBObjectToMapHandlesIds(){
		DBObject doc = new BasicDBObject();
		doc.put(MongoClient.MONGO_INTERNAL_SPARSE_UUID_FIELD, "ID");
		Map<String,Object> cleaned = MongoUtils.convertDBObjectToMap(doc);

		assertTrue(cleaned.containsKey(MongoClient.MONGO_INTERNAL_ID_FIELD));
		assertFalse(cleaned.containsKey(MongoClient.MONGO_INTERNAL_SPARSE_UUID_FIELD));
		assertEquals("ID", cleaned.get(MongoClient.MONGO_INTERNAL_ID_FIELD));
	}

	// --- Dates and Calendars
	@Test
	public void testCleanPropertiesPreservesDates(){
		Calendar cal = new GregorianCalendar();
		Date date = cal.getTime();
		Map<String,Object> props = new HashMap<String, Object>();
		props.put("cal", cal);
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		DBObject set = (DBObject)cleaned.get(Operators.SET);
		assertEquals(date, set.get("cal"));
	}

	@Test
	public void testCleanPropertiesSavesCalendarTimeZone(){
		Calendar cal = new GregorianCalendar();
		Date date = cal.getTime();
		TimeZone tz = cal.getTimeZone();
		Map<String,Object> props = new HashMap<String, Object>();
		props.put("cal", cal);
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		DBObject set = (DBObject)cleaned.get(Operators.SET);
		Date cleanedDate = (Date)set.get("cal");
		String cleanedTz = (String)set.get("_:mongo:tz:cal");
		assertEquals(date, cleanedDate);
		assertEquals(tz.getID(), cleanedTz);
	}

	@Test
	public void testConvertDBObjectToMapHandlesCalendars(){
		DBObject doc = new BasicDBObject();
		Date date = new Date();
		TimeZone tz = TimeZone.getDefault();
		doc.put("cal", date);
		doc.put("_:mongo:tz:cal", tz.getID());
		Map<String,Object> cleaned = MongoUtils.convertDBObjectToMap(doc);

		Calendar cal = (Calendar)cleaned.get("cal");
		assertTrue(cleaned.containsKey("cal"));
		assertFalse(cleaned.containsKey("_:mongo:tz:cal"));
		assertEquals(tz, cal.getTimeZone());
		assertEquals(date, cal.getTime());
	}

	@Test
	public void testCalendars(){
		Calendar cal = new GregorianCalendar();
		Map<String,Object> props = new HashMap<String, Object>();
		props.put("cal", cal);
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		Map<String,Object> props2 = MongoUtils.convertDBObjectToMap((DBObject)cleaned.get(Operators.SET));

		Calendar cal2 = (Calendar)props2.get("cal");
		assertEquals(cal, cal2);
		assertEquals(cal.getTime(), cal2.getTime());
		assertEquals(cal.getTimeZone(), cal2.getTimeZone());
	}

	// --- Big Decimals
	@Test
	public void testCleanPropertiesPreservesBigDecimals(){
		BigDecimal bd = new BigDecimal(BigInteger.TEN);

		Map<String,Object> props = new HashMap<String, Object>();
		props.put("bigone", bd);
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		DBObject set = (DBObject)cleaned.get(Operators.SET);
		assertEquals(bd, new BigDecimal((String)set.get("_:mongo:bd:bigone")));
	}

	@Test
	public void testConvertDBObjectToMapHandlesBigDecimals(){
		DBObject doc = new BasicDBObject();
		BigDecimal bd = new BigDecimal(10);

		doc.put("bigone", bd);
		doc.put("_:mongo:bd:bigone", "10");
		Map<String,Object> cleaned = MongoUtils.convertDBObjectToMap(doc);

		assertTrue(cleaned.containsKey("bigone"));
		assertFalse(cleaned.containsKey("_:mongo:bd:bigone"));

		BigDecimal bd2 = (BigDecimal)cleaned.get("bigone");
		assertEquals(bd, bd2);

	}

	@Test
	public void testBigDecimals(){
		BigDecimal bd = new BigDecimal(10);
		Map<String,Object> props = new HashMap<String, Object>();
		props.put("bigone", bd);
		DBObject cleaned = MongoUtils.cleanPropertiesForInsert(props);
		Map<String,Object> props2 = MongoUtils.convertDBObjectToMap((DBObject)cleaned.get(Operators.SET));
		assertEquals(props.get("bigone"), props2.get("bigone"));
	}

	// -- Field Names
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
