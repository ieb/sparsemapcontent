package uk.co.tfd.sm.api.search;

import java.util.Map.Entry;

public interface InputDocument {

	String getIndexName();

	String getDocumentType();

	String getDocumentId();

	boolean isDelete();

	Iterable<Entry<String, Object>> getKeyData();

}
