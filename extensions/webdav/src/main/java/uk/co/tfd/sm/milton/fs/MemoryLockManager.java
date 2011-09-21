package uk.co.tfd.sm.milton.fs;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.Resource;

public class MemoryLockManager {

	public LockResult lock(LockTimeout timeout, LockInfo lockInfo,
			Resource fsResource) {
		// TODO Auto-generated method stub
		return null;
	}

	public LockResult refresh(String token, Resource fsResource) {
		// TODO Auto-generated method stub
		return null;
	}

	public void unlock(String tokenId, Resource fsResource) {
		// TODO Auto-generated method stub
		
	}

	public LockToken getCurrentToken(Resource fsResource) {
		// TODO Auto-generated method stub
		return null;
	}

}
