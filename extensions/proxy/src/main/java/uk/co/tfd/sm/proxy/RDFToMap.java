package uk.co.tfd.sm.proxy;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("restriction")
public class RDFToMap {

	private static final String FQ_ABOUT = "http://www.w3.org/1999/02/22-rdf-syntax-ns#about";
	private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final QName RDF_RESOURCE = new QName(RDF_NS, "resource");
	private static final QName RDF_ABOUT = new QName(RDF_NS, "about");
	private static final QName RDF_DESCRIPTION = new QName(RDF_NS,
			"Description");
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RDFToMap.class);
	private XMLInputFactory xmlInputFactory;
	private Map<String, String> nsPrefixMap;
	private Map<String, Map<String, Object>> tripleMap;
	private Map<String, Object> resolvedMap;

	public RDFToMap(Map<String, String> nsPrefixMap) {
		init(nsPrefixMap);
	}
	

	public RDFToMap(String namespaceMapConfig) {
		String[] pairs = StringUtils.split(namespaceMapConfig, ";");
		Builder<String, String> b = ImmutableMap.builder();
		if ( pairs != null ) {
			for (String pair : pairs) {
				String[] kv = StringUtils.split(pair, "=", 2);
				
				if (kv == null || kv.length == 0 ) {
					throw new RuntimeException(
							"Names space key value pairs must be of the form ns=nsuri;ns=nsuri failed to parse "
									+ namespaceMapConfig);
				} else if ( kv.length == 1) {
					b.put(kv[0],"");
				} else {
					b.put(kv[1], kv[0]);
				}
			}
		}
		init(b.build());
	}


	private void init(Map<String, String> nsPrefixMap) {
		xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		this.nsPrefixMap = nsPrefixMap;
	}


	public RDFToMap readMap(Reader reader)
			throws XMLStreamException {

		XMLEventReader eventReader = xmlInputFactory
				.createXMLEventReader(reader);
		// Create a filtered reader
		XMLEventReader filteredEventReader = xmlInputFactory
				.createFilteredReader(eventReader, new EventFilter() {

					public boolean accept(XMLEvent event) {
						// Exclude PIs
						return (!event.isProcessingInstruction());
					}
				});

		tripleMap = Maps.newHashMap();
		Map<String, Object> currentMap = null;
		String key = null;
		StringBuilder body = null;
		int state = 1;
		while (filteredEventReader.hasNext()) {
			XMLEvent e = filteredEventReader.nextEvent();
			switch (e.getEventType()) {
			case XMLEvent.START_ELEMENT:
				StartElement startElement = e.asStartElement();
				QName name = startElement.getName();
				if (state == 1 && name.equals(RDF_DESCRIPTION)) {
					Attribute desc = startElement.getAttributeByName(RDF_ABOUT);
					currentMap = getMap(desc.getValue(), tripleMap);

					state = 2;
				} else if (state == 2) {
					Attribute resource = startElement
							.getAttributeByName(RDF_RESOURCE);
					key = name.getNamespaceURI() + name.getLocalPart();
					if (resource != null) {
						putMap(currentMap, key,
								"rdf:resource:" + processNamespaceURI(resource.getValue()));
						state = 4;
					} else {
						body = new StringBuilder();
						state = 3;
					}
				}
				break;
			case XMLEvent.END_ELEMENT:
				if (state == 2) {
					currentMap = null;
					state = 1;
				} else if (state == 3) {
					putMap(currentMap, key, body.toString());
					body = null;
					key = null;
					state = 2;
				} else if (state == 4) {
					state = 2;
				}
				break;
			case XMLEvent.CDATA:
			case XMLEvent.CHARACTERS:
				if (state == 3) {
					// accumulate the body
					Characters characters = e.asCharacters();
					if (characters.isWhiteSpace()
							&& !characters.isIgnorableWhiteSpace()
							|| !characters.isWhiteSpace()) {
						body.append(characters.getData());
					}

				}
				break;
			}
		}
		return this;
	}

	public RDFToMap resolveToFullJson() {
		Set<String> resolving = Sets.newHashSet();
		resolvedMap = Maps.newHashMap();
		resolveToFullJson(resolvedMap, tripleMap, tripleMap, resolving);
		Map<String, String> invertedNsPrefixMap = invertMap(nsPrefixMap);
		accumulate(resolvedMap, "_namespaces", invertedNsPrefixMap);
		accumulate(resolvedMap, "_default", invertedNsPrefixMap.get(""));
		return this;
	}
	
	
	public String toJson(boolean indented) {
		if (indented ) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			return gson.toJson(resolvedMap);
		} else {
			Gson gson = new Gson();
			return gson.toJson(resolvedMap);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void accumulate(Map<String, Object> output, String key,
			Object value) {
		if (output.containsKey(key)) {
			Object o = output.get(key);
			if ( o instanceof List) {
				((List) o).add(value);
			} else {
				output.put(key, Lists.newArrayList(o, value));
			}
		} else {
			output.put(key, value);
		}
	}

	private Map<String, String> invertMap(Map<String, String> map) {
		Builder<String, String> b = ImmutableMap.builder();
		for ( Entry<String, String> e : map.entrySet()) {
			b.put(e.getValue(), e.getKey());
		}
		return b.build();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void resolveToFullJson(Map<String, Object> output,
			Map<String, Map<String, Object>> baseMap, Map<String, ?> m, Set<String> resolving) {
		for (Entry<String, ?> e : m.entrySet()) {
			Object o = e.getValue();
			if (o instanceof Map) {
				Map<String, Object> nobj = Maps.newHashMap();
				accumulate(output, e.getKey(), nobj);
				accumulate(nobj, processNamespaceURI(FQ_ABOUT), e.getKey());
				resolveToFullJson(nobj, baseMap, (Map<String, ?>) o, resolving);
			} else if (o instanceof Set) {
				for (Object ov : (Set) o) {
					accumulate(output, e.getKey(), ov);
				}
			} else if ( o instanceof String && ((String) o).startsWith("rdf:resource:")) {
				String key = ((String) o).substring("rdf:resource:".length());
				if (!resolving.contains(key) && baseMap.containsKey(key) && baseMap.get(key) instanceof Map  ) {
					Map<String, Object> nobj = Maps.newHashMap();
					accumulate(output, e.getKey(), nobj);
					accumulate(nobj, processNamespaceURI(FQ_ABOUT), key);
					resolving.add(key);
					resolveToFullJson(nobj, baseMap, baseMap.get(key), resolving);
					resolving.remove(key);
				} else {
					accumulate(output, e.getKey(), e.getValue());
				}
			} else {
				accumulate(output, e.getKey(), e.getValue());
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void putMap(Map<String, Object> map, String keyWithNamespace, String value) {
		String key = processNamespaceURI(keyWithNamespace);
		if (map.containsKey(key)) {
			Object o = map.get(key);
			if (o instanceof Set) {
				((Set) o).add(value);
			} else {
				map.put(key, Sets.newHashSet((String) o, value));
			}
		} else {
			map.put(key, value);
		}

	}

	private Map<String, Object> getMap(String keyWithNamespace,
			Map<String, Map<String, Object>> tripleMap) {
		String key = processNamespaceURI(keyWithNamespace);
		if (tripleMap.containsKey(key)) {
			LOGGER.info("Map for {} already exists ", key);
			return tripleMap.get(key);

		} else {
			Map<String, Object> m = Maps.newHashMap();
			tripleMap.put(key, m);
			return m;
		}
	}

	private String processNamespaceURI(String keyWithNamespace) {
		for ( Entry<String, String> e : nsPrefixMap.entrySet() ) {
			if ( keyWithNamespace.startsWith(e.getKey())) {
				String ns = e.getValue();
				if ( ns.length() > 0 ) {				
					return ns+"_"+keyWithNamespace.substring(e.getKey().length());
				} else {
					return keyWithNamespace.substring(e.getKey().length());
				}
			}
		}
		return keyWithNamespace;
	}


	public Map<String, Object> toMap() {
		return resolvedMap;
	}

}
