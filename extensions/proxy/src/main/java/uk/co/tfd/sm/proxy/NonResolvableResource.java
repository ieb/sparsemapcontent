package uk.co.tfd.sm.proxy;

public class NonResolvableResource {

	private String value;
	public NonResolvableResource(String value) {
		this.value = value;
	}
	public boolean isReference() {
		return true;
	}
	@Override
	public String toString() {
		return value;
	}


}
