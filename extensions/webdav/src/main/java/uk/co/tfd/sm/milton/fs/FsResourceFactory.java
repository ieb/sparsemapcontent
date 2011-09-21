package uk.co.tfd.sm.milton.fs;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.SecurityManager;

public class FsResourceFactory implements ResourceFactory {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FsResourceFactory.class);
	private com.bradmcevoy.http.SecurityManager securityManager;
	private File root;
	private String contextPath;
	private String ssoPrefix;
	private Long maxAgeSeconds;
	private boolean digestAllowed;
	private MemoryLockManager lockManager;
	private boolean readOnly;
	private String defaultPage;

	public FsResourceFactory(File root,
			com.bradmcevoy.http.SecurityManager securityManager,
			String contextPath, String ssoPrefix, long maxAgeSeconds, boolean readOnly, String defaultPage) {
		this.securityManager = securityManager;
		this.root = root;
		this.contextPath = contextPath;
		this.ssoPrefix = ssoPrefix;
		this.maxAgeSeconds = maxAgeSeconds;
		this.lockManager = new MemoryLockManager();
		this.readOnly = readOnly;
		this.defaultPage = defaultPage;
		check();
	}

	private void check() {
		if (!root.exists()) {
			throw new RuntimeException("Root folder does not exist: "
					+ root.getAbsolutePath());
		}
		if (!root.isDirectory()) {
			throw new RuntimeException("Root exists but is not a directory: "
					+ root.getAbsolutePath());
		}

	}

	public String getSupportedLevels() {
		return "1,2";
	}

	public Resource getResource(String host, String url) {
		LOGGER.debug("getResource: host: {} - url: {} ", host, url);
		url = stripContext(url);
		File requested = resolvePath(root, url);
		return resolveFile(host, requested);
	}

	public Resource resolveFile(String host, File file) {
		if (!file.exists()) {
			LOGGER.debug("file not found: " + file.getAbsolutePath());
			return null;
		} else if (file.isDirectory()) {
			if (readOnly) {
				return new FsDirectoryResource(host, this, file, ssoPrefix);
			} else {
				return new FsReadOnlyDirectoryResource(host, this, file,
						ssoPrefix);
			}
		} else {
			if (readOnly) {
				return new FsFileResource(host, this, file, ssoPrefix);
			} else {
				return new FsReadOnlyFileResource(host, this, file, ssoPrefix);
			}
		}
	}

	public File resolvePath(File root, String url) {
		Path path = Path.path(url);
		File f = root;
		for (String s : path.getParts()) {
			f = new File(f, s);
		}
		return f;
	}

	public String getRealm(String host) {
		return securityManager.getRealm(host);
	}

	/**
	 * 
	 * @return - the caching time for files
	 */
	public Long maxAgeSeconds(FsReadOnlyResource resource) {
		return maxAgeSeconds;
	}

	private String stripContext(String url) {
		if (this.contextPath != null && contextPath.length() > 0) {
			url = url.replaceFirst('/' + contextPath, "");
			LOGGER.debug("stripped context: " + url);
			return url;
		} else {
			return url;
		}
	}

	boolean isDigestAllowed() {
		boolean b = digestAllowed && securityManager != null
				&& securityManager.isDigestAllowed();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("isDigestAllowed: " + b);
		}
		return b;
	}

	/**
	 * @return
	 */
	public MemoryLockManager getLockManager() {
		return lockManager;
	}

	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	public String getDefaultPage() {
		return defaultPage;
	}

	public File getRoot() {
		return root;
	}


}
