package com.interface21.orm.hibernate;

import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interface21.dao.DataAccessException;

/**
 * Helper class that simplifies Hibernate data access code, and converts
 * checked HibernateExceptions into unchecked HibernateDataAccessExceptions,
 * compatible to the com.interface21.dao exception hierarchy.
 *
 * <p>The central method is "execute", supporting Hibernate code implementing
 * the HibernateCallback interface. It provides Hibernate Session handling
 * such that neither the HibernateCallback implementation nor the calling
 * code needs to explicitly care about retrieving/closing Hibernate Sessions,
 * or handling Session lifecycle exceptions.
 *
 * <p>Typically used to implement data access or business logic services that
 * use Hibernate within their implementation but are Hibernate-agnostic in
 * their interface. The latter resp. code calling the latter only have to deal
 * with business objects, query objects, and com.interface21.dao exceptions.
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a SessionFactory reference, or get prepared in an application context
 * and given to services as bean reference. Note: The SessionFactory should
 * always be configured as bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>Note that even if HibernateTransactionManager is used for transaction
 * demarcation in higher-level services, all those services above the data
 * access layer don't need need to be Hibernate-aware. Setting such a special
 * PlatformTransactionManager is a configuration issue, without introducing
 * a code dependency. For example, switching to JTA is just a matter of
 * Spring and Hibernate configuration, without touching applicaiton code.
 *
 * <p>Registering Hibernate with JNDI is only advisable when using
 * Hibernate's JCA Connector, i.e. when the application server cares for
 * initialization and JTA notification. Else, portability is rather limited:
 * Manual JNDI binding isn't properly supported by all application servers
 * (e.g. Tomcat), and manual JTA integration involves a container-specific
 * JTA TransactionManager lookup.
 *
 * <p>Note: This class, like all of Spring's Hibernate support, requires
 * Hibernate 2.0 (initially developed with RC1).
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateCallback
 * @see HibernateTransactionManager
 * @see LocalSessionFactoryBean
 * @see com.interface21.jndi.JndiObjectFactoryBean
 * @see com.interface21.jndi.JndiObjectEditor
 */
public class HibernateTemplate {

	protected final Log logger = LogFactory.getLog(getClass());

	private SessionFactory sessionFactory;

	private boolean forceFlush;

	/**
	 * Create a new HibernateTemplate instance.
	 */
	public HibernateTemplate() {
	}

	/**
	 * Create a new HibernateTemplate instance.
	 * @param sessionFactory SessionFactory to create Sessions
	 */
	public HibernateTemplate(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

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
	 * If a flush of the Hibernate Session should be forced after executing the
	 * callback code. By default, the template will only trigger a flush if not in
	 * a Hibernate transaction, as a final flush will occur on commit anyway.
	 * <p>A forced flush leads to immediate synchronization with the database,
	 * even if in a Hibernate transaction. This causes inconsistencies to show up
	 * and throw a respective exception immediately. But the drawbacks are:
	 * <ul>
	 * <li>additional communication roundtrips with the database, instead of a
	 * single batch at transaction commit;
	 * <li>the fact that an actual database rollback is needed if the Hibernate
	 * transaction rolls back (due to already submitted SQL statements).
	 * </ul>
	 */
	public void setForceFlush(boolean forceFlush) {
		this.forceFlush = forceFlush;
	}

	/**
	 * Return if a flush should be forced after executing the callback code.
	 */
	public boolean isForceFlush() {
		return forceFlush;
	}

	/**
	 * Execute the specified action assuming that the result object is a List.
	 * This is a convenience method for executing Hibernate find calls within
	 * an action.
	 * @param action action object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of Hibernate errors
	 * @throws RuntimeException in case of application exceptions thrown by
	 * the action object
	 */
	public List executeFind(HibernateCallback action) throws DataAccessException, RuntimeException {
		return (List) execute(action);
	}

	/**
	 * Executes the action specified by the given action object within a session.
	 * Application exceptions thrown by the action object get propagated to the
	 * caller, Hibernate exceptions are transformed into appropriate DAO ones.
	 * Allows for returning a result object created within the transaction,
	 * i.e. a business object or a collection of business objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like HibernateTransactionManager.
	 * @param action action object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of Hibernate errors
	 * @throws RuntimeException in case of application exceptions thrown by
	 * the action object
	 * @see HibernateTransactionManager
	 * @see com.interface21.dao
	 * @see com.interface21.transaction
	 */
	public Object execute(HibernateCallback action) throws DataAccessException, RuntimeException {
		Session session = SessionFactoryUtils.openSession(this.sessionFactory);
		try {
			Object result = action.doInHibernate(session);
			if (this.forceFlush || !SessionFactoryUtils.isSessionBoundToThread(session, this.sessionFactory)) {
				session.flush();
			}
			return result;
		}
		catch (HibernateException ex) {
			logger.error("Callback code threw Hibernate exception", ex);
			throw SessionFactoryUtils.convertHibernateAccessException(ex);
		}
		catch (RuntimeException ex) {
			logger.error("Callback code threw application exception", ex);
			throw ex;
		}
		finally {
			SessionFactoryUtils.closeSessionIfNecessary(session, this.sessionFactory);
		}
	}

}
