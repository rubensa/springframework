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
 * Simple example of how we can do fine-grained security checks on
 * field writes, using a security manager set using DI.
 * This is a singleton aspect.
 * @author Rod Johnson
 * @version $Id$
 */
public aspect BalanceChangeSecurityAspect {
	
	private SecurityManager securityManager;
	
	/**
	 * Set security manager using DI.
	 * Spring will call this method
	 */
	public void setSecurityManager(SecurityManager securityManager) {
		this.securityManager = securityManager;
	}
	
	private pointcut balanceChanged() : set(int Account.balance);
	
	before() : balanceChanged() {
		this.securityManager.checkAuthorizedToModify();
	}

}
