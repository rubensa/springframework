package org.springframework.orm.hibernate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.type.Type;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper class that simplifies Hibernate data access code, and converts
 * checked HibernateExceptions into unchecked DataAccessExceptions,
 * compatible to the org.springframework.dao exception hierarchy.
 * Uses the same SQLExceptionTranslator mechanism as JdbcTemplate.
 *
 * <p>Typically used to implement data access or business logic services that
 * use Hibernate within their implementation but are Hibernate-agnostic in
 * their interface. The latter resp. code calling the latter only have to deal
 * with business objects, query objects, and org.springframework.dao exceptions.
 *
 * <p>The central method is "execute", supporting Hibernate code implementing
 * the HibernateCallback interface. It provides Hibernate Session handling
 * such that neither the HibernateCallback implementation nor the calling
 * code needs to explicitly care about retrieving/closing Hibernate Sessions,
 * or handling Session lifecycle exceptions. For typical single step actions,
 * there are various convenience methods (find, load, saveOrUpdate, delete).
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a SessionFactory reference, or get prepared in an application context
 * and given to services as bean reference. Note: The SessionFactory should
 * always be configured as bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>This class can be considered a programmatic alternative to
 * HibernateInterceptor. The major advantage is its straightforwardness, the
 * major disadvantage that no checked application exceptions can get thrown
 * from within data access code. Respective checks and the actual throwing of
 * such exceptions can often be deferred to after callback execution, though. 
 *
 * <p>Note that even if HibernateTransactionManager is used for transaction
 * demarcation in higher-level services, all those services above the data
 * access layer don't need need to be Hibernate-aware. Setting such a special
 * PlatformTransactionManager is a configuration issue, without introducing
 * code dependencies. For example, switching to JTA is just a matter of
 * Spring configuration (use JtaTransactionManager instead), without needing
 * to touch application code.
 *
 * <p>LocalSessionFactoryBean is the preferred way of obtaining a reference
 * to a specific Hibernate SessionFactory, at least in a non-EJB environment.
 *
 * <p>Note: Spring's Hibernate support requires Hibernate 2.1 (as of Spring 1.0).
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateCallback
 * @see HibernateInterceptor
 * @see HibernateTransactionManager
 * @see LocalSessionFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see net.sf.hibernate.Session
 */
public class HibernateTemplate extends HibernateAccessor implements HibernateOperations {

