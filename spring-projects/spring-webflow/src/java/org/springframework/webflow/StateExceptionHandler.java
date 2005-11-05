package org.springframework.webflow;

/**
 * A strategy for handling an exception that occurs in a state during the
 * execution of a flow definition.
 * 
 * @author Keith Donald
 */
public interface StateExceptionHandler {

	/**
	 * Can this handler handle the given exception?
	 * @param e the exception that occured
	 * @return true if yes, false if no
	 */
	public boolean handles(StateException e);

	/**
	 * Handle this exception in the context of the current request and
	 * optionally select an error view that should be displayed.
	 * @param e the exception that occured
	 * @param context the flow control context
	 * @return the selected error view that should be displayed (may be null if
	 * the handler chooses not to select a view)
	 */
	public ViewSelection handle(StateException e, FlowExecutionControlContext context);
}
