package uk.co.tfd.sm.resource;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.Resource;
import uk.co.tfd.sm.api.resource.ResponseFactory;
import uk.co.tfd.sm.api.resource.binding.ResponseBinding;
import uk.co.tfd.sm.api.resource.binding.ResponseBindingList;
import uk.co.tfd.sm.api.resource.binding.ResponseBindings;

@ResponseBindings(value={
		@ResponseBinding(method={"GET"},extension={},selectors={},type={})
})
public class DefaultGetResponseFactory implements ResponseFactory {

	
	public int compareTo(ResponseFactory arg0) {
		return 1; // always last
	}

	public ResponseBindingList getBindings() {
		return new ResponseBindingList();
	}

	public Adaptable getResponse(final Adaptable adaptable) {
		return new Adaptable() {
			
			@GET
			public Response doGet() {
				Resource resource = adaptable.adaptTo(Resource.class);
				return null;
			}
			
			public <T> T adaptTo(Class<T> type) {
				return adaptable.adaptTo(type);
			}
		};
	}

}
