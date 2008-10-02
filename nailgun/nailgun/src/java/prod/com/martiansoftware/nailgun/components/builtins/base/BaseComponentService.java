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

package com.martiansoftware.nailgun.components.builtins.base;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * <p>Title:BaseComponentService</p>
 * <p>Description: A base class for NGServer component classes.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public abstract class BaseComponentService implements MBeanRegistration {
	/** The MBeanServer where the cache's management interface is registered */
	protected MBeanServer server = null;
	/** The JMX ObjectName of the cache's management interface */
	protected ObjectName objectName = null;
	public static final PrintStream out;
	public static final PrintStream err;
	
	/** The total number of component calls */
	protected SynchronizedLong totalComponentCalls = new SynchronizedLong(0);
	/** The total number of failed component calls */
	protected SynchronizedLong totalComponentErrors = new SynchronizedLong(0);	
	/** The total elapsed time of component calls */
	protected SynchronizedLong totalComponentCallTime = new SynchronizedLong(0);
	
	
	
	
	static {
		out = System.out;
		err = System.err;
	}

	
	/**
	 * Returns an array of strings containing all strings in the passed array that start with the provided prefix.
	 * @param prefix The prefix to look for.
	 * @param array The array to search.
	 * @return A string array of matches.
	 */
	public static String[] getPrefixedStrings(String prefix, String[] array) {
		if(array==null || array.length<1) return new String[0];
		List matches = new ArrayList();
		for(int i = 0; i < array.length; i++) {
			if(array[i].startsWith(prefix)) {
				matches.add(array[i]);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}
	
	/**
	 * Removes matching strings from the passed array.
	 * @param prefix The prefix to match strings against that will be removed from the array.
	 * @param array The array to remove matches from.
	 * @return The new array with matching strings removed.
	 */
	public static String[] removePrefixedStrings(String prefix, String[] array) {
		if(array==null || array.length<1) return new String[0];
		List matches = new ArrayList();
		for(int i = 0; i < array.length; i++) {
			if(!array[i].startsWith(prefix)) {
				matches.add(array[i]);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}
	
	/**
	 * Removes the prefix from each member of the array.
	 * @param prefix The prefix to remove. 
	 * @param array The array to remove prefixes from.
	 * @return The prefix removed array.
	 */
	public static String[] removePrefixes(String prefix, String[] array) {
		if(array==null || array.length<1) return new String[0];
		for(int i = 0; i < array.length; i++) {
			if(array[i].startsWith(prefix)) {
				array[i] = array[i].substring(prefix.length());
			}
		}
		return array;		
	}
	
	/**
	 * Appends the passed items to the passed array.
	 * @param items The array of items to append.
	 * @param array The array to append the items to.
	 * @return The appended array.
	 */
	public static Object[] appendToArray(Object[] items, Object[] array) {
		if(items==null || items.length < 1) return array;
		Object[] newArr = new Object[array.length + items.length];
		System.arraycopy(array, 0, newArr, 0, array.length);
		System.arraycopy(items, 0, newArr, array.length, items.length);
		return newArr;
	}
	
	/**
	 * Prefixes the passed items to the passed array.
	 * @param items The array of items to prefix.
	 * @param array The array to prefix the items to.
	 * @return The prefixed array.
	 */
	public static Object[] prefixToArray(Object[] items, Object[] array) {
		if(items==null || items.length < 1) return array;
		Object[] newArr = new Object[array.length + items.length];
		System.arraycopy(items, 0, newArr, 0, items.length);
		System.arraycopy(array, 0, newArr, items.length, array.length);
		return newArr;
	}
	
	/**
	 * Extracts the range of array items from the passed array and returns them in a new array.
	 * Range indexes are inclusive.
	 * @param array The array to extract from.
	 * @param start The starting index of the range.
	 * @param end The ending index of the range.
	 * @return
	 */
	public static String[] extract(String[] array, int start, int end) {
		if(array==null || array.length < 1) return array;
		if(start<0) throw new RuntimeException("Start index is < 1");
		if(end<0) throw new RuntimeException("End index is < 1");
		if(end<start) throw new RuntimeException("Start index is greater than end index");
		if(end>array.length-1) throw new RuntimeException("End index exceeds size of array");
		String[] newArray = new String[end-start+1];
		int x = 0;
		for(int i = 0; i < array.length; i++) {
			if(i >= start && i <= end) {
				newArray[x] = array[i];
				x++;
			}
		}
		return newArray;
	}
	
	
	/**
	 * Reports the passed error string to the NGServer console and the client error stream.
	 * @param message The error message
	 * @param consoleErr The console output
	 * @param clientErr The client output
	 */
	public static void reportError(String message, PrintStream consoleErr, PrintStream clientErr) {		
		consoleErr.println(message);
		clientErr.println("ERROR: " + message);
	}
	
	
	
	/**
	 * The calculated average elapsed time of successful component calls.
	 * @return the average elapsed time of calls.
	 */
	public long getAverageComponentCallTime() {
		return average(totalComponentCalls.get(), totalComponentCallTime.get());
	}
	
	/**
	 * The total number of successful component calls
	 * @return the totalCalls
	 */
	public long getComponentTotalCalls() {
		return totalComponentCalls.get();
	}


	/**
	 * The total elapsed time of successful component  calls
	 * @return the totalCallTime
	 */
	public long getComponentTotalCallTime() {
		return totalComponentCallTime.get();
	}


	/**
	 * The total number of errors encountered calling the component Service.
	 * @return the totalErrors
	 */
	public long getComponentTotalErrors() {
		return totalComponentErrors.get();
	}
	
	
	/**
	 * The default JMX ObjectName for the component's management interface
	 * @return 
	 */
	public abstract String getDefaultObjectName();
	
	/**
	 * The MBeanServer where the CacheService mamagement interface is registered.
	 * @return the mbeanserver
	 */
	public MBeanServer getServer() {
		return server;
	}

	/**
	 * Sets the MBeanServer where the CacheService mamagement interface is registered.
	 * If the interface has not been registered, it will be registrered with this MBeanServer.
	 * @param server the server to set
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
		if(this.server != null) {
			objectName = generateUniqueObjectName();
			if(objectName != null) {
				try {
					server.registerMBean(this, objectName);
				} catch (Exception e) {
					System.err.println("Failed to register CacheService component under ObjectName [" + objectName + "]:" + e);
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Returns the default ObjectName for the cache management interface.
	 * If that ObjectName is already in use, a limited number of numerical variations will be attempted.
	 * If a unique name cannot be found, or an error occurs or this object's MBeanServer has not been assigned, will return null.
	 * @return The generated ObjectName or null if one cannot be generated. 
	 */
	protected ObjectName generateUniqueObjectName() {
		try {
			ObjectName on = null;
			int maxCounter = 10;
			String defaultObjectName = getDefaultObjectName();
			if(server !=null) {
				on = new ObjectName(defaultObjectName);
				if(!server.isRegistered(on)) {
					return on;
				} else {
					for(int i = 1; i < maxCounter; i++) {
						on = new ObjectName(defaultObjectName + ",sequence=" + i);
						if(server.isRegistered(on)) {
							return on;
						}
					}
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	

	/**
	 * 
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	public void postDeregister() {
		objectName = null;
	}

	/**
	 * @param success
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	public void postRegister(Boolean success) {
		
	}

	/**
	 * @throws Exception
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {
		
	}

	/**
	 * @param server
	 * @param objectName
	 * @return
	 * @throws Exception
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName objectName)
			throws Exception {
		this.server = server;
		this.objectName = objectName;
		return this.objectName;
	}
	
	/**
	 * Outputs standard message back to calling client.
	 * @param message
	 */
	public void out(Object message) {
		System.out.println(message);
	}
	
	/**
	 * Outputs error message back to calling client.
	 * @param message
	 */		
	public void err(Object message) {
		System.err.println(message);
	}
	
	/**
	 * Outputs standard message on server console.
	 * @param message
	 */
	public static void cout(Object message) {
		out.println(message);
	}
	
	/**
	 * Outputs error message on server console.
	 * @param message
	 */		
	public static void cerr(Object message) {
		err.println(message);
	}
	
	
	/**
	 * Calculates an integral percentage.
	 * @param part
	 * @param total
	 * @return
	 */
	public static int percentage(long part, long total) {
		float p = part;
		float t = total;
		float per = p/t*100;
		return (int)per;
	}
	
	/**
	 * Calcualtes an integral average.
	 * @param count
	 * @param window
	 * @return
	 */
	public static long average(long count, long window) {
		float c = count;
		float w = window;
		float avg = window/count;
		return (long)avg;
	}
	
	
	
}
