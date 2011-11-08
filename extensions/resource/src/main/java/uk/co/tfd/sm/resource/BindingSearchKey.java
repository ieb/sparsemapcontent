package uk.co.tfd.sm.resource;

public class BindingSearchKey implements Comparable<BindingSearchKey> {

	/**
	 * Matches any combination of the type.
	 */
	public static final String ANY = "ANY";

	/**
	 * Requires that there are none of the type to match the binding.
	 */
	public static final String NONE = "NONE";

	private String method;
	private String type;
	private String selector;
	private String extension;

	private String key;

	private int hashCode;

	private int sortOrder;

	public BindingSearchKey(String method, String type,
			String selector, String extension) {
		this.method = checkAny(method);
		this.type = checkAny(type);
		this.selector = checkAny(selector);
		this.extension = checkAny(extension);
		init();
	}
	
	private void init() {
		key = method+";"+type+";"+selector+";"+extension;
		hashCode = key.hashCode();
		sortOrder = 0;
		if ( !ANY.equals(method) ) {
			sortOrder += 8;
		}
		if ( !ANY.equals(type) ) {
			sortOrder += 4;
		}
		if ( !ANY.equals(selector) ) {
			sortOrder += 2;
		}
		if ( !ANY.equals(extension) ) {
			sortOrder += 1;
		}
	}

	private String checkAny(String v) {
		if ( v == null || v.trim().length() == 0 ) {
			return BindingSearchKey.ANY;
		}
		return v;
	}

	public String getBindingKey() {
		return key;
	}


	public BindingSearchKey anyExtention() {
		if ( ANY.equals(extension)) {
			return this;
		}
		return new BindingSearchKey(method, type, selector, ANY);
	}

	public BindingSearchKey anySelector() {
		if ( ANY.equals(selector)) {
			return this;
		}
		return new BindingSearchKey(method, type, ANY, extension);
	}


	public BindingSearchKey anyType() {
		if ( ANY.equals(type)) {
			return this;
		}
		return new BindingSearchKey(method, ANY, type, extension);
	}

	public BindingSearchKey anyMethod() {
		if ( ANY.equals(method)) {
			return this;
		}
		return new BindingSearchKey(ANY, type, selector, extension);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	@Override
	public boolean equals(Object obj) {
		if ( obj == null ) {
			return false;
		}
		return hashCode() == obj.hashCode();
	}

	@Override
	public int compareTo(BindingSearchKey o) {
		return o.sortOrder - sortOrder;
	}
	

}
