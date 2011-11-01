package uk.co.tfd.sm.api.resource.binding;

/**
 * A response binding.
 * @author ieb
 *
 */
public class RuntimeResponseBinding {

	/**
	 * Matches any combination of the type.
	 */
	public static final String ANY = "ANY";

	/**
	 * Requires that there are none of the type to match the binding.
	 */
	public static final String NONE = "NONE";
	
	private String bindingKey;
	/**
	 * Create a response binding, defaulting any null values to match all values.
	 * @param method
	 * @param type
	 * @param selector
	 * @param extension
	 */
	public RuntimeResponseBinding(String method, String type, String selector, String extension ) {
		bindingKey = checkAny(method)+";"+checkAny(type)+";"+checkAny(selector)+";"+checkAny(extension);
	}
	private String checkAny(String v) {
		if ( v == null || v.trim().length() == 0 ) {
			return ANY;
		}
		return v;
	}
	public String getBindingKey() {
		return bindingKey;
	}

}
