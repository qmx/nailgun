/*   

  Copyright 2004, Martian Software, Inc.

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

package com.martiansoftware.nailgun;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.martiansoftware.nailgun.builtins.DefaultNail;
import com.martiansoftware.nailgun.components.ComponentAlias;
import com.martiansoftware.nailgun.components.NGApplicationContext;
import com.martiansoftware.nailgun.components.NGReference;

/**
 * <p>Listens for new connections from NailGun clients and launches
 * NGSession threads to process them.</p>
 * 
 * <p>This class can be run as a standalone server or can be embedded
 * within larger applications as a means of providing command-line
 * interaction with the application.</p>
 * 
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 */
public class NGServer implements Runnable {

    /**
     * Default size for thread pool
     */
    public static final int DEFAULT_SESSIONPOOLSIZE = 10;
        
	/**
	 * The address on which to listen, or null to listen on all
	 * local addresses
	 */
	private InetAddress addr = null;
	
	/**
	 * The port on which to listen, or zero to select a port automatically
	 */
	private int port = 0;
	
	/**
	 * The socket doing the listening
	 */
	private ServerSocket serversocket;
	
	/**
	 * True if this NGServer has received instructions to shut down
	 */
	private boolean shutdown = false;
	
	/**
	 * True if this NGServer has been started and is accepting connections
	 */
	private boolean running = false;
	
	/**
	 * This NGServer's AliasManager, which maps aliases to classes
	 */
	private AliasManager aliasManager;
	
	/**
	 * If true, fully-qualified classnames are valid commands
	 */
	private boolean allowNailsByClassName = true;
	
	/**
	 * The default class to use if an invalid alias or classname is
	 * specified by the client.
	 */
	private Class defaultNailClass = null;
	
	/**
	 * A pool of NGSessions ready to handle client connections
	 */
	private NGSessionPool sessionPool = null;
	
	/**
	 * <code>System.out</code> at the time of the NGServer's creation
	 */
	public final PrintStream out = System.out;

	/**
	 * <code>System.err</code> at the time of the NGServer's creation
	 */
	public final PrintStream err = System.err;
	
	/**
	 * <code>System.in</code> at the time of the NGServer's creation
	 */
	public final InputStream in = System.in;
	
	/**
	 * a collection of all classes executed by this server so far
	 */
	private Map allNailStats = null;
	
	/**
	 * Remember the security manager we start with so we can restore it later
	 */
	private SecurityManager originalSecurityManager = null;
	
	/**
	 * Determines if spring is enabled.
	 */
	private static boolean springEnabled = false;
	
	/**
	 * Determines if the running JVM is Java 1.5 or above.
	 */
	private static boolean java15plus = false;
	
	/**
	 * The NGServer components Spring application context.
	 */
	private NGApplicationContext applicationContext = null;
	
	/**
	 * Creates a new NGServer that will listen at the specified address and
	 * on the specified port with the specified session pool size.
	 * This does <b>not</b> cause the server to start listening.  To do
	 * so, create a new <code>Thread</code> wrapping this <code>NGServer</code>
	 * and start it.
	 * @param addr the address at which to listen, or <code>null</code> to bind
	 * to all local addresses
	 * @param port the port on which to listen.
     * @param sessionPoolSize the max number of idle sessions allowed by the pool
     * @param applicationContext The NGServer component application context
	 */
	public NGServer(InetAddress addr, int port, int sessionPoolSize, NGApplicationContext applicationContext) {
		init(addr, port, sessionPoolSize, applicationContext);
	}
	
	/**
	 * Creates a new NGServer that will listen at the specified address and
	 * on the specified port with the default session pool size.
	 * This does <b>not</b> cause the server to start listening.  To do
	 * so, create a new <code>Thread</code> wrapping this <code>NGServer</code>
	 * and start it.
	 * @param addr the address at which to listen, or <code>null</code> to bind
	 * to all local addresses
	 * @param port the port on which to listen.
     * @param sessionPoolSize the max number of idle sessions allowed by the pool
	 */
	public NGServer(InetAddress addr, int port) {
		init(addr, port, DEFAULT_SESSIONPOOLSIZE, null);
	}

        /**
	 * Creates a new NGServer that will listen on the default port
	 * (defined in <code>NGConstants.DEFAULT_PORT</code>).
	 * This does <b>not</b> cause the server to start listening.  To do
	 * so, create a new <code>Thread</code> wrapping this <code>NGServer</code>
	 * and start it.
	 */
	public NGServer() {
		init(null, NGConstants.DEFAULT_PORT, DEFAULT_SESSIONPOOLSIZE, null);
	}
	
