package com.nextlabs.rms.config;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.infinispan.manager.DefaultCacheManager;

import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.repository.CachedFile;
import com.nextlabs.rms.repository.FileCacheId;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class RMSCacheManager {
	
	private static final RMSCacheManager instance = new RMSCacheManager();

	private final Logger logger = Logger.getLogger(RMSCacheManager.class);

	private CacheManager manager = null;
	
	public static final String CACHEID_REPOSITORY = "CACHEID_REPOSITORY";
	
	public static final String CACHEID_FILECONTENT = "CACHEID_FILECONTENT"; 	
	
	public static final String CACHEID_CONFIG = "CACHEID_CONFIG";
	
	public static final String CACHEID_USER_ATTR = "CACHEID_USER_ATTR";
	
	private static final int MAX_ENTRIES_IN_CACHE = 10000;
	
	private DefaultCacheManager ispnCacheManager = null;
	
	private RMSCacheManager(){
		Configuration cacheMgrConfig = new Configuration().
					diskStore(new DiskStoreConfiguration().
								path(GlobalConfigManager.getInstance().getTempDir()));
		manager = CacheManager.create(cacheMgrConfig);
		createRepoCache();
		createFileContentCache();
		createConfigCache();
		
		try {
			System.setProperty("rms.ispn.file.store", GlobalConfigManager.getInstance().getTempDir());
			String configFile = GlobalConfigManager.INFINISPAN_CONFIG_FILE_NAME;
			
			if (GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_CLUSTERED_MODE)) {
				System.setProperty("rms.ispn.jgroups.conf.location", GlobalConfigManager.getInstance().getDataDir());
				configFile = GlobalConfigManager.INFINISPAN_CLUSTERED_CONFIG_FILE_NAME;
				logger.info("Using Infinispan Cache in clustered mode");
			} else {
				logger.info("Using Infinispan Cache in local mode");
			}
			ispnCacheManager = new DefaultCacheManager(GlobalConfigManager.getInstance().getDataDir() + 
					File.separator + configFile);
			ispnCacheManager.start();		
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public Object getFromCache(Object key, String cacheName) {
		if (logger.isTraceEnabled()) {
			logger.trace("Get from cache. Key: " + key + " cacheName: " + cacheName);
		}
		if (CACHEID_USER_ATTR.equals(cacheName)) {
			if (ispnCacheManager == null){
				logger.error("ispnCacheManager has not been initialized!!!");
				return null;
			}
			org.infinispan.Cache<Object, Object> cache = ispnCacheManager.getCache(CACHEID_USER_ATTR);
			return cache.get(key);
		} else {
			Ehcache cache = getCache(cacheName);
			Element element= cache.get(key);
			
			if ( element != null) {
				return element.getObjectValue();
			}
		}
		return null;
	}
	
	public boolean putInCache(Object key, Object value, String cacheName, long ttl, TimeUnit unit) {
		if (logger.isTraceEnabled()) {
			logger.trace("Put in cache. Key: " + key + "value: " + value + " cacheName: " + cacheName);
		}
		if (CACHEID_USER_ATTR.equals(cacheName)) {
			if (ispnCacheManager == null){
				logger.error("ispnCacheManager has not been initialized!!!");
				return false;
			}
			org.infinispan.Cache<Object, Object> cache = ispnCacheManager.getCache(CACHEID_USER_ATTR);
			cache.put(key, value, ttl, unit);
			return true;
		} else {
			Ehcache cache = getCache(cacheName);
			cache.put(new Element(key, value));
			return true;
		}
	}
	
	public Object removeFromCache(Object key, String cacheName) {
		if (logger.isTraceEnabled()) {
			logger.trace("Remove from cache. Key: " + key + " cacheName: " + cacheName);
		}
		if (CACHEID_USER_ATTR.equals(cacheName)) {
			if (ispnCacheManager == null){
				logger.error("ispnCacheManager has not been initialized!!!");
				return null;
			}
			org.infinispan.Cache<Object, Object> cache = ispnCacheManager.getCache(CACHEID_USER_ATTR);
			return cache.remove(key);
		} else {
			Ehcache cache = getCache(cacheName);
			Object value = cache.get(key);
			cache.remove(key);
			return value;
		}	
	}
	

	private void createConfigCache() {
		int cacheTimeoutMins = GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.CONFIG_CACHE_TIMEOUT_MINS);
		if(cacheTimeoutMins<=0){
			logger.debug("Using default value for Config Cache Timeout");
			cacheTimeoutMins = 30;
		}
		logger.debug("Config Cache Timeout set to: "+cacheTimeoutMins+" mins");

		Cache configCache = new Cache(new CacheConfiguration(CACHEID_CONFIG, MAX_ENTRIES_IN_CACHE)
							.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
							.eternal(false)
							.timeToIdleSeconds(cacheTimeoutMins*60)
							.persistence(
									new PersistenceConfiguration()
											.strategy(Strategy.LOCALTEMPSWAP)));
		manager.addCache(configCache);
	}
	
	private void createFileContentCache() {
		int cacheTimeoutMins = GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.FILECONTENT_CACHE_TIMEOUT_MINS);
		if(cacheTimeoutMins<=0){
			logger.debug("Using default value for FileContent Cache Timeout");
			cacheTimeoutMins = 30;
		}
		logger.debug("FileContent Cache Timeout set to: "+cacheTimeoutMins+" mins");
		int maxBytesLocalHeap = GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.FILECONTENT_CACHE_MAXMEM_MB);
		if(maxBytesLocalHeap<=0){
			logger.debug("Using default value for maxBytesLocalHeap");
			maxBytesLocalHeap = 100;
		}
		CacheConfiguration config = new CacheConfiguration();
		config.setName(CACHEID_FILECONTENT);		
		Cache fileContentCache = new Cache(config
							.maxBytesLocalHeap(maxBytesLocalHeap, MemoryUnit.MEGABYTES)
							.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
							.eternal(false)
							.timeToIdleSeconds(cacheTimeoutMins*60)
							.persistence(
									new PersistenceConfiguration()
											.strategy(Strategy.LOCALTEMPSWAP)));
		manager.addCache(fileContentCache);
	}

	private void createRepoCache() {
		int cacheTimeoutMins = GlobalConfigManager.getInstance().getIntProperty(GlobalConfigManager.REPO_CACHE_TIMEOUT_MINS);
		if(cacheTimeoutMins<=0){
			logger.debug("Using default value for Repo Cache Timeout");
			cacheTimeoutMins = 60;
		}
		logger.debug("Repo Cache Timeout set to: "+cacheTimeoutMins+" mins");
		Cache repoCache = new Cache(new CacheConfiguration(CACHEID_REPOSITORY, 1000000)
							.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
							.eternal(false)
							.timeToIdleSeconds(cacheTimeoutMins*60)
							.persistence(
									new PersistenceConfiguration()
											.strategy(Strategy.NONE)));
		manager.addCache(repoCache);
	}
	
	public static RMSCacheManager getInstance(){
		return instance;
	}
	
	public Ehcache getCache(String cacheName){
		return manager.getEhcache(cacheName);
	}
	
	public void shutdown() {
		if (manager != null) {
			manager.shutdown();
		}
		if (ispnCacheManager != null) {
			ispnCacheManager.stop();
		}
	}
	
	public CachedFile getCachedFile(FileCacheId fileCacheId) throws RMSException {
		logger.debug("About to get documentID:"+fileCacheId.getDocId() +" from cache");    	
		Ehcache cache = RMSCacheManager.getInstance().getCache(RMSCacheManager.CACHEID_FILECONTENT);
		Element element = cache.get(fileCacheId);	
			if(element==null){//Element not found. Try decoding the documentId.setDocId(URLDecoder.decode(fileCacheId.getDocId(),"UTF-8"))
				try {
					fileCacheId=new FileCacheId(fileCacheId.getSessionId(), fileCacheId.getUserName(), URLDecoder.decode(fileCacheId.getDocId(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					logger.error("Error occurred while decoding documentId", e);
					throw new RMSException(RMSMessageHandler.getClientString("fileNotFound"));
				}
				element = cache.get(fileCacheId);
			}
			if(element==null){
				throw new RMSException(RMSMessageHandler.getClientString("fileNotFound"));
			}
		return (CachedFile)element.getObjectValue();
	}
}