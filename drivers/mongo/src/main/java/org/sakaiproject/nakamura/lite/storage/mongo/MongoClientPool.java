package org.sakaiproject.nakamura.lite.storage.mongo;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;

@Component(immediate = true, metatype = true)
@Service
public class MongoClientPool implements StorageClientPool {

	protected Mongo mongo;
	protected DB db;

	private static final String DEFAULT_MONGO_URI = "mongodb://127.0.0.1/?maxpoolsize=1000";
	@Property(value = DEFAULT_MONGO_URI)
	public static final String PROP_MONGO_URI = "mongo.uri";

	private static final String DEFAULT_MONGO_DB = "nakamura";
	@Property(value = DEFAULT_MONGO_DB)
	public static final String PROP_MONGO_DB = "mongo.db";

	private static final String DEFAULT_MONGO_USER = "nakamura";
	@Property(value = DEFAULT_MONGO_USER)
	public static final String PROP_MONGO_USER = "mongo.user";

	private static final String DEFAULT_MONGO_PASSWORD = "nakamura";
	@Property(value = DEFAULT_MONGO_PASSWORD)
	public static final String PROP_MONGO_PASSWORD = "mongo.password";

	private static final String DEFAULT_BUCKET = "smc_content_bodies";
	@Property(value = DEFAULT_BUCKET)
	public static final String PROP_BUCKET = "mongo.gridfs.bucket";

	public static final String PROP_AUTHORIZABLE_COLLECTION = "au";
	public static final String PROP_ACL_COLLECTION = "ac";
	public static final String PROP_CONTENT_COLLECTION = "cn";

	public static final String PROP_ALT_KEYS = "mongo.alternate.keys";
	public static final String[] DEFAULT_ALT_KEYS = new String[] { "ac:" + AccessControlManagerImpl._KEY , };

	
	
	private StorageCacheManager storageManagerCache;

	@Reference
	private Configuration configuration;

	private Map<String,Object> props;


	@Activate
	@Modified
	public void activate(Map<String,Object> props) throws MongoException, UnknownHostException {
		this.props = new HashMap<String, Object>(props);
		this.mongo = new Mongo(new MongoURI(StorageClientUtils.getSetting(props.get(PROP_MONGO_URI), DEFAULT_MONGO_URI)));
		this.db = mongo.getDB(StorageClientUtils.getSetting(props.get(PROP_MONGO_DB), DEFAULT_MONGO_DB));

        this.props.put(PROP_AUTHORIZABLE_COLLECTION, configuration.getAuthorizableColumnFamily());
        this.props.put(PROP_ACL_COLLECTION, configuration.getAclColumnFamily());
        this.props.put(PROP_CONTENT_COLLECTION, configuration.getContentColumnFamily());
        
        Builder<String,String> altKeyBuilder = new ImmutableMap.Builder<String,String>();
        String[] altKeyConfigs = StorageClientUtils.getSetting(props.get(PROP_ALT_KEYS), DEFAULT_ALT_KEYS);
        for (String altKey : altKeyConfigs){
        	String[] spl = StringUtils.split(altKey, ":");
        	altKeyBuilder.put(spl[0], spl[1]);
        }
        this.props.put(PROP_ALT_KEYS, altKeyBuilder.build());

        initCache();
        initIndexes();
	}

	private void initIndexes() {
		// index _smcid on au and cn
		for (String name: new String[] {configuration.getContentColumnFamily(), configuration.getAuthorizableColumnFamily() }){
			if (!db.collectionExists(name)){
				DBCollection collection = db.createCollection(name, null);
				collection.ensureIndex(new BasicDBObject(MongoClient.MONGO_INTERNAL_SPARSE_UUID_FIELD, 1),
						MongoClient.MONGO_INTERNAL_SPARSE_UUID_FIELD + "_index", true);
			}
		}

		DBCollection collection;

		// index _aclKey on ac
		collection = db.createCollection(configuration.getAclColumnFamily(), null);
		collection.ensureIndex(new BasicDBObject(AccessControlManagerImpl._KEY, 1),
								AccessControlManagerImpl._KEY + "_index",
								false);

		// Apply the other indexes
		for (String toIndex: configuration.getIndexColumnNames()){
			String columnFamily = StringUtils.trimToNull(StringUtils.substringBefore(toIndex, ":"));
			String keyName = StringUtils.trimToNull(StringUtils.substringAfter(toIndex, ":"));
			if (columnFamily != null && keyName != null){
				collection = db.getCollection(columnFamily);
				collection.ensureIndex(new BasicDBObject(keyName, 1), keyName + "_index", false);
			}
		}
	}

	private void initCache() {
	}

	public StorageClient getClient() throws ClientPoolException {
		return new MongoClient(db, props);
	}

	public StorageCacheManager getStorageCacheManager() {
        if ( storageManagerCache != null ) {
            return storageManagerCache;
        }
        return null;
    }

    public void bindConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
