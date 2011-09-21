package uk.co.tfd.sm.milton.fs;

import java.io.File;
import java.util.Date;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;

public abstract class FsReadOnlyResource implements Resource, DigestResource {

	File file;
	final FsResourceFactory factory;
	final String host;
	String ssoPrefix;

	public FsReadOnlyResource(String host, FsResourceFactory factory,
			File file, String ssoPrefix) {
		this.host = host;
		this.factory = factory;
		this.file = file;
		this.ssoPrefix = ssoPrefix;
	}

	public File getFile() {
		return file;
	}

	public String getUniqueId() {
		String s = file.lastModified() + "_" + file.length() + "_" + file.getAbsolutePath();
		return s.hashCode() + "";
	}

	public String getName() {
		return file.getName();
	}

	public Object authenticate(String user, String password) {
		return factory.getSecurityManager().authenticate(user, password);
	}
	
	public Object authenticate(DigestResponse digestRequest) {
		return factory.getSecurityManager().authenticate(digestRequest);
	}

	public boolean isDigestAllowed() {
		return factory.isDigestAllowed();
	}

	public boolean authorise(Request request, Method method, Auth auth) {
		return factory.getSecurityManager().authorise(request, method, auth, this);
	}

	public String getRealm() {
		return factory.getRealm(this.host);
	}

	public Date getModifiedDate() {
		return new Date(file.lastModified());
	}
	
	public Date getCreateDate() {
		return null;
	}

	public int compareTo(Resource o) {
		return this.getName().compareTo(o.getName());
	}

}