	/**
	 * Sets up the NGServer internals
	 * @param addr the InetAddress to bind to
	 * @param port the port on which to listen
     * @param sessionPoolSize the max number of idle sessions allowed by the pool
     * @param applicationContext The NGServer component application context
	 */
	private void init(InetAddress addr, int port, int sessionPoolSize, NGApplicationContext applicationContext) {
		this.addr = addr;
		this.port = port;
		this.applicationContext = applicationContext;		
		this.aliasManager = new AliasManager();
		// populate component aliases
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		if(beanNames != null) {
			for(int i = 0; i < beanNames.length; i++) {
				ComponentAlias calias = new ComponentAlias(beanNames[i], "Spring Bean:" + applicationContext.getBean(beanNames[i]).getClass().getName(), applicationContext);
				aliasManager.addComponentAlias(calias);				
			}
		}
		allNailStats = new java.util.HashMap();
		// allow a maximum of 10 idle threads.  probably too high a number
		// and definitely should be configurable in the future
		sessionPool = new NGSessionPool(this, sessionPoolSize, applicationContext);
		// if components are enabled, and the NGReference bean is registered,
		// populate the values.
		if(isSpringEnabled()) {
			try {
				NGReference ref = (NGReference)applicationContext.getBean("NGReference");
				if(ref==null) throw new Exception();
				ref.setServer(this);
				ref.setAliasManager(aliasManager);
				ref.setSessionPool(sessionPool);
				System.out.println("NGReference Loaded.");
			} catch (Exception e) {
				System.out.println("NGReference could not be located.");
			}
			
		} 
	}

	/**
	 * Sets a flag that determines whether Nails can be executed by class name.
	 * If this is false, Nails can only be run via aliases (and you should
	 * probably remove ng-alias from the AliasManager).
	 * 
	 * @param allowNailsByClassName true iff Nail lookups by classname are allowed
	 */
	public void setAllowNailsByClassName(boolean allowNailsByClassName) {
		this.allowNailsByClassName = allowNailsByClassName;
	}
	
	/**
	 * Returns a flag that indicates whether Nail lookups by classname
	 * are allowed.  If this is false, Nails can only be run via aliases.
	 * @return a flag that indicates whether Nail lookups by classname
	 * are allowed.
	 */
	public boolean allowsNailsByClassName() {
		return (allowNailsByClassName);
	}
	
	/**
	 * Sets the default class to use for the Nail if no Nails can
	 * be found via alias or classname. (may be <code>null</code>,
	 * in which case NailGun will use its own default)
	 * @param defaultNailClass the default class to use for the Nail
	 * if no Nails can be found via alias or classname.
	 * (may be <code>null</code>, in which case NailGun will use
	 * its own default)
	 */
	public void setDefaultNailClass(Class defaultNailClass) {
		this.defaultNailClass = defaultNailClass;
	}
	
	/**
	 * Returns the default class that will be used if no Nails
	 * can be found via alias or classname.
	 * @return the default class that will be used if no Nails
	 * can be found via alias or classname.
	 */
	public Class getDefaultNailClass() {
		return ((defaultNailClass == null) ? DefaultNail.class : defaultNailClass) ;
	}
	
	/**
	 * Returns the current NailStats object for the specified class, creating
	 * a new one if necessary
	 * @param nailClass the class for which we're gathering stats
	 * @return a NailStats object for the specified class
	 */
	private NailStats getOrCreateStatsFor(Class nailClass) {
		NailStats result = null;
		synchronized(allNailStats) {
			result = (NailStats) allNailStats.get(nailClass);
			if (result == null) {
				result = new NailStats(nailClass);
				allNailStats.put(nailClass, result);
			}
		}
		return (result);
	}
	
	/**
	 * Provides a means for an NGSession to register the starting of
	 * a nail execution with the server.
	 * 
	 * @param nailClass the nail class that was launched
	 */
	void nailStarted(Class nailClass) {
		NailStats stats = getOrCreateStatsFor(nailClass);
		stats.nailStarted();
	}
	
	/**
	 * Provides a means for an NGSession to register the completion of
	 * a nails execution with the server.
	 * 
	 * @param nailClass the nail class that finished
	 */
	void nailFinished(Class nailClass) {
		NailStats stats = (NailStats) allNailStats.get(nailClass);
		stats.nailFinished();
	}
	
