package uk.co.tfd.sm.resource.types;

public interface RequestParameterType<T> {

	public final static String STRING = "String";
	public static final String INTEGER = "Integer";
	public static final String LONG = "Long";
	public static final String DOUBLE = "Double";
	public static final String CALENDAR = "Calendar";
	public static final String BOOLEAN = "Boolean";

	/**
	 * @return the extenal name of the type.
	 */
	String getType();

	/**
	 * A new instance
	 * @param value the value, not a scalar.
	 * @return
	 */
	T newInstance(Object value);

	/**
	 * @return the class that this RequestParameterType implementation produces.
	 */
	Class<T> getComponentType();

}
