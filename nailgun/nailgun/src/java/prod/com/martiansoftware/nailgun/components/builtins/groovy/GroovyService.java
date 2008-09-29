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

package com.martiansoftware.nailgun.components.builtins.groovy;

import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import com.martiansoftware.nailgun.NGContext;
import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;
import com.martiansoftware.nailgun.components.builtins.cache.CacheService;

/**
 * <p>Title:GroovyService</p>
 * <p>Description: A Groovy Script management component. Scripts are compiled on the fly with subsequent calls using the compiled script until a change is detected in the source and it is recompiled.
 * The service depends on the <code>CacheService</code> which is used to cache compiled scripts.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class GroovyService extends BaseComponentService {
	
	/** The groovy compilation properties */
	protected Properties groovyCompilerProperties = new Properties();
	/** Additional properties to be passed into the script's binding */
	protected Properties scriptProperties = new Properties();
	/** The cache service to store groovy compiled scripts */
	protected CacheService cache = null;

	/** The default JMX ObjectName for the GroovyService management interface */
	public static final String DEFAULT_OBJECT_NAME = "com.martiansoftware.components.cache:service=Groovy";
	
	/** The cache key name prefix to avoid namespace collisions. */
	public static final String SCRIPT_PREFIX = "groovy/";
	
	/**
	 * Creates a new GroovyService component.
	 * @param cache The cache service to store groovy compiled scripts
	 */
	public GroovyService(CacheService cache) {
		this.cache = cache;
	}

	
	/*
	 * 1. Create new script and call: Source URL, Name, args[]
	 * 2. Call script: Name, args[]
	 */
	
	/**
	 * GroovyScriptManager(URL sourceUrl, Properties groovyProperties, Properties scriptProperties, Map args, PrintStream out)
	 * Call patterns:<ul>
	 * <li>Source URL, scriptName, methodName, args</li>
	 * <li>scriptName, methodName, args</li>
	 * </ul>
	 * @param context
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.io.IOException
	 */
	public void nailMain(NGContext context) throws java.security.NoSuchAlgorithmException, java.io.IOException {
		scriptProperties.put("out", context.out);
		scriptProperties.put("err", context.err);
		scriptProperties.put("ngcontext", context);
		
		String[] args = context.getArgs();
		URL scriptUrl = null;
		String scriptName = null;
		String methodName = null;
		Object[] scriptArgs = null;
		int argsStartIndex = 0;
		// if the first arg is a valid URL, then a new script is being submitted.
		try {
			scriptUrl = new URL(args[0]);
			scriptName = SCRIPT_PREFIX + args[1];
			methodName = args[2];
			argsStartIndex = 3;
		} catch (Exception e) {
			scriptUrl = null;
			scriptName = SCRIPT_PREFIX + args[0];
			methodName = args[1];
			argsStartIndex = 2;
		}
		scriptArgs = new Object[args.length-argsStartIndex];
		System.arraycopy(args, argsStartIndex, scriptArgs, 0, args.length-argsStartIndex);
		GroovyScriptManager gsm = (GroovyScriptManager)cache.get(scriptName);
		if(gsm==null) {
			if(scriptUrl==null) {
				err.println("Invalid Source URL [" + args[0] + "] or script not found in cache");
				context.err.println("ERROR: Invalid Source URL [" + args[0] + "] or script not found in cache");
				return;
			}
			gsm = new GroovyScriptManager(scriptUrl, groovyCompilerProperties, scriptProperties, new HashMap(), out);
			cache.put(scriptName, gsm);
		} else {
			if( scriptUrl!= null ) {
				long cacheTime = cache.getLastUpdateTime(scriptName);
				if(GroovyScriptManager.isModified(scriptUrl, cacheTime)) {
					gsm = new GroovyScriptManager(scriptUrl, groovyCompilerProperties, scriptProperties, new HashMap(), out);
					cache.put(scriptName, gsm);					
				}
			}
		}
		gsm.invokeMethod(methodName, scriptArgs);
	}
	
	/**
	 * Generates a sensible default objectName for this class.
	 * @return An objectName string.
	 * @see com.martiansoftware.nailgun.components.builtins.base.BaseComponentService#getDefaultObjectName()
	 */
	public String getDefaultObjectName() {
		return DEFAULT_OBJECT_NAME;
	}



	/**
	 * The script manager's groovy compilation properties.
	 * @return the groovyCompilerProperties
	 */
	public Properties getGroovyCompilerProperties() {
		return groovyCompilerProperties;
	}



	/**
	 * Adds the passed properties to the compilation properties used to compile each managed script.
	 * @param groovyCompilerProperties the groovyCompilerProperties to add
	 */
	public void setGroovyCompilerProperties(Properties groovyCompilerProperties) {
		this.groovyCompilerProperties.putAll(groovyCompilerProperties);
	}

	/**
	 * The managed scripts' built-in properties.
	 * @return the scriptProperties
	 */
	public Properties getScriptProperties() {
		return scriptProperties;
	}

	/**
	 * Adds the passed properties to all managed scripts' built-in properties. 
	 * @param scriptProperties the scriptProperties to add
	 */
	public void setScriptProperties(Properties scriptProperties) {
		this.scriptProperties.putAll(scriptProperties);
	}

	public int getScriptCacheSize() {
		return cache.getNames("groovy/.*").length;
	}



}
