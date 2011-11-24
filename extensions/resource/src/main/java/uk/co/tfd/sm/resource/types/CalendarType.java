package uk.co.tfd.sm.resource.types;

import java.util.Calendar;
import java.util.Date;

import org.sakaiproject.nakamura.api.lite.util.ISO8601Date;

public class CalendarType implements RequestParameterType<ISO8601Date>{

	@Override
	public String getType() {
		return RequestParameterType.CALENDAR;
	}

	@Override
	public ISO8601Date newInstance(Object value) {
		if ( value instanceof ISO8601Date) {
			return (ISO8601Date) value;
		} else if ( value instanceof Calendar) {
			ISO8601Date c = new ISO8601Date();
			c.setTimeInMillis(((Calendar) value).getTimeInMillis());
			c.setTimeZone(((Calendar) value).getTimeZone());
			return c;	
		} else if ( value instanceof Date) {
			ISO8601Date c = new ISO8601Date();
			c.setTime((Date) value);
			return c;	
		} else if ( value instanceof Long ) {
			ISO8601Date c = new ISO8601Date();
			c.setTimeInMillis((Long) value);
			return c;
		}
		
		return new ISO8601Date(String.valueOf(value));
	}

	@Override
	public Class<ISO8601Date> getComponentType() {
		return ISO8601Date.class;
	}

}
