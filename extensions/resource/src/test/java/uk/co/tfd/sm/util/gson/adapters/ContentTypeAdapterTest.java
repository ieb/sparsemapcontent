package uk.co.tfd.sm.util.gson.adapters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.util.ISO8601Date;

import uk.co.tfd.sm.util.http.ResponseUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class ContentTypeAdapterTest {

	
	private static final String TEST_PATTERN = "{\n" +
			"  \"prop1\": \"2011-02-01T00:00:00Z\",\n" +
			"  \"1\": {\n" +
			"    \"prop2\": \"2011-02-02T00:00:00Z\"\n" +
			"  },\n" +
			"  \"2\": {\n" +
			"    \"prop3\": \"2011-02-03T00:00:00Z\"\n" +
			"  }\n" +
			"}";


	@Test
	public void test() throws UnsupportedEncodingException, IOException {
		Content content = Mockito.mock(Content.class);
		Mockito.when(content.getPath()).thenReturn("/path/a/b/c");
		Content contentChild1 = Mockito.mock(Content.class);
		Content contentChild2 = Mockito.mock(Content.class);
		Mockito.when(contentChild1.getPath()).thenReturn("/path/a/b/c/1");
		Mockito.when(contentChild2.getPath()).thenReturn("/path/a/b/c/2");
		Mockito.when(content.getProperties()).thenReturn( ImmutableMap.of("prop1", (Object)new ISO8601Date("20110201T000000Z")));
		Mockito.when(contentChild1.getProperties()).thenReturn(ImmutableMap.of("prop2", (Object)new ISO8601Date("20110202T000000Z")));
		Mockito.when(contentChild2.getProperties()).thenReturn(ImmutableMap.of("prop3", (Object)new ISO8601Date("20110203T000000Z")));
		
		
		List<Content> children = Lists.newArrayList(contentChild1, contentChild2);
		Mockito.when(content.listChildren()).thenReturn(children);
		List<Content> nochildren = ImmutableList.of();
		Mockito.when(contentChild1.listChildren()).thenReturn(nochildren);
		Mockito.when(contentChild2.listChildren()).thenReturn(nochildren);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResponseUtils.writeTree(content, new String[]{"-1","pp"}, baos);
		String result = baos.toString("UTF-8");
		Assert.assertEquals(TEST_PATTERN, result);
		
	}
}
