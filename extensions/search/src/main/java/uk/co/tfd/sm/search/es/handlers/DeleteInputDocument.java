package uk.co.tfd.sm.search.es.handlers;

import java.util.Map.Entry;

import org.sakaiproject.nakamura.api.lite.util.Iterables;

import uk.co.tfd.sm.api.search.InputDocument;

/**
 * An input documement to be deleted.
 * @author ieb
 *
 */
public class DeleteInputDocument implements InputDocument {

	private static final String[] EMPTY = new String[0];
	@SuppressWarnings("unchecked")
	private static final Entry<String, Object>[] EMPTY_ENTRY = new Entry[0];
	private String path;
	private String indexName;
	private String stringValue;

	public DeleteInputDocument(String indexName, String path) {
		this.indexName = indexName;
		this.path = path;
		stringValue = indexName+": Delete "+path;
	}

	@Override
	public String getIndexName() {
		return indexName;
	}

	@Override
	public String getDocumentType() {
		return null;
	}

	@Override
	public String getDocumentId() {
		return path;
	}

	@Override
	public boolean isDelete() {
		return true;
	}

	@Override
	public Iterable<Entry<String, Object>> getKeyData() {
		return Iterables.of(EMPTY_ENTRY);
	}

	@Override
	public String[] getFieldNames() {
		return EMPTY;
	}

	@Override
	public Object getFieldValue(String fieldName) {
		return null;
	}

	@Override
	public void addField(String fieldName, Object principal) {
	}

	@Override
	public void setField(String fieldName, Object property) {
	}

	@Override
	public boolean contains(String fieldName) {
		return false;
	}

	@Override
	public void removeField(String fieldName) {
	}
	
	@Override
	public String toString() {
		return stringValue;
	}

}
