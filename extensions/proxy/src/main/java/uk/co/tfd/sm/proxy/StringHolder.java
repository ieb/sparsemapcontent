package uk.co.tfd.sm.proxy;

import java.nio.charset.Charset;

public interface StringHolder {

	Charset getCharset();

	String getString();

	String getMimeType();

}
