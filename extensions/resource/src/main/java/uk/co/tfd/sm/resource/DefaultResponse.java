package uk.co.tfd.sm.resource;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.ContentType;
import uk.co.tfd.sm.api.resource.SafeMethodResponse;

public class DefaultResponse implements SafeMethodResponse {

	private Adaptable parent;

	public DefaultResponse(Adaptable parent) {
		this.parent = parent;
	}
	
	@GET
	public Response defaultResponse() {
		InputStream in = adaptTo(InputStream.class);
		if ( in == null ) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(adaptTo(InputStream.class), adaptTo(ContentType.class).toString()).build();
	}

	public <T> T adaptTo(Class<T> type) {
		return parent.adaptTo(type);
	}

}
