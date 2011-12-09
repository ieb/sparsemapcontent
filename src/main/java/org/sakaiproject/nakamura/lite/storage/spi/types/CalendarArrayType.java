package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CalendarArrayType implements Type<Calendar[]> {

    public int getTypeId() {
        return 1004;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        Calendar[] values = (Calendar[]) object;
        dos.writeInt(values.length);
        for ( Calendar s : values) {
            dos.writeLong(s.getTimeInMillis());
            dos.writeUTF(s.getTimeZone().getID());
        }
    }

    public Calendar[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        Calendar[] values = new Calendar[l];
        for ( int i = 0; i < l; i++ ) {
            long millis = in.readLong();
            TimeZone zone = TimeZone.getTimeZone(in.readUTF());
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeInMillis(millis);
            gregorianCalendar.setTimeZone(zone);
            values[i] = gregorianCalendar;
        }
        return values;
    }

    public Class<Calendar[]> getTypeClass() {
        return Calendar[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof Calendar[]);
    }

}
