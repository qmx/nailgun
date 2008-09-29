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

import java.util.Map;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;

/**
 * <p>Title:CacheEntry</p>
 * <p>Description: A container class for cached objects and assocaited metadata.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class CacheEntry {
	/** The name of the cache entry */
	protected String name = null;
	/** The cached object */
	protected Object cachedItem = null;
	/** The designated timeout */
	protected long timeOut = -1;
	/** The timestamp of the last access */
	protected SynchronizedLong lastAccess = new SynchronizedLong(-1);
	/** The timestamp of the last update */
	protected SynchronizedLong lastUpdate = new SynchronizedLong(-1);
	
	/** A reference to the cache service's map so it can be updated for expired items. */
	protected Map cache = null;
	/** The number of expired items */
	protected SynchronizedLong expirations = null;	

	/**
	 * Creates a new CacheEntry with a defined timeout.
	 * @param name The name of the cache entry.
	 * @param cachedItem The cached item.
	 * @param timeOut The idle timeout in ms.
	 * @param cache A reference to the service's Map so expired items can be removed.
	 * @param expirations A reference to the service's shared expiration counter. 
	 */
	public CacheEntry(String name, Object cachedItem, long timeOut, Map cache, SynchronizedLong expirations) {
		this.name = name;
		this.cachedItem = cachedItem;
		this.timeOut = timeOut;
		this.cache = cache;
		this.expirations = expirations;
		long currentTime = System.currentTimeMillis();
		lastAccess.set(currentTime);
		lastUpdate.set(currentTime);
	}
	
	/**
	 * Creates a new CacheEntry with no timeout.
	 * @param name The name of the cache entry.
	 * @param cachedItem The cached item.
	 * @param cache A reference to the service's Map so expired items can be removed.
	 */
	public CacheEntry(String name, Object cachedItem, Map cache) {
		this.name = name;
		this.cachedItem = cachedItem;
		this.cache = cache;
		long currentTime = System.currentTimeMillis();
		lastUpdate.set(currentTime);		
	}
	
	
	
	/**
	 * Retrieves the object. If the value has timed out, the item will be nulled and a null will be returned.
	 * Every access refreshes the last Access timestamp.
	 * @return The cached object or null if it has timed out.
	 */
	public Object get() {
		long currentTime = System.currentTimeMillis();
		if(testForExpiration()) {
			return null;
		} else {
			lastAccess.set(currentTime);
			return cachedItem;						
		}
	}
	
	/**
	 * Updates the cached object and refreshes the last Access timestamp.
	 * @param cachedItem The new item to cache under the current name.
	 */
	public void set(Object cachedItem) {
		this.cachedItem = cachedItem;
		long currentTime = System.currentTimeMillis();
		lastAccess.set(currentTime);
		lastUpdate.set(currentTime);		
	}
	
	/**
	 * Tests the cached Item to see if it is configured for time out and if it is, will expire it if it the expiry time has elapsed.
	 * @param currentTime The current time of the text.
	 * @return true if item was expired, false if not.
	 */
	public boolean testForExpiration(long currentTime) {		
		if(timeOut > 0) {			
			BaseComponentService.cout("[" + Thread.currentThread().getName() + "]- Checking [" + name + "]");
			if(currentTime-lastAccess.get() >= timeOut) {
				// item has expired.
				BaseComponentService.cout("[" + Thread.currentThread().getName() + "]- Expiring [" + name + "]");
				cache.remove(name);
				expirations.increment();
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}		
	}

	/**
	 * Tests the cached Item to see if it is configured for time out and if it is, will expire it if it the expiry time has elapsed.
	 * @return true if item was expired, false if not.
	 */
	public boolean testForExpiration() {
		return testForExpiration(System.currentTimeMillis());
	}
	
	/**
	 * Called by the cache service when the item is removed from cache.
	 * No-Op for this class.
	 */
	public void removed() {
	
	}

	/**
	 * The last time the cached item was updated.
	 * @return the lastUpdate of the cached item
	 */
	public long getLastUpdate() {
		return lastUpdate.get();
	}
	
	/**
	 * The last time the cached item was accessed.
	 * @return the lastAccessed of the cached item
	 */
	public long getLastAccess() {
		return lastAccess.get();
	}	
	
}
