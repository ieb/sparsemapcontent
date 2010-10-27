package org.sakaiproject.nakamura.api.lite.accesscontrol;

public class AccessDeniedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1838096370030304249L;
	private String objectPath;
	private String objectType;
	private String description;

	public AccessDeniedException( String objectType, String objectPath, String description) {
		this.objectType =objectType;
		this.objectPath = objectPath;
		this.description = description;
	}
	
	@Override
	public String getMessage() {
		return "Denied for "+objectType+":"+objectPath+"  performing "+description;
	}



}
