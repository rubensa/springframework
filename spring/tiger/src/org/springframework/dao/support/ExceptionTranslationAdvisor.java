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
 */

package org.springframework.dao.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

/**
 * Spring AOP exception translation access for use at Repository or DAO layer level.
 * @author Rod Johnson
 */
public class ExceptionTranslationAdvisor extends DefaultPointcutAdvisor {
	
	private static ExceptionTranslationAdvisor instance = new ExceptionTranslationAdvisor();
	
	public static ExceptionTranslationAdvisor getInstance() {
		return instance;
	}
	
	private ExceptionTranslationAdvisor() {
		super(
				new RepositoryAnnotationMatchingPointcut(),
				new ExceptionTranslationInterceptor()
		);
	}
	
	private static class ExceptionTranslationInterceptor implements MethodInterceptor {
		public Object invoke(MethodInvocation mi) throws Throwable {
			try {
				return mi.proceed();
			}
			catch (RuntimeException ex) {
				DataAccessException dex = null;
				if (dex != null) {
					throw dex;
				}
				else {
					throw ex;
				}
			}
			finally {
				
			}
		}
	}
	
	private static class RepositoryAnnotationMatchingPointcut implements Pointcut {

		public ClassFilter getClassFilter() {
			return new ClassFilter() {
				public boolean matches(java.lang.Class clazz) {
					return clazz.isAnnotationPresent(Repository.class);
				}
			};
		}

		public MethodMatcher getMethodMatcher() {
			return MethodMatcher.TRUE;
		}
		
	}
	
}
