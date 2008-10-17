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

package com.martiansoftware.nailgun.util.io;

import java.io.File;
import java.io.FilenameFilter;

/**
 * <p>Title:ConfigurableFileExtensionFilter</p>
 * <p>Description: File filter for selecting files with defined extensions.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class ConfigurableFileExtensionFilter implements FilenameFilter {
	/** An array of file extensions that will be accepted */
	protected String[] extensions = null;
	/** Indicates if filter is case sensitive */
	protected boolean caseSensitive = false;
	
	/**
	 * Creates a new ConfigurableFileExtensionFilter which is not case sensitive
	 * and will accept files with the passed extensions.
	 * @param extensions File extensions this filter should accept.
	 */
	public ConfigurableFileExtensionFilter(String[] extensions) {
		this.extensions = extensions;
		if(this.extensions==null) this.extensions = new String[]{}; 
	}
	
	/**
	 * Creates a new ConfigurableFileExtensionFilter with the defined case sensitivity
	 * and will accept files with the passed extensions. 
	 * @param caseSensitive if true, file extension tests will be case sensitive.
	 * @param extensions File extensions this filter should accept.
	 */
	public ConfigurableFileExtensionFilter(boolean caseSensitive, String[] extensions) {
		this(extensions);
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Tests if a specified file should be included in a file list.  
	 * @param dir the directory in which the file was found.
	 * @param name the name of the file. 
	 * @return true if and only if the name matches one of the configured extensions.
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File dir, String name) {
		for(int i = 0; i < extensions.length; i++) {			
			if(caseSensitive) {
				return name.endsWith(extensions[i]);
			} else {
				return name.toUpperCase().endsWith(extensions[i].toUpperCase());
			}
		}
		return false;
	}
	
}