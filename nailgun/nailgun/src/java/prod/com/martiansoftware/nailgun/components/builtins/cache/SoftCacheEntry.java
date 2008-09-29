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

import java.lang.ref.SoftReference;
import java.util.Map;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * <p>Title:SoftCacheEntry</p>
 * <p>Description:</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class SoftCacheEntry extends CacheEntry {
	
	/** A reference to the cache service's counter of gc collected items */
	protected SynchronizedLong collections = new SynchronizedLong(0);
	/** Indicates the item was already removed from cache by means other than gc. */
	protected boolean inactive = false;

	
	/**
	 * Creates a new SoftCacheEntry.
	 * @param name The name of the cache entry.
	 * @param cachedItem The cached item.
	 * @param timeOut The idle timeout in ms.
	 * @param cache A reference to the service's Map so expired items can be removed.
	 * @param expirations A reference to the service's shared expiration counter. 
	 * @param collections A reference to the service's shared collection counter.
	 */
	public SoftCacheEntry(String name, Object cachedItem, long timeOut,
			Map cache, SynchronizedLong expirations, SynchronizedLong collections) {
		super(name, new SoftReference(cachedItem), timeOut, cache, expirations);
		this.collections = collections;
	}

	/**
	 * Creates a new SoftCacheEntry with no timeout.
	 * @param name The name of the cache entry.
	 * @param cachedItem The cached item.
	 * @param cache A reference to the service's Map so expired items can be removed.
	 * @param collections A reference to the service's shared collection counter.
	 */
	public SoftCacheEntry(String name, Object cachedItem, Map cache, SynchronizedLong collections) {
		super(name, new SoftReference(cachedItem), cache);
	}	
	
	/**
	 * Updates the cached object and refreshes the last Access timestamp.
	 * @param cachedItem The new item to cache under the current name.
	 */
	public void set(Object cachedItem) {
		SoftReference reference = new SoftReference(cachedItem);
		super.set(reference);
	}	

	/**
	 * Retrieves the object. If the value has timed out, the item will be nulled and a null will be returned.
	 * Every access refreshes the last Access timestamp.
	 * @return The cached object or null if it has timed out.
	 */
	public Object get() {
		SoftReference reference = (SoftReference)super.get();
		if(reference==null || reference.get()==null) {
			cache.remove(name);
			return null;
		} else {
			return reference.get();
		}		
	}	
	
	/**
	 * Removes this item from cache and increments the service's collection counter when the object is finalized.
	 * @throws Throwable
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		if(!inactive) {
			try {
				cache.remove(name);
				collections.increment();
			} catch (Throwable t) {}
		}
		super.finalize();
	}
	
	/**
	 * Tests the cached Item to see if it is configured for time out and if it is, will expire it if it the expiry time has elapsed.
	 * @param currentTime The current time of the text.
	 * @return true if item was expired, false if not.
	 */
	public boolean testForExpiration(long currentTime) {
		boolean expired = super.testForExpiration(currentTime);
		if(expired) inactive = true;
		return expired;
	}
	
	/**
	 * Called by the cache service when the item is removed from cache.
	 */
	public void removed() {
		inactive = true;
	}
	
}
