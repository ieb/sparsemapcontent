package uk.co.tfd.sm.resource.types;

public class StringType implements RequestParameterType<String> {

	@Override
	public String getType() {
		return RequestParameterType.STRING;
	}

	@Override
	public String newInstance(Object value) {
		return String.valueOf(value);
	}

	@Override
	public Class<String> getComponentType() {
		return String.class;
	}

}
