package uk.co.tfd.sm.resource;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.content.Content;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ContentTypeAdapter implements JsonSerializer<Content> {

	private int depth;

	public ContentTypeAdapter(int recursion) {
		depth = recursion;
	}

	@Override
	public JsonElement serialize(Content content, Type type,
			JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();
		for ( Entry<String, Object> e : content.getProperties().entrySet() ) {
			jsonObject.add(e.getKey(), context.serialize(e.getValue()));
		}
		if ( depth > 0 ) {
			depth--;
			for ( Content child : content.listChildren()) {
				jsonObject.add(StorageClientUtils.getObjectName(child.getPath()), context.serialize(child));
			}
			depth++;
		}
		return jsonObject;
	}
	

}
