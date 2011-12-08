package uk.co.tfd.sm.util.http;

/**
 * Enumeration of property types.
 * @author ieb
 *
 */
public enum ParameterType {
	REMOVE("Removed ","","@Delete"),
	ADD("Added ","",""),
	OPERATION("Added ",":",""),
	SPECIAL("Added ","",":")
	;
    
	private static ParameterType[] CHECK_SEQUENCE = new ParameterType[] {
		REMOVE,
		SPECIAL,
		OPERATION,
		ADD
	}; 
	                                                                  
	private String feedback;
	private String prefix;
	private String suffix;

	private int prefixLength;

	private int suffixLength;

	private ParameterType(String feedback, String prefix, String suffix) {
		this.feedback = feedback;
		this.prefix = prefix;
		this.suffix = suffix;
		this.prefixLength = prefix.length();
		this.suffixLength = suffix.length();
		
	}

	/**
	 * @param propertyName
	 * @return feedback message for the request parameter.
	 */
	public String feedback(String propertyName) {
		return feedback+propertyName;
	}
	
	/**
	 * @param name
	 * @return the property name including any value type information but excluding ParameterType information. 
	 */
	public String getPropertyName(String name) {
		if ( name == null ) {
			return name;
		}
		return name.substring(prefixLength, name.length()-suffixLength);
	}
	
	/**
	 * @param propertyName
	 * @return a ParameterType name.
	 */
	public String getParameterName(String propertyName) {
		return prefix+propertyName+suffix;
	}
	
	/**
	 * @param name
	 * @return the ParameterType for the supplied ParameterName
	 */
	public static ParameterType typeOfRequestParameter(String name) {
		if ( name == null || name.length() == 1) {
			return ADD;
		}
		for ( ParameterType p : CHECK_SEQUENCE) {
			if ( p.prefixLength+p.suffixLength == 0 ) {
				return p;
			} else if ( p.prefixLength == 0 && name.endsWith(p.suffix)) {
				return p;
			} else if ( p.suffixLength == 0 && name.startsWith(p.prefix)) {
				return p;
			} else if ( name.startsWith(p.prefix) && name.endsWith(p.suffix)) {
				return p;
			}
		}
		return ADD;
	}

}
