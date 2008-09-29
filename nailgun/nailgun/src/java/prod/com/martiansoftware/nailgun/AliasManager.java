/*   

  Copyright 2004, Martian Software, Inc.

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

package com.martiansoftware.nailgun;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.martiansoftware.nailgun.components.ComponentAlias;

/**
 * An AliasManager is used to store and lookup command Aliases by name.
 * See <a href="Alias.html">Alias</a> for more details.
 * 
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 */
public class AliasManager {
	
	/**
	 * actual alias storage
	 */
	private Map aliases;
	
	/**
	 * component alias storage
	 */
	private Map componentAliases;
	
	
	/**
	 * Creates a new AliasManager, populating it with
	 * default Aliases.
	 */
	public AliasManager() {
		aliases = new java.util.HashMap();
		componentAliases = new java.util.HashMap();
		
		try {
			Properties props = new Properties();
			props.load(getClass().getClassLoader().getResourceAsStream("com/martiansoftware/nailgun/builtins/builtins.properties"));
			loadFromProperties(props);
		} catch (java.io.IOException e) {
			System.err.println("Unable to load builtins.properties: " + e.getMessage());
		}
	}
	
	/**
	 * Loads Aliases from a java.util.Properties file located at the
	 * specified URL.  The properties must be of the form:
	 * <pre><code>[alias name]=[fully qualified classname]</code></pre>
	 * each of which may have an optional
	 * <pre><code>[alias name].desc=[alias description]</code></pre>
	 * 
	 * For example, to create an alias called "<code>myprog</code>" for
	 * class <code>com.mydomain.myapp.MyProg</code>, the following properties
	 * would be defined:
	 * 
	 * <pre><code>myprog=com.mydomain.myapp.MyProg
	 *myprog.desc=Runs my program.
	 * </code></pre>
	 * @param properties the Properties to load.
	 */
	public void loadFromProperties(java.util.Properties properties) {
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (!key.endsWith(".desc")) {
				try {
					Class clazz = Class.forName(properties.getProperty(key));
					String desc = properties.getProperty(key + ".desc", "");
					addAlias(new Alias(key, desc, clazz));
				} catch (ClassNotFoundException e) {
					System.err.println("Unable to locate class " + properties.getProperty(key));
				}
			}
		}
	}
	
	/**
	 * Adds an Alias, replacing any previous entries with the
	 * same name.
	 * @param alias the Alias to add
	 */
	public void addAlias(Alias alias) {
		synchronized (aliases) {
			aliases.put(alias.getName(), alias);
		}
	}
	
	/**
	 * Adds a component Alias, replacing any previous entries with the
	 * same name. 
	 * @param alias the component Alias to add
	 */
	public void addComponentAlias(ComponentAlias alias) {
		synchronized (componentAliases) {
			componentAliases.put(alias.getName(), alias);
		}
	}	
	
	/**
	 * Returns a Set that is a snapshot of the Alias list.
	 * Modifications to this Set will not impact the AliasManager
	 * in any way.
	 * @return a Set that is a snapshot of the Alias list.
	 */
	public Set getAliases() {
		Set result = new java.util.TreeSet();
		synchronized(aliases) {
			result.addAll(aliases.values());
		}
		return (result);
	}
	
	/**
	 * Returns a Set that is a snapshot of the ComponentAlias list.
	 * Modifications to this Set will not impact the AliasManager
	 * in any way.
	 * @return a Set that is a snapshot of the ComponentAlias list.
	 */
	public Set getComponentAliases() {
		Set result = new java.util.TreeSet();
		synchronized(componentAliases) {
			result.addAll(componentAliases.values());
		}
		return (result);
	}	

	/**
	 * Removes the Alias with the specified name from the AliasManager.
	 * If no such Alias exists in this AliasManager, this method has no effect.
	 * @param aliasName the name of the Alias to remove
	 */
	public void removeAlias(String aliasName) {
		synchronized (aliases) {
			aliases.remove(aliasName);
		}
	}
	
	/**
	 * Removes the ComponentAlias with the specified name from the AliasManager.
	 * If no such ComponentAlias exists in this AliasManager, this method has no effect.
	 * @param aliasName the name of the ComponentAlias to remove
	 */
	public void removeComponentAlias(String aliasName) {
		synchronized (componentAliases) {
			componentAliases.remove(aliasName);
		}
	}
	

	/**
	 * Returns the Alias with the specified name
	 * @param aliasName the name of the Alias to retrieve
	 * @return the requested Alias, or null if no such Alias
	 * is defined in this AliasManager.
	 */
	public Alias getAlias(String aliasName) {
		return ((Alias) aliases.get(aliasName));
	}
	
	/**
	 * Returns the ComponentAlias with the specified name
	 * @param aliasName the name of the ComponentAlias to retrieve
	 * @return the requested ComponentAlias, or null if no such ComponentAlias
	 * is defined in this AliasManager.
	 */
	public ComponentAlias getComponentAlias(String aliasName) {
		return ((ComponentAlias) componentAliases.get(aliasName));
	}	

}
