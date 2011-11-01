package uk.co.tfd.sm.api.resource;

/**
 * Access to the Resource in the mode attempted was forbidden.
 * 
 * @author ieb
 * 
 */
public class ResourceForbiddenException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5403610617596262186L;

	public ResourceForbiddenException(String message, Exception e) {
		super(message, e);
	}

}
