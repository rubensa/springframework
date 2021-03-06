
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
public aspect PerThis perthis(newAdvised(Account)) {
		
		//newAdvised(Account)) {
	
	private String greeting;
	
	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}
	
	public pointcut newAdvised(Account account) :
		execution (Account.new()) && target(account);
	
	private pointcut accountBalance() : 
		execution (int Account.getBalance());
	
	
	String around (Account account) : execution (String Account.toString()) && target(account)  {
		return proceed(account) + " accountHc=" + account.hashCode() + "; " + greeting + " mixin=" + this;
	}

}
