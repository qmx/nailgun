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

/**
 * <p>Title:CacheServiceMBean</p>
 * <p>Description: Management interface for the CacheService.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public interface CacheServiceMBean {
	
	/** The default JMX ObjectName for the CacheService management interface */
	public static final String DEFAULT_OBJECT_NAME = "com.martiansoftware.components.cache:service=Cache";
	
	/** The default expiry thread sleeptime (ms) */
	public static final long EXPIRE_THREAD_SLEEP_TIME = 60000;
	

	/**
	 * Adds or updates an exisitng cached object keyed by name.
	 * @param name The key of the cached object.
	 * @param cacheItem The object to cache.
	 */
	public abstract void put(String name, Object cacheItem);

	/**
	 * Adds or updates an exisitng cached object keyed by name with a defined expiration period. 
	 * @param name The key of the cached object.
	 * @param cacheItem The object to cache.
	 */
	public abstract void put(String name, Object cacheItem, long timeOut);

	/**
	 * Retrieves the named item from cache.
	 * @param name The cache key name to retrieve the item for.
	 * @return The cached item, or null if was not found.
	 */
	public abstract Object get(String name);

	/**
	 * Removes the named item from cache.
	 * @param name The cache key name to remove the item for.
	 * @return The removed item or null.
	 */
	public abstract Object remove(String name);

	/**
	 * Returns an array containing all the names currently used as keys in the cache.
	 * @return An array of cache keys.
	 */
	public abstract String[] getNames();

	/**
	 * The number of the entries in the cache.
	 * @return the size of the cache.
	 */
	public abstract int getSize();

	/**
	 * Returns the number of expirations that have occured in the cache since it was created.
	 * @return the number of expirations
	 */
	public abstract long getExpirations();

	/**
	 * Returns the number of cached items gc collected from the cache since it was created.
	 * @return the number of gc collections
	 */
	public abstract long getCollections();

	/**
	 * Returns the number of get calls on the cache since it was created.
	 * @return the number of get calls 
	 */
	public abstract long getAccessCount();

	/**
	 * Returns the number of hits on the cache since it was created.
	 * @return the number of hits
	 */
	public abstract long getHitCount();

	/**
	 * Returns the number of misses on the cache since it was created.
	 * @return the number of misses
	 */
	public abstract long getMissCount();

	/**
	 * Returns the average elapsed time of all get calls against the cache since it started.
	 * @return the average elapsed time of all get calls 
	 */
	public abstract long getAverageAccessTime();

	/**
	 * Returns the hit ratio of the cache, or the percentage of all get calls against the cache that returned the requested object.
	 * @return the cache hit ratio
	 */
	public abstract long getHitRatio();
	
	/**
	 * Returns the timestamp of the named cache item's last update time, or -1 if the named entry cannot be found.
	 * @param name The key of the cached item.
	 * @return the timestamp of the last update or -1.
	 */
	public long getLastUpdateTime(String name);
	
	/**
	 * Returns the timestamp of the named cache item's last access time, or -1 if the named entry cannot be found.
	 * @param name The key of the cached item.
	 * @return the timestamp of the last access or -1.
	 */
	public long getLastAccessTime(String name);
}