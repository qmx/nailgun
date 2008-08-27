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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.helios.jmx.dynamic.annotations.JMXAttribute;
import org.helios.jmx.dynamic.annotations.JMXManagedObject;
import org.helios.jmx.dynamic.annotations.JMXOperation;
import org.helios.jmx.dynamic.annotations.JMXParameter;
import org.helios.jmx.dynamic.annotations.options.AttributeMutabilityOption;

/**
 * <p>Title: NGCache</p>
 * <p>Description: An in memory soft reference cache for arbitrary objects where nails can reference items in the NGServer.</p> 
 * <p>Copyright 2008, Martian Software, Inc.</p>
 * @author Whitehead (nwhitehead@heliosdev.org)
 * @version $Revision$
 */
@JMXManagedObject (annotated=true, declared=true)
public class NGCache implements Invalidateable {
	/** the map backing the cache */
	protected ConcurrentHashMap<String, SoftReference<Object>> cache = new ConcurrentHashMap<String, SoftReference<Object>>();
	/** A count of the total number of invalidated cache entries */
	protected AtomicLong invalidations = new AtomicLong(0);

	



	/**
	 * Creates a new instance of a NGCache.
	 */
	public NGCache() {
		
	}
	
	
	/**
	 * Invalidates the cache entries identified by each of the passed keys.
	 * @param keys The keys of the cache entries to invalidate.
	 * @return an array of successfully invalidated keys.
	 */
	@JMXOperation (name="invalidate", description="Invalidates the cache entries identified by each of the passed keys.")
	public String[] invalidate(
			@JMXParameter(name="keys", description="The keys of the cache entries to invalidate.") String... keys) {
		List<String> matches = new ArrayList<String>();
		for(String key: keys) {
			if(key!=null) {
				if(cache.get(key) != null) {
					matches.add(key);					
				}
			}
		}
		invalidations.getAndAdd(matches.size());
		return matches.toArray(new String[matches.size()]);
	}
	
	/**
	 * The total number of invalidations that have occurred since the cache started or the last reset.
	 * @return the invalidations
	 */
	@JMXAttribute(name="Invalidations", description="The total number of invalidations that have occurred since the cache started or the last reset.", mutability=AttributeMutabilityOption.READ_ONLY)
	public long getInvalidations() {
		return invalidations.get();
	}	
}
