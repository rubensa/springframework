/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.beans.groovy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;

/**
 * Introduction interceptor that provides DynamicScript
 * implementation for all Groovy objects.
 * <br>This class also handles reloads through implementing
 * the BeanFactoryAware and BeanNameAware interfaces, which will
 * cause it to receive callbacks by the BeanFactory.
 * <br>It also kicks of a background poller (ScriptReloader)
 * if the pollIntervalSeconds constructor argument is
 * positive.
 * @author Rod Johnson
 * @version $Id$
 */
class DynamicScriptInterceptor extends DelegatingIntroductionInterceptor
		implements BeanFactoryAware, BeanNameAware, DynamicScript, DisposableBean {
	
	protected final static Log log = LogFactory.getLog(DynamicScriptInterceptor.class);

	/**
	 * Target used to wrap the Groovy object,
	 * which we may replace dynamically.
	 */
	private HotSwappableTargetSource targetSource;
	
	/**
	 * BeanFactory that owns this Groovy object. We need
	 * to hold a reference to it so that we can configure
	 * the Groovy object's properties if the instance changes.
	 */
	private BeanFactory owningFactory;
	
	private String beanName;
	
	private String className;
	
	private int loads = 1;
	
	private int pollIntervalSeconds;
	
	private ScriptReloader reloader;
	
	private long lastReloadMillis = System.currentTimeMillis();
	
	public DynamicScriptInterceptor(String className, HotSwappableTargetSource targetSource, int pollIntervalSeconds) {
		this.className = className;
		this.targetSource = targetSource;
		this.pollIntervalSeconds = pollIntervalSeconds;
	}
	
	public void setBeanFactory(BeanFactory owningFactory) {
		this.owningFactory = owningFactory;
		
		//if (this.owningFactory.isSingleton(beanName)) {
		//	throw new RuntimeException("Groovy usage incorrect, '" + beanName + "' can't be a singleton");
		//}
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
		logger.info("Configuring dynamic reloadable Groovy bean with name '" + beanName + "'");
		
		if (pollIntervalSeconds > 0) {
			log.info("Will poll for modifications every " + pollIntervalSeconds + " seconds");
			reloader = new ScriptReloader(this, pollIntervalSeconds);
		}
	}
	
	public void destroy() {
		// OR STOP TIMER?
		if (reloader != null) {
			reloader.cancel();
		}
	}
	
	/**
	 * @see org.springframework.beans.groovy.DynamicScript#reload()
	 */
	public void reload() throws GroovyScriptException {
		//System.out.println(owningFactory);		
		
		long startTime = System.currentTimeMillis();
		
		Object newInstance = owningFactory.getBean(beanName);
		
		this.targetSource.swap(newInstance);
		long et = System.currentTimeMillis() - startTime;
		
		logger.info("RELOADED dynamic Groovy bean with name '" + beanName + "' in " + et + "ms");
		
		++loads;
		this.lastReloadMillis = System.currentTimeMillis();
	}

	/**
	 * @see org.springframework.beans.groovy.DynamicScript#getLoads()
	 */
	public int getLoads() {
		return loads;
	}

	/**
	 * @see org.springframework.beans.groovy.DynamicScript#getLastReloadMillis()
	 */
	public long getLastReloadMillis() {
		return lastReloadMillis;
	}

	public String getClassName() {
		return className;
	}
}
