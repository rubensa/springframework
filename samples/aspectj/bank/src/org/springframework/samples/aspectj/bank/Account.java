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
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class Account {
	
	private int balance;
	
	private int overdraft;

	public int withdraw(int amount) throws InsufficientFundsException {
		int newBalance = balance - amount;
		if (newBalance < -overdraft) {
			throw new InsufficientFundsException();
		}
		balance = newBalance;
		return balance;
	}
	
	public int deposit(int amount) {
		balance += amount;
		return balance;
	}
	
	public int getBalance() {
		return balance;
	}


	/**
	 * @return Returns the overdraft.
	 */
	public int getOverdraft() {
		return overdraft;
	}
	/**
	 * @param overdraft The overdraft to set.
	 */
	public void setOverdraft(int overdraft) {
		this.overdraft = overdraft;
	}
	
	public String toString() {
		return "Account: balance=" + balance + "; overdraft=" + overdraft;
	}
}
