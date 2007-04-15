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

package org.springframework.jms.config;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * A stub transaction manager for testing.
 * 
 * @author Mark Fisher
 */
public class StubTransactionManager implements PlatformTransactionManager {

	public void commit(TransactionStatus status) throws TransactionException {
	}

	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		return null;
	}

	public void rollback(TransactionStatus status) throws TransactionException {
	}

}
