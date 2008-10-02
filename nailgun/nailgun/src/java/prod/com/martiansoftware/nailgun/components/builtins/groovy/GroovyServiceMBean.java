package com.martiansoftware.nailgun.components.builtins.groovy;

import java.util.Properties;

public interface GroovyServiceMBean {

	/** The default JMX ObjectName for the GroovyService management interface */
	public static final String DEFAULT_OBJECT_NAME = "com.martiansoftware.components.cache:service=Groovy";
	
	/** The cache key name prefix to avoid namespace collisions. */
	public static final String SCRIPT_PREFIX = "groovy/";
	
	/**
	 * Generates a sensible default objectName for this class.
	 * @return An objectName string.
	 * @see com.martiansoftware.nailgun.components.builtins.base.BaseComponentService#getDefaultObjectName()
	 */
	public abstract String getDefaultObjectName();

	/**
	 * The script manager's groovy compilation properties.
	 * @return the groovyCompilerProperties
	 */
	public abstract Properties getGroovyCompilerProperties();

	/**
	 * Adds the passed properties to the compilation properties used to compile each managed script.
	 * @param groovyCompilerProperties the groovyCompilerProperties to add
	 */
	public abstract void setGroovyCompilerProperties(
			Properties groovyCompilerProperties);

	/**
	 * The managed scripts' built-in properties.
	 * @return the scriptProperties
	 */
	public abstract Properties getScriptProperties();

	/**
	 * Adds the passed properties to all managed scripts' built-in properties. 
	 * @param scriptProperties the scriptProperties to add
	 */
	public abstract void setScriptProperties(Properties scriptProperties);

	public abstract int getScriptCacheSize();

	/**
	 * The calculated average elapsed time of successful groovy calls.
	 * @return the average elapsed time of calls.
	 */
	public abstract long getAverageCallTime();

	/**
	 * The total number of successful groovy calls
	 * @return the totalCalls
	 */
	public abstract long getTotalCalls();

	/**
	 * The total elapsed time of successful groovy calls
	 * @return the totalCallTime
	 */
	public abstract long getTotalCallTime();

	/**
	 * The total number of errors encountered calling the Groovy Service.
	 * @return the totalErrors
	 */
	public abstract long getTotalErrors();

}