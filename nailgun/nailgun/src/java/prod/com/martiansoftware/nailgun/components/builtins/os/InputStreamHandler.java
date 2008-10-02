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

package com.martiansoftware.nailgun.components.builtins.os;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * <p>Title:InputStreamHandler</p>
 * <p>Description: Defines a class that can be passed an input stream for processing.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public interface InputStreamHandler {
	
	/**
	 * Accepts an input stream for processing aling with the NG client's output and error stream.
	 * @param is The input stream to process.
	 * @param clientOut The NG Client's Standard Out.
	 * @param clientErr The NG Client's Standard Error.
	 * @param socket The client socket which the handler should close when op. is complete.
	 */
	public void processOutputStream(InputStream is, PrintStream clientOut, PrintStream clientErr, Socket clientSocket);
	
	
}
