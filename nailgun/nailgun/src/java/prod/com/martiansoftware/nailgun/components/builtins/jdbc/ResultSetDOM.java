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

package com.martiansoftware.nailgun.components.builtins.jdbc;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;

/**
 * <p>Title:ResultSetDOM</p>
 * <p>Description: Converts a ReasultSet to an XML DOM</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class ResultSetDOM implements BeanFactoryAware {
	protected BeanFactory beanFactory = null;
	
	
	/**
	 * Generates a XML Document from the passed ResultSet.
	 * @param rset The result to convert to a DOM.
	 * @return
	 */
	public Document generateDOM(ResultSet rset) {
		try {
			ResultSetMetaData rsmd = rset.getMetaData();
			int columnCount = rsmd.getColumnCount();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element root = document.createElement("resultset");
			document.appendChild(root);
			String[] columnNames = new String[columnCount];
			Element names = document.createElement("names");
			root.appendChild(names);
			for (int i = 0; i < columnCount; i++){			
				columnNames[i] = rsmd.getColumnName(i + 1);
				Element nextNameNode = document.createElement("name");
				Text nextName = document.createTextNode(columnNames[i]);
				nextNameNode.appendChild(nextName);
				names.appendChild(nextNameNode);
				
			}
			/* Move the cursor through the data one row at a time. */
			while(rset.next()){
				/* Create an Element node for each row of data. */
				Element nextRow = document.createElement("row");				
				for (int i = 0; i < columnCount; i++){
					/* Create an Element node for each column value. */
					Element nextNode = document.createElement(columnNames[i]);
					/* the first column is 1, the second is 2, ... */
					/* getString() will retrieve any of the basic SQL types*/
					Text text = document.createTextNode(rset.getString(i + 1));
					nextNode.appendChild(text);
					nextRow.appendChild(nextNode);
				}
				root.appendChild(nextRow);
			}
			return document;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Exception Converting ResultSet to DOM", e);
		}
	}

	/**
	 * Callback from Spring to pass a handle of the beanFactory to instances of this class.
	 * @param beanFactory The Spring BeanFactory.
	 * @throws BeansException
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;		
	}
	
	/**
	 * Transforms the XML Document using the passed stylesheet provider.
	 * @param document The document to transform.
	 * @param streamSource A provider for a stylesheet.
	 * @param delimeter An optional field delimeter. Ignored if null.
	 * @return A rendered XSLT output.
	 * @throws TransformerException
	 */
	public String transform(Document document, StreamSource streamSource, String delimeter) throws TransformerException {
		try {
			TransformerFactory transformerfactory = TransformerFactory.newInstance();
			Transformer transformer = transformerfactory.newTransformer(streamSource);
			if(delimeter != null) {
				transformer.setParameter("delim", delimeter);
			}		
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			transformer.transform(new DOMSource(document.getDocumentElement()), result);
			return sw.toString();
		} catch (Exception e) {
			e.printStackTrace(BaseComponentService.err);
			return "ERROR:" + e;
		}
	}
	
	/**
	 * Convenience call that generates the documents and the transform output
	 * @param rset The result set
	 * @param streamSource the style sheet provider
	 * @return The rendered output
	 * @throws TransformerException
	 */	
	public String transform(ResultSet rset, StreamSource streamSource) throws TransformerException {
		return transform(generateDOM(rset), streamSource, null);
	}
	
	public String transform(ResultSet rset, StreamSource streamSource, String delimeter) throws TransformerException {
		return transform(generateDOM(rset), streamSource, delimeter);
	}
	
	
	
}
