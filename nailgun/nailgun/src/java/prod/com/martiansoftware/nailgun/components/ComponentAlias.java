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

package com.martiansoftware.nailgun.components;


/**
 * <p>Title:ComponentAlias</p>
 * <p>Description: Provides a means to map memorable, short names to component instances in order
 * to make the issuing of commands against managed components more convenient.  For example, a
 * ComponentAlias can map the <code>getJMXAttribute</code> to a managed component called <code>JMXClient</code>.
 * </p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */


public class ComponentAlias {
	/**
	 * The component alias name
	 */
	private String name;
	
	/**
	 * The component alias description (may be used to provide help to users)
	 */
	private String description;
	
	/**
	 * The managed component application context. We do not want to hold a reference directly to the object, since it may change.
	 * So each invocation will re-acquire the object instance by name from the <code>NGApplicationContext</code>. 
	 */
	private NGApplicationContext applicationContext;

	/**
	 * Creates a new ComponentAlias.
	 * @param name The name of the alias.
	 * @param description The description of the alias.
	 * @param applicationContext The component manager application context.
	 */
	public ComponentAlias(String name, String description,
			NGApplicationContext applicationContext) {
		super();
		this.name = name;
		this.description = description;
		this.applicationContext = applicationContext;
	}

	/**
	 * Returns the name of the component alias.
	 * @return the name of the component alias.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the name of the component description.
	 * @return the name of the component description.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Retrieves the component's object instance from the application context.
	 * @return The component's object instance
	 * @throws ComponentAliasException thrown if the component's object instance cannot be found or any other exception occurs when the object instance is de-referenced.  
	 */
	public Object getComponent() throws ComponentAliasException {
		try {
			Object component = applicationContext.getBean(name);
			if(component==null) throw new Exception("The component aliased [" + name + "] could not be located");
			return component;
		} catch (Exception e) {
			throw new ComponentAliasException("The component aliased [" + name + "] could not be retrieved", e);
		}
	}
		
}
