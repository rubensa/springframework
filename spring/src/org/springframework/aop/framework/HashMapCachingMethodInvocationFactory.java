/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class HashMapCachingMethodInvocationFactory extends MethodInvocationFactorySupport {
	
	
	private HashMap methodCache = new HashMap();
	
	public HashMapCachingMethodInvocationFactory() {
	}

	/**
	 * @see org.springframework.aop.framework.MethodInvocationFactory#refresh(org.springframework.aop.framework.ProxyConfig)
	 */
	public void refresh(ProxyConfig pc) {
		super.refresh(pc);
		methodCache.clear();
	}
	
	protected List getInterceptorsAndDynamicInterceptionAdvice(ProxyConfig config, Object proxy, Method method, Class targetClass) {
		
		List cached = (List) methodCache.get(method);
		if (cached == null) {
			cached = MethodInvocationFactorySupport.GetInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
			methodCache.put(method, cached);
		}
		return cached;
	}

}
