package uk.co.tfd.sm.api.resource;

/**
 * An error performing some operation on the resource. In the http context this
 * should generate a 500 status response.
 * 
 * @author ieb
 * 
 */
public class ResourceErrorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4860594814908186212L;

	public ResourceErrorException(String message, Exception e) {
		super(message, e);
	}

}
