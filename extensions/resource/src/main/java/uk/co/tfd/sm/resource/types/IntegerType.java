package uk.co.tfd.sm.resource.types;

public class IntegerType implements RequestParameterType<Integer> {

	@Override
	public String getType() {
		return RequestParameterType.INTEGER;
	}

	@Override
	public Integer newInstance(Object value) {
		if ( value instanceof Integer ) {
			return (Integer) value;
		}
		return Integer.parseInt(String.valueOf(value));
	}

	@Override
	public Class<Integer> getComponentType() {
		return Integer.class;
	}

}
