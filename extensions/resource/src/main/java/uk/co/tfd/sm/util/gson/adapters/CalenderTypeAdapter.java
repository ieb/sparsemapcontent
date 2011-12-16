package uk.co.tfd.sm.util.gson.adapters;

import java.lang.reflect.Type;
import java.util.Calendar;

import org.sakaiproject.nakamura.api.lite.util.ISO8601Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CalenderTypeAdapter implements JsonSerializer<Calendar>{

	@Override
	public JsonElement serialize(Calendar calendar, Type calendarType,
			JsonSerializationContext context) {
		if ( calendar instanceof ISO8601Date ) {
			return new JsonPrimitive(calendar.toString());
		}
		ISO8601Date d = new ISO8601Date();
		d.setTimeInMillis(calendar.getTimeInMillis());
		d.setTimeZone(calendar.getTimeZone());
		return new JsonPrimitive(d.toString());
	}

}
