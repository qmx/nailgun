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
import javax.management.remote.JMXConnectorServer;

/**
 * <p>Title:JMXServerConnectionPrinter</p>
 * <p>Description: Prints the details of a JMXServerConnector.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class JMXServerConnectionPrinter {
	public JMXServerConnectionPrinter(JMXConnectorServer server) {
		StringBuffer b = new StringBuffer("JMXConnectorServer:");
		if(server==null) {
			b.append("null");
		} else {
			b.append("\n\tAddress:[").append(server.getAddress().toString()).append("]");
			b.append("\n\tActive:[").append(server.isActive()).append("]");
		}
		
		System.out.println(b);
	}
	
}
