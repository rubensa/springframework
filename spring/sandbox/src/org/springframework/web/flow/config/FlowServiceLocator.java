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
package org.springframework.web.flow.config;

import org.springframework.web.flow.Action;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.ServiceLookupException;

/**
 * Service locator interface used by flow builders at configuration time to
 * retrieve needed artifacts.
 * <p>
 * Note that this service locator is a configuration time object. It is not used
 * during flow execution!
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 */
public interface FlowServiceLocator extends FlowLocator {

	/**
	 * Request that the registry backed by this locator instantiate the action
	 * of the specified implementation class, using the given autowire policy.
	 * Note: not all registries may support this advanced feature (Spring does
	 * though ;-)).
	 * @param implementationClass the action implementation class
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) action
	 * @throws ServiceLookupException when the action cannot be created
	 */
	public Action createAction(Class implementationClass, AutowireMode autowireMode);

	/**
	 * Lookup an action with specified id.
	 * @param actionId the action id
	 * @return the action
	 * @throws ServiceLookupException when the action cannot be found
	 */
	public Action getAction(String actionId) throws ServiceLookupException;

	/**
	 * Lookup an action of specified implementation class.
	 * @param actionImplementationClass the required implementation class
	 * @return the action
	 * @throws ServiceLookupException when the action cannot be found
	 */
	public Action getAction(Class actionImplementationClass) throws ServiceLookupException;

	/**
	 * Request that the registry backed by this locator instantiate the flow
	 * attribute mapper of the specified implementation class, using the given
	 * autowire policy. Note: not all registries may support this advanced
	 * feature (Spring does though ;-)).
	 * @param attributeMapperImplementationClass the implementation class
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) attribute mapper
	 */
	public FlowAttributeMapper createFlowAttributeMapper(Class attributeMapperImplementationClass, AutowireMode autowireMode);

	/**
	 * Lookup a flow model mapper with specified id.
	 * @param flowAttributeMapperId the flow model mapper id
	 * @return the flow model mapper
	 * @throws ServiceLookupException when the flow model mapper cannot be found
	 */
	public FlowAttributeMapper getFlowAttributeMapper(String flowAttributeMapperId) throws ServiceLookupException;

	/**
	 * Lookup a flow model mapper of specified implementation class.
	 * @param flowAttributeMapperImplementationClass the required implementation
	 *        class
	 * @return the flow model mapper
	 * @throws ServiceLookupException when the flow model mapper cannot be found
	 */
	public FlowAttributeMapper getFlowAttributeMapper(Class flowAttributeMapperImplementationClass)
			throws ServiceLookupException;

}