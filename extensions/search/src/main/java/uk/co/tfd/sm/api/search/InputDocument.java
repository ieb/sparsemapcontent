package uk.co.tfd.sm.api.search;

import java.util.Map.Entry;

public interface InputDocument {

	String getIndexName();

	String getDocumentType();

	String getDocumentId();

	boolean isDelete();

	Iterable<Entry<String, Object>> getKeyData();

	String[] getFieldNames();

	Object getFieldValue(String fieldName);

	void addField(String fieldName, Object principal);

	void setField(String fieldName, Object property);

	boolean contains(String fieldName);

	void removeField(String fieldName);

}
