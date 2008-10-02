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

import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

/**
 * <p>Title:SimpleStyleSheetProvider</p>
 * <p>Description: A bean hat can be defined with the text of a style sheet and provide
 * instances of <code>javax.xml.transform.stream.StreamSource</code>s.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class SimpleStyleSheetProvider implements StreamSourceProvider {
	protected String styleSheet = null;
	
	/**
	 * Creates a new SimpleStyleSheetProvider that will generate instances of a <code>StreamSource</code>
	 * based on the <code>styleSheet</code> the instance is cofigured with.
	 * @return A StreamSource
	 * @see com.martiansoftware.nailgun.components.builtins.jdbc.StreamSourceProvider#getInstance()
	 */
	public StreamSource getInstance() {
		StringReader sr = new StringReader(styleSheet);
		StreamSource ss = new StreamSource(sr);		
		return ss;
	}

	/**
	 * @param styleSheet
	 */
	public SimpleStyleSheetProvider(String styleSheet) {
		super();
		this.styleSheet = styleSheet;
	}

	/**
	 * @return the styleSheet
	 */
	public String getStyleSheet() {
		return styleSheet;
	}

	/**
	 * @param styleSheet the styleSheet to set
	 */
	public void setStyleSheet(String styleSheet) {
		this.styleSheet = styleSheet;
	}
}
