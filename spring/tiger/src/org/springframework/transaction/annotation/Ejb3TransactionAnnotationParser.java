/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.transaction.annotation;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;

import javax.ejb.ApplicationException;
import javax.ejb.TransactionAttributeType;

import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Strategy implementation for parsing EJB3's {@link javax.ejb.TransactionAttribute}
 * annotation.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class Ejb3TransactionAnnotationParser implements TransactionAnnotationParser, Serializable {

	public TransactionAttribute parseTransactionAnnotation(AnnotatedElement ae) {
		javax.ejb.TransactionAttribute ann = ae.getAnnotation(javax.ejb.TransactionAttribute.class);
		if (ann != null) {
			return new Ejb3TransactionAttribute(ann.value());
		}
		else {
			return null;
		}
	}


	/**
	 * EJB3-specific TransactionAttribute, implementing EJB3's rollback rules
	 * which are based on annotated exceptions.
	 */
	private static class Ejb3TransactionAttribute extends DefaultTransactionDefinition
			implements TransactionAttribute {

		public Ejb3TransactionAttribute(TransactionAttributeType type) {
			setPropagationBehaviorName(PREFIX_PROPAGATION + type.name());
		}

		public boolean rollbackOn(Throwable ex) {
			ApplicationException ann = ex.getClass().getAnnotation(ApplicationException.class);
			return (ann != null ? ann.rollback() : (ex instanceof RuntimeException || ex instanceof Error));
		}
	}

}
