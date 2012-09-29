package uk.co.tfd.sm.search.es;

import org.apache.commons.lang.StringUtils;

public class Utils {

	@SuppressWarnings("unchecked")
	public static <T> T get(Object object, Object defaultValue) {
		if (object == null) {
			return (T) defaultValue;
		}
		if (defaultValue instanceof String) {
			return (T) String.valueOf(object);
		} else if (defaultValue instanceof String[]) {
			if (object instanceof String[]) {
				return (T) object;
			}
			return (T) StringUtils.split(String.valueOf(object), ",");
		} else if (defaultValue instanceof Boolean) {
			if (object instanceof Boolean) {
				return (T) object;
			}
			return (T) Boolean.valueOf(String.valueOf(object));
		} else if (defaultValue instanceof Integer) {
			if (object instanceof Integer) {
				return (T) object;
			}
			return (T) Integer.valueOf(String.valueOf(object));

		} else if (defaultValue instanceof Long) {
			if (object instanceof Long) {
				return (T) object;
			}
			return (T) Long.valueOf(String.valueOf(object));
		}
		return (T) object;
	}

}
