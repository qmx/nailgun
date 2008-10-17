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
import java.util.HashSet;
import java.util.Set;


/**
 * <p>Title:RecursiveDirectorySearch</p>
 * <p>Description: A utility for searching a set of directories recursively for matches.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class RecursiveDirectorySearch {
	/** A directory filter to locate nested directories. */
	protected static DirectoryFileFilter dff = new DirectoryFileFilter();
	
	/**
	 * Recursively searches all the passed directories and returns a set of filenames that matched the passed filter.
	 * @param filter A file name filter. Ignored if null.
	 * @param dirs An array of strings representing directories to search.
	 * @return An array of strings representing the fully qualified file name of located files.
	 */
	public static String[] searchDirectories(FilenameFilter filter, String[] dirs) {
		Set locatedFiles = new HashSet();
		
		for(int i = 0; i < dirs.length; i++) {
			//dname
			File dir = new File(dirs[i]);
			if(dir.isDirectory()) {
				locatedFiles.addAll(recurseDir(filter, dir.getAbsolutePath()));
			}
		}
		return (String[]) locatedFiles.toArray(new String[locatedFiles.size()]);
	}
	
	/**
	 * Recursive file locator for one directory.
	 * @param filter A filename filter. Ignored if null.
	 * @param dirName The name of the directory to recurse.
	 * @return A set of located matching files.
	 */
	protected static Set recurseDir(FilenameFilter filter, String dirName) {
		Set locatedFiles = new HashSet();
		File file = new File(dirName);
		File[] matchedFiles = null;
		File[] matchedDirs = null;
		if(file.isDirectory()) {
			if(filter != null) {
				matchedFiles = file.listFiles(filter);
			} else {
				matchedFiles = file.listFiles();
			}
			for(int i = 0; i < matchedFiles.length; i++) {			
				if(matchedFiles[i].isDirectory()) {
					locatedFiles.addAll(recurseDir(filter, matchedFiles[i].getAbsolutePath()));					
				} else {
					locatedFiles.add(matchedFiles[i].getAbsolutePath());
				}
			}
			matchedDirs = file.listFiles(dff);
			for(int i = 0; i < matchedDirs.length; i++) {
				locatedFiles.addAll(recurseDir(filter, matchedDirs[i].getAbsolutePath()));
			}
			
		}
		return locatedFiles;
	}
	
	public static void log(Object message) {
		System.out.println(message);
	}
	
	public static void main(String args[]) {
		log("RecursiveDirectorySearch Test");
		ConfigurableFileExtensionFilter cfef = new ConfigurableFileExtensionFilter(new String[]{"tmp"});
		String[] files = searchDirectories(cfef, args);
		log("Located [" + files.length + "] Files");
		for(int i = 0; i < files.length; i++) {		
			log("\t" + files[i]);
		}
	}

}
