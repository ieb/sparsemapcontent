package uk.co.tfd.sm.util.http;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

public class ResponseUtilsTest {

	@Test
	public void test() {
		testResponse(99, "testMessage");
		testResponse(100, "testMessage");
		testResponse(200, "testMessage");
		testResponse(201, "testMessage");
		testResponse(300, "testMessage");
		testResponse(301, "testMessage");
		testResponse(400, "testMessage");
		testResponse(401, "testMessage");
		testResponse(500, "testMessage");
	}

	private void testResponse(int code, String message) {
		Response r = ResponseUtils.getResponse(code, message);// TODO Auto-generated method stub
		Assert.assertEquals(code,r.getStatus());
		
	}
}
