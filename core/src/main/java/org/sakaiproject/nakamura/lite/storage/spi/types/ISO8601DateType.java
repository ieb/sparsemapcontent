package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.TimeZone;

import org.sakaiproject.nakamura.api.lite.util.ISO8601Date;

public class ISO8601DateType implements Type<ISO8601Date> {

    public int getTypeId() {
        return 8;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        ISO8601Date calendar = (ISO8601Date) object;
        dos.writeLong(calendar.getTimeInMillis());
        dos.writeUTF(calendar.getTimeZone().getID());
        dos.writeBoolean(calendar.isDate());
    }

    public ISO8601Date load(DataInputStream in) throws IOException {
        long millis = in.readLong();
        TimeZone zone = TimeZone.getTimeZone(in.readUTF());
        boolean date = in.readBoolean();
        ISO8601Date calendar = new ISO8601Date();
        calendar.setTimeInMillis(millis);
        calendar.setTimeZone(zone);
        calendar.setDate(date);
        return calendar;
    }

    public Class<ISO8601Date> getTypeClass() {
        return ISO8601Date.class;
    }

    public boolean accepts(Object object) {
        return (object instanceof ISO8601Date);
    }

}
