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

package com.martiansoftware.nailgun.components.console;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.resource.FileResource;


/**
 * <p>Title:JettyContextBuilder</p>
 * <p>Description: A spring bean to register spring deployed servlets to jetty</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class JettyContextBuilder {
	protected Server server = null;
	protected Map servlets = new HashMap();
	protected Map resourceHandlers = new HashMap();
	
	
	
	/**
	 * @param resourceHandlers the resourceHandlers to set
	 */
	public void setResourceHandlers(Map resourceHandlers) {
		this.resourceHandlers = resourceHandlers;
	}

	public void init() throws Exception {
		HandlerList handlers = new HandlerList();
		ServletHandler servletHandler = new ServletHandler();
		//server.setHandler(servletHandler);
		for(Iterator servletIter = servlets.keySet().iterator(); servletIter.hasNext();) {
			String key = servletIter.next().toString();
			servletHandler.addServletWithMapping(new ServletHolder((Servlet)servlets.get(key)), key);
		}
		handlers.addHandler(servletHandler);
		
		handlers.addHandler(new DefaultHandler());
		server.setHandlers(handlers.getHandlers());
		
        
		server.start();
	}
	
	/**
	 * @return the server
	 */
	public Server getServer() {
		return server;
	}
	/**
	 * @param server the server to set
	 */
	public void setServer(Server server) {
		this.server = server;
	}
	/**
	 * @param contexts the contexts to set
	 */
	public void setServlets(Map servlets) {
		this.servlets = servlets;
	}

}
