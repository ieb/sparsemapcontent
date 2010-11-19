package org.sakaiproject.nakamura.api.lite.accesscontrol;

public class AccessDeniedException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = -1838096370030304249L;
    private String objectPath;
    private String objectType;
    private String description;
    private String user;

    public AccessDeniedException(String objectType, String objectPath, String description, String user) {
        this.objectType = objectType;
        this.objectPath = objectPath;
        this.description = description;
        this.user = user;
    }

    @Override
    public String getMessage() {
        return "Denied "+user+" on " + objectType + ":" + objectPath + "  performing " + description;
    }

}
