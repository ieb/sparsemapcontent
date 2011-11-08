package uk.co.tfd.sm.template;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TemplateServiceImplTest {

	@Test
	public void test() throws IOException {
		TemplateServiceImpl ts = new TemplateServiceImpl();
		// Google collections not a dep of this project.
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("debug", true);
		ts.activate(props);
		Assert.assertFalse(ts.checkTemplateExists("does-not-exist"));
		Assert.assertTrue(ts.checkTemplateExists("testtemplate.vm"));
		StringWriter writer = new StringWriter();
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("world", "Cruel World");
		ts.process(context, "UTF-8", writer, "testtemplate.vm");
		String op = writer.toString();
		Assert.assertEquals("Hello Cruel World", op);

		writer = new StringWriter();
		ts.evaluate(context, writer, "FromString", "Goodby ${world}");
		op = writer.toString();
		Assert.assertEquals("Goodby Cruel World", op);

		writer = new StringWriter();
		StringReader reader = new StringReader("Fairwell ${world}");
		ts.evaluate(context, writer, "FromReader", reader);
		op = writer.toString();
		Assert.assertEquals("Fairwell Cruel World", op);

	}
}
