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

package org.springframework.orm.hibernate;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Constants;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * Base class for HibernateTemplate and HibernateInterceptor, defining common
 * properties like SessionFactory and flushing behavior.
 *
 * <p>Not intended to be used directly. See HibernateTemplate and HibernateInterceptor.
 *
 * @author Juergen Hoeller
 * @since 29.07.2003
 * @see HibernateTemplate
 * @see HibernateInterceptor
 * @see #setFlushMode
 */
public abstract class HibernateAccessor implements InitializingBean, BeanFactoryAware {

	/**
	 * Never flush is a good strategy for read-only units of work.
	 * Hibernate will not track and look for changes in this case,
	 * avoiding any overhead of modification detection.
	 * @see #setFlushMode
	 */
	public static final int FLUSH_NEVER = 0;

	/**
	 * Automatic flushing is the default mode for a Hibernate session.
	 * A session will get flushed on transaction commit or session closing,
	 * and on certain find operations that might involve already modified
	 * instances, but not after each unit of work like with eager flushing.
	 * @see #setFlushMode
	 */
	public static final int FLUSH_AUTO = 1;

	/**
	 * Eager flushing leads to immediate synchronization with the database,
	 * even if in a transaction. This causes inconsistencies to show up and throw
	 * a respective exception immediately, and JDBC access code that participates
	 * in the same transaction will see the changes as the database is already
	 * aware of them then. But the drawbacks are:
	 * <ul>
	 * <li>additional communication roundtrips with the database, instead of a
	 * single batch at transaction commit;
	 * <li>the fact that an actual database rollback is needed if the Hibernate
	 * transaction rolls back (due to already submitted SQL statements).
	 * </ul>
	 * @see #setFlushMode
	 */
	public static final int FLUSH_EAGER = 2;


	/** Constants instance for HibernateAccessor */
	private static final Constants constants = new Constants(HibernateAccessor.class);

	protected final Log logger = LogFactory.getLog(getClass());


	private SessionFactory sessionFactory;

	private Object entityInterceptor;

	private SQLExceptionTranslator jdbcExceptionTranslator;

	private int flushMode = FLUSH_AUTO;

	/**
	 * Just needed for entityInterceptorBeanName.
	 * @see #setEntityInterceptorBeanName
	 */
	private BeanFactory beanFactory;


