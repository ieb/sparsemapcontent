package uk.co.tfd.sm.resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.ResponseFactory;
import uk.co.tfd.sm.api.resource.ResponseFactoryManager;
import uk.co.tfd.sm.api.resource.binding.ResponseBinding;
import uk.co.tfd.sm.api.resource.binding.ResponseBindingList;
import uk.co.tfd.sm.api.resource.binding.ResponseBindings;
import uk.co.tfd.sm.api.resource.binding.RuntimeResponseBinding;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component(immediate = true, metatype = true)
@Service(value = ResponseFactoryManager.class)
@Reference(name = "responseFactory", referenceInterface = ResponseFactory.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC, strategy = ReferenceStrategy.EVENT, unbind = "unbind", bind = "bind")
public class ResponseFactoryManagerImpl implements ResponseFactoryManager {

	private static final String[] ANY_ARRAY = { BindingSearchKey.ANY };
	private static final String[] EMPTY_ARRAY = { BindingSearchKey.NONE };
	private Map<String, Set<ResponseFactory>> responseFactoryBindingsStore = Maps
			.newHashMap();
	private Map<String, Set<ResponseFactory>> responseFactoryBindings = ImmutableMap
			.of();

	public Adaptable createResponse(Adaptable resource) {
		ResponseBindingList responseBinding = resource
				.adaptTo(ResponseBindingList.class);
		
		Set<BindingSearchKey> searchKeySet = Sets.newHashSet();
		for ( RuntimeResponseBinding rm : responseBinding ) {
			searchKeySet.addAll(rm.getRequestBindingKeys());
		}
		Set<ResponseFactory> responseFactoryCandidates = Sets.newHashSet();
		BindingSearchKey[] searchKeys = searchKeySet.toArray(new BindingSearchKey[searchKeySet.size()]);
		Arrays.sort(searchKeys);
		for ( BindingSearchKey rm : searchKeys ) {
			String bindingKey = rm.getBindingKey();
			Set<ResponseFactory> bindingSet = responseFactoryBindings
					.get(bindingKey);
			if (bindingSet != null) {
				responseFactoryCandidates.addAll(bindingSet);
			}
		}
		if (responseFactoryCandidates.size() == 0) {
			return new DefaultResponse(resource);
		} else {
			return Collections.max(responseFactoryCandidates).getResponse(
					resource);
		}
	}

	protected void bind(ResponseFactory responseFactory) {
		synchronized (responseFactoryBindingsStore) {
			ResponseBindings responseBindings = responseFactory.getClass()
					.getAnnotation(ResponseBindings.class);
			if (responseBindings != null) {
				for (ResponseBinding rb : responseBindings.value()) {
					String[] methods = checkAny(rb.method());
					String[] types = checkAny(rb.type());
					String[] selectors = checkEmpty(rb.selectors());
					String[] extensions = checkEmpty(rb.extension());
					for (String m : methods) {
						for (String t : types) {
							for (String s : selectors) {
								for (String e : extensions) {
									addBinding(new RuntimeResponseBinding(m, t,
											s, e), responseFactory);
								}
							}
						}
					}
				}
			}

			ResponseBindingList bindings = responseFactory.getBindings();
			if (bindings != null) {
				for (RuntimeResponseBinding rm : bindings) {
					addBinding(rm, responseFactory);
				}
			}
			save();
		}
	}

	protected void unbind(ResponseFactory responseFactory) {
		synchronized (responseFactoryBindingsStore) {
			ResponseBindings responseBindings = responseFactory.getClass()
					.getAnnotation(ResponseBindings.class);
			if (responseBindings != null) {
				for (ResponseBinding rb : responseBindings.value()) {
					String[] methods = checkAny(rb.method());
					String[] types = checkAny(rb.type());
					String[] selectors = checkEmpty(rb.selectors());
					String[] extensions = checkEmpty(rb.extension());
					for (String m : methods) {
						for (String t : types) {
							for (String s : selectors) {
								for (String e : extensions) {
									removeBinding(new RuntimeResponseBinding(m,
											t, s, e), responseFactory);
								}
							}
						}
					}
				}
			}

			ResponseBindingList bindings = responseFactory.getBindings();
			if (bindings != null) {
				for (RuntimeResponseBinding rm : bindings) {
					removeBinding(rm, responseFactory);
				}
			}
			save();
		}
	}

	private void save() {
		Builder<String, Set<ResponseFactory>> b = ImmutableMap.builder();
		for (Entry<String, Set<ResponseFactory>> e : responseFactoryBindingsStore
				.entrySet()) {
			b.put(e.getKey(), ImmutableSet.copyOf(e.getValue()));
		}
		responseFactoryBindings = b.build();
	}

	private void removeBinding(RuntimeResponseBinding rm,
			ResponseFactory responseFactory) {
		String bindingKey = rm.getBindingKey();
		Set<ResponseFactory> bindingSet = responseFactoryBindingsStore
				.get(bindingKey);
		if (bindingSet != null) {
			bindingSet.remove(responseFactory);
			if (bindingSet.size() == 0) {
				responseFactoryBindingsStore.remove(bindingKey);
			}
		}
	}

	private void addBinding(RuntimeResponseBinding rm, ResponseFactory rf) {
		String bindingKey = rm.getBindingKey();
		Set<ResponseFactory> bindingSet = responseFactoryBindingsStore
				.get(bindingKey);
		if (bindingSet == null) {
			bindingSet = Sets.newHashSet();
			responseFactoryBindingsStore.put(bindingKey, bindingSet);
		}
		bindingSet.add(rf);
	}

	private String[] checkEmpty(String[] v) {
		if (v == null || v.length == 0) {
			return EMPTY_ARRAY;
		}
		return v;
	}

	private String[] checkAny(String[] v) {
		if (v == null || v.length == 0) {
			return ANY_ARRAY;
		}
		return v;
	}

}
