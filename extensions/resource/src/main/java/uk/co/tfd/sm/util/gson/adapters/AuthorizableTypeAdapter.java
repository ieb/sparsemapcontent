package uk.co.tfd.sm.util.gson.adapters;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AuthorizableTypeAdapter implements JsonSerializer<Authorizable> {


	@Override
	public JsonElement serialize(Authorizable authorizable, Type type,
			JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();
		for ( Entry<String, Object> e : authorizable.getProperties().entrySet() ) {
			jsonObject.add(e.getKey(), context.serialize(e.getValue()));
		}
		return jsonObject;
	}
	

}
