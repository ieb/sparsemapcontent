package uk.co.tfd.sm.http.batch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CaptureResponseWriter extends Writer {

	private PrintWriter writer;
	private StringWriter internalWriter;

	public CaptureResponseWriter(PrintWriter writer) {
		this.writer = writer;
		this.internalWriter = new StringWriter();
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
		this.internalWriter.close();
		
	}

	@Override
	public void flush() throws IOException {
		this.writer.flush();
		this.internalWriter.flush();
	}

	@Override
	public void write(char[] buff, int off, int len) throws IOException {
		writer.write(buff,off,len);
		this.internalWriter.write(buff, off, len);
	}
	
	@Override
	public String toString() {
		return this.internalWriter.toString();
	}
	
	
	
}
