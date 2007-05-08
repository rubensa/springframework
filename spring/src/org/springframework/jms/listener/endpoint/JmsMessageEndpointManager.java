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

package org.springframework.jms.listener.endpoint;

import javax.jms.MessageListener;
import javax.resource.ResourceException;

import org.springframework.jca.endpoint.GenericMessageEndpointManager;

/**
 * Extension of the generic JCA 1.5
 * {@link org.springframework.jca.endpoint.GenericMessageEndpointManager},
 * adding JMS-specific support for ActivationSpec configuration.
 *
 * <p>Allows for defining a common {@link JmsActivationSpecConfig} object
 * that gets converted into a provided-specific JCA 1.5 ActivationSpec
 * object for activating the endpoint.
 *
 * <p><b>NOTE:</b> This JCA-based endpoint manager supports standard JMS
 * {@link javax.jms.MessageListener} endpoints only. It does <i>not</i> support
 * Spring's {@link org.springframework.jms.listener.SessionAwareMessageListener}
 * variant, simply because the JCA endpoint management contract does not allow
 * for obtaining the current JMS {@link javax.jms.Session}.
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see javax.jms.MessageListener
 * @see #setActivationSpecConfig
 * @see JmsActivationSpecConfig
 * @see JmsActivationSpecFactory
 * @see JmsMessageEndpointFactory
 */
public class JmsMessageEndpointManager extends GenericMessageEndpointManager {

	private final JmsMessageEndpointFactory endpointFactory = new JmsMessageEndpointFactory();

	private boolean messageListenerSet = false;

	private JmsActivationSpecFactory activationSpecFactory;

	private JmsActivationSpecConfig activationSpecConfig;


	/**
	 * Set the JMS MessageListener for this endpoint.
	 * <p>This is a shortcut for configuring a dedicated JmsMessageEndpointFactory.
	 * @see JmsMessageEndpointFactory#setMessageListener
	 */
	public void setMessageListener(MessageListener messageListener) {
		this.endpointFactory.setMessageListener(messageListener);
		this.messageListenerSet = true;
	}

	/**
	 * Set the the XA transaction manager to use for wrapping endpoint
	 * invocations, enlisting the endpoint resource in each such transaction.
	 * <p>The passed-in object may be a transaction manager which implements
	 * Spring's {@link org.springframework.transaction.jta.TransactionFactory}
	 * interface, or a plan {@link javax.transaction.TransactionManager}.
	 * <p>If no transaction manager is specified, the endpoint invocation
	 * will simply not be wrapped in an XA transaction. Check out your
	 * resource provider's ActivationSpec documentation for local
	 * transaction options of your particular provider.
	 * <p>This is a shortcut for configuring a dedicated JmsMessageEndpointFactory.
	 * @see JmsMessageEndpointFactory#setTransactionManager
	 */
	public void setTransactionManager(Object transactionManager) {
		this.endpointFactory.setTransactionManager(transactionManager);
	}

	/**
	 * Set the factory for concrete JCA 1.5 ActivationSpec objects,
	 * creating JCA ActivationSpecs based on
	 * {@link #setActivationSpecConfig JmsActivationSpecConfig} objects.
	 * <p>This factory is dependent on the concrete JMS provider, e.g. on ActiveMQ.
	 * For this reason, there is no default - it needs to be configured.
	 */
	public void setActivationSpecFactory(JmsActivationSpecFactory activationSpecFactory) {
		this.activationSpecFactory = activationSpecFactory;
	}

	/**
	 * Specify the {@link JmsActivationSpecConfig} object that this endpoint manager
	 * should use for activating its listener.
	 * <p>This config object will be turned into a concrete JCA 1.5 ActivationSpec
	 * object through a {@link #setActivationSpecFactory JmsActivationSpecFactory}.
	 */
	public void setActivationSpecConfig(JmsActivationSpecConfig activationSpecConfig) {
		this.activationSpecConfig = activationSpecConfig;
	}


	public void afterPropertiesSet() throws ResourceException {
		if (this.messageListenerSet) {
			setMessageEndpointFactory(this.endpointFactory);
		}
		if (this.activationSpecConfig != null) {
			if (this.activationSpecFactory == null) {
				throw new IllegalStateException(
						"Property 'activationSpecConfig' requires property 'activationSpecFactory' to be set");
			}
			setActivationSpec(this.activationSpecFactory.createActivationSpec(this.activationSpecConfig));
		}
		super.afterPropertiesSet();
	}

}
