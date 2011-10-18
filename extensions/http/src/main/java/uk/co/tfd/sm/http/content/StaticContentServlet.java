package uk.co.tfd.sm.http.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;

public class StaticContentServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3447511065435638313L;
	private File basePathFile;
	private Map<String, String> mimeTypes;
	private Map<String, byte[]> contentCache;
	private String alias;
	private String basePath;
	private String baseAbsolutePath;
	private boolean debugAllowed;

	
	public StaticContentServlet(String alias, String path,
			Map<String, String> mimeTypes, boolean withCaching) {
		if ( withCaching ) {
			this.contentCache = Maps.newConcurrentMap();
		}
		this.basePathFile = new File(path);
		this.baseAbsolutePath = basePathFile.getAbsolutePath();
		this.mimeTypes = mimeTypes;
		this.alias = alias;
		this.basePath = path;
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
			if ( debugAllowed && Boolean.parseBoolean(request.getParameter("debug")) ) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Alias:["+alias+"] Request:["+request.getRequestURI()+"]");					
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
		String k = alias+":"+basePath+":"+path;
		String kgz = null;
		String accept=request.getHeader("Accept-Encoding");
        boolean acceptsGz = (accept!=null && accept.indexOf("gzip")>=0);
        boolean gzipped = false;
        byte[] content = null;
        if ( acceptsGz ) {
    		kgz = k+".gz";
    		content = contentCache.get(kgz);
        }
        if ( content == null) {
        	content = contentCache.get(k);
        } else {
    		gzipped = true;
        }
		if (content == null) {
			if ( !f.getAbsolutePath().startsWith(baseAbsolutePath) ) {
				if ( debugAllowed && Boolean.parseBoolean(request.getParameter("debug")) ) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN,"Alias:["+alias+"] Request:["+request.getRequestURI()+"] File:["+f.getAbsolutePath()+"]");					
				} else {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
				}
				return;
			}
			if (f.exists() && f.isFile()) {
				File gzfile = new File(f.getAbsolutePath()+".gz");
				if ( gzfile.exists() && f.isFile()) {
					FileInputStream in = new FileInputStream(gzfile);
					content = IOUtils.toByteArray(in);					
		    		gzipped = true;
					in.close();
					contentCache.put(kgz, content);
				} else {
					FileInputStream in = new FileInputStream(f);
					content = IOUtils.toByteArray(in);					
					in.close();
					contentCache.put(k, content);
				}
			} else {
				if ( debugAllowed && Boolean.parseBoolean(request.getParameter("debug")) ) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND,"Alias:["+alias+"] Request:["+request.getRequestURI()+"] File:["+f.getAbsolutePath()+"]");					
				} else {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
				return;
			}
		}
		if ( gzipped ) {
           response.setHeader("Content-Encoding","gzip");
		}
		response.getOutputStream().write(content);
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
