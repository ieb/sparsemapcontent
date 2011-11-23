package org.sakaiproject.nakamura.api.lite.util;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnabledPeriod {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnabledPeriod.class);

    public static boolean isInEnabledPeriod(String enabledPeriod) {
        Calendar[] period = getEnabledPeriod(enabledPeriod);
        Calendar now = new ISO8601Date();
        now.setTimeInMillis(System.currentTimeMillis());
        if (period[0] != null && period[0].compareTo(now) > 0) {
            return false;
        }
        if (period[1] != null && period[1].compareTo(now) <= 0) {
            return false;
        }
        return true;
    }

    public static Calendar[] getEnabledPeriod(String enabledPeriod) {
        try {
            if (enabledPeriod != null) {
                enabledPeriod = enabledPeriod.trim();
                if (enabledPeriod.startsWith(",")) {
                    return new Calendar[] { null, new ISO8601Date(enabledPeriod.substring(1)) };
                } else if (enabledPeriod.endsWith(",")) {
                    return new Calendar[] {
                            new ISO8601Date(enabledPeriod.substring(0, enabledPeriod.length() - 1)),
                            null };
                } else {
                    String[] period = StringUtils.split(enabledPeriod, ",");
                    return new Calendar[] { new ISO8601Date(period[0]), new ISO8601Date(period[1]) };
                }
            }
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Invalid date specified ", e);
        }
        return new Calendar[] { null, null };
    }

    public static String getEnableValue(long from, long to, boolean day, TimeZone zone) {
        StringBuilder sb = new StringBuilder();
        if (from > 0) {
            ISO8601Date before = new ISO8601Date();
            before.setTimeInMillis(from);
            before.setTimeZone(zone);
            before.setDate(day);
            sb.append(before.toString());
        }
        sb.append(",");
        if (to > 0) {
            ISO8601Date after = new ISO8601Date();
            after.setTimeInMillis(to);
            after.setTimeZone(zone);
            after.setDate(day);
            sb.append(after.toString());
        }
        if (sb.length() > 1) {
            return sb.toString();
        }
        return null;
    }

}
