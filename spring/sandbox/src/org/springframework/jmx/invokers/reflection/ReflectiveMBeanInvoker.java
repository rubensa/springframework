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
package org.springframework.jmx.invokers.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jmx.invokers.AbstractMBeanInvoker;
import org.springframework.jmx.invokers.MethodKey;

/**
 * An implementation of <tt>MBeanInvoker</tt> that uses reflection
 * to get/set attribute values and to invoke methods. Methods
 * that are accessed using reflection are cached for increased
 * speed on subsequent invocations of the same method.
 * @author Rob Harrop
 */
public class ReflectiveMBeanInvoker extends AbstractMBeanInvoker {

	/**
	 * BeanWrapper instance around the managed resource. Used
	 * for accessing property values using JavaBean style naming
	 */
	private BeanWrapper resourceWrapper = null;

	/**
	 * Cache to store methods retreived using reflection for faster
	 * retreival on subsequent invocations.
	 * @see ReflectiveMBeanInvoker.MethodKey
	 */
	private static Map methodCache = new HashMap();

	/**
	 * The <tt>Class</tt> of the managed resource.
	 */
	private Class managedResourceClass = null;

	/**
	 * Retreive the value of the named attribute using reflection.
	 * @param attributeName The name of the attribute whose value you want to retreive
	 * @return The value of the named attribute
	 */
	public Object getAttribute(String attributeName)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		return resourceWrapper.getPropertyValue(attributeName);
	}

	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		resourceWrapper.setPropertyValue(attribute.getName(), attribute
				.getValue());
	}

	/**
	 * Invoke a method using reflection.
	 */
	public Object invoke(String method, Object[] args, String[] signature)
			throws MBeanException, ReflectionException {

		// check for invalid attribute invocation
		checkForInvalidAttributeInvoke(method);

		// get cache key
		MethodKey mk = new MethodKey(method, signature);

		// attempt to retreive from cache
		Method m = (Method) methodCache.get(mk);
	
		
		try {

			// if the method was not in cache then locate it
			if (m == null) {
				Class[] types = typeNamesToTypes(signature);

				m = managedResourceClass.getMethod(method, types);
				methodCache.put(mk, m);
			}
			
			return m.invoke(managedResource, args);

		} catch (NoSuchMethodException ex) {
			throw new ReflectionException(ex, "Unable to find method: "
					+ method + " with signature: " + signature);
		} catch (InvocationTargetException ex) {
			throw new ReflectionException(ex,
					"An error occured when invoking method: " + method
							+ " with signature: " + signature);
		} catch (IllegalAccessException ex) {
			throw new ReflectionException(ex, "Access to method: " + method
					+ " is denied");
		} catch (ClassNotFoundException ex) {
			throw new ReflectionException(ex, "Invalid argument type specified");
		}
	}

	/**
	 * Creates a BeanWrapper instance around the managed resource 
	 * and also stores the <tt>Class</tt> of the managed resource.
	 */
	protected void afterManagedResourceSet() {
		resourceWrapper = new BeanWrapperImpl(managedResource);
		managedResourceClass = managedResource.getClass();
	}
}