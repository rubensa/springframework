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

package org.springframework.orm.ojb;

import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBrokerFactory;

import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 * Abstract base class for OJB transaction managers. Provides handling
 * of a PBKey that identifies the PersistenceBroker configuration to use.
 * @author Juergen Hoeller
 * @since 02.07.2004
 * @see OjbAccessor
 */
public abstract class AbstractOjbTransactionManager extends AbstractPlatformTransactionManager {

	private PBKey pbKey = PersistenceBrokerFactory.getDefaultKey();

	/**
	 * Set the JDBC Connection Descriptor alias of the PersistenceBroker
	 * configuration to use. Default is the default connection configured for OJB.
	 */
	public void setJcdAlias(String jcdAlias) {
		this.pbKey = new PBKey(jcdAlias);
	}

	/**
	 * Set the PBKey of the PersistenceBroker configuration to use.
	 * Default is the default connection configured for OJB.
	 */
	public void setPbKey(PBKey pbKey) {
		this.pbKey = pbKey;
	}

	/**
	 * Return the PBKey of the PersistenceBroker configuration used.
	 */
	public PBKey getPbKey() {
		return pbKey;
	}

}
