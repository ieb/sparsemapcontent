package uk.co.tfd.sm.resource.types;


public class BooleanType implements RequestParameterType<Boolean>{

	@Override
	public String getType() {
		return RequestParameterType.BOOLEAN;
	}

	@Override
	public Boolean newInstance(Object value) {
		if ( value instanceof Boolean ) {
			return (Boolean) value;
		}
		return Boolean.parseBoolean(String.valueOf(value));
	}

	@Override
	public Class<Boolean> getComponentType() {
		return Boolean.class;
	}

}
