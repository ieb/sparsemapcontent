package uk.co.tfd.sm.http.batch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.util.Dictionary;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

public class BatchProcessor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BatchProcessor.class);
	private Cache<String> responseCache;
	
	public BatchProcessor(Cache<String> cache) {
		this.responseCache = cache;
	}

	/**
	 * Takes the original request and starts the batching.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void batchRequest(HttpServletRequest request,
			HttpServletResponse response, String jsonRequest,
			boolean allowModify) throws IOException, ServletException {
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(jsonRequest);
		if (!element.isJsonArray()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Failed to parse the requests parameter");
			return;
		}

		JsonArray arr = element.getAsJsonArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String key = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			key = Base64.encodeBase64URLSafeString(md.digest(jsonRequest
					.getBytes("UTF-8")));
			String cachedResult = responseCache.get(key);
			if (cachedResult != null) {
				LOGGER.info("Using Cache");
				response.getWriter().write(cachedResult);
				return;
			}
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}

		boolean cache = (key != null);
		CaptureResponseWriter captureResponseWriter = new CaptureResponseWriter(
				response.getWriter());
		JsonWriter write = new JsonWriter(captureResponseWriter);
		write.beginObject();
		write.name("results");
		write.beginArray();
		for (int i = 0; i < arr.size(); i++) {
			JsonObject obj = arr.get(i).getAsJsonObject();
			try {
				RequestInfo r = new RequestInfo(obj);
				if (r.isValid() && (allowModify || r.isSafe())) {
					cache = doRequest(request, response, r, write) && cache;
				} else {
					outputFailure("Bad request, ignored " + obj.toString(),
							write);
				}
			} catch (MalformedURLException e) {
				outputFailure("Bad request, ignored " + obj.toString(), write);
			}
		}
		write.endArray();
		write.endObject();
		write.flush();
		if (cache) {
			responseCache.put(key, captureResponseWriter.toString());
		}
	}

	private void outputFailure(String message, JsonWriter write)
			throws IOException {
		write.beginObject();
		write.name("success");
		write.value(false);
		write.name("status");
		write.value(400);
		write.name("message");
		write.value(message);
		write.endObject();
	}

	private boolean doRequest(HttpServletRequest request,
			HttpServletResponse response, RequestInfo requestInfo,
			JsonWriter write) throws ServletException, IOException {

		boolean cache = true;
		if (!"GET".equals(requestInfo.getMethod())) {
			cache = false;
			String user = request.getRemoteUser();

			if (user == null || user.length() == 0
					|| User.ANON_USER.equals(request.getRemoteUser())) {
				response.reset();
				throw new ServletException(
						"Anon Users may only perform GET operations");
			}
		}
		String requestPath = requestInfo.getUrl();

		// Wrap the request and response.
		RequestWrapper requestWrapper = new RequestWrapper(request, requestInfo);
		ResponseWrapper responseWrapper = new ResponseWrapper(response);
		RequestDispatcher requestDispatcher;
		try {
			requestDispatcher = request.getRequestDispatcher(requestPath);
			requestDispatcher.forward(requestWrapper, responseWrapper);
			cache =  writeResponse(write, responseWrapper, requestInfo) && cache;
		} catch (ServletException e) {
			writeFailedRequest(write, requestInfo);
			cache = false;
		} catch (IOException e) {
			writeFailedRequest(write, requestInfo);
			cache = false;
		}

		return cache;

	}

	private boolean writeResponse(JsonWriter write,
			ResponseWrapper responseWrapper, RequestInfo requestData)
			throws IOException {
		boolean cache = true;
		try {
			String body = responseWrapper.getDataAsString();
			write.beginObject();
			write.name("url");
			write.value(requestData.getUrl());
			write.name("success");
			write.value(true);
			write.name("body");
			write.value(body);
			write.name("status");
			write.value(responseWrapper.getResponseStatus());
			write.name("statusmessage");
			write.value(responseWrapper.getResponseStatusMessage());
			write.name("headers");
			write.beginObject();
			Dictionary<String, String> headers = responseWrapper
					.getResponseHeaders();
			Enumeration<String> keys = headers.keys();
			while (keys.hasMoreElements()) {
				String k = keys.nextElement();
				if ("cache-control".equalsIgnoreCase(k)
						&& (headers.get(k).contains("private") || headers
								.get(k).contains("no-cache"))) {
					cache = false;
				}
				write.name(k);
				write.value(headers.get(k));
			}
			write.endObject();
			write.endObject();
		} catch (UnsupportedEncodingException e) {
			writeFailedRequest(write, requestData);
			cache = false;
		}
		return cache;
	}

	private void writeFailedRequest(JsonWriter write, RequestInfo requestData)
			throws IOException {
		write.beginObject();
		write.name("url");
		write.value(requestData.getUrl());
		write.name("success");
		write.value(false);
		write.endObject();
	}

}
