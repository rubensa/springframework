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
 * Singleton aspect...configured by Spring
 * @author Rod Johnson
 * @version $Id: AbstractVetoableChangeListener.java,v 1.1.1.1 2003/08/14 16:20:14 trisberg Exp $
 */
public aspect WithdrawalAspect {
	
	private int threshold;
	
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	
	
	public pointcut withdrawals(Account account, int amount) :
		execution (int Account.withdraw(int)) &&
			target(account) &&
			args (amount);
	
	before(Account account, int amount) : withdrawals(account, amount) {
		System.out.println("Want to withdraw " + amount + " from " + account + 
				" my threshold (set by DI) is " + threshold);
	}

}
