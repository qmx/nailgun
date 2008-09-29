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

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

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
	protected static PrintStream out = null;
	protected static PrintStream err = null;
	
	static {
		out = System.out;
		err = System.err;
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
	
	
	
	
	
}
