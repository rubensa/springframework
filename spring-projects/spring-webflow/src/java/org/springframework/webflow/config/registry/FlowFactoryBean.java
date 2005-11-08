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
package org.springframework.webflow.config.registry;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.webflow.Flow;
import org.springframework.webflow.config.FlowBuilder;

/**
 * A FlowAssembler that is also a Spring FactoryBean, for convenient deployment
 * within a Spring BeanFactory.
 * 
 * Usage example:
 * 
 * <pre>
 *     &lt;bean id=&quot;myFlow&quot; class=&quot;org.springframework.webflow.config.registry.FlowFactoryBean&quot;&gt;
 *         &lt;constructor-arg&gt;
 *             &lt;bean class=&quot;example.MyFlowBuilder&quot;/&gt;
 *         &lt;/constructor-arg&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowFactoryBean implements FactoryBean {

	/**
	 * The assembler this factory bean will delegate to for flow construction.
	 */
	private FlowAssembler flowAssembler;

	/**
	 * Creates a new flow factory bean.
	 * @param flowBuilder the flow builder
	 */
	public FlowFactoryBean(FlowBuilder flowBuilder) {
		flowAssembler = new FlowAssembler(flowBuilder);
	}

	/**
	 * Returns the flow builder used by this factory bean.
	 */
	protected FlowBuilder getFlowBuilder() {
		return flowAssembler.getFlowBuilder();
	}

	public Class getObjectType() {
		return Flow.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		return getFlow();
	}

	/**
	 * Returns the fully assembled flow.
	 */
	protected Flow getFlow() {
		return flowAssembler.getFlow();
	}
}