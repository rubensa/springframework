/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.flow.Flow;

/**
 * Factory bean that acts as a director for assembling flows, delegating to a
 * builder to construct the Flow.
 * @author Keith Donald
 */
public class FlowFactoryBean implements FactoryBean, InitializingBean {

	private FlowBuilder flowBuilder;

	private Flow flow;

	public FlowFactoryBean() {

	}

	public FlowFactoryBean(FlowBuilder flowBuilder) {
		setFlowBuilder(flowBuilder);
	}

	public void setFlowBuilder(FlowBuilder flowBuilder) {
		this.flowBuilder = flowBuilder;
	}

	public void afterPropertiesSet() {
		Assert.state(flowBuilder != null, "The flow builder is required to assemble the flow produced by this factory");
	}

	/**
	 * Does this factory bean build the specified FlowBuilder implementation?
	 * @param requiredBuilderImplementationClass The builder implementation
	 * @return true if yes, false otherwise
	 */
	public boolean buildsWith(Class requiredBuilderImplementationClass) throws IllegalArgumentException {
		if (requiredBuilderImplementationClass == null) {
			return false;
		}
		if (!FlowBuilder.class.isAssignableFrom(requiredBuilderImplementationClass)) {
			throw new IllegalArgumentException("The flow builder implementation class '"
					+ requiredBuilderImplementationClass + "' you provided to this method does not implement the '"
					+ FlowBuilder.class.getName() + "' interface");
		}
		return getFlowBuilder().getClass().equals(requiredBuilderImplementationClass);
	}

	protected FlowBuilder getFlowBuilder() {
		return this.flowBuilder;
	}
	
	public Object getObject() throws Exception {
		return getFlow();
	}

	public Flow getFlow() {
		if (this.flow == null) {
			new FlowAssembler(this.flowBuilder).assemble();
			this.flow = this.flowBuilder.getResult();
		}
		return this.flow;
	}

	public Class getObjectType() {
		return Flow.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public static class FlowAssembler {
		private FlowBuilder builder;

		public FlowAssembler(FlowBuilder builder) {
			this.builder = builder;
		}

		public void assemble() {
			this.builder.init();
			this.builder.buildStates();
			this.builder.buildExecutionListeners();
		}
	}
}