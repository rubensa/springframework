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

package org.springframework.transaction.aspectj;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.MapTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * 
 * @author Rod Johnson
 * @version $Id$
 */
public class TransactionAspectTests extends AbstractTransactionAspectTests2 {
	
	private TransactionAspect ta;
	
	
	protected void setUp() {
		 ta = TestTransactionAspect.aspectOf();
	}
	
	
	protected Object advised(Object o, PlatformTransactionManager ptm, TransactionAttributeSource tas) {		
		ta.setTransactionManager(ptm);
		ta.setTransactionAttributeSource(tas);
		assertSame(tas, ta.getTransactionAttributeSource());
		return o;
	}


}
