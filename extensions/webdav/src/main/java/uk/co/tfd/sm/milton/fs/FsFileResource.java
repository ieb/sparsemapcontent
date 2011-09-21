package uk.co.tfd.sm.milton.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.PartialGetHelper;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;

/**
 *
 */
public class FsFileResource extends FsReadOnlyResource implements CopyableResource,
		DeletableResource, GetableResource, MoveableResource,
		PropFindableResource, PropPatchableResource, FsWritableResource {

	private static final Logger log = LoggerFactory
			.getLogger(FsFileResource.class);
	private FsWriteHelper fsWriteHelper;

	/**
	 * 
	 * @param host
	 *            - the requested host. E.g. www.mycompany.com
	 * @param factory
	 * @param file
	 */
	public FsFileResource(String host, FsResourceFactory factory, File file,
			String ssoPrefix) {
		super(host, factory, file, ssoPrefix);
		fsWriteHelper = new FsWriteHelper(host, factory,file, ssoPrefix, this);
	}

	public Long getContentLength() {
		return file.length();
	}

	public String getContentType(String preferredList) {
		String mime = ContentTypeUtils.findContentTypes(this.file);
		String s = ContentTypeUtils.findAcceptableContentType(mime,
				preferredList);
		if (log.isTraceEnabled()) {
			log.trace("getContentType: preferred: {} mime: {} selected: {}",
					new Object[] { preferredList, mime, s });
		}
		return s;
	}

	public String checkRedirect(Request arg0) {
		return null;
	}

	public void sendContent(OutputStream out, Range range,
			Map<String, String> params, String contentType) throws IOException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			if (range != null) {
				log.debug("sendContent: ranged content: "
						+ file.getAbsolutePath());
				PartialGetHelper.writeRange(in, range, out);
			} else {
				log.debug("sendContent: send whole file "
						+ file.getAbsolutePath());
				IOUtils.copy(in, out);
			}
			out.flush();
		} catch (ReadingException e) {
			throw new IOException(e);
		} catch (WritingException e) {
			throw new IOException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/** @{@inheritDoc */
	public Long getMaxAgeSeconds(Auth auth) {
		return factory.maxAgeSeconds(this);
	}

	/** @{@inheritDoc */
	@Override
	protected void doCopy(File dest) {
		try {
			FileUtils.copyFile(file, dest);
		} catch (IOException ex) {
			throw new RuntimeException("Failed doing copy to: "
					+ dest.getAbsolutePath(), ex);
		}
	}

	@Deprecated
	public void setProperties(Fields fields) {
		// MIL-50
		// not implemented. Just to keep MS Office sweet
	}

	public void moveTo(CollectionResource newParent, String newName)
			throws ConflictException, NotAuthorizedException,
			BadRequestException {
		fsWriteHelper.moveTo(newParent, newName);
	}

	public void delete() throws NotAuthorizedException, ConflictException,
			BadRequestException {
		fsWriteHelper.delete();
	}

	public void copyTo(CollectionResource newParent, String newName)
			throws NotAuthorizedException, BadRequestException,
			ConflictException {
		fsWriteHelper.copyTo(newParent, newName);
		
	}
}
