/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

/**
 * Tests with ControlFlowFactory return
 * @author Rod Johnson
 * @version $Id$
 */
public class Jdk14ControlFlowTests extends AbstractControlFlowTests {
	
	public Jdk14ControlFlowTests(String s) {
		super(s);
	}
	
	/** 
	 * Necessary only because
	 * Eclipse won't run test suite unless it declares some methods
	 * as well as inherited methods
	 */
	public void testThisClassPlease() {
	}

	/**
	 * @see org.springframework.aop.support.AbstractControlFlowTests#createControlFlow()
	 */
	protected ControlFlow createControlFlow() {
		return new Jdk14ControlFlow();
	}

}
