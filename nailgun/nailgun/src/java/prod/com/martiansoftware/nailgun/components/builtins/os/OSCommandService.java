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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Properties;

import com.martiansoftware.nailgun.NGContext;
import com.martiansoftware.nailgun.components.NGApplicationContext;
import com.martiansoftware.nailgun.components.SocketHandler;
import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;

/**
 * <p>Title:OSCommandService</p>
 * <p>Description: Service that executes OS shell commands and redirects the output to an output handler or back to the client.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class OSCommandService extends BaseComponentService  implements SocketHandler, InputStreamHandler {

	/** The default JMX ObjectName for the GroovyService management interface */
	public static final String DEFAULT_OBJECT_NAME = "com.martiansoftware.components.cache:service=OSCommand";
	/**	true if platform is windows */
	public static final boolean isWindows = (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1);

	/**
	 * Creates a new OSCommandService 
	 */
	public OSCommandService() {
	}
	
	/**
	 * @param context
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.io.IOException
	 */
	public void nailMain(NGContext context) throws java.security.NoSuchAlgorithmException, java.io.IOException {
		PrintStream cliOut = context.out;
		PrintStream cliErr = context.err;
		NGApplicationContext appContext = context.getApplicationContext();
		Socket clientSocket = context.getClientSocket();
		String[] args = context.getArgs();
		String[] commands = null;  // all remaining non e: values.
		String[] environment = null; // all remaining e: values
		String workingDir = null; // -dir=		
		String inputProcessor = null; // -p=
		File workingDirectory = null;
		InputStreamHandler streamHandler = null;
		boolean passOffCompleted = false;
		try {
			// =============================
			// Extract the input processor
			// =============================
			String[] processorArgs = getPrefixedStrings("-p=",args);
			if(processorArgs.length > 1) {
				reportError("Multiple InputStream Processors Defined", err, cliErr);
				return;
			}
			if(processorArgs.length > 0) {
				args = removePrefixedStrings("-p=", args);
				inputProcessor = processorArgs[0];
				try {
					streamHandler = (InputStreamHandler)appContext.getBean(inputProcessor);
				} catch (Exception e) {
					reportError("Specified InputStreamHandler [" + inputProcessor + "] Could Not Be Acquired:" + e, err, cliErr);
					return;										
				}
			}
			// =============================
			// Extract the working directory
			// =============================
			String[] workingDirArgs = getPrefixedStrings("-d=",args);
			if(workingDirArgs.length > 1) {
				reportError("Multiple Working Directories Defined", err, cliErr);
				return;
			}
			if(workingDirArgs.length > 0) {
				args = removePrefixedStrings("-d=", args);
				workingDir = workingDirArgs[0];
				workingDirectory = new File(workingDir);
				if(!workingDirectory.exists()) {
					reportError("Working Directory [" + workingDir + "] Does Not Exist", err, cliErr);
					return;					
				}
				if(!workingDirectory.isDirectory()) {
					reportError("Working Directory [" + workingDir + "] Is Not A Directory", err, cliErr);
					return;										
				}
				
			}
			// =============================
			// Extract the env entries
			// What's left is the commands.
			// =============================
			
			environment = removePrefixes("-e=", getPrefixedStrings("-e=",args));
			Properties env = context.getEnv();
			String[] addEnvs = new String[env.size()];
			int i = 0;
			for(Iterator envIter = env.keySet().iterator(); envIter.hasNext();) {
				String key = envIter.next().toString();
				String value = env.getProperty(key);
				addEnvs[i] = key + "=" + value; 
			}
			commands = removePrefixedStrings("-e=", args);
			appendToArray(addEnvs, environment);
			if(commands.length < 1) {
				reportError("No Commands Found", err, cliErr);
				return;				
			}
			// If this is windows, prefix the commands with "cmd /c"
			if(isWindows) {
				String[] tmp = new String[commands.length + 2];
				tmp[0] = "cmd";
				tmp[1] = "/c";
				System.arraycopy(commands, 0, tmp, 2, commands.length);
				commands = tmp;
			}
			
			Process process = null;
			try {
				if(workingDir!=null) {
					process = Runtime.getRuntime().exec(commands, environment, workingDirectory);
				} else {
					process = Runtime.getRuntime().exec(commands, environment);
				}
			} catch (Exception e) {
				reportError("Exception Creating OSCommand Process:" + e, err, cliErr);
				return;								
			}
			InputStream inputStream = process.getInputStream();
			// Need to add this to the process.
			InputStream errorStream = process.getErrorStream();
			if(streamHandler!=null) {
				StreamHandlerThread sht = new StreamHandlerThread(streamHandler, inputStream, err, cliErr, cliOut, clientSocket);
				sht.start();	
				passOffCompleted=true;
			} else {
				//cliOut.println("Starting Stream Processor...");
				//cliOut.flush();
				try {
					StreamHandlerThread sht = new StreamHandlerThread(this, inputStream, err, cliErr, cliOut, clientSocket);
					StreamHandlerThread shtErr = new StreamHandlerThread(this, errorStream, err, cliErr, cliErr, null);
					shtErr.start();
					sht.start();	
					
					int exitCode = process.waitFor();								
					cliOut.println("Process Return Code:" + exitCode);
					out.println("Process Return Code:" + exitCode);
					cliOut.flush();
				} finally {
					//try { clientSocket.close(); } catch (Exception e){};
				}
			}
			
		} catch (Throwable e) {
			reportError("Unexpected Exception Processing OS Command:" + e, err, cliErr);
			e.printStackTrace(err);
			return;							
		} finally {
			try { if(!passOffCompleted) clientSocket.close(); } catch (Exception f) {}
		}
	}

	/**
	 * @param is
	 * @param clientOut
	 * @param clientErr
	 * @param clientSocket
	 * @see com.martiansoftware.nailgun.components.builtins.os.InputStreamHandler#processOutputStream(java.io.InputStream, java.io.PrintStream, java.io.PrintStream, java.net.Socket)
	 */
	public void processOutputStream(InputStream is, PrintStream clientOut,
			PrintStream clientErr, Socket clientSocket) {
		BufferedInputStream in = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		byte[] buffer = new byte[2048];
		int bytesRead = 0;		
		try {
			while(true) {
				bytesRead = in.read(buffer);
				if(bytesRead==-1) break;
				baos.write(buffer, 0, bytesRead);
			}
			// For now, we'll assume the byte array is a String.
			buffer = baos.toByteArray();
			out.println("OSCommandService Processed " + buffer.length + " InputStream Bytes");
			String output = new String(buffer);
			clientOut.println(output);
			clientOut.flush();
		} catch (Exception e) {
			reportError("Unexpected Exception Processing OS Command:" + e, err, clientErr);
			return;																
		} finally {
			try { in.close(); } catch (Exception e) {}
			//try { clientSocket.close(); } catch (Exception e) {}
		}		
	}
	
	/**
	 * Generates a sensible default objectName for this class.
	 * @return An objectName string.
	 * @see com.martiansoftware.nailgun.components.builtins.base.BaseComponentService#getDefaultObjectName()
	 */
	public String getDefaultObjectName() {		
		return DEFAULT_OBJECT_NAME;
	}
	

}

class StreamHandlerThread extends Thread {
	protected InputStreamHandler streamHandler = null;
	protected PrintStream consoleErr = null;
	protected PrintStream clientErr = null;
	protected PrintStream clientOut = null;
	protected InputStream is = null;
	protected Socket clientSocket = null;
	protected String handlerName = null;
	
	
	/**
	 * The stream handler to be run in this thread.
	 * @param streamHandler
	 * @param is
	 * @param consoleErr
	 * @param clientErr
	 */
	public StreamHandlerThread(InputStreamHandler streamHandler, InputStream is,
			PrintStream consoleErr, PrintStream clientErr, PrintStream clientOut, Socket clientSocket) {
		super();
		this.streamHandler = streamHandler;
		this.consoleErr = consoleErr;
		this.clientErr = clientErr;
		this.is = is;
		this.clientOut = clientOut;
		this.clientSocket = clientSocket;
		
	}



	public void run() {
		try {
			streamHandler.processOutputStream(is, clientOut, clientErr, clientSocket);
		} catch (Exception e) {
			OSCommandService.reportError("Exception Executing OSCommand Input Processing:" + e, consoleErr, clientErr);
			return;											
		} finally {
			try { clientSocket.close(); } catch (Exception e) {}
		}
	}
}

