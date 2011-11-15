package uk.co.tfd.sm.resource.types;

public class DoubleType  implements RequestParameterType<Double> {

	@Override
	public String getType() {
		return RequestParameterType.DOUBLE;
	}

	@Override
	public Double newInstance(Object value) {
		if ( value instanceof Double ) {
			return (Double) value;
		}
		return Double.parseDouble(String.valueOf(value));
	}

	@Override
	public Class<Double> getComponentType() {
		return Double.class;
	}

}
