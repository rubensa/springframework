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

package org.springframework.test.context.transaction;

import org.springframework.core.style.ToStringCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

/**
 * <p>
 * Configuration attributes for configuring transactional tests.
 * </p>
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 2.1
 * @see TransactionConfiguration
 */
public class TransactionConfigurationAttributes {

	private final String transactionManagerName;

	private final boolean defaultRollback;


	/**
	 * Constructs a new TransactionConfigurationAttributes instance from the
	 * supplied arguments.
	 *
	 * @param transactionManagerName The bean name of the
	 * {@link PlatformTransactionManager} that is to be used to drive
	 * transactions.
	 * @param defaultRollback Boolean flag denoting whether or not transactions
	 * should be rolled back by default.
	 * @throws IllegalArgumentException if the supplied transaction manager bean
	 * name is <code>null</code>.
	 */
	public TransactionConfigurationAttributes(final String transactionManagerName, final boolean defaultRollback) {

		Assert.notNull(transactionManagerName, "transactionManagerName can not be null.");
		this.transactionManagerName = transactionManagerName;
		this.defaultRollback = defaultRollback;
	}


	/**
	 * Gets the bean name of the {@link PlatformTransactionManager} that is to
	 * be used to drive transactions.
	 *
	 * @return The transaction manager bean name.
	 */
	public final String getTransactionManagerName() {

		return this.transactionManagerName;
	}

	/**
	 * Whether or not transactions should be rolled back by default.
	 *
	 * @return The <em>default rollback</em> flag.
	 */
	public final boolean isDefaultRollback() {

		return this.defaultRollback;
	}

	@Override
	public String toString() {

		return new ToStringCreator(this)
				.append("transactionManagerName", this.transactionManagerName)
				.append("defaultRollback", this.defaultRollback)
				.toString();
	}

}
