/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.aop.aspectj.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscovererTests;

/**
 * @author acolyer
 * 
 * Additional parameter name discover tests that need Java5.
 * Yes this will re-run the tests from the superclass, but that
 * doesn't matter in the grand scheme of things...
 *
 */
public class AspectJAdviceParameterNameDiscoverAnnotationTests extends
		AspectJAdviceParameterNameDiscovererTests {

	@Retention(RetentionPolicy.RUNTIME)
	@interface MyAnnotation {}
	
	public void pjpAndAnAnnotation(ProceedingJoinPoint pjp, MyAnnotation ann) {}

	public void testAnnotationBinding() {
		assertParameterNames(getMethod("pjpAndAnAnnotation"),
				"execution(* *(..)) && @annotation(ann)",
				new String[] {"thisJoinPoint","ann"});
	}
}