	/**
	 * Returns a snapshot of this NGServer's nail statistics.  The result is a <code>java.util.Map</code>,
	 * keyed by class name, with <a href="NailStats.html">NailStats</a> objects as values.
	 * 
	 * @return a snapshot of this NGServer's nail statistics.
	 */
	public Map getNailStats() {
		Map result = new java.util.TreeMap();
		synchronized(allNailStats) {
			for (Iterator i = allNailStats.keySet().iterator(); i.hasNext();) {
				Class nailclass = (Class) i.next();
				result.put(nailclass.getName(), ((NailStats) allNailStats.get(nailclass)).clone());
			}
		}
		return (result);
	}
	
	/**
	 * Returns the AliasManager in use by this NGServer.
	 * @return the AliasManager in use by this NGServer.
	 */
	public AliasManager getAliasManager() {
		return (aliasManager);
	}

	/**
	 * <p>Shuts down the server.  The server will stop listening
	 * and its thread will finish.  Any running nails will be allowed
	 * to finish.</p>
	 * 
	 * <p>Any nails that provide a
	 * <pre><code>public static void nailShutdown(NGServer)</code></pre>
	 * method will have this method called with this NGServer as its sole
	 * parameter.</p>
	 * 
	 * @param exitVM if true, this method will also exit the JVM after
	 * calling nailShutdown() on any nails.  This may prevent currently
	 * running nails from exiting gracefully, but may be necessary in order
	 * to perform some tasks, such as shutting down any AWT or Swing threads
	 * implicitly launched by your nails.
	 */
	public void shutdown(boolean exitVM) {
		synchronized(this) {
			if (shutdown) return;
			shutdown = true;
		}
		
		try {
			serversocket.close();
		} catch (Throwable toDiscard) {}
		
		sessionPool.shutdown();
		
		Class[] argTypes = new Class[1];
		argTypes[0] = NGServer.class;
		Object[] argValues = new Object[1];
		argValues[0] = this;
		
		// make sure that all aliased classes have associated nailstats
		// so they can be shut down.
		for (Iterator i = getAliasManager().getAliases().iterator(); i.hasNext();) {
			Alias alias = (Alias) i.next();
			getOrCreateStatsFor(alias.getAliasedClass());
		}
		
		synchronized(allNailStats) {
			for (Iterator i = allNailStats.values().iterator(); i.hasNext();) {
				NailStats ns = (NailStats) i.next();
				Class nailClass = ns.getNailClass();
				
				// yes, I know this is lazy, relying upon the exception
				// to handle the case of no nailShutdown method.
				try {
					Method nailShutdown = nailClass.getMethod("nailShutdown", argTypes);
					nailShutdown.invoke(null, argValues);
				} catch (Throwable toDiscard) {}
			}
		}
		
		// restore system streams
		System.setIn(in);
		System.setOut(out);
		System.setErr(err);
		
		System.setSecurityManager(originalSecurityManager);
		
		if (exitVM) {
			System.exit(0);
		}
	}
	
	/**
	 * Returns true iff the server is currently running.
	 * @return true iff the server is currently running.
	 */
	public boolean isRunning() {
		return (running);
	}
	
	/**
	 * Returns the port on which this server is (or will be) listening.
	 * @return the port on which this server is (or will be) listening.
	 */
	public int getPort() {
		return ((serversocket == null) ? port : serversocket.getLocalPort());
	}
	
	/**
	 * Listens for new connections and launches NGSession threads
	 * to process them.
	 */
	public void run() {
		running = true;
		NGSession sessionOnDeck = null;
		
		originalSecurityManager = System.getSecurityManager();
        System.setSecurityManager(
                new NGSecurityManager(
                        originalSecurityManager));
  

		synchronized(System.in) {
			if (!(System.in instanceof ThreadLocalInputStream)) {
				System.setIn(new ThreadLocalInputStream(in));
				System.setOut(new ThreadLocalPrintStream(out));
				System.setErr(new ThreadLocalPrintStream(err));				
			}
		}
		
		try {
			if (addr == null) {
				serversocket = new ServerSocket(port);
			} else {
				serversocket = new ServerSocket(port, 0, addr);
			}
			
			while (!shutdown) {
				sessionOnDeck = sessionPool.take();
				Socket socket = serversocket.accept();
				sessionOnDeck.run(socket);
			}

		} catch (Throwable t) {
			// if shutdown is called while the accept() method is blocking,
			// an exception will be thrown that we don't care about.  filter
			// those out.
			if (!shutdown) {
				t.printStackTrace();
			}
		}
		if (sessionOnDeck != null) {
			sessionOnDeck.shutdown();
		}
		running = false;
	}
	