	private boolean allowCreate = true;

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
		setSessionFactory(sessionFactory);
		afterPropertiesSet();
	}

	/**
	 * Create a new HibernateTemplate instance.
	 * @param sessionFactory SessionFactory to create Sessions
	 * @param allowCreate if a new Session should be created
	 * if no thread-bound found
	 */
	public HibernateTemplate(SessionFactory sessionFactory, boolean allowCreate) {
		setSessionFactory(sessionFactory);
		setAllowCreate(allowCreate);
		afterPropertiesSet();
	}

	/**
	 * Set if a new Session should be created if no thread-bound found.
	 * <p>HibernateTemplate is aware of a respective Session bound to the
	 * current thread, for example when using HibernateTransactionManager.
	 * If allowCreate is true, a new Session will be created if none found.
	 * If false, an IllegalStateException will get thrown in this case.
	 * @see SessionFactoryUtils#getSession(SessionFactory, boolean)
	 */
	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

	/**
	 * Return if a new Session should be created if no thread-bound found.
	 */
	public boolean isAllowCreate() {
		return allowCreate;
	}

	/**
	 * Execute the action specified by the given action object within a session.
	 * Application exceptions thrown by the action object get propagated to the
	 * caller (can only be unchecked). Hibernate exceptions are transformed into
	 * appropriate DAO ones. Allows for returning a result object, i.e. a domain
	 * object or a collection of domain objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like HibernateTransactionManager.
	 * Generally, callback code must not touch any Session lifecycle methods,
	 * like close, disconnect, or reconnect, to let the template do its work.
	 * @param action callback object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of Hibernate errors
	 * @see HibernateTransactionManager
	 * @see org.springframework.dao
	 * @see org.springframework.transaction
	 */
	public Object execute(HibernateCallback action) throws DataAccessException {
		Session session = (!this.allowCreate ?
				SessionFactoryUtils.getSession(getSessionFactory(), false) :
				SessionFactoryUtils.getSession(getSessionFactory(), getEntityInterceptor(),
																			 getJdbcExceptionTranslator()));
		boolean existingTransaction = TransactionSynchronizationManager.hasResource(getSessionFactory());
		if (!existingTransaction && getFlushMode() == FLUSH_NEVER) {
			session.setFlushMode(FlushMode.NEVER);
		}
		try {
			Object result = action.doInHibernate(session);
			flushIfNecessary(session, existingTransaction);
			return result;
		}
		catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
		catch (SQLException ex) {
			throw convertJdbcAccessException(ex);
		}
		catch (RuntimeException ex) {
			// callback code threw application exception
			throw ex;
		}
		finally {
			SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
		}
	}

	/**
	 * Execute the specified action assuming that the result object is a List.
	 * <p>This is a convenience method for executing Hibernate find calls
	 * within an action.
	 * @param action action object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of Hibernate errors
	 */
	public List executeFind(HibernateCallback action) throws DataAccessException {
		return (List) execute(action);
	}


	//-------------------------------------------------------------------------
	// Convenience methods for load, save, update, delete
	//-------------------------------------------------------------------------

	public Object get(final Class entityClass, final Serializable id) throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.get(entityClass, id);
			}
		});
	}

	public Object get(final Class entityClass, final Serializable id, final LockMode lockMode)
			throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.get(entityClass, id, lockMode);
			}
		});
	}

	public Object load(final Class entityClass, final Serializable id) throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.load(entityClass, id);
			}
		});
	}

	public Object load(final Class entityClass, final Serializable id, final LockMode lockMode)
			throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.load(entityClass, id, lockMode);
			}
		});
	}
	
	public List loadAll(final Class entityClass) throws DataAccessException {
		return find("from " + entityClass.getName());
	}

	public void evict(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.evict(entity);
				return null;
			}
		});
	}

	public void lock(final Object entity, final LockMode lockMode) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.lock(entity, lockMode);
				return null;
			}
		});
	}

	public Serializable save(final Object entity) throws DataAccessException {
		return (Serializable) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.save(entity);
			}
		});
	}

	public void save(final Object entity, final Serializable id) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.save(entity, id);
				return null;
			}
		});
	}

	public void saveOrUpdate(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.saveOrUpdate(entity);
				return null;
			}
		});
	}

	public Object saveOrUpdateCopy(final Object entity) throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.saveOrUpdateCopy(entity);
			}
		});
	}

	public void update(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.update(entity);
				return null;
			}
		});
	}

	public void update(final Object entity, final LockMode lockMode) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.update(entity);
				session.lock(entity, lockMode);
				return null;
			}
		});
	}

	public void delete(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.delete(entity);
				return null;
			}
		});
	}

	public void delete(final Object entity, final LockMode lockMode) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.lock(entity, lockMode);
				session.delete(entity);
				return null;
			}
		});
	}

	public void deleteAll(final Collection entities) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				for (Iterator it = entities.iterator(); it.hasNext();) {
					session.delete(it.next());
				}
				return null;
			}
		});
	}


	//-------------------------------------------------------------------------
	// Convenience finder methods
	//-------------------------------------------------------------------------

	public List find(final String queryString) throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				return queryObject.list();
			}
		});
	}

	public List find(final String queryString, final Object value) throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				queryObject.setParameter(0, value);
				return queryObject.list();
			}
		});
	}

	public List find(final String queryString, final Object value, final Type type)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				queryObject.setParameter(0, value, type);
				return queryObject.list();
			}
		});
	}

	public List find(final String queryString, final Object[] values) throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					queryObject.setParameter(i, value);
				}
				return queryObject.list();
			}
		});
	}

	public List find(final String queryString, final Object[] values, final Type[] types)
			throws DataAccessException {
		if (values.length != types.length) {
			throw new IllegalArgumentException("Length of values array must match length of types array");
		}
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					queryObject.setParameter(i, value, types[i]);
				}
				return queryObject.list();
			}
		});
	}

	public List findByValueBean(final String queryString, final Object valueBean)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = createQuery(session, queryString);
				queryObject.setProperties(valueBean);
				return queryObject.list();
			}
		});
	}

	public List findByNamedQuery(final String queryName) throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				return queryObject.list();
			}
		});
	}

	public List findByNamedQuery(final String queryName, final Object value)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				queryObject.setParameter(0, value);
				return queryObject.list();
			}
		});
	}

	public List findByNamedQuery(final String queryName, final Object value, final Type type)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				queryObject.setParameter(0, value, type);
				return queryObject.list();
			}
		});
	}

	public List findByNamedQuery(final String queryName, final Object[] values)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					queryObject.setParameter(i, value);
				}
				return queryObject.list();
			}
		});
	}

	public List findByNamedQuery(final String queryName, final Object[] values, final Type[] types)
			throws DataAccessException {
		if (values.length != types.length) {
			throw new IllegalArgumentException("Length of values array must match length of types array");
		}
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				for (int i = 0; i < values.length; i++) {
					Object value = values[i];
					queryObject.setParameter(i, value, types[i]);
				}
				return queryObject.list();
			}
		});
	}

	public List findByNamedQueryAndValueBean(final String queryName, final Object valueBean)
			throws DataAccessException {
		return executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				queryObject.setProperties(valueBean);
				return queryObject.list();
			}
		});
	}
	

	/**
	 * Create a Query object for the given Session and the given query string.
	 * Applies a transaction timeout, if any. To be used within a HibernateCallback.
	 * @param session current Hibernate Session
	 * @param queryString the HQL query string
	 * @return the Query object
	 * @throws HibernateException if the query could not be created
	 */
	public Query createQuery(Session session, String queryString) throws HibernateException {
		Query queryObject = session.createQuery(queryString);
		SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
		return queryObject;
	}

	/**
	 * Create a named Query object for the given Session and the given query name.
	 * Applies a transaction timeout, if any. To be used within a HibernateCallback.
	 * @param session current Hibernate Session
	 * @param queryName the name of the query in the Hibernate mapping file
	 * @return the Query object
	 * @throws HibernateException if the query could not be created
	 */
	public Query getNamedQuery(Session session, String queryName) throws HibernateException {
		Query queryObject = session.getNamedQuery(queryName);
		SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
		return queryObject;
	}

}
