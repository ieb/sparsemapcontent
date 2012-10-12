package uk.co.tfd.sm.search.es.handlers;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import uk.co.tfd.sm.api.search.InputDocument;

import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;

/**
 * A simple map backed input document that uses a configuration map to control what is indexed.
 * @author ieb
 *
 */
public class PropertiesInputDocument implements InputDocument {

	private String indexName;
	private Map<String, Object> properties;
	private String path;
	private String documentType;

	public PropertiesInputDocument(String indexName, String path, 
			Map<String, Object> metadata, Map<String, Object> config) {
		this.indexName = indexName;
		this.path = path;
		this.properties = Maps.newHashMap();
		documentType = (String) properties.get("sling:resourceType");
		String[] indexmappings = (String[]) config.get("sling:indexmapping");
		for ( String indexmap : indexmappings) {
			String[] im = StringUtils.split(indexmap,"=");
			properties.put(im[1], metadata.get(im[0]) );
		}
	}

	@Override
	public String getIndexName() {
		return indexName;
	}

	@Override
	public String getDocumentType() {
		return documentType;
	}

	@Override
	public String getDocumentId() {
		return path;
	}

	@Override
	public boolean isDelete() {
		return false;
	}

	@Override
	public Iterable<Entry<String, Object>> getKeyData() {
		return properties.entrySet();
	}

	@Override
	public String[] getFieldNames() {
		return properties.keySet().toArray(new String[properties.size()]);
	}

	@Override
	public Object getFieldValue(String fieldName) {
		return properties.get(fieldName);
	}

	@Override
	public void addField(String fieldName, Object value) {
		Object o = properties.get(fieldName);
		if ( o == null ) {
			properties.put(fieldName, value);
		} else if ( o instanceof Object[] ) {
			Object[] o2 = Arrays.copyOf((Object[])o, ((Object[]) o).length+1);
			o2[o2.length-1] = value;
			properties.put(fieldName, o2);
		} else {
			Object[] o2 = ObjectArrays.newArray(value.getClass(), 1);
			o2[0] = value;
			properties.put(fieldName, o2);
		}
	}

	@Override
	public void setField(String fieldName, Object value) {
		properties.put(fieldName, value);	
	}

	@Override
	public boolean contains(String fieldName) {
		return properties.containsKey(fieldName);
	}

	@Override
	public void removeField(String fieldName) {
		properties.remove(fieldName);
	}

}
