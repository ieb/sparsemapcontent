package uk.co.tfd.sm.search.es;

import org.apache.commons.lang.StringUtils;

public class Utils {

	public static String getParentPath(String path) {
		if ("/".equals(path)) {
			return "/";
		}
		int i = path.lastIndexOf('/');
		if (i == path.length() - 1) {
			i = path.substring(0, i).lastIndexOf('/');
		}
		String res = path;
		if (i > 0) {
			res = path.substring(0, i);
		} else if (i == 0) {
			return "/";
		}
		return res;
	}

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
