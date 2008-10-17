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
import java.io.FileFilter;

/**
 * <p>Title:DirectoryFileFilter</p>
 * <p>Description: Filter for directories.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class DirectoryFileFilter implements FileFilter {

	/**
	 * Tests whether or not the specified abstract pathname represents a directory. 
	 * @param pathname The file to test.
	 * @return true if the file is a directory, false if it is not.
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File pathname) {
		return pathname.isDirectory();
	}

}
