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

package com.martiansoftware.nailgun.components.builtins.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.martiansoftware.nailgun.NGContext;
import com.martiansoftware.nailgun.components.NGApplicationContext;
import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;

/**
 * <p>Title:JMXAttributeService</p>
 * <p>Description:</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class JMXAttributeService extends BaseComponentService {

	/** The default JMX ObjectName for the GroovyService management interface */
	public static final String DEFAULT_OBJECT_NAME = "com.martiansoftware.components.cache:service=JMXAttributes";

	/**
	 * Generates a sensible JMX object name for this component.
	 * @return A JMX ObjectName
	 * @see com.martiansoftware.nailgun.components.builtins.base.BaseComponentService#getDefaultObjectName()
	 */
	public String getDefaultObjectName() {
		return DEFAULT_OBJECT_NAME;
	}
	
	public void nailMain(NGContext context) {
		String[] args = context.getArgs();
		NGApplicationContext appContext = context.getApplicationContext();
		try {
			String connectionName = args[0];
			String objectName = args[1];
			String[] attributes = extract(args, 2, args.length-1);
			MBeanServerConnection server = (MBeanServerConnection) appContext.getBean(connectionName);
			ObjectName on = null;
			try {
				on = new ObjectName(objectName);
			} catch (Exception e) {
				reportError("JMXAttributeService Saw Invalid Object Name [" + objectName + "]:" + e, err, context.err);
				totalComponentErrors.increment();
				return;																								
			}			
			try {
				AttributeList results = server.getAttributes(on, attributes);
				StringBuffer b = new StringBuffer();
				for(int i = 0; i < results.size(); i++) {
					Attribute attribute = (Attribute) results.get(i);
					b.append(attribute.getName()).append("=").append(attribute.getValue().toString()).append("\n");
				}				
				context.out.println(b.toString());
				context.out.flush();
			} catch (Exception e) {
				reportError("Error In JMXAttributeService Retrieving From [" + connectionName + "] for [" + objectName + "]:" + e, err, context.err);
				totalComponentErrors.increment();
				return;																								
			}		
		} catch (Exception e) {
			reportError("Unexpected Error In JMXAttributeService:" + e, err, context.err);
			totalComponentErrors.increment();
			e.printStackTrace(err);
			return;																				
		}
	}

}
