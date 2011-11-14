package uk.co.tfd.sm.api.resource.binding;

import java.util.Set;

import uk.co.tfd.sm.resource.BindingSearchKey;

import com.google.common.collect.Sets;

/**
 * A response binding.
 * @author ieb
 *
 */
public class RuntimeResponseBinding {

	
	private BindingSearchKey bindingKey;

	private Set<BindingSearchKey> bindingSet = Sets.newHashSet();
	/**
	 * Create a response binding, defaulting any null values to match all values.
	 * @param method
	 * @param type
	 * @param selector
	 * @param extension
	 */
	public RuntimeResponseBinding(String method, String type, String selector, String extension ) {
		bindingKey = new BindingSearchKey(method, type, selector, extension);
		bindingSet.add(bindingKey.anyExtention());
		bindingSet.add(bindingKey.anySelector());
		bindingSet.add(bindingKey.anySelector().anyExtention());
		bindingSet.add(bindingKey.anyType());
		bindingSet.add(bindingKey.anyType().anyExtention());
		bindingSet.add(bindingKey.anyType().anySelector());
		bindingSet.add(bindingKey.anyType().anySelector().anyExtention());
		bindingSet.add(bindingKey.anyMethod());
		bindingSet.add(bindingKey.anyMethod().anyType());
		bindingSet.add(bindingKey.anyMethod().anyType().anyExtention());
		bindingSet.add(bindingKey.anyMethod().anyType().anySelector());
		bindingSet.add(bindingKey.anyMethod().anyType().anySelector().anyExtention());
		
	}
	public String getBindingKey() {
		return bindingKey.getBindingKey();
	}
	
	public Set<BindingSearchKey> getRequestBindingKeys() {
		return bindingSet;
	}

}
