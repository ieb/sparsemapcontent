package uk.co.tfd.sm.api.resource;

import uk.co.tfd.sm.api.resource.binding.ResponseBindingList;

/**
 * A factory for responses.
 * 
 * @author ieb
 * 
 */
public interface ResponseFactory extends Comparable<ResponseFactory> {

	/**
	 * @return any dynamic bindings that the ResponseFactory might decide to
	 *         have. Static bindings are specified with the annotations
	 *         {@link ResponseBindings} and {@link ResponseBinding}
	 */
	ResponseBindingList getBindings();

	/**
	 * Get the response as an Adaptable. The response is handed back to JAX-RS
	 * for further processing and should contain JAX-RS annotations to control
	 * the processing.
	 * 
	 * @param resource
	 * @return a response class instance as an adaptable. If this is no a per
	 *         request instance, it must be thread safe. The normal approach is
	 *         to create a new instance, bind it to the current parent
	 *         adaptable, and hand it back.
	 */
	Adaptable getResponse(Adaptable resource);

}
