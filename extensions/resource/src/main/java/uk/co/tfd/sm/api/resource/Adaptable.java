package uk.co.tfd.sm.api.resource;

/**
 * Adaptables can adapt to other things. To achieve the adaption the adaptable
 * may chain to other Adaptables.
 * 
 * @author ieb
 * 
 */
public interface Adaptable {

	/**
	 * Adapt this adaptable to the type requested.
	 * @param <T>
	 * @param type
	 * @return the requested type or null if not possible.
	 */
	<T> T adaptTo(Class<T> type);

}
