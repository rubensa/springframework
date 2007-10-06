/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jmx.access;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.JMX;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.JdkVersion;
import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * {@link org.aopalliance.intercept.MethodInterceptor} that routes calls to an
 * MBean running on the supplied <code>MBeanServerConnection</code>.
 * Works for both local and remote <code>MBeanServerConnection</code>s.
 *
 * <p>By default, the <code>MBeanClientInterceptor</code> will connect to the
 * <code>MBeanServer</code> and cache MBean metadata at startup. This can
 * be undesirable when running against a remote <code>MBeanServer</code>
 * that may not be running when the application starts. Through setting the
 * {@link #setConnectOnStartup(boolean) connectOnStartup} property to "false",
 * you can defer this process until the first invocation against the proxy.
 *
 * <p>Requires JMX 1.2's <code>MBeanServerConnection</code> feature.
 * As a consequence, this class will not work on JMX 1.0.
 *
 * <p>This functionality is usually used through {@link MBeanProxyFactoryBean}.
 * See the javadoc of that class for more information.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see MBeanProxyFactoryBean
 * @see #setConnectOnStartup
 */
public class MBeanClientInterceptor
		implements MethodInterceptor, BeanClassLoaderAware, InitializingBean, DisposableBean {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private MBeanServerConnection server;

	private JMXServiceURL serviceUrl;

	private String agentId;

	private boolean connectOnStartup = true;

	private ObjectName objectName;

	private boolean useStrictCasing = true;

	private Class managementInterface;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private JMXConnector connector;

	private MBeanServerInvocationHandler invocationHandler;

	private Map allowedAttributes;

	private Map allowedOperations;

	private final Map signatureCache = new HashMap();

	private final Object preparationMonitor = new Object();


	/**
	 * Set the <code>MBeanServerConnection</code> used to connect to the
	 * MBean which all invocations are routed to.
	 */
	public void setServer(MBeanServerConnection server) {
		this.server = server;
	}

	/**
	 * Set the service URL of the remote <code>MBeanServer</code>.
	 */
	public void setServiceUrl(String url) throws MalformedURLException {
		this.serviceUrl = new JMXServiceURL(url);
	}

	/**
	 * Set the agent id of the <code>MBeanServer</code> to locate.
	 * <p>Default is none. If specified, this will result in an
	 * attempt being made to locate the attendant MBeanServer, unless
	 * the {@link #setServiceUrl "serviceUrl"} property has been set.
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 */
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	/**
	 * Set whether or not the proxy should connect to the <code>MBeanServer</code>
	 * at creation time ("true") or the first time it is invoked ("false").
	 * Default is "true".
	 */
	public void setConnectOnStartup(boolean connectOnStartup) {
		this.connectOnStartup = connectOnStartup;
	}

	/**
	 * Set the <code>ObjectName</code> of the MBean which calls are routed to,
	 * as <code>ObjectName</code> instance or as <code>String</code>.
	 */
	public void setObjectName(Object objectName) throws MalformedObjectNameException {
		this.objectName = ObjectNameManager.getInstance(objectName);
	}

	/**
	 * Set whether to use strict casing for attributes. Enabled by default.
	 * <p>When using strict casing, a JavaBean property with a getter such as
	 * <code>getFoo()</code> translates to an attribute called <code>Foo</code>.
	 * With strict casing disabled, <code>getFoo()</code> would translate to just
	 * <code>foo</code>.
	 */
	public void setUseStrictCasing(boolean useStrictCasing) {
		this.useStrictCasing = useStrictCasing;
	}

	/**
	 * Set the management interface of the target MBean, exposing bean property
	 * setters and getters for MBean attributes and conventional Java methods
	 * for MBean operations.
	 */
	public void setManagementInterface(Class managementInterface) {
		this.managementInterface = managementInterface;
	}

	/**
	 * Return the management interface of the target MBean,
	 * or <code>null</code> if none specified.
	 */
	protected final Class getManagementInterface() {
		return this.managementInterface;
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	/**
	 * Prepares the <code>MBeanServerConnection</code> if the "connectOnStartup"
	 * is turned on (which it is by default).
	 */
	public void afterPropertiesSet() {
		if (this.connectOnStartup) {
			prepare();
		}
	}

	/**
	 * Ensures that an <code>MBeanServerConnection</code> is configured and attempts
	 * to detect a local connection if one is not supplied.
	 */
	public void prepare() {
		synchronized (this.preparationMonitor) {
			if (this.server == null) {
				connect();
			}
			this.invocationHandler = null;
			if (this.useStrictCasing) {
				// Use the JDK's own MBeanServerInvocationHandler,
				// in particular for native MXBean support on Java 6.
				if (JdkVersion.isAtLeastJava16()) {
					this.invocationHandler =
							new MBeanServerInvocationHandler(this.server, this.objectName,
									(this.managementInterface != null && JMX.isMXBeanInterface(this.managementInterface)));
				}
				else {
					this.invocationHandler = new MBeanServerInvocationHandler(this.server, this.objectName);
				}
			}
			else {
				// Non-strict asing can only be achieved through custom
				// invocation handling. Only partial MXBean support available!
				retrieveMBeanInfo();
			}
		}
	}

	/**
	 * Connects to the remote <code>MBeanServer</code> using the configured <code>JMXServiceURL</code>.
	 * @see #setServiceUrl(String)
	 * @see #setConnectOnStartup(boolean)
	 */
	private void connect() throws MBeanServerNotFoundException {
		if (this.serviceUrl != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Connecting to remote MBeanServer at URL [" + this.serviceUrl + "]");
			}
			try {
				this.connector = JMXConnectorFactory.connect(this.serviceUrl);
				this.server = this.connector.getMBeanServerConnection();
			}
			catch (IOException ex) {
				throw new MBeanServerNotFoundException(
						"Could not connect to remote MBeanServer at URL [" + this.serviceUrl + "]", ex);
			}
		}
		else {
			logger.debug("Attempting to locate local MBeanServer");
			this.server = locateMBeanServer(this.agentId);
		}
	}

	/**
	 * Attempt to locate an existing <code>MBeanServer</code>.
	 * Called if no {@link #setServiceUrl "serviceUrl"} was specified.
	 * <p>The default implementation attempts to find an <code>MBeanServer</code> using
	 * a standard lookup. Subclasses may override to add additional location logic.
	 * @param agentId the agent identifier of the MBeanServer to retrieve.
	 * If this parameter is <code>null</code>, all registered MBeanServers are
	 * considered.
	 * @return the <code>MBeanServer</code> if found
	 * @throws org.springframework.jmx.MBeanServerNotFoundException
	 * if no <code>MBeanServer</code> could be found
	 * @see JmxUtils#locateMBeanServer(String)
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 */
	protected MBeanServer locateMBeanServer(String agentId) throws MBeanServerNotFoundException {
		return JmxUtils.locateMBeanServer(agentId);
	}

	/**
	 * Loads the management interface info for the configured MBean into the caches.
	 * This information is used by the proxy when determining whether an invocation matches
	 * a valid operation or attribute on the management interface of the managed resource.
	 */
	private void retrieveMBeanInfo() throws MBeanInfoRetrievalException {
		try {
			MBeanInfo info = this.server.getMBeanInfo(this.objectName);

			// get attributes
			MBeanAttributeInfo[] attributeInfo = info.getAttributes();
			this.allowedAttributes = new HashMap(attributeInfo.length);

			for (int x = 0; x < attributeInfo.length; x++) {
				this.allowedAttributes.put(attributeInfo[x].getName(), attributeInfo[x]);
			}

			// get operations
			MBeanOperationInfo[] operationInfo = info.getOperations();
			this.allowedOperations = new HashMap(operationInfo.length);

			for (int x = 0; x < operationInfo.length; x++) {
				MBeanOperationInfo opInfo = operationInfo[x];
				Class[] paramTypes = JmxUtils.parameterInfoToTypes(opInfo.getSignature(), this.beanClassLoader);
				this.allowedOperations.put(new MethodCacheKey(opInfo.getName(), paramTypes), opInfo);
			}
		}
		catch (ClassNotFoundException ex) {
			throw new MBeanInfoRetrievalException("Unable to locate class specified in method signature", ex);
		}
		catch (IntrospectionException ex) {
			throw new MBeanInfoRetrievalException("Unable to obtain MBean info for bean [" + this.objectName + "]", ex);
		}
		catch (InstanceNotFoundException ex) {
			// if we are this far this shouldn't happen, but...
			throw new MBeanInfoRetrievalException("Unable to obtain MBean info for bean [" + this.objectName +
					"]: it is likely that this bean was unregistered during the proxy creation process",
					ex);
		}
		catch (ReflectionException ex) {
			throw new MBeanInfoRetrievalException("Unable to read MBean info for bean [ " + this.objectName + "]", ex);
		}
		catch (IOException ex) {
			throw new MBeanInfoRetrievalException(
					"An IOException occurred when communicating with the MBeanServer. " +
							"It is likely that you are communicating with a remote MBeanServer. " +
							"Check the inner exception for exact details.", ex);
		}
	}

	/**
	 * Return whether this client interceptor has already been prepared,
	 * i.e. has already looked up the server and cached all metadata.
	 */
	protected boolean isPrepared() {
		synchronized (this.preparationMonitor) {
			return (this.invocationHandler != null || this.allowedAttributes != null);
		}
	}


	/**
	 * Route the invocation to the configured managed resource. Correctly routes JavaBean property
	 * access to <code>MBeanServerConnection.get/setAttribute</code> and method invocation to
	 * <code>MBeanServerConnection.invoke</code>. Any attempt to invoke a method that does not
	 * correspond to an attribute or operation defined in the management interface of the managed
	 * resource results in an <code>InvalidInvocationException</code>.
	 * @param invocation the <code>MethodInvocation</code> to re-route.
	 * @return the value returned as a result of the re-routed invocation.
	 * @throws InvocationFailureException if the invocation does not match an attribute or
	 * operation on the management interface of the resource.
	 * @throws Throwable typically as the result of an error during invocation
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// Lazily connect to MBeanServer if necessary.
		synchronized (this.preparationMonitor) {
			if (!isPrepared()) {
				prepare();
			}
		}
		try {
			Object result = null;
			if (this.invocationHandler != null) {
				result = this.invocationHandler.invoke(
						invocation.getThis(), invocation.getMethod(), invocation.getArguments());
			}
			else {
				PropertyDescriptor pd = BeanUtils.findPropertyForMethod(invocation.getMethod());
				if (pd != null) {
					result = invokeAttribute(pd, invocation);
				}
				else {
					result = invokeOperation(invocation.getMethod(), invocation.getArguments());
				}
			}
			return convertResultValueIfNecessary(result, invocation.getMethod().getReturnType());
		}
		catch (MBeanException ex) {
			throw ex.getTargetException();
		}
		catch (RuntimeMBeanException ex) {
			throw ex.getTargetException();
		}
		catch (RuntimeErrorException ex) {
			throw ex.getTargetError();
		}
		catch (RuntimeOperationsException ex) {
			// This one is only thrown by the JMX 1.2 RI, not by the JDK 1.5 JMX code.
			RuntimeException rex = ex.getTargetException();
			if (rex instanceof RuntimeMBeanException) {
				throw ((RuntimeMBeanException) rex).getTargetException();
			}
			else if (rex instanceof RuntimeErrorException) {
				throw ((RuntimeErrorException) rex).getTargetError();
			}
			else {
				throw rex;
			}
		}
		catch (OperationsException ex) {
			throw new InvalidInvocationException(ex.getMessage());
		}
		catch (JMException ex) {
			throw new InvocationFailureException("JMX access failed", ex);
		}
		catch (IOException ex) {
			throw new InvocationFailureException("JMX access failed", ex);
		}
	}

	private Object invokeAttribute(PropertyDescriptor pd, MethodInvocation invocation)
			throws JMException, IOException {

		String attributeName = JmxUtils.getAttributeName(pd, this.useStrictCasing);
		MBeanAttributeInfo inf = (MBeanAttributeInfo) this.allowedAttributes.get(attributeName);
		// If no attribute is returned, we know that it is not defined in the
		// management interface.
		if (inf == null) {
			throw new InvalidInvocationException(
					"Attribute '" + pd.getName() + "' is not exposed on the management interface");
		}
		if (invocation.getMethod().equals(pd.getReadMethod())) {
			if (inf.isReadable()) {
				return this.server.getAttribute(this.objectName, attributeName);
			}
			else {
				throw new InvalidInvocationException("Attribute '" + attributeName + "' is not readable");
			}
		}
		else if (invocation.getMethod().equals(pd.getWriteMethod())) {
			if (inf.isWritable()) {
				this.server.setAttribute(this.objectName, new Attribute(attributeName, invocation.getArguments()[0]));
				return null;
			}
			else {
				throw new InvalidInvocationException("Attribute '" + attributeName + "' is not writable");
			}
		}
		else {
			throw new IllegalStateException(
					"Method [" + invocation.getMethod() + "] is neither a bean property getter nor a setter");
		}
	}

	/**
	 * Routes a method invocation (not a property get/set) to the corresponding
	 * operation on the managed resource.
	 * @param method the method corresponding to operation on the managed resource.
	 * @param args the invocation arguments
	 * @return the value returned by the method invocation.
	 */
	private Object invokeOperation(Method method, Object[] args) throws JMException, IOException {
		MethodCacheKey key = new MethodCacheKey(method.getName(), method.getParameterTypes());
		MBeanOperationInfo info = (MBeanOperationInfo) this.allowedOperations.get(key);
		if (info == null) {
			throw new InvalidInvocationException("Operation '" + method.getName() +
					"' is not exposed on the management interface");
		}
		String[] signature = null;
		synchronized (this.signatureCache) {
			signature = (String[]) this.signatureCache.get(method);
			if (signature == null) {
				signature = JmxUtils.getMethodSignature(method);
				this.signatureCache.put(method, signature);
			}
		}
		return this.server.invoke(this.objectName, method.getName(), args, signature);
	}

	/**
	 * Convert the given result object (from attribute access or operation invocation)
	 * to the specified target class for returning from the proxy method.
	 * @param result the result object as returned by the <code>MBeanServer</code>
	 * @param targetClass the result type of the proxy method that's been invoked
	 * @return the converted result object, or the passed-in object if no conversion
	 * is necessary
	 */
	protected Object convertResultValueIfNecessary(Object result, Class targetClass) {
		try {
			if (result == null) {
				return null;
			}
			if (ClassUtils.isAssignableValue(targetClass, result)) {
				return result;
			}
			if (result instanceof CompositeData) {
				Method fromMethod = targetClass.getMethod("from", new Class[] {CompositeData.class});
				return ReflectionUtils.invokeMethod(fromMethod, null, new Object[] {result});
			}
			else if (result instanceof TabularData) {
				Method fromMethod = targetClass.getMethod("from", new Class[] {TabularData.class});
				return ReflectionUtils.invokeMethod(fromMethod, null, new Object[] {result});
			}
			else {
				throw new InvocationFailureException(
						"Incompatible result value [" + result + "] for target type [" + targetClass.getName() + "]");
			}
		}
		catch (NoSuchMethodException ex) {
			throw new InvocationFailureException(
					"Could not obtain 'find(CompositeData)' / 'find(TabularData)' method on target type [" +
							targetClass.getName() + "] for conversion of MXBean data structure [" + result + "]");
		}
	}


	/**
	 * Closes any <code>JMXConnector</code> that may be managed by this interceptor.
	 */
	public void destroy() throws Exception {
		if (this.connector != null) {
			this.connector.close();
		}
	}


	/**
	 * Simple wrapper class around a method name and its signature.
	 * Used as the key when caching methods.
	 */
	private static class MethodCacheKey {

		private final String name;

		private final Class[] parameterTypes;

		/**
		 * Create a new instance of <code>MethodCacheKey</code> with the supplied
		 * method name and parameter list.
		 * @param name the name of the method
		 * @param parameterTypes the arguments in the method signature
		 */
		public MethodCacheKey(String name, Class[] parameterTypes) {
			this.name = name;
			this.parameterTypes = (parameterTypes != null ? parameterTypes : new Class[0]);
		}

		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other == this) {
				return true;
			}
			MethodCacheKey otherKey = null;
			if (other instanceof MethodCacheKey) {
				otherKey = (MethodCacheKey) other;
				return this.name.equals(otherKey.name) && Arrays.equals(this.parameterTypes, otherKey.parameterTypes);
			}
			else {
				return false;
			}
		}

		public int hashCode() {
			return this.name.hashCode();
		}
	}

}
