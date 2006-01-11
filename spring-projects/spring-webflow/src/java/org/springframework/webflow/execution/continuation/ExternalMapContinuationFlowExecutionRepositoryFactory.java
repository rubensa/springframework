package org.springframework.webflow.execution.continuation;

import org.springframework.webflow.execution.ExternalMapFlowExecutionRepositoryFactory;

/**
 * A subclass of {@link ExternalMapFlowExecutionRepositoryFactory} that simply
 * uses a
 * {@link ContinuationFlowExecutionRepositoryCreator continuation-based flow execution repository factory}
 * by default.
 * <p>
 * This is a convenience implementation that makes it easy to use a server-side
 * continuation-based flow execution storage strategy with a
 * {@link org.springframework.webflow.execution.FlowExecutionManagerImpl}.
 * 
 * @see ContinuationFlowExecutionRepositoryCreator
 * 
 * @author Keith Donald
 */
public class ExternalMapContinuationFlowExecutionRepositoryFactory extends ExternalMapFlowExecutionRepositoryFactory {
	public ExternalMapContinuationFlowExecutionRepositoryFactory() {
		setRepositoryCreator(new ContinuationFlowExecutionRepositoryCreator());
	}
}