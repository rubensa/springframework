/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.DelegatingFlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryServices;
import org.springframework.webflow.execution.repository.SharedMapFlowExecutionRepositoryFactory;

/**
 * A convenient implementation that encapsulates the assembly of a server-side
 * continuation-based flow execution repository factory and delegates to it at
 * runtime.
 * <p>
 * Specifically, this delegating repository factory:
 * <ul>
 * <li>Sets a {@link SharedMapFlowExecutionRepositoryFactory} to manage flow
 * execution repository implementations statefully in the
 * {@link ExternalContext#getSessionMap()}, typically backed by the HTTP
 * session.
 * <li>Configures it with a {@link ContinuationFlowExecutionRepositoryCreator}
 * to create instances of {@link ContinuationFlowExecutionRepository} when
 * requested for placement in the session map.
 * </ul>
 * <p>
 * This class inherits from {@link FlowExecutionRepositoryServices} to allow for
 * direct configuration of services needed by the repositories created by this
 * factory.
 * 
 * @see ContinuationFlowExecutionRepositoryCreator
 * @see ContinuationFlowExecutionRepository
 * 
 * @author Keith Donald
 */
public class ContinuationFlowExecutionRepositoryFactory extends DelegatingFlowExecutionRepositoryFactory {

	/**
	 * Creates a new simple flow execution repository factory.
	 * @param flowLocator the locator for loading flow definitions for which
	 * flow executions are created from
	 */
	public ContinuationFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
		setRepositoryFactory(new SharedMapFlowExecutionRepositoryFactory(
				new ContinuationFlowExecutionRepositoryCreator(this)));
	}

	/**
	 * Helper that returns the configured repository creator used by this
	 * factory.
	 */
	protected ContinuationFlowExecutionRepositoryCreator getRepositoryCreator() {
		SharedMapFlowExecutionRepositoryFactory factory = (SharedMapFlowExecutionRepositoryFactory)getRepositoryFactory();
		return (ContinuationFlowExecutionRepositoryCreator)factory.getRepositoryCreator();
	}

	/**
	 * Sets the continuation factory that encapsulates the construction of
	 * continuations stored in this repository.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		getRepositoryCreator().setContinuationFactory(continuationFactory);
	}

	/**
	 * Sets the maximum number of continuations allowed per conversation in this
	 * repository.
	 */
	public void setMaxContinuations(int maxContinuations) {
		getRepositoryCreator().setMaxContinuations(maxContinuations);
	}
}