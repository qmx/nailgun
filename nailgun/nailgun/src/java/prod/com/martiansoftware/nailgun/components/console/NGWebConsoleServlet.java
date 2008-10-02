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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.AliasManager;
import com.martiansoftware.nailgun.NGConstants;
import com.martiansoftware.nailgun.NGServer;
import com.martiansoftware.nailgun.NGSessionPool;
import com.martiansoftware.nailgun.NailStats;
import com.martiansoftware.nailgun.components.NGReference;

/**
 * <p>Title:NGWebConsoleServlet</p>
 * <p>Description: A servlet that generates the Nailgun Web Console</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class NGWebConsoleServlet extends HttpServlet implements BeanFactoryAware {
	
	protected BeanFactory beanFactory = null;
	protected String templateFileName = null;
	protected String template = null;
	protected volatile NGReference reference = null;
	/**
	 * Creates a new NGWebConsoleServlet
	 */
	public NGWebConsoleServlet(String templateFileName) {
		this.templateFileName=templateFileName;
		
	}
	
	/**
	 * Called by the servlet container to indicate to a servlet that the servlet is being placed into service. 
	 * @param config a ServletConfig object containing the servlet's configuration and initialization parameters 
	 * @throws ServletException
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init() throws ServletException {
		try {
			File f = new File(templateFileName);
			if(!f.exists() || !f.canRead()) throw new Exception("Cannot read template file [" + templateFileName + "]");
			log("Reading template file ["+ templateFileName + "]");
			BufferedReader br = null;
			try {
				StringBuffer b = new StringBuffer();
				br = new BufferedReader(new FileReader(f));
				String line = null;
				while((line=br.readLine()) != null) {
					b.append(line);
				}
				template = b.toString();
				log("Read template file ["+ templateFileName + "] successfully.");
			} finally {
				try { br.close(); } catch (Exception e) {}
			}
		} catch (Exception e) {			
			log("Failed to start NGWebConsoleServlet:" + e);
			throw new ServletException("Failed to start NGWebConsoleServlet", e);
		}
	}
	
	public void log(String message) {
		System.out.println("[NGWebConsoleServlet]:" + message);
	}
	
	/**
	 * Processes requests to render the console.
	 * @param req
	 * @param resp
	 * @throws IOException
	 * @throws ServletException
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		// Test the NG reference
		try {
			if(reference==null) {
				synchronized(this) {
					if(reference==null) {
						reference = (NGReference)beanFactory.getBean("NGReference");
						if(reference.getServer()==null) {
							throw new Exception("NGReference loaded but server was null");
						}
					}
				}
			}
		} catch (Exception e) {
			resp.sendError(500, "Error. NGReference Component Could Not Be Loaded:" + e);
			return;
		}
		// if we get here, the reference is ok
		try {
			
			String output = template.replaceAll("###VERSION###", NGConstants.VERSION);
			output = output.replaceFirst("###ALIASES###", generateAliases(reference.getAliasManager()));			
			output = output.replaceFirst("###COMPONENTS###", generateComponents());
			output = output.replaceFirst("###NGSTATS###", generateNailStats(reference.getServer()));
			output = output.replaceFirst("###NGSESSIONPOOL###", generatePoolStats(reference.getSessionPool()));
			
			PrintWriter pw = resp.getWriter();
			resp.setBufferSize(output.length());
			resp.setContentLength(output.length());
			pw.print(output);
			pw.flush();
			pw.close();
		} catch (Exception e) {
			throw new ServletException("Unexpected error rendering console", e);
		}
		
		
	}

	/**
	 * Callback from the Spring container, passing in a reference to the bean factory.
	 * @param beanFactory The Spring bean factory.
	 * @throws BeansException
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	/**
	 * Generates an HTML table of the registered aliases.
	 * @param aliasManager The alias manager
	 * @return An HTML table string
	 */
	public String generateAliases(AliasManager aliasManager) {
		StringBuffer b = new StringBuffer("<table border='1'><tr><th align='center'>Alias</th><th align='center'>Class</th><th align='center'>Description</th></tr>");
		Iterator aliases = aliasManager.getAliases().iterator();
		while(aliases.hasNext()) {
			Alias alias = (Alias)aliases.next();
			b.append("<tr>");
			b.append("<td>").append(alias.getName()).append("</td>");
			b.append("<td>").append(alias.getAliasedClass().getName()).append("</td>");
			b.append("<td>").append(alias.getDescription()).append("</td>");			
			b.append("</tr>");
		}
		b.append("</table>");
		return b.toString().replaceAll("\\$", "\\\\" + "\\$");
	}
	
	/**
	 * Generates an HTML table of the session pool stats
	 * @param pool The NGSession pool
	 * @return An HTML table string
	 */
	public String generatePoolStats(NGSessionPool pool) {
		StringBuffer b = new StringBuffer("<table border='1'><tr><th align='center'>Pool Size</th><th align='center'>Sessions Available</th><th align='center'>Overages</th></tr>");	
		b.append("<tr>");
		b.append("<td>").append(pool.getPoolSize()).append("</td>");
		b.append("<td>").append(pool.getPoolEntries()).append("</td>");
		b.append("<td>").append(pool.getOverages()).append("</td>");			
		b.append("</tr>");
		b.append("</table>");
		return b.toString().replaceAll("\\$", "\\\\" + "\\$");
	}
	
	/**
	 * Generates an HTML table of the nail stats
	 * @param server The NGServer
	 * @return An HTML table string
	 */
	public String generateNailStats(NGServer server) {
		StringBuffer b = new StringBuffer("<table border='1'><tr><th align='center'>Class</th><th align='center'>Run Count</th><th align='center'>Ref Count</th></tr>");
		Map stats = server.getNailStats();
		for(Iterator statsIter = stats.keySet().iterator(); statsIter.hasNext();) {
			String nailClass = (String)statsIter.next();
			NailStats stat = (NailStats)stats.get(nailClass);
			b.append("<tr>");
			b.append("<td>").append(nailClass).append("</td>");
			b.append("<td>").append(stat.getRunCount()).append("</td>");
			b.append("<td>").append(stat.getRefCount()).append("</td>");
			b.append("</tr>");
		}
		b.append("</table>");
		return b.toString().replaceAll("\\$", "\\\\" + "\\$");
	}
	
	/**
	 * Generates an HTML table of the registered components.
	 * @return An HTML table string
	 */
	public String generateComponents() {
		StringBuffer b = new StringBuffer("<table border='1'><tr><th align='center'>Name</th><th align='center'>Class</th><th align='center'>Constructor Args</th><th align='center'>Properties</th></tr>");
		
		DefaultListableBeanFactory appContext = (DefaultListableBeanFactory)beanFactory;
		String[] componentNames = appContext.getBeanDefinitionNames();
		Object component = null;
		for(int i = 0; i < componentNames.length; i++) {
			component = appContext.getBean(componentNames[i]);
			BeanDefinition beanDef = appContext.getBeanDefinition(componentNames[i]);
			b.append("<tr>");
			b.append("<td>").append(componentNames[i]).append("</td>");
			b.append("<td>").append(component.getClass().getName()).append("</td>");
			b.append("<td>N/A");
//			ConstructorArgumentValues cav = beanDef.getConstructorArgumentValues();
//			b.append("<ul>");
//			Map args = cav.getIndexedArgumentValues();
//			
//			for(int c = 0; c < args.size(); c++) {
//				b.append("<li>").append(((ConstructorArgumentValues.ValueHolder)args.get(new Integer(c))).getConvertedValue()).append("</li>");
//			}
//			b.append("</ul>");
			b.append("</td>");
			b.append("<td>N/A");
			b.append("</td>");
			
			b.append("</tr>");			
		}
		b.append("</table>");
		return b.toString().replaceAll("\\$", "\\\\" + "\\$");
	}
	

	//public static final String TEMPLATE = 
		
}
