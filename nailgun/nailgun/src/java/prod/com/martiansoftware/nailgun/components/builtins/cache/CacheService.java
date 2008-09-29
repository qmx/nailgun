/*   

  Copyright 2008, Martian Software, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

*/

package com.martiansoftware.nailgun.components.builtins.cache;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;

/**
 * <p>Title:CacheService</p>
 * <p>Description:An arbitrary object cache keyed by simple string based names. The service can optionally support soft reference based caching 
 * for memory friendliness and passive or active cache item time based expiration.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class CacheService extends BaseComponentService implements CacheServiceMBean {
	/** The core caching construct for the cache service */
	protected ConcurrentReaderHashMap cache = null; 
	/** Overrides the default initial size (32) of the cache */
	protected int initialSize = -1;
	/** Overrides the default load factor (1.0) of the cache */
	protected float loadFactor = -1L;
	/** Indicates if the cache should use soft references */
	protected boolean softReference = false; 
	/** Indicates if the cache should start a background thread to check for expired cache items and purge them */
	protected boolean activeExpiration = false;
	/** The number of expired items */
	protected SynchronizedLong expirations = new SynchronizedLong(0);	
	/** The number of gc collected items */
	protected SynchronizedLong collections = new SynchronizedLong(0);
	/** The total number of gets */
	protected SynchronizedLong getCount = new SynchronizedLong(0);
	/** The total number of hits */
	protected SynchronizedLong getHitCount = new SynchronizedLong(0);
	/** The total number of misses */
	protected SynchronizedLong getMissCount = new SynchronizedLong(0);
	/** The total get time */
	protected SynchronizedLong totalTime = new SynchronizedLong(0);
	/** The cache expiry thread */
	protected ExpiryThread expiryThread = null;
	
	

	/**
	 * Creates a new CacheService.
	 * @param initialSize The initial size of the underlying cache HashMap.
	 * @param loadFactor The load factor of the underlying cache HashMap.
	 * @param softReference Indicates if the cache should use soft references, allowing the GC to collect cache items if memory is needed.
	 * @param activeExpiration Indicates of the cache should start a reaper thread to actively purge expired items. 
	 * If false, expired items will be purged on GC (if soft reference is true) or on access.
	 */
	public CacheService(int initialSize, float loadFactor, boolean softReference, boolean activeExpiration) {
		this.initialSize = initialSize;
		this.loadFactor = loadFactor;
		this.softReference = softReference;
		this.activeExpiration = activeExpiration;
		cache = new ConcurrentReaderHashMap(initialSize, loadFactor);
		if(activeExpiration) {
			expiryThread = new ExpiryThread(cache, EXPIRE_THREAD_SLEEP_TIME);
			expiryThread.start();
		}
	}
	
	/**
	 * Creates a new CacheService with default cache initial size and load factor.
	 * @param softReference Indicates if the cache should use soft references, allowing the GC to collect cache items if memory is needed.
	 * @param activeExpiration Indicates of the cache should start a reaper thread to actively purge expired items. 
	 * If false, expired items will be purged on GC (if soft reference is true) or on access.
	 */
	public CacheService(boolean softReference, boolean activeExpiration) {
		this(ConcurrentReaderHashMap.DEFAULT_INITIAL_CAPACITY, ConcurrentReaderHashMap.DEFAULT_LOAD_FACTOR, softReference, activeExpiration);
	}	
	
	/**
	 * Adds or updates an exisitng cached object keyed by name.
	 * @param name The key of the cached object.
	 * @param cacheItem The object to cache.
	 */
	public void put(String name, Object cacheItem) {
		CacheEntry entry = null;
		if(softReference) {
			entry = new SoftCacheEntry(name, cacheItem, cache, collections);
		} else {
			entry = new CacheEntry(name, cacheItem, cache);
		}
		cache.put(name, entry);
	}
	
	/**
	 * Returns the timestamp of the named cache item's last update time, or -1 if the named entry cannot be found.
	 * @param name The key of the cached item.
	 * @return the timestamp of the last update or -1.
	 */
	public long getLastUpdateTime(String name) {
		CacheEntry entry = (CacheEntry)cache.get(name);
		if(entry==null) return -1;
		else return entry.getLastUpdate();
	}
	
	/**
	 * Returns the timestamp of the named cache item's last access time, or -1 if the named entry cannot be found.
	 * @param name The key of the cached item.
	 * @return the timestamp of the last access or -1.
	 */
	public long getLastAccessTime(String name) {
		CacheEntry entry = (CacheEntry)cache.get(name);
		if(entry==null) return -1;
		else return entry.getLastAccess();
	}
	
	
	/**
	 * Adds or updates an exisitng cached object keyed by name.
	 * @param name The key of the cached object.
	 * @param cacheItem The object to cache.
	 */
	public void put(String name, String cacheItem) {
		put(name, (Object)cacheItem);
		out("Cached [" + name + "]");
	}
	
	/**
	 * Adds or updates an exisitng cached object keyed by name with a defined expiration period. 
	 * @param name The key of the cached object.
	 * @param cacheItem The object to cache.
	 * @param the expiry time of the cached item. The object will time out in this time (ms) if not accessed.
	 */
	public void put(String name, Object cacheItem, long timeOut) {
		CacheEntry entry = null;
		if(softReference) {
			entry = new SoftCacheEntry(name, cacheItem, timeOut, cache, expirations, collections);
		} else {
			entry = new CacheEntry(name, cacheItem, timeOut, cache, expirations);
		}
		cache.put(name, entry);
	}
	
	/**
	 * Adds or updates an exisitng cached object keyed by name with a defined expiration period. 
	 * @param name The key of the cached object.
	 * @param cacheItem The object to cache.
	 * @param the expiry time of the cached item. The object will time out in this time (ms) if not accessed.
	 */
	public void put(String name, String cacheItem, String timeOut) {
		put(name, (Object)cacheItem, Long.parseLong(timeOut));
		out("Cached [" + name + "] with timeout:" + timeOut);
	}
	
	/**
	 * Retrieves the named item from cache.
	 * @param name The cache key name to retrieve the item for.
	 * @return The cached item, or null if was not found.
	 */
	public Object get(String name) {
		Object retValue = null;
		long start = System.currentTimeMillis();
		try {
			CacheEntry entry = (CacheEntry)cache.get(name);
			if(entry==null) {
				//out("ERROR:No value found for key [" + name + "]");
				return null;
			} else {
				retValue = entry.get();
				//out(retValue);
				return retValue;
			}
		} finally {
			totalTime.add(System.currentTimeMillis()-start);
			getCount.increment();
			if(retValue != null) {
				getHitCount.increment();
			} else {
				getMissCount.increment();
			}			
		}
	}
	
	/**
	 * Removes the named item from cache.
	 * @param name The cache key name to remove the item for.
	 * @return The removed item or null.
	 */
	public Object remove(String name) {		
		CacheEntry entry = (CacheEntry)cache.remove(name);
		if(entry==null) {
			return null;
		} else {
			entry.removed();
			return entry.get();
		}
	}
	
	
	/**
	 * Returns an array containing all the names currently used as keys in the cache.
	 * @return An array of cache keys.
	 */
	public String[] getNames() {		
		return (String[]) cache.keySet().toArray(new String[cache.size()]);
	}
	
	/**
	 * The number of the entries in the cache.
	 * @return the size of the cache.
	 */
	public int getSize() {
		return cache.size();
	}
	
	/**
	 * Returns the number of expirations that have occured in the cache since it was created.
	 * @return the number of expirations
	 */
	public long getExpirations() {
		return expirations.get();
	}
	
	/**
	 * Returns the number of cached items gc collected from the cache since it was created.
	 * @return the number of gc collections
	 */
	public long getCollections() {
		return collections.get();
	}
	
	/**
	 * Returns the number of get calls on the cache since it was created.
	 * @return the number of get calls 
	 */
	public long getAccessCount() {
		return getCount.get();
	}
	
	/**
	 * Returns the number of hits on the cache since it was created.
	 * @return the number of hits
	 */
	public long getHitCount() {
		return getHitCount.get();
	}
	
	/**
	 * Returns the number of misses on the cache since it was created.
	 * @return the number of misses
	 */
	public long getMissCount() {
		return getMissCount.get();
	}
	
	/**
	 * Returns the average elapsed time of all get calls against the cache since it started.
	 * @return the average elapsed time of all get calls 
	 */
	public long getAverageAccessTime() {
		float totalAccessTime = this.totalTime.get();
		float totalAccessCalls = this.getCount.get();
		float avg = totalAccessTime/totalAccessCalls;
		return (long)avg;
	}
	
	/**
	 * Returns the hit ratio of the cache, or the percentage of all get calls against the cache that returned the requested object.
	 * @return the cache hit ratio
	 */
	public long getHitRatio() {
		float totalHits = this.getHitCount.get();
		float totalAccessCalls = this.getCount.get();
		float avg = totalHits/totalAccessCalls*100;
		return (long)avg;
		
	}
	
	/**
	 * Generates a sensible default objectName for this class.
	 * @return An objectName string.
	 * @see com.martiansoftware.nailgun.components.builtins.base.BaseComponentService#getDefaultObjectName()
	 */
	public String getDefaultObjectName() {
		return DEFAULT_OBJECT_NAME + ",soft=" + softReference + ",active=" + activeExpiration;
	}
	
	
	
	
}

