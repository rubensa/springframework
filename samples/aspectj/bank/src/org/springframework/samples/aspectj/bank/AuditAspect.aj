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

package org.springframework.samples.aspectj.bank;


/**
 * Demonstrates how a singleton aspect can introduce additional state into advised objects.
 * Whereas the only option in Spring's own AOP framework is to have a distinct mixin
 * object per advised target, AspectJ allows us to add additional fields and methods
 * to the advised object itself. This means that the aspect doesn't need to be threadsafe:
 * it's not holding the state itself.
 * <p>
 * This aspect makes Account implement the Audited interface, adding a numberOfChanges
 * field to Account.
 * <p>
 * The "quiet" property is configured by Springt using Dependency Injection.
 * Note that only Setter Injection is available to configure aspects, as there is no constructor.
 * We obtain aspect instances using the static aspectOf() method, and can then invoke setter methods
 * on them.
 * @author Rod Johnson
 * @version $Id$
 */
public aspect AuditAspect {
	
	declare parents : Account implements Audited;
	
	private boolean quiet = true;
	
	public void setQuiet(boolean quiet) {
		System.out.println("Dependency Injection used on AuditAspect to set quiet=" + quiet);
		this.quiet = quiet;
	}
	
	public boolean getQuiet() {
		return this.quiet;
	}
	
	private int Account.numberOfChanges;
	
	
	public pointcut stateChanges(Account account) :
			(execution (int Account.withdraw(int)) || execution(int Account.deposit(int))) &&
			target(account);
	
	
	after(Account account) : stateChanges(account) {
		if (!quiet) {
			System.out.println("State change detected on account");
		}
		++account.numberOfChanges;
	}
	
	public int Account.changeCount() {
		return numberOfChanges;
	}

	public String toString() {
		return super.toString() + "; quiet=" + quiet;
	}

}