	/**
	 * Set the Hibernate SessionFactory that should be used to create
	 * Hibernate Sessions.
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the Hibernate SessionFactory that should be used to create
	 * Hibernate Sessions.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Set the bean name of a Hibernate entity interceptor that allows to inspect
	 * and change property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this transaction manager.
	 * <p>Requires the bean factory to be known, to be able to resolve the bean
	 * name to an interceptor instance on session creation. Typically used for
	 * prototype interceptors, i.e. a new interceptor instance per session.
	 * <p>Can also be used for shared interceptor instances, but it is recommended
	 * to set the interceptor reference directly in such a scenario.
	 * @param entityInterceptorBeanName the name of the entity interceptor in
	 * the bean factory
	 * @see #setBeanFactory
	 * @see #setEntityInterceptor
	 */
	public void setEntityInterceptorBeanName(String entityInterceptorBeanName) {
		this.entityInterceptor = entityInterceptorBeanName;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any <b>new</b> Session created by this object.
	 * <p>Such an interceptor can either be set at the SessionFactory level,
	 * i.e. on LocalSessionFactoryBean, or at the Session level, i.e. on
	 * HibernateTemplate, HibernateInterceptor, and HibernateTransactionManager.
	 * It's preferable to set it on LocalSessionFactoryBean or HibernateTransactionManager
	 * to avoid repeated configuration and guarantee consistent behavior in transactions.
	 * @see #setEntityInterceptorBeanName
	 * @see LocalSessionFactoryBean#setEntityInterceptor
	 * @see HibernateTransactionManager#setEntityInterceptor
	 */
	public void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Return the current Hibernate entity interceptor, or null if none.
	 * Resolves an entity interceptor bean name via the bean factory,
	 * if necessary.
	 * @throws IllegalStateException if bean name specified but no bean factory set
	 * @throws org.springframework.beans.BeansException if bean name resolution via the bean factory failed
	 * @see #setEntityInterceptor
	 * @see #setEntityInterceptorBeanName
	 * @see #setBeanFactory
	 */
	public Interceptor getEntityInterceptor() throws IllegalStateException, BeansException {
		if (this.entityInterceptor instanceof String) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("Cannot get entity interceptor via bean name if no bean factory set");
			}
			return (Interceptor) this.beanFactory.getBean((String) this.entityInterceptor, Interceptor.class);
		}
		return (Interceptor) this.entityInterceptor;
	}

	/**
	 * Set the JDBC exception translator for this instance.
	 * Applied to SQLExceptions thrown by callback code, be it direct
	 * SQLExceptions or wrapped Hibernate JDBCExceptions.
	 * <p>The default exception translator is either a SQLErrorCodeSQLExceptionTranslator
	 * if a DataSource is available, or a SQLStateSQLExceptionTranslator else.
	 * @param jdbcExceptionTranslator the exception translator
	 * @see java.sql.SQLException
	 * @see net.sf.hibernate.JDBCException
	 * @see SessionFactoryUtils#newJdbcExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	/**
	 * Return the JDBC exception translator for this instance.
	 * <p>Creates a default SQLErrorCodeSQLExceptionTranslator or SQLStateSQLExceptionTranslator
	 * for the specified SessionFactory, if no exception translator explicitly specified.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator() {
		if (this.jdbcExceptionTranslator == null) {
			this.jdbcExceptionTranslator = SessionFactoryUtils.newJdbcExceptionTranslator(getSessionFactory());
		}
		return this.jdbcExceptionTranslator;
	}

	/**
	 * Set the flush behavior by the name of the respective constant
	 * in this class, e.g. "FLUSH_AUTO". Default is FLUSH_AUTO.
	 * Will get applied to any <b>new</b> Session created by this object.
	 * @param constantName name of the constant
	 * @see #setFlushMode
	 * @see #FLUSH_AUTO
	 */
	public void setFlushModeName(String constantName) {
		setFlushMode(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set the flush behavior to one of the constants in this class.
	 * Default is FLUSH_AUTO. Will get applied to any <b>new</b> Session
	 * created by this object.
	 * @see #setFlushModeName
	 * @see #FLUSH_AUTO
	 */
	public void setFlushMode(int flushMode) {
		this.flushMode = flushMode;
	}

	/**
	 * Return if a flush should be forced after executing the callback code.
	 */
	public int getFlushMode() {
		return flushMode;
	}

	/**
	 * The bean factory just needs to be known for resolving entity interceptor
	 * bean names. It does not need to be set for any other mode of operation.
	 * @see #setEntityInterceptorBeanName
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() {
		if (getSessionFactory() == null) {
			throw new IllegalArgumentException("sessionFactory is required");
		}
	}


	/**
	 * Flush the given Hibernate Session if necessary.
	 * @param session the current Hibernate Session
	 * @param existingTransaction if executing within an existing transaction
	 * @throws HibernateException in case of Hibernate flushing errors
	 */
	protected void flushIfNecessary(Session session, boolean existingTransaction) throws HibernateException {
		if (getFlushMode() == FLUSH_EAGER || (!existingTransaction && getFlushMode() == FLUSH_AUTO)) {
			logger.debug("Eagerly flushing Hibernate session");
			session.flush();
		}
	}

	/**
	 * Convert the given HibernateException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy. Will automatically detect
	 * wrapped SQLExceptions and convert them accordingly.
	 * <p>The default implementation delegates to SessionFactoryUtils
	 * and convertJdbcAccessException. Can be overridden in subclasses.
	 * @param ex HibernateException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #convertJdbcAccessException(net.sf.hibernate.JDBCException)
	 * @see SessionFactoryUtils#convertHibernateAccessException
	 */
	public DataAccessException convertHibernateAccessException(HibernateException ex) {
		if (ex instanceof JDBCException) {
			return convertJdbcAccessException((JDBCException) ex);
		}
		return SessionFactoryUtils.convertHibernateAccessException(ex);
	}

	/**
	 * Convert the given JDBCException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy. Can be overridden in subclasses.
	 * @param ex JDBCException that occured, wrapping a SQLException
	 * @return the corresponding DataAccessException instance
	 * @see #setJdbcExceptionTranslator
	 */
	protected DataAccessException convertJdbcAccessException(JDBCException ex) {
		return getJdbcExceptionTranslator().translate(
				"Hibernate operation: " + ex.getMessage(), null, ex.getSQLException());
	}

	/**
	 * Convert the given SQLException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy. Can be overridden in subclasses.
	 * <p>Note that a direct SQLException can just occur when callback code
	 * performs direct JDBC access via <code>Session.connection()</code>.
	 * @param ex the SQLException
	 * @return the corresponding DataAccessException instance
	 * @see #setJdbcExceptionTranslator
	 * @see org.hibernate.Session#connection()
	 */
	protected DataAccessException convertJdbcAccessException(SQLException ex) {
		return getJdbcExceptionTranslator().translate("Hibernate operation", null, ex);
	}

}
