package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.FlowLocator;

/**
 * A base for decorators that encapsulate the construction and configuration of
 * a custom flow execution repository factory delegate. The delegate is invoked
 * at runtime in standard decorator fashion.
 * <p>
 * Also exposes a convenient configuration interface for clients to configure
 * common {@link FlowExecutionRepositoryServices repository services} directly,
 * allowing for easy customization over the behavior of repositories created by the
 * delegate factory.
 * 
 * @author Keith Donald
 */
public abstract class DelegatingFlowExecutionRepositoryFactory extends FlowExecutionRepositoryServices implements
		FlowExecutionRepositoryFactory {

	/**
	 * The repository to delegate to.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * Creates a new delegating flow execution repository factory.
	 * @param flowLocator the low locator service to be used by repositories
	 * created by this factory
	 */
	protected DelegatingFlowExecutionRepositoryFactory(FlowLocator flowLocator) {
		super(flowLocator);
	}

	/**
	 * Returns the wrapped repository factory delegate.
	 */
	protected FlowExecutionRepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	/**
	 * Called by superclasses to set the configured repository factory delegate
	 * after construction.
	 */
	protected void setRepositoryFactory(FlowExecutionRepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}

	/*
	 * Simply delegates to the wrapped repository factory.
	 * @see org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory#getRepository(org.springframework.webflow.ExternalContext)
	 */
	public FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}
}