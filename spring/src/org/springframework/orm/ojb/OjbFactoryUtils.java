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

import org.apache.ojb.broker.OJBRuntimeException;
import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper class featuring methods for OJB PersistenceBroker handling,
 * allowing for reuse of PersistenceBroker instances within transactions.
 *
 * <p>Used by PersistenceBrokerTemplate and PersistenceBrokerTransactionManager.
 * Can also be used directly in application code.
 *
 * @author Juergen Hoeller
 * @since 02.07.2004
 */
public abstract class OjbFactoryUtils {

	/**
	 * Get an OJB PersistenceBroker for the given PBKey. Is aware of a
	 * corresponding PersistenceBroker bound to the current thread, for
	 * example when using PersistenceBrokerTransactionManager. Will
	 * create a new PersistenceBroker else.
	 * @param pbKey PBKey to create the PersistenceBroker for
	 * @return the PersistenceManager
	 * @throws DataAccessResourceFailureException if the PersistenceManager couldn't be created
	 */
	public static PersistenceBroker getPersistenceBroker(PBKey pbKey)
	    throws DataAccessResourceFailureException {

		PersistenceBrokerHolder pbHolder =
		    (PersistenceBrokerHolder) TransactionSynchronizationManager.getResource(pbKey);
		if (pbHolder != null) {
			return pbHolder.getPersistenceBroker();
		}

		try {
			PersistenceBroker pb = PersistenceBrokerFactory.createPersistenceBroker(pbKey);
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				pbHolder = new PersistenceBrokerHolder(pb);
				TransactionSynchronizationManager.bindResource(pbKey, pbHolder);
				TransactionSynchronizationManager.registerSynchronization(
				    new PersistenceBrokerSynchronization(pbHolder, pbKey));
			}
			return pb;
		}
		catch (OJBRuntimeException ex) {
			throw new DataAccessResourceFailureException("Could not open OJB persistence broker", ex);
		}
	}

	/**
	 * Close the given PersistenceBroker, created for the given PBKey,
	 * if it isn't bound to the thread.
	 * @param pb PersistenceBroker to close
	 * @param pbKey PBKey that the PersistenceBroker was created with
	 */
	public static void closePersistenceBrokerIfNecessary(PersistenceBroker pb, PBKey pbKey) {
		if (pb == null || TransactionSynchronizationManager.hasResource(pbKey)) {
			return;
		}
		pb.close();
	}


	/**
	 * Callback for resource cleanup at the end of a non-OJB transaction
	 * (e.g. when participating in a JTA transaction).
	 */
	private static class PersistenceBrokerSynchronization extends TransactionSynchronizationAdapter {

		private final PersistenceBrokerHolder persistenceBrokerHolder;

		private final PBKey pbKey;

		private PersistenceBrokerSynchronization(PersistenceBrokerHolder pbHolder, PBKey pbKey) {
			this.persistenceBrokerHolder = pbHolder;
			this.pbKey = pbKey;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.pbKey);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.pbKey, this.persistenceBrokerHolder);
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.pbKey);
			closePersistenceBrokerIfNecessary(this.persistenceBrokerHolder.getPersistenceBroker(), this.pbKey);
		}
	}

}
