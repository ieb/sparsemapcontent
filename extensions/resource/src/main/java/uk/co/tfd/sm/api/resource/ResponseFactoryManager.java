package uk.co.tfd.sm.api.resource;

import uk.co.tfd.sm.api.resource.Adaptable;

/**
 * Manages the creation of Responses stimulated by Adaptables.
 * 
 * @author ieb
 * 
 */
public interface ResponseFactoryManager {

	/**
	 * Create the response as an adaptable routing the response creation request
	 * to the most suitable {@link ResponseFactory}
	 * 
	 * @param resource the resource to base this reponse on, as an adaptable.
	 * @return the response that can handle the request, as an adaptable.
	 */
	Adaptable createResponse(Adaptable resource);

}
