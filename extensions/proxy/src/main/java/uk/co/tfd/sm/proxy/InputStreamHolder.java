package uk.co.tfd.sm.proxy;

import java.io.InputStream;

public interface InputStreamHolder {

	InputStream getStream();

	String getMimeType();

	String getFileName();

}
