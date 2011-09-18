package ignore.com.bradmcevoy.http.webdav;


public class RuntimeBadRequestException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -1004634408897616343L;
	private String reason;

	public RuntimeBadRequestException(String message, Exception e) {
		super(message, e);
		this.reason = message;
	}

	public String getReason() {
		return reason;
	}

	
}
