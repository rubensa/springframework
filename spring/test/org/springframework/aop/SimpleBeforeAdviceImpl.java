package org.springframework.aop;

/**
 * 
 * @author Dmitriy Kopylenko
 * @version $Id$
 */
public class SimpleBeforeAdviceImpl implements SimpleBeforeAdvice {
	
	private int invocationCounter;

	/**
	 * @see org.springframework.aop.SimpleBeforeAdvice#before()
	 */
	public void before() throws Throwable {
		System.out.println("before() method is called on " + getClass().getName());
		++invocationCounter;
	}

	public int getInvocationCounter() {
		return invocationCounter;
	}

}
