/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;

/**
 * Tests with ControlFlowFactory return
 * @author Rod Johnson
 * @version $Id$
 */
public class DefaultControlFlowTests extends AbstractControlFlowTests {
	
	public DefaultControlFlowTests(String s) {
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
	 *
	 * @see org.springframework.aop.support.AbstractControlFlowTests#createControlFlow()
	 */
	protected ControlFlow createControlFlow() {
		ControlFlow cf = ControlFlowFactory.getInstance().createControlFlow();
		boolean is14 = System.getProperty("java.version").indexOf("1.4") != -1;
		assertEquals("Autodetection of JVM succeeded", is14, cf instanceof Jdk14ControlFlow);
		return cf;
	}

}
