package org.springframework.aop;

import org.springframework.aop.BeforeAdvice;

/**
 * Simple BeforeAdvice targeted for testing
 * @author Dmitriy Kopylenko
 * @version $Id$
 */
public interface SimpleBeforeAdvice extends BeforeAdvice {
	
	void before() throws Throwable;

}
