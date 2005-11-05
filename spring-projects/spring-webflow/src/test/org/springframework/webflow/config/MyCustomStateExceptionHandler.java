package org.springframework.webflow.config;

import org.springframework.webflow.FlowControlContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.ViewSelection;

public class MyCustomStateExceptionHandler implements StateExceptionHandler {

	public boolean handles(StateException e) {
		return false;
	}

	public ViewSelection handle(StateException e, FlowControlContext context) {
		return null;
	}

}
