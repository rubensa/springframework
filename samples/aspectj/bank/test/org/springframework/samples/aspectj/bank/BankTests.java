package org.springframework.samples.aspectj.bank;
import java.io.IOException;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

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

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class BankTests extends TestCase {

	/**
	 * Constructor for BankTests.
	 * @param arg0
	 */
	public BankTests(String arg0) {
		super(arg0);
	}
	
	
	public void test1() throws Exception {
		ClassPathXmlApplicationContext beanFactory = new ClassPathXmlApplicationContext("/org/springframework/samples/aspectj/bank/beans.xml");

		Bank b = (Bank) beanFactory.getBean("bank");
		
		Account acc = b.findAccount(1);		
		System.out.println(acc);
		// Use introduced Audited interface
		assertEquals(0, acc.changeCount());
		acc.deposit(200);
		
		// Will attach mixin
		assertEquals(200, acc.getBalance());
		acc.withdraw(16);
		assertEquals(2, acc.changeCount());
		assertEquals(184, acc.getBalance());
		System.out.println(acc);
		
		Account acc2 = b.findAccount(2);
		assertNotSame(acc, acc2);
		assertEquals(0, acc2.changeCount());
		assertEquals(2, acc.changeCount());
		System.out.println(acc2);
		// Will attach perthis aspect
		assertEquals(0, acc2.getBalance());
		
		System.out.println(acc2);
		
		// Show that we can get an aspect from the factory and check it
		AuditAspect auditAspect = (AuditAspect) beanFactory.getBean("auditAspect");
		assertFalse("Factory should have used DI to turn quiet off (default is true)",
				auditAspect.getQuiet());
		System.out.println(auditAspect);
		assertSame("Singleton instance", auditAspect, beanFactory.getBean("auditAspect"));
	}

}
