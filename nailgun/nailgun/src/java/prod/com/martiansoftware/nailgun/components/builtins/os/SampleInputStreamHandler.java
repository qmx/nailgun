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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * <p>Title:SampleInputStreamHandler</p>
 * <p>Description: Example implementation of an InputStreamHandler bean that prints every third line of the inputstream back to the ng client.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class SampleInputStreamHandler implements InputStreamHandler {

	/**
	 * Accepts an input stream for processing aling with the NG client's output and error stream.
	 * @param is The input stream to process.
	 * @param clientOut The NG Client's Standard Out.
	 * @param clientErr The NG Client's Standard Error.
	 * @param socket The client socket which the handler should close when op. is complete.
	 */
	public void processOutputStream(InputStream is, PrintStream clientOut, PrintStream clientErr, Socket clientSocket) {
		int lineCount = 3;
		int counter = 1;
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		try {
			String line = "";
			while((line=in.readLine()) != null) {
				counter++;
				if(counter == lineCount) {
					try {
						clientOut.println(line);
						clientOut.flush();
					} catch (Exception e) {
						
					}
					counter = 1;
				}
			}
		} catch (Exception e) {
			String error = "Exception Processing InputStream:" + e;
			System.err.println(error);
			try {
				clientErr.println("ERROR: " + error);
			} catch (Exception e2) {}
			try {
				is.close();
			} catch (Exception e2) {}
			
			return;										
		} finally {
			try { clientSocket.close(); } catch (Exception e) {}
		}
		
		
	}


}
