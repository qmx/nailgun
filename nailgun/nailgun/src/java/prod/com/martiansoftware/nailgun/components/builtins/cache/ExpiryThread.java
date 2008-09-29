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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;

/**
 * <p>Title:ExpiryThread</p>
 * <p>Description: A thread that periodically spins the cache and expires cache items.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class ExpiryThread extends Thread {
	protected Map cache = null;
	protected long sleepTime = 0L;
	protected PrintStream out = null;
	protected static SynchronizedInt serial = new SynchronizedInt(0); 
	
	/**
	 * Creates a new expiry thread.
	 * @param cache The cache to expire items from.
	 * @param sleepTime The sleep peiod between expirations.
	 */
	public ExpiryThread(Map cache, long sleepTime) {
		this.cache = cache;
		this.sleepTime = sleepTime;
		this.setDaemon(true);
		serial.increment();
		this.setName("Expiry Thread#" + serial.get());
		out = System.out;
	}
	
	/**
	 * Starts the expiration thread. Sleeps the defined time, then iterates the cache, testing each item for expiration.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		while(true) {
			try {
				Thread.sleep(sleepTime);
			} catch (Exception e) {
				BaseComponentService.cerr(e);
			}
			long currentTime = System.currentTimeMillis();
			BaseComponentService.cout("Starting Expiry Sweep");
			for(Iterator cacheIterator = cache.values().iterator(); cacheIterator.hasNext();) {
				CacheEntry cacheEntry = (CacheEntry)cacheIterator.next();			
				cacheEntry.testForExpiration(currentTime);
			}
		}
	}
	
	
}
