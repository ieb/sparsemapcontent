package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CalendarType implements Type<Calendar> {

    public int getTypeId() {
        return 4;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        Calendar calendar = (Calendar) object;
        dos.writeLong(calendar.getTimeInMillis());
        dos.writeUTF(calendar.getTimeZone().getID());
    }

    public Calendar load(DataInputStream in) throws IOException {
        long millis = in.readLong();
        TimeZone zone = TimeZone.getTimeZone(in.readUTF());
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(millis);
        gregorianCalendar.setTimeZone(zone);
        return gregorianCalendar;
    }

    public Class<Calendar> getTypeClass() {
        return Calendar.class;
    }

    public boolean accepts(Object object) {
        return (object instanceof Calendar);
    }

}
