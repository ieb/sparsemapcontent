package uk.co.tfd.sm.api.resource.binding;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;


/**
 * A list of response bindings.
 * @author ieb
 *
 */
public class ResponseBindingList implements Iterable<RuntimeResponseBinding> {
	
	private List<RuntimeResponseBinding> list;

	public ResponseBindingList(RuntimeResponseBinding ... bindings) {
		list = ImmutableList.copyOf(bindings);
	}

	public Iterator<RuntimeResponseBinding> iterator() {
		return list.iterator();
	}

}
