/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;




/**
 * TODO base Advice interface? ONLY WAY TO DO INTRODUCTION
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @version $Id$
 */
public interface InterceptionIntroductionAdvisor extends InterceptionAdvisor {
	
	ClassFilter getClassFilter();
	
	IntroductionInterceptor getIntroductionInterceptor();
	
	Class[] getInterfaces();
	

}
