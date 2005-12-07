/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.orm.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

/**
 * Base class for JpaTemplate and JpaInterceptor, defining common
 * properties like EntityManagerFactory and flushing behavior.
 *
 * <p>Note: With JPA, modifications to persistent objects are just possible
 * within a transaction (in contrast to Hibernate). Therefore, eager flushing
 * will just get applied when in a transaction. Furthermore, there is no
 * explicit notion of flushing never, as this would not imply a performance
 * gain due to JPA's field interception mechanism that doesn't involve
 * the overhead of snapshot comparisons.
 *
 * <p>Eager flushing is just available for specific JPA implementations.
 * You need to a corresponding JpaDialect to make eager flushing work.
 *
 * <p>Not intended to be used directly. See JpaTemplate and JpaInterceptor.
 *
 * @author Juergen Hoeller
 * @since 1.3
 * @see JpaTemplate
 * @see JpaInterceptor
 * @see #setFlushEager
 */
public abstract class JpaAccessor implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private EntityManagerFactory entityManagerFactory;

	private EntityManager entityManager;

	private JpaDialect jpaDialect = new DefaultJpaDialect();

	private boolean flushEager = false;


	/**
	 * Set the JPA EntityManagerFactory that should be used to create
	 * EntityManagers.
	 */
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.entityManagerFactory = emf;
	}

	/**
	 * Return the JPA EntityManagerFactory that should be used to create
	 * EntityManagers.
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	/**
	 * Set the JPA EntityManager to use.
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Return the JPA EntityManager to use.
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Set the JPA dialect to use for this accessor.
	 * <p>The dialect object can be used to retrieve the underlying JDBC
	 * connection and to eagerly flush changes to the database.
	 */
	public void setJpaDialect(JpaDialect jpaDialect) {
		this.jpaDialect = (jpaDialect != null ? jpaDialect : new DefaultJpaDialect());
	}

	/**
	 * Return the JPA dialect to use for this accessor.
	 * <p>Creates a default one for the specified EntityManagerFactory if none set.
	 */
	public JpaDialect getJpaDialect() {
		return this.jpaDialect;
	}

	/**
	 * Set if this accessor should flush changes to the database eagerly.
	 * <p>Eager flushing leads to immediate synchronization with the database,
	 * even if in a transaction. This causes inconsistencies to show up and throw
	 * a respective exception immediately, and JDBC access code that participates
	 * in the same transaction will see the changes as the database is already
	 * aware of them then. But the drawbacks are:
	 * <ul>
	 * <li>additional communication roundtrips with the database, instead of a
	 * single batch at transaction commit;
	 * <li>the fact that an actual database rollback is needed if the JPA
	 * transaction rolls back (due to already submitted SQL statements).
	 * </ul>
	 */
	public void setFlushEager(boolean flushEager) {
		this.flushEager = flushEager;
	}

	/**
	 * Return if this accessor should flush changes to the database eagerly.
	 */
	public boolean isFlushEager() {
		return flushEager;
	}

	/**
	 * Eagerly initialize the JPA dialect, creating a default one
	 * for the specified EntityManagerFactory if none set.
	 */
	public void afterPropertiesSet() {
		if (getEntityManagerFactory() == null && getEntityManager() == null) {
			throw new IllegalArgumentException("entityManagerFactory or entityManager is required");
		}
		getJpaDialect();
	}


	/**
	 * Flush the given JPA entity manager if necessary.
	 * @param em the current JPA PersistenceManage
	 * @param existingTransaction if executing within an existing transaction
	 * @throws javax.persistence.PersistenceException in case of JPA flushing errors
	 */
	public void flushIfNecessary(EntityManager em, boolean existingTransaction) throws PersistenceException {
		if (this.flushEager) {
			logger.debug("Eagerly flushing JPA entity manager");
			em.flush();
		}
	}

	/**
	 * Convert the given PersistenceException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy. Delegates to the JpaDialect
	 * if set, falling back to EntityManagerFactoryUtils' standard exception
	 * translation else.
	 * <p>May be overridden in subclasses.
	 * @param ex PersistenceException that occured
	 * @return the corresponding DataAccessException instance
	 * @see JpaDialect#translateException
	 * @see EntityManagerFactoryUtils#convertJpaAccessException
	 */
	public DataAccessException convertJpaAccessException(PersistenceException ex) {
		return getJpaDialect().translateException(ex);
	}

}
