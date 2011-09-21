package uk.co.tfd.sm.milton.fs;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

	/**
	 *
	 */
public class FsWriteHelper    {

		private static final Logger LOGGER = LoggerFactory.getLogger(FsWriteHelper.class);
		private File file;
		private FsResourceFactory factory;
		private Resource resource;





		public FsWriteHelper(String host, FsResourceFactory factory2,
				File file2, String ssoPrefix, FsWritableResource fsFileResource) {
			// TODO Auto-generated constructor stub
		}





		/* (non-Javadoc)
		 * @see com.bradmcevoy.http.MoveableResource#moveTo(com.bradmcevoy.http.CollectionResource, java.lang.String)
		 */
		public void moveTo(CollectionResource newParent, String newName) {
			if (newParent instanceof FsDirectoryResource) {
				FsDirectoryResource newFsParent = (FsDirectoryResource) newParent;
				File dest = new File(newFsParent.getFile(), newName);
				boolean ok = this.file.renameTo(dest);
				if (!ok) {
					throw new RuntimeException("Failed to move to: " + dest.getAbsolutePath());
				}
				this.file = dest;
			} else {
				throw new RuntimeException("Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
			}
		}

		public void copyTo(CollectionResource newParent, String newName) {
			if (newParent instanceof FsDirectoryResource) {
				FsDirectoryResource newFsParent = (FsDirectoryResource) newParent;
				File dest = new File(newFsParent.getFile(), newName);
				doCopy(dest);
			} else {
				throw new RuntimeException("Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
			}
		}

		public void delete() {
			boolean ok = file.delete();
			if (!ok) {
				throw new RuntimeException("Failed to delete");
			}
		}

		public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException {
			return factory.getLockManager().lock(timeout, lockInfo, resource);
		}

		public LockResult refreshLock(String token) throws NotAuthorizedException {
			return factory.getLockManager().refresh(token, resource);
		}

		public void unlock(String tokenId) throws NotAuthorizedException {
			factory.getLockManager().unlock(tokenId, resource);
		}

		public LockToken getCurrentLock() {
			if (factory.getLockManager() != null) {
				return factory.getLockManager().getCurrentToken(resource);
			} else {
				log.warn("getCurrentLock called, but no lock manager: file: " + file.getAbsolutePath());
				return null;
			}
		}

}
