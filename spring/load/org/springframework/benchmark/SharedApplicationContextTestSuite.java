/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.benchmark;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.load.BeanFactoryTestSuite;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class SharedApplicationContextTestSuite extends BeanFactoryTestSuite {
	
	private String location = "/org/springframework/benchmark/beans.xml";
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		setFixture(new ClassPathXmlApplicationContext(location));
	}

}
