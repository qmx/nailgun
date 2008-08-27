/**   

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
package com.martiansoftware.nailgun.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Title: InactivityExpiringCacheEntryContainer</p>
 * <p>Description: </p> 
 * <p>Copyright 2008, Martian Software, Inc.</p>
 * @author Whitehead (nwhitehead@heliosdev.org)
 * @version $Revision$
 */

public class InactivityExpiringCacheEntryContainer<T> implements
		ExpiringCacheEntry {
	protected T cacheEntry = null;
	protected AtomicLong lastAccessTime = new AtomicLong(System.currentTimeMillis());
	
	/**
	 * 
	 */
	public InactivityExpiringCacheEntryContainer(T cacheEntry) {
		this.cacheEntry = cacheEntry;
	}
	
	/**
	 * Returns the cache entry.
	 * @return
	 */
	public T get() {
		access();
		return cacheEntry;
	}

	/**
	 * 
	 * @see com.martiansoftware.nailgun.cache.ExpiringCacheEntry#access()
	 */
	@Override
	public void access() {
		lastAccessTime.set(System.currentTimeMillis());
	}

	/**
	 * @return
	 * @see com.martiansoftware.nailgun.cache.ExpiringCacheEntry#isExpired()
	 */
	public boolean isExpired() {
		// TODO Auto-generated method stub
		return false;
	}

}
