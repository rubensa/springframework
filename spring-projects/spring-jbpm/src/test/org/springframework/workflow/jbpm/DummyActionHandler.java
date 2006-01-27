/**
 * Created on Jan 24, 2006
 *
 * $Id$
 * $Revision$
 */
package org.springframework.workflow.jbpm;

import junit.framework.TestCase;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * Dummy action used for testing the Spring Handler.
 * @author Costin Leau
 *
 */
public class DummyActionHandler implements ActionHandler {
	static final String TEST_LABEL = "testSpringHandler";

	public void execute(ExecutionContext executionContext) throws Exception {
		Object obj = executionContext.getContextInstance().getTransientVariable(TEST_LABEL);
		TestCase.assertSame(this, obj);
	}

	public DummyActionHandler() {
		System.out.println("DummyActionHandler constructor");
	}

}
