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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.martiansoftware.nailgun.NGContext;
import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;

/**
 * <p>Title:JDBCService</p>
 * <p>Description: A JDBC Database Interface NGServer Component.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class JDBCService extends BaseComponentService {

	/** The default JMX ObjectName for the GroovyService management interface */
	public static final String DEFAULT_OBJECT_NAME = "com.martiansoftware.components.cache:service=JDBC";
	
	/** The static code representing table formatted output */
	public static final String TAB_FORMAT = "T";
	/** The static code representing HTML formatted output */
	public static final String HTML_FORMAT = "H";	
	
	/** The static code representing csv output */
	public static final String CSV_FORMAT = "C";
	/** The static code representing xml output */
	public static final String XML_FORMAT = "X";
	/** The static code representing an EOL */
	public static final String EOL = System.getProperty("line.separator");

	/**
	 * 
	 */
	public JDBCService() {
	}

	/**
	 * @return
	 * @see com.martiansoftware.nailgun.components.builtins.base.BaseComponentService#getDefaultObjectName()
	 */
	public String getDefaultObjectName() {
		return DEFAULT_OBJECT_NAME;
	}
	
	/**
	 * Arguments:<ul>
	 * <li>pool name</li>
	 * <li>format</li>
	 * <li>sql</li>
	 * </ul>
	 * @param context
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.io.IOException
	 */
	public void nailMain(NGContext context) throws java.security.NoSuchAlgorithmException, java.io.IOException {
		String[] args = context.getArgs();
		String poolName = args[0];
		String formatCode = args[1];
		String sqlText = null;
		String delimeter = null;
		if(args.length==4) {
			delimeter = args[2];
			sqlText = args[3];
		} else {
			sqlText = args[2];
		}
//		String poolName = args[0];
//		String formatCode = args[1];
//		String sqlText = args[2];
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		ResultSetMetaData rsmd = null;
		
		try {
			try {
				conn = ((DataSource)context.getApplicationContext().getBean(poolName)).getConnection();
			} catch (Exception e) {
				String error = "Exception Acquiring Connection From Pool [" + poolName + "]:" + e;
				err.println(error);
				context.err.println("ERROR: " + error);
				totalComponentErrors.increment();
				return;							
			}
			try {
				ps = conn.prepareStatement(sqlText);
			} catch (Exception e) {
				String error = "Exception Compiling SQL Against Pool [" + poolName + "]:" + e;
				err.println(error);
				context.err.println("ERROR: " + error);
				totalComponentErrors.increment();
				return;							
			}
			try {
				
				rset = ps.executeQuery();
				ResultSetDOM trans = new ResultSetDOM();
				StreamSourceProvider styleSheet = null;
				try {
					styleSheet = (StreamSourceProvider)context.getApplicationContext().getBean(formatCode.toUpperCase() + "StyleSheet");
				} catch (Exception e) {
					reportError("Unable to locate transformer for [" + formatCode + "]", err, context.err);
					totalComponentErrors.increment();
					return;																	
				}				
				context.out.println(trans.transform(rset, styleSheet.getInstance(), delimeter));
				out.flush();
				return;
			} catch (Exception e) {				
				String error = "Exception Processing ResultSet from Pool [" + poolName + "]:" + e;
				err.println(error);
				e.printStackTrace(err);
				context.err.println("ERROR: " + error);
				totalComponentErrors.increment();
				return;							
			}
			
		} catch (Exception e) {
			String error = "Unexpected Exception Issuing SQL against pool [" + poolName + "]:" + e;
			err.println(error);
			e.printStackTrace(err);
			context.err.println("ERROR: " + error);
			totalComponentErrors.increment();
			return;			
		} finally {			
			try { if(rset!=null)rset.close(); } catch (Exception e) {}
			try { if(ps!=null)ps.close(); } catch (Exception e) {}
			try { if(conn!=null)conn.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Generates a comma separated values output for the passed result set.
	 * @param rset
	 * @return
	 * @throws SQLException
	 */
	private String generateCsv(ResultSet rset) throws SQLException {
		ResultSetMetaData rsmd;
		rsmd = rset.getMetaData();
		StringBuffer b = new StringBuffer();
		int columnCount = rsmd.getColumnCount();
		while(rset.next()) {
			for(int i = 1; i <=columnCount; i++) {
				String tmp = rset.getString(i);
				if(rset.wasNull()) {
					b.append("NULL");
				} else {
					b.append(tmp);
				}						
				if(i<columnCount) {
					b.append(",");
				}
			}
			b.append(EOL);
		}
		return b.toString();
	}

}
