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

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

import org.springframework.util.ObjectUtils;

import org.springframework.transaction.interceptor.TransactionAspectSupport;


/**
 * AspectJ transaction aspect extending Spring's TransactionAspectSupport
 * superclass for transaction aspects.
 * <p>Extend this aspect to implement the abstract transactionalOperation()
 * pointcut identifying where transactions should apply.
 * @author Rod Johnson
 * @version $Id$
 */
public abstract aspect TransactionAspect extends TransactionAspectSupport {	
	
	
	protected abstract pointcut transactionalOperation(Object targ);
	
	
	before(Object targ) : transactionalOperation(targ)  {
	
		System.out.println("Before");
		
		Class targetClass = targ.getClass();
		Method m = null;
		
		MethodSignature signature = (MethodSignature) thisJoinPointStaticPart.getSignature();
		
		try {
			// TODO This is going to be slow...
			// Should we get transaction attributes in a totally different way?
			m = targetClass.getMethod(signature.getName(), signature.getParameterTypes());
		}
		catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
		
		System.out.println(m + "; targetClass=" + targetClass);
		
		TransactionInfo txInfo = createTransactionIfNecessary(m, targetClass);
		System.err.println(txInfo);
	}
	
	after(Object targ) throwing (Throwable t) : transactionalOperation(targ) {
		// Cannot be null
		TransactionInfo txInfo = currentTransactionInfo();		
		System.out.println("After throwing " + t);
		doCloseTransactionAfterThrowing(txInfo, t);		
		
		// On exception here??
	}
	
	after (Object targ) returning : transactionalOperation(targ) {			
		System.out.println("After returning...");
		
		// MEans goes before removing stuff
		TransactionInfo txInfo = currentTransactionInfo();
		doCommitTransactionAfterReturning(txInfo);

	}
	
	after(Object targ) : transactionalOperation(targ) {
		
		TransactionInfo txInfo = currentTransactionInfo();
		doFinally(txInfo);
		
	}
	
}
