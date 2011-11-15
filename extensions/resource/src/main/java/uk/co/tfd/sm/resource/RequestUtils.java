package uk.co.tfd.sm.resource;

import java.lang.reflect.Array;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.resource.types.BooleanType;
import uk.co.tfd.sm.resource.types.CalendarType;
import uk.co.tfd.sm.resource.types.DoubleType;
import uk.co.tfd.sm.resource.types.IntegerType;
import uk.co.tfd.sm.resource.types.LongType;
import uk.co.tfd.sm.resource.types.RequestParameterType;
import uk.co.tfd.sm.resource.types.StringType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class RequestUtils {

	private static final Logger LOGGER = LoggerFactory
	.getLogger(RequestUtils.class);
	@SuppressWarnings("unchecked")
	private static final Class<RequestParameterType<?>>[] TYPE_CLASSES = new Class[] { 
		StringType.class,
		IntegerType.class,
		LongType.class,
		BooleanType.class,
		CalendarType.class,
		DoubleType.class};
	private static final Map<String, RequestParameterType<?>> TYPES = createScalarTypes();

	public static boolean isDelete(String name) {
		return name.endsWith("@Delete");
	}

	private static Map<String, RequestParameterType<?>> createScalarTypes() {
		Builder<String, RequestParameterType<?>> b = ImmutableMap.builder();
		for (Class<RequestParameterType<?>> typeClass : TYPE_CLASSES) {
			try {
				RequestParameterType<?> o = typeClass.newInstance();
				b.put(o.getType(), o);
			} catch (InstantiationException e) {
				LOGGER.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return b.build();
	}

	/**
	 * @param name
	 *            the property name complete with type hints operations or
	 *            stream names.
	 * @return Just the property name excluding [] markers and all type, stream
	 *         hints.
	 */
	public static String propertyName(String name) {
		String[] parts = StringUtils.split(name, "@", 2);
		if (parts == null || parts.length == 0 ) {
			return null;
		}
		String propertyName = parts[0];
		if (propertyName.endsWith("[]")) {
			propertyName = propertyName.substring(0, propertyName.length() - 2);
		}
		return propertyName;
	}

	/**
	 * Creates the most suitable type.
	 * 
	 * @param name
	 *            the name. Types are specified as name@Type where Type is the
	 *            name of the type. If the name ends with a [] the type is
	 *            forced to be an array.
	 * @param value
	 *            the value the value. If its an array, the type is assumed to
	 *            be an array provided there is more than one element. If there
	 *            is one element and the name ends with [] an array is created,
	 *            otherwise a scalar is created.
	 * @return an instance
	 */
	public static Object toValue(String name, Object value) {
		String[] parts = StringUtils.split(name, "@", 2);
		String fieldName = null;
		String fieldType = "String";
		if (parts.length == 2) {
			fieldType = parts[1];
			fieldName = parts[0];
		} else if (parts.length == 1) {
			fieldName = parts[0];
		} else {
			throw new IllegalArgumentException("Invalid property name");
		}
		try {
			int l = Array.getLength(value);
			RequestParameterType<?> rpt = TYPES.get(fieldType);
			if (rpt == null) {
				rpt = TYPES.get(RequestParameterType.STRING);
			}
			if (!fieldName.endsWith("[]") && l == 1) {
				return rpt.newInstance(Array.get(value, 0));
			}
			Class<?> componentType = rpt.getComponentType();
			Object[] a = (Object[]) Array.newInstance(componentType, l);
			for (int i = 0; i < l; i++) {
				a[i] = rpt.newInstance(Array.get(value, i));
			}
			return a;
		} catch (IllegalArgumentException e) {
			RequestParameterType<?> rpt = TYPES.get(fieldType);
			if (rpt == null) {
				rpt = TYPES.get(RequestParameterType.STRING);
			}
			return rpt.newInstance(value);
		}
	}

	/**
	 * @param name
	 *            the property name, with the stream specified as
	 *            name@StreamName. Only file uploads have stream names.
	 * @return the stream name or null if there is none.
	 */
	public static String getStreamName(String name) {
		
		String[] parts = StringUtils.split(name, "@", 2);
		if (parts != null && parts.length == 2) {
			return parts[1];
		}
		return null;
	}

}
