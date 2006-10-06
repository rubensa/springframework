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
 *
 * Created on 6 Oct 2006 by Adrian Colyer
 */
package org.springframework.aop.aspectj.autoproxy;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author Adrian Colyer
 * @since 2.0
 */
public class AtAspectJAnnotationBindingTests extends
		AbstractDependencyInjectionSpringContextTests {

	private AnnotatedTestBean testBean;
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] {"org/springframework/aop/aspectj/autoproxy/ataspectj-around-advice-tests.xml"};
	}

	public void setTestBean(AnnotatedTestBean testBean) {
		this.testBean = testBean;
	}

	public void testAnnotationBindingInAroundAdvice() {
		assertEquals("this value doThis",testBean.doThis());
		assertEquals("that value doThat",testBean.doThat());
	}
	
	public void testNoMatchingWithoutAnnotationPresent() {
		assertEquals("doTheOther",testBean.doTheOther());
	}
}
