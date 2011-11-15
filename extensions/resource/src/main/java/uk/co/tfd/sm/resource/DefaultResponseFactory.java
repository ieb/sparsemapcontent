package uk.co.tfd.sm.resource;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.ResponseFactory;
import uk.co.tfd.sm.api.resource.binding.ResponseBinding;
import uk.co.tfd.sm.api.resource.binding.ResponseBindingList;
import uk.co.tfd.sm.api.resource.binding.ResponseBindings;
import uk.co.tfd.sm.api.resource.binding.RuntimeResponseBinding;

@ResponseBindings(value = { @ResponseBinding(method = { "GET" }, extension = {}, selectors = {}, type = {}) })
public class DefaultResponseFactory implements ResponseFactory {

	public int compareTo(ResponseFactory arg0) {
		return 1; // always last
	}

	public ResponseBindingList getBindings() {
		return new ResponseBindingList(new RuntimeResponseBinding("GET",BindingSearchKey.ANY, BindingSearchKey.ANY, BindingSearchKey.ANY));
	}

	public Adaptable getResponse(Adaptable adaptable) {
		return new DefaultResponse(adaptable);
	}
}