	private static void usage() {
		System.err.println("Usage: java com.martiansoftware.nailgun.NGServer [springdir=dir1,dir2,dir3.....dirn]");
		System.err.println("   or: java com.martiansoftware.nailgun.NGServer port [springdir=dir1,dir2,dir3.....dirn]");
		System.err.println("   or: java com.martiansoftware.nailgun.NGServer IPAddress [springdir=dir1,dir2,dir3.....dirn]");
		System.err.println("   or: java com.martiansoftware.nailgun.NGServer IPAddress:port [springdir=dir1,dir2,dir3.....dirn]");
	}
	
	/**
	 * Creates and starts a new <code>NGServer</code>.  
	 * There are two optional parameters:<ul>
	 * <li><b>The NGServer listening interface</b>: This specified which IPAddress and port NGServer will listen on. It can be specified as: <ul>
	 * <li><b>IPAddress</b>: NGServer will bind to <code>NGServer.DEFAULT_PORT</code> on this address.</li>
	 * <li><b>port</b>: NGServer will bind to this port on all addresses.</li>
	 * <li><b>IPAddress:port</b>NGServer will bind to the specified port on the specified address.</li>
	 * <li><b>If omitted</b>: Will listen on <code>NGServer.DEFAULT_PORT</code> on all addresses.</li>
	 * </ul>
	 * </li>
	 * <li><b>springdir=dir1,dir2,dir3.....dirn</b>: This parameter indicates that the Spring based NGServer component module should be loaded. 
	 * The = sign should be followed by a list of comma separated directories that will be searched for *.xml files that will be loaded by the Spring
	 * bean factory. If the list is blank or references no valid directories, a warning will be emitted then the parameter will be ignored. 
	 * If the parameter is omitted or ignored, the NGServer Component module will not be activated.</li>
	 * </ul>
	 * A single optional
	 * argument is valid, specifying the port on which this <code>NGServer</code>
	 * should listen.  If omitted, <code>NGServer.DEFAULT_PORT</code> will be used.
	 * @param args a single optional argument specifying the port on which to listen.
	 * @throws NumberFormatException if a non-numeric port is specified
	 */
	public static void main(String[] args) throws NumberFormatException, UnknownHostException {
		NGApplicationContext appContext = null; 
		if (args.length > 2) {
			usage();
			return;
		}
		
		// figure out which arg is which and assign to one of these vars.
		String listeningArg = null;  // the ipaddress and port argument.
		String springDirArg = null;  // the springdir argument.
		for(int i = 0; i < args.length; i++) {
			if(args[i] != null && args[i].toUpperCase().startsWith("SPRINGDIR")) {
				if(springDirArg != null) {
					usage();
					return;
				}
				springDirArg = args[i];
			} else {
				if(args[i] != null) {
					if(listeningArg != null) {
						usage();
						return;
					}
					listeningArg = args[i];
				}
			}
		}
		
		// set the JVM version flag
		setJavaVersion();

		// null server address means bind to everything local
		InetAddress serverAddress = null;
		int port = NGConstants.DEFAULT_PORT;
		String[] springDirectories = springDirArg.split("=")[1].split(",");
		Set springDirList = recurseDirectory(springDirectories, new ConfigurableFileExtensionFilter(".xml"), new DirectoryFilter());		
		try {
			if(springDirList.size() > 0) {
				springEnabled = true;
				System.out.println("Enabling NGServer Components. Descriptors are: [" + springDirList + "]" );
				appContext = new NGApplicationContext(springDirList);
				System.out.println("Starting NGServer Component Context");
				appContext.start();
				System.out.println("Started NGServer Component Context");
			} else {
				System.out.println("NGServer Components requested but no valid directories supplied. Disabling NGServer Components.");
				springEnabled = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			springEnabled = false;			
		}
		

		
		// parse the listening command line parameter, which
		// may be an inetaddress to bind to, a port number,
		// or an inetaddress followed by a port, separated
		// by a colon
		if (listeningArg != null) {
			String[] argParts = listeningArg.split(":");
			String addrPart = null;
			String portPart = null;
			if (argParts.length == 2) {
				addrPart = argParts[0];
				portPart = argParts[1];
			} else if (argParts[0].indexOf('.') >= 0) {
				addrPart = argParts[0];
			} else {
				portPart = argParts[0];
			}
			if (addrPart != null) {
				serverAddress = InetAddress.getByName(addrPart);
			}
			if (portPart != null) {
				port = Integer.parseInt(portPart);
			}
		}

		NGServer server = new NGServer(serverAddress, port, DEFAULT_SESSIONPOOLSIZE, appContext);
		Thread t = new Thread(server);
		t.setName("NGServer(" + serverAddress + ", " + port + ")");
		t.start();

		Runtime.getRuntime().addShutdownHook(new NGServerShutdowner(server));
		
		// if the port is 0, it will be automatically determined.
		// add this little wait so the ServerSocket can fully
		// initialize and we can see what port it chose.
		int runningPort = server.getPort();
		while (runningPort == 0) {
			try { Thread.sleep(50); } catch (Throwable toIgnore) {}
			runningPort = server.getPort();
		}
		
		System.out.println("NGServer started on "
							+ ((serverAddress == null) 
								? "all interfaces" 
								: serverAddress.getHostAddress())
		                    + ", port " 
							+ runningPort 
							+ ".");
	}
	
	
	/**
	 * Checks the JVM environment and sets the static var <code>java15plus</code>
	 * to true if the JVM is 1.5+, or to false if it is not.
	 */
	private static void setJavaVersion() {
		try {
			Class.forName("java.lang.Enum");
			java15plus = true;
		} catch (Throwable t) {
			java15plus = false;
		}
	}

	/**
	 * A shutdown hook that will cleanly bring down the NGServer if it
	 * is interrupted.
	 * 
	 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
	 */
	private static class NGServerShutdowner extends Thread {
		private NGServer server = null;
		
		NGServerShutdowner(NGServer server) {
			this.server = server;
		}
		
		
		public void run() {
			
			int count = 0;
			server.shutdown(false);
			
			// give the server up to five seconds to stop.  is that enough?
			// remember that the shutdown will call nailShutdown in any
			// nails as well
			while (server.isRunning() && (count < 50)) {

				try {Thread.sleep(100);} catch(InterruptedException e) {}
				++count;
			}
			
			if (server.isRunning()) {
				System.err.println("Unable to cleanly shutdown server.  Exiting JVM Anyway.");
			} else {
				System.out.println("NGServer shut down.");
			}
		}
	}

	/**
	 * Determines if Spring is enabled.
	 * @return true if spring is enabled, false if it is not.
	 */
	public static boolean isSpringEnabled() {
		return springEnabled;
	}

	/**
	 * Determines if the current JVM is Java 1.5 or above.
	 * @return true if the current JVM is 1.5+, false if it is below.
	 */
	public static boolean isJava15Plus() {
		return java15plus;
	}
	

	/**
	 * Recurses through an directories and returns a set of files matching the passed file filter.
	 * @param dirNames Ann array of directory names to start at
	 * @param filter A file name filter
	 * @param df A directory filter
	 * @return A set of located files
	 */
	public static Set recurseDirectory(String[] dirNames, FilenameFilter filter, FilenameFilter df) {
		Set matches = new HashSet();
		if(dirNames==null || dirNames.length < 1) return matches;
		for(int c = 0; c < dirNames.length; c++) {
			File dir = new File(dirNames[c]);
			if(!dir.exists() || !dir.isDirectory()) continue;			
			// get xml files in this dir.
			String[] files = dir.list(filter);
			for(int i=0; i < files.length; i++) {
				matches.add(dir.getAbsolutePath() + File.separator + files[i]);
			}
			// process each dir in this dir
			String[] subDirList = dir.list(df);
			for(int i=0; i < subDirList.length; i++) {
				subDirList[i] = dir.getAbsolutePath() + File.separator + subDirList[i];
			}
			matches.addAll(recurseDirectory(subDirList, filter, df));
		}
		return matches;
	}
	
}


class DirectoryFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return new File(dir.getAbsolutePath() + File.separator + name).isDirectory();
	}	
}


class ConfigurableFileExtensionFilter implements FilenameFilter {
	protected String extension = null;
	/**
	 * @param extension
	 */
	public ConfigurableFileExtensionFilter(String extension) {
		super();
		this.extension = extension.toUpperCase();
	}
	public boolean accept(File dir, String name) {		
		return name.toUpperCase().endsWith(extension);
	}	
}