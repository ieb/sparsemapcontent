package uk.co.tfd.sm.resource.types;

import java.util.Calendar;
import java.util.Date;

public class CalendarType implements RequestParameterType<Calendar>{

	@Override
	public String getType() {
		return RequestParameterType.CALENDAR;
	}

	@Override
	public Calendar newInstance(Object value) {
		if ( value instanceof Calendar) {
			return (Calendar) value;
		} else if ( value instanceof Date) {
			Calendar c = Calendar.getInstance();
			c.setTime((Date) value);
			return c;	
		} else if ( value instanceof Long ) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis((Long) value);
			return c;
			
		}
		
		return new ISO8601Date(String.valueOf(value));
	}

	@Override
	public Class<Calendar> getComponentType() {
		// TODO Auto-generated method stub
		return null;
	}

}
