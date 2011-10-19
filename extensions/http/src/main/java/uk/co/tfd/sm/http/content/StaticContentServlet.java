package uk.co.tfd.sm.http.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StaticContentServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3447511065435638313L;
	private File basePathFile;
	private Map<String, String> mimeTypes;
	private String alias;
	private String baseAbsolutePath;
	private boolean debugAllowed;
	private ThreadLocal<ByteBuffer> copyBuffer = new ThreadLocal<ByteBuffer>(){
		protected ByteBuffer initialValue() {
			return ByteBuffer.allocate(100*1024);
		};
	};

	public StaticContentServlet(String alias, String path,
			Map<String, String> mimeTypes) {
		this.basePathFile = new File(path);
		this.baseAbsolutePath = basePathFile.getAbsolutePath();
		this.mimeTypes = mimeTypes;
		this.alias = alias;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String path = request.getRequestURI().substring(alias.length());
		if (path.contains("..")) {
			if (debugAllowed
					&& Boolean.parseBoolean(request.getParameter("debug"))) {
				response.sendError(
						HttpServletResponse.SC_BAD_REQUEST,
						"Alias:[" + alias + "] Request:["
								+ request.getRequestURI() + "]");
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
			return;
		}

		File f = new File(basePathFile, path);
		String n = f.getName();
		String mimeType = getMimeType(n);
		if (mimeType != null) {
			response.setContentType(mimeType);
		}
		String accept = request.getHeader("Accept-Encoding");
		boolean acceptsGz = (accept != null && accept.indexOf("gzip") >= 0);
		if (!f.getAbsolutePath().startsWith(baseAbsolutePath)) {
			if (debugAllowed
					&& Boolean.parseBoolean(request.getParameter("debug"))) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Alias:["
						+ alias + "] Request:[" + request.getRequestURI()
						+ "] File:[" + f.getAbsolutePath() + "]");
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
			return;
		}
		if (f.exists() && f.isFile()) {
			File gzfile = new File(f.getAbsolutePath() + ".gz");
			if (acceptsGz && gzfile.exists() && f.isFile()) {
				response.setHeader("Content-Encoding", "gzip");
				FileInputStream in = new FileInputStream(gzfile);
				
				copy(in, response.getOutputStream());
				in.close();
			} else {
				FileInputStream in = new FileInputStream(f);
				copy(in, response.getOutputStream());
				in.close();
			}
		} else {
			if (debugAllowed
					&& Boolean.parseBoolean(request.getParameter("debug"))) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Alias:["
						+ alias + "] Request:[" + request.getRequestURI()
						+ "] File:[" + f.getAbsolutePath() + "]");
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
	}

	private void copy(FileInputStream in, ServletOutputStream outputStream) throws IOException {
		ByteBuffer bb = copyBuffer.get();
		FileChannel inc = in.getChannel();
		bb.rewind();
		while(inc.read(bb) >= 0 ) {
			if ( bb.position() > 0 ) {
				outputStream.write(bb.array(), 0, bb.position());
				bb.rewind();
			} else {
				Thread.yield();
			}
		}
	}

	public String getMimeType(String fileName) {
		int i = fileName.lastIndexOf('.');
		String m = null;
		if (i > 0) {
			String ext = fileName.substring(i + 1);
			if (ext.endsWith("/")) {
				ext = ext.substring(0, ext.length() - 1);
			}
			m = mimeTypes.get(ext);
		}
		return m;
	}

}
