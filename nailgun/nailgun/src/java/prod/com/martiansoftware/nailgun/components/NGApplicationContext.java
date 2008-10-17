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

package com.martiansoftware.nailgun.components;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * <p>Title:NGApplicationContext</p>
 * <p>Description: The Spring application context providing Spring support to the NGServer.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class NGApplicationContext implements BeanFactory {
	
	protected GenericApplicationContext appContext = null;
	protected String[] configDirs = null;
	
	public NGApplicationContext(String[] configDirs) {
		this.configDirs = configDirs;
		//appContext = new FileSystemXmlApplicationContext((String[])configDirs.toArray(new String[configDirs.size()]), true);
		appContext = new GenericApplicationContext();
	}

	/**
	 * @param listener
	 * @see org.springframework.context.support.AbstractApplicationContext#addApplicationListener(org.springframework.context.ApplicationListener)
	 */
	public void addApplicationListener(ApplicationListener listener) {
		appContext.addApplicationListener(listener);
	}

	/**
	 * @param beanFactoryPostProcessor
	 * @see org.springframework.context.support.AbstractApplicationContext#addBeanFactoryPostProcessor(org.springframework.beans.factory.config.BeanFactoryPostProcessor)
	 */
	public void addBeanFactoryPostProcessor(
			BeanFactoryPostProcessor beanFactoryPostProcessor) {
		appContext.addBeanFactoryPostProcessor(beanFactoryPostProcessor);
	}



	/**
	 * 
	 * @see org.springframework.context.support.AbstractApplicationContext#close()
	 */
	public void close() {
		appContext.close();
	}

	/**
	 * @param name
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#containsBean(java.lang.String)
	 */
	public boolean containsBean(String name) {
		return appContext.containsBean(name);
	}

	/**
	 * @param name
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#containsBeanDefinition(java.lang.String)
	 */
	public boolean containsBeanDefinition(String name) {
		return appContext.containsBeanDefinition(name);
	}

	/**
	 * @param name
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#containsLocalBean(java.lang.String)
	 */
	public boolean containsLocalBean(String name) {
		return appContext.containsLocalBean(name);
	}

	/**
	 * 
	 * @see org.springframework.context.support.AbstractApplicationContext#destroy()
	 */
	public void destroy() {
		appContext.destroy();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return appContext.equals(obj);
	}

	/**
	 * @param name
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getAliases(java.lang.String)
	 */
	public String[] getAliases(String name) {
		return appContext.getAliases(name);
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getApplicationListeners()
	 */
	public List getApplicationListeners() {
		return appContext.getApplicationListeners();
	}

	/**
	 * @return
	 * @throws IllegalStateException
	 * @see org.springframework.context.support.AbstractApplicationContext#getAutowireCapableBeanFactory()
	 */
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory()
			throws IllegalStateException {
		return appContext.getAutowireCapableBeanFactory();
	}

	/**
	 * @param name
	 * @param requiredType
	 * @return
	 * @throws BeansException
	 * @see org.springframework.context.support.AbstractApplicationContext#getBean(java.lang.String, java.lang.Class)
	 */
	public Object getBean(String name, Class requiredType)
			throws BeansException {
		return appContext.getBean(name, requiredType);
	}

	/**
	 * @param name
	 * @param args
	 * @return
	 * @throws BeansException
	 * @see org.springframework.context.support.AbstractApplicationContext#getBean(java.lang.String, java.lang.Object[])
	 */
	public Object getBean(String name, Object[] args) throws BeansException {
		return appContext.getBean(name, args);
	}

	/**
	 * @param name
	 * @return
	 * @throws BeansException
	 * @see org.springframework.context.support.AbstractApplicationContext#getBean(java.lang.String)
	 */
	public Object getBean(String name) throws BeansException {
		return appContext.getBean(name);
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getBeanDefinitionCount()
	 */
	public int getBeanDefinitionCount() {
		return appContext.getBeanDefinitionCount();
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getBeanDefinitionNames()
	 */
	public String[] getBeanDefinitionNames() {
		return appContext.getBeanDefinitionNames();
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractRefreshableApplicationContext#getBeanFactory()
	 */
	public final ConfigurableListableBeanFactory getBeanFactory() {
		return appContext.getBeanFactory();
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactoryPostProcessors()
	 */
	public List getBeanFactoryPostProcessors() {
		return appContext.getBeanFactoryPostProcessors();
	}

	/**
	 * @param type
	 * @param includePrototypes
	 * @param allowEagerInit
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getBeanNamesForType(java.lang.Class, boolean, boolean)
	 */
	public String[] getBeanNamesForType(Class type, boolean includePrototypes,
			boolean allowEagerInit) {
		return appContext.getBeanNamesForType(type, includePrototypes,
				allowEagerInit);
	}

	/**
	 * @param type
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getBeanNamesForType(java.lang.Class)
	 */
	public String[] getBeanNamesForType(Class type) {
		return appContext.getBeanNamesForType(type);
	}

	/**
	 * @param type
	 * @param includePrototypes
	 * @param allowEagerInit
	 * @return
	 * @throws BeansException
	 * @see org.springframework.context.support.AbstractApplicationContext#getBeansOfType(java.lang.Class, boolean, boolean)
	 */
	public Map getBeansOfType(Class type, boolean includePrototypes,
			boolean allowEagerInit) throws BeansException {
		return appContext.getBeansOfType(type, includePrototypes,
				allowEagerInit);
	}

	/**
	 * @param type
	 * @return
	 * @throws BeansException
	 * @see org.springframework.context.support.AbstractApplicationContext#getBeansOfType(java.lang.Class)
	 */
	public Map getBeansOfType(Class type) throws BeansException {
		return appContext.getBeansOfType(type);
	}

	/**
	 * @return
	 * @see org.springframework.core.io.DefaultResourceLoader#getClassLoader()
	 */
	public ClassLoader getClassLoader() {
		return appContext.getClassLoader();
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getDisplayName()
	 */
	public String getDisplayName() {
		return appContext.getDisplayName();
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getId()
	 */
	public String getId() {
		return appContext.getId();
	}

	/**
	 * @param resolvable
	 * @param locale
	 * @return
	 * @throws NoSuchMessageException
	 * @see org.springframework.context.support.AbstractApplicationContext#getMessage(org.springframework.context.MessageSourceResolvable, java.util.Locale)
	 */
	public String getMessage(MessageSourceResolvable resolvable, Locale locale)
			throws NoSuchMessageException {
		return appContext.getMessage(resolvable, locale);
	}

	/**
	 * @param code
	 * @param args
	 * @param locale
	 * @return
	 * @throws NoSuchMessageException
	 * @see org.springframework.context.support.AbstractApplicationContext#getMessage(java.lang.String, java.lang.Object[], java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, Locale locale)
			throws NoSuchMessageException {
		return appContext.getMessage(code, args, locale);
	}

	/**
	 * @param code
	 * @param args
	 * @param defaultMessage
	 * @param locale
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getMessage(java.lang.String, java.lang.Object[], java.lang.String, java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, String defaultMessage,
			Locale locale) {
		return appContext.getMessage(code, args, defaultMessage, locale);
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getParent()
	 */
	public ApplicationContext getParent() {
		return appContext.getParent();
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getParentBeanFactory()
	 */
	public BeanFactory getParentBeanFactory() {
		return appContext.getParentBeanFactory();
	}

	/**
	 * @param arg0
	 * @return
	 * @see org.springframework.core.io.DefaultResourceLoader#getResource(java.lang.String)
	 */
	public Resource getResource(String arg0) {
		return appContext.getResource(arg0);
	}

	/**
	 * @param locationPattern
	 * @return
	 * @throws IOException
	 * @see org.springframework.context.support.AbstractApplicationContext#getResources(java.lang.String)
	 */
	public Resource[] getResources(String locationPattern) throws IOException {
		return appContext.getResources(locationPattern);
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#getStartupDate()
	 */
	public long getStartupDate() {
		return appContext.getStartupDate();
	}

	/**
	 * @param name
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 * @see org.springframework.context.support.AbstractApplicationContext#getType(java.lang.String)
	 */
	public Class getType(String name) throws NoSuchBeanDefinitionException {
		return appContext.getType(name);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return appContext.hashCode();
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#isActive()
	 */
	public boolean isActive() {
		return appContext.isActive();
	}

	/**
	 * @param name
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 * @see org.springframework.context.support.AbstractApplicationContext#isPrototype(java.lang.String)
	 */
	public boolean isPrototype(String name)
			throws NoSuchBeanDefinitionException {
		return appContext.isPrototype(name);
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#isRunning()
	 */
	public boolean isRunning() {
		return appContext.isRunning();
	}

	/**
	 * @param name
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 * @see org.springframework.context.support.AbstractApplicationContext#isSingleton(java.lang.String)
	 */
	public boolean isSingleton(String name)
			throws NoSuchBeanDefinitionException {
		return appContext.isSingleton(name);
	}

	/**
	 * @param name
	 * @param targetType
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 * @see org.springframework.context.support.AbstractApplicationContext#isTypeMatch(java.lang.String, java.lang.Class)
	 */
	public boolean isTypeMatch(String name, Class targetType)
			throws NoSuchBeanDefinitionException {
		return appContext.isTypeMatch(name, targetType);
	}

	/**
	 * @param event
	 * @see org.springframework.context.support.AbstractApplicationContext#publishEvent(org.springframework.context.ApplicationEvent)
	 */
	public void publishEvent(ApplicationEvent event) {
		appContext.publishEvent(event);
	}

	/**
	 * @throws BeansException
	 * @throws IllegalStateException
	 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
	 */
	public void refresh() throws BeansException, IllegalStateException {
		appContext.refresh();
	}

	/**
	 * 
	 * @see org.springframework.context.support.AbstractApplicationContext#registerShutdownHook()
	 */
	public void registerShutdownHook() {
		appContext.registerShutdownHook();
	}


	/**
	 * @param classLoader
	 * @see org.springframework.core.io.DefaultResourceLoader#setClassLoader(java.lang.ClassLoader)
	 */
	public void setClassLoader(ClassLoader classLoader) {
		appContext.setClassLoader(classLoader);
	}


	/**
	 * @param displayName
	 * @see org.springframework.context.support.AbstractApplicationContext#setDisplayName(java.lang.String)
	 */
	public void setDisplayName(String displayName) {
		appContext.setDisplayName(displayName);
	}

	/**
	 * @param id
	 * @see org.springframework.context.support.AbstractRefreshableConfigApplicationContext#setId(java.lang.String)
	 */
	public void setId(String id) {
		appContext.setId(id);
	}

	/**
	 * @param parent
	 * @see org.springframework.context.support.AbstractApplicationContext#setParent(org.springframework.context.ApplicationContext)
	 */
	public void setParent(ApplicationContext parent) {
		appContext.setParent(parent);
	}

	/**
	 * 
	 * @see org.springframework.context.support.AbstractApplicationContext#start()
	 */
	public void start() {		
		XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader(appContext);
		if(configDirs!=null) {
			for(int i = 0; i < configDirs.length; i++) {
				xbdr.loadBeanDefinitions(new FileSystemResource(configDirs[i]));
			}
		}
		appContext.refresh();
		int beanCount = appContext.getBeanDefinitionCount();		
		System.out.println("Created [" + beanCount + "] NGServer Components");
		String[] beanNames = appContext.getBeanDefinitionNames();
		System.out.println("Bean Names:");
		if(beanNames != null) {
			for(int i = 0; i < beanNames.length; i++) {
				System.out.println("\t" + beanNames[i]);
			}
		}
		appContext.start();
	}

	/**
	 * 
	 * @see org.springframework.context.support.AbstractApplicationContext#stop()
	 */
	public void stop() {
		appContext.stop();
	}

	/**
	 * @return
	 * @see org.springframework.context.support.AbstractApplicationContext#toString()
	 */
	public String toString() {
		return appContext.toString();
	}
}
