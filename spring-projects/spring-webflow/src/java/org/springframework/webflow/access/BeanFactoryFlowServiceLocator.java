/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.access;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.binding.format.support.LabeledEnumFormatter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.Transition;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.support.FlowConversionService;

/**
 * A flow service locator that uses a Spring bean factory to lookup flow-related
 * services.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class BeanFactoryFlowServiceLocator implements FlowServiceLocator, BeanFactoryAware {

	/**
	 * The default autowire mode for services creating by this locator.
	 */
	private AutowireMode defaultAutowireMode = AutowireMode.NONE;
	
	/**
	 * The flow creation strategy.
	 */
	private FlowCreator flowCreator = new DefaultFlowCreator();
	
	/**
	 * The webflow data type conversion service.
	 */
	private ConversionService conversionService = new FlowConversionService();
	
	/**
	 * The wrapped bean factory.
	 */
	private BeanFactory beanFactory;

	/**
	 * Create a new service locator locating services in the bean factory that
	 * will be passed in using the <code>setBeanFactory()</code> method.
	 */
	public BeanFactoryFlowServiceLocator() {
	}

	/**
	 * Create a new service locator locating services in given bean factory.
	 * @param beanFactory the bean factory
	 */
	public BeanFactoryFlowServiceLocator(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}

	/**
	 * Returns the factory used to create Flow objects.
	 */
	public FlowCreator getFlowCreator() {
		return flowCreator;
	}

	/**
	 * Set the factory used to create flow objects.
	 */
	public void setFlowCreator(FlowCreator flowCreator) {
		Assert.notNull(flowCreator, "The flow creator is required");
		this.flowCreator = flowCreator;
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * Set the conversion service to provide type converters for use by 
	 * the webflow system artifacts.
	 */
	public void setConversionService(ConversionService conversionService) {
		Assert.notNull(conversionService, "The flow conversion service is required");
		this.conversionService = conversionService;
	}

	/**
	 * Returns the default autowire mode. This defaults to
	 * {@link AutowireMode#NONE}.
	 */
	public AutowireMode getDefaultAutowireMode() {
		return defaultAutowireMode;
	}

	/**
	 * Set the default autowire mode.
	 */
	public void setDefaultAutowireMode(AutowireMode defaultAutowireMode) {
		// avoid infinite loops!
		Assert.isTrue(defaultAutowireMode != AutowireMode.DEFAULT, "The default auto wire must not equal 'default'");
		this.defaultAutowireMode = defaultAutowireMode;
	}
	
	/**
	 * Convenience setter that performs a string to AutowireMode conversion for you.
	 * @param encodedAutowireMode the encoded autowire mode string
	 * @throws InvalidFormatException the encoded value was invalid
	 */
	public void setDefaultAutowireModeAsString(String encodedAutowireMode) throws InvalidFormatException {
		this.defaultAutowireMode =
			(AutowireMode)new LabeledEnumFormatter().parseValue(encodedAutowireMode, AutowireMode.class);
	}
	
	/**
	 * Returns the bean factory used to lookup services.
	 */
	protected BeanFactory getBeanFactory() {
		if (this.beanFactory == null) {
			throw new IllegalStateException("The bean factory reference has not yet been set -- call setBeanFactory()");
		}
		return beanFactory;
	}

	/**
	 * Returns the bean factory used to lookup services.
	 */
	protected ListableBeanFactory getListableBeanFactory() {
		return (ListableBeanFactory)getBeanFactory();
	}

	/**
	 * Returns the bean factory used to autowire services.
	 */
	protected AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
		return (AutowireCapableBeanFactory)getBeanFactory();
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	// helper methods
	
	/**
	 * Helper to have the application context instantiate a service class
	 * and wire up any dependencies.
	 * @param expectedClass the expected service (super) class
	 * @param implementationClass the implementation class
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) service
	 * @throws ServiceLookupException when the services cannot be created
	 */
	protected Object createService(Class expectedClass, Class implementationClass, AutowireMode autowireMode)
			throws ServiceLookupException {
		Assert.isTrue(expectedClass.isAssignableFrom(implementationClass),
				"The service to instantiate must be a '" + ClassUtils.getShortName(expectedClass)
				+ "', the implementation class '" + implementationClass + "' you provided is not.");
		if (autowireMode == AutowireMode.DEFAULT) {
			return createService(expectedClass, implementationClass, getDefaultAutowireMode());
		}
		try {
			if (autowireMode == AutowireMode.NONE) {
				return BeanUtils.instantiateClass(implementationClass);
			}
			else {
				return getAutowireCapableBeanFactory().autowire(implementationClass, autowireMode.getShortCode(), false);
			}
		}
		catch (BeansException e) {
			throw new ServiceCreationException(expectedClass, implementationClass,
					"Cannot create service object with autowire mode '" + autowireMode + "'", e);
		}
	}
	
	/**
	 * Autowire given service object.
	 * @param service the object to wire up
	 * @param autowireMode the autowire mode to use
	 */
	protected void autowireService(Object service, AutowireMode autowireMode) {
		if (autowireMode == AutowireMode.DEFAULT) {
			autowireService(service, getDefaultAutowireMode());
		}
		else if (autowireMode != AutowireMode.NONE) {
			getAutowireCapableBeanFactory().autowireBeanProperties(service, autowireMode.getShortCode(), false);
		}
	}
	
	/**
	 * Lookup a service by id.
	 * @param expectedClass the expected service (super) class
	 * @param id the service id
	 * @return the service object
	 * @throws ServiceLookupException when the identified service cannot be found
	 */
	protected Object lookupService(Class expectedClass, String id) throws ServiceLookupException {
		try {
			return getBeanFactory().getBean(id, expectedClass);
		}
		catch (BeansException e) {
			throw new ServiceLookupException(expectedClass, id, e);
		}
	}

	/**
	 * Lookup a service by implementation class.
	 * @param expectedClass the expected service (super) class
	 * @param implementationClass the required implementation class
	 * @return the service object
	 * @throws ServiceLookupException when the service object cannot be found
	 */
	protected Object lookupService(Class expectedClass, Class implementationClass) throws ServiceLookupException {
		try {
			Assert.isTrue(expectedClass.isAssignableFrom(implementationClass), 
					"The '" + ClassUtils.getShortName(expectedClass) + "' implementation  '" + implementationClass
					+ "' you wish to retrieve must be a subclass of '" + ClassUtils.getShortName(expectedClass) + "'");
			return BeanFactoryUtils.beanOfType(getListableBeanFactory(), implementationClass);
		}
		catch (BeansException e) {
			throw new ServiceLookupException(expectedClass, implementationClass, e);
		}
	}
	
	/**
	 * Turn given service object into an action. If the given service object implements the
	 * <code>Action</code> interface, it is returned as is, otherwise it is wrapped in
	 * an action that can invoke a method on the service bean.
	 * @param service the service bean
	 * @return the action
	 */
	protected Action toAction(Object service) {
		if (service instanceof Action) {
			return (Action)service;
		}
		else {
			return new LocalBeanInvokingAction(service);
		}
	}
	
	// implementing FlowServiceLocator
	
	public Flow createFlow(AutowireMode autowireMode) throws ServiceLookupException {
		Flow flow = getFlowCreator().createFlow();
		autowireService(flow, autowireMode);
		return flow;
	}
	
	public Flow createFlow(Class implementationClass, AutowireMode autowireMode) throws ServiceLookupException {
		return (Flow)createService(Flow.class, implementationClass, autowireMode);
	}
	
	public Flow getFlow(String id) throws ServiceLookupException {
		return (Flow)lookupService(Flow.class, id);
	}

	public Flow getFlow(Class implementationClass) throws ServiceLookupException {
		return (Flow)lookupService(Flow.class, implementationClass);
	}
	
	public State createState(Class implementationClass, AutowireMode autowireMode) throws ServiceLookupException {
		return (State)createService(State.class, implementationClass, autowireMode);
	}
	
	public State getState(String id) throws ServiceLookupException {
		return (State)lookupService(State.class, id);
	}
	
	public State getState(Class implementationClass) throws ServiceLookupException {
		return (State)lookupService(State.class, implementationClass);
	}
	
	public Transition createTransition(Class implementationClass, AutowireMode autowireMode)
			throws ServiceLookupException {
		return (Transition)createService(Transition.class, implementationClass, autowireMode);
	}
	
	public Transition getTransition(String id) throws ServiceLookupException {
		return (Transition)lookupService(Transition.class, id);
	}
	
	public Transition getTransition(Class implementationClass) throws ServiceLookupException {
		return (Transition)lookupService(Transition.class, implementationClass);
	}
	
	public Action createAction(Class implementationClass, AutowireMode autowireMode) {
		return toAction(createService(Object.class, implementationClass, autowireMode));
	}

	public Action getAction(String id) throws ServiceLookupException {
		return toAction(lookupService(Object.class, id));
	}

	public Action getAction(Class implementationClass) throws ServiceLookupException {
		return toAction(lookupService(Object.class, implementationClass));
	}

	public Object createBean(Class implementationClass, AutowireMode autowireMode) {
		return createService(Object.class, implementationClass, autowireMode);
	}

	public Object getBean(String id) throws ServiceLookupException {
		return lookupService(Object.class, id);
	}

	public Object getBean(Class implementationClass) throws ServiceLookupException {
		return lookupService(Object.class, implementationClass);
	}

	public FlowAttributeMapper createFlowAttributeMapper(Class implementationClass, AutowireMode autowireMode) {
		return (FlowAttributeMapper)createService(FlowAttributeMapper.class, implementationClass, autowireMode);
	}

	public FlowAttributeMapper getFlowAttributeMapper(String id) throws ServiceLookupException {
		return (FlowAttributeMapper)lookupService(FlowAttributeMapper.class, id);
	}

	public FlowAttributeMapper getFlowAttributeMapper(Class implementationClass)
			throws ServiceLookupException {
		return (FlowAttributeMapper)lookupService(FlowAttributeMapper.class, implementationClass);
	}
}