package uk.co.tfd.sm.api.resource;

import org.sakaiproject.nakamura.api.lite.content.Content;

public class ContentType {

	public static final String PROP_TYPE = "type";
	private String ct;

	public ContentType(Content content) {
		ct = (String) content.getProperty(PROP_TYPE);
		if (ct == null) {
			ct = (String) content.getProperty(Content.MIMETYPE_FIELD);
		}
	}

	public String getType() {
		return ct;
	}

}
