package uk.co.tfd.sm.util.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.content.Content;

import uk.co.tfd.sm.util.gson.adapters.AuthorizableTypeAdapter;
import uk.co.tfd.sm.util.gson.adapters.CalenderTypeAdapter;
import uk.co.tfd.sm.util.gson.adapters.ContentTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ResponseUtils {

	public static Response getResponse(final int code, final String message) {
		Family family = Family.SERVER_ERROR;
		if ( code < 100 ) {
			family = Family.OTHER;
		} else if ( code < 200 ) {
			family = Family.INFORMATIONAL;
		} else if ( code < 300 ) {
			family = Family.SUCCESSFUL;
		} else if ( code < 400 ) {
			family = Family.REDIRECTION;
		} else if ( code < 500 ) {
			family = Family.CLIENT_ERROR;
		}
		final Family ffamily = family;
		return Response.status(new StatusType() {

			public int getStatusCode() {
				return code;
			}

			public String getReasonPhrase() {
				return message;
			}

			public Family getFamily() {
				return ffamily;
			}
		}).build();
	}

	public static void writeTree(Content content, String[] selectors, OutputStream output) throws UnsupportedEncodingException, IOException {
		GsonBuilder gb = new GsonBuilder();
		if ( contains(selectors, "pp", "tidy") ) {
			gb.setPrettyPrinting();
		}
		int recursion = 0;
		if ( contains(selectors, "-1", "infinity")) {
			recursion = Integer.MAX_VALUE;
		} else if ( selectors != null && selectors.length > 0 ) {
			try {
				recursion = Integer.parseInt(selectors[selectors.length-1]);
			} catch ( NumberFormatException e ) {
				recursion = 0;
			}
		}
		gb.registerTypeHierarchyAdapter(Content.class, new ContentTypeAdapter(recursion));
		gb.registerTypeHierarchyAdapter(Calendar.class, new CalenderTypeAdapter());
		Gson gson = gb.create();
		output.write(gson.toJson(content).getBytes("UTF-8"));
	}

	public static void writeTree(Authorizable authorizable, String format, OutputStream output) throws UnsupportedEncodingException, IOException {
		GsonBuilder gb = new GsonBuilder();
		if ( "pp.json".equals(format) || "tidy.json".equals(format) ) {
			gb.setPrettyPrinting();
		}
		gb.registerTypeHierarchyAdapter(Authorizable.class, new AuthorizableTypeAdapter());
		gb.registerTypeHierarchyAdapter(Calendar.class, new CalenderTypeAdapter());
		Gson gson = gb.create();
		output.write(gson.toJson(authorizable).getBytes("UTF-8"));
	}

	private static boolean contains(String[] selectors, String ... value) {
		if ( selectors == null ) {
			return false;
		}
		for ( String s : selectors) {
			for ( String v : value ) {
				if (v.equals(s)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void writeFeedback(List<String> feedback, OutputStream output) throws UnsupportedEncodingException, IOException {
		output.write(new GsonBuilder().setPrettyPrinting().create().toJson(feedback).getBytes("UTF-8"));
	}


}
