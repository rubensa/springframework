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

package org.springframework.orm.jdo.support;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.jdo.JdoAccessor;
import org.springframework.orm.jdo.PersistenceManagerFactoryUtils;
import org.springframework.orm.jdo.PersistenceManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring web HandlerInterceptor that binds a JDO PersistenceManager to the
 * thread for the entire processing of the request. Intended for the "Open
 * PersistenceManager in View" pattern, i.e. to allow for lazy loading in
 * web views despite the original transactions already being completed.
 *
 * <p>This filter works similar to the AOP JdoInterceptor: It just makes JDO
 * PersistenceManagers available via the thread. It is suitable for
 * non-transactional execution but also for middle tier transactions via
 * JdoTransactionManager or JtaTransactionManager. In the latter case,
 * PersistenceManagers pre-bound by this filter will automatically be used
 * for the transactions.
 *
 * <p>In contrast to OpenPersistenceManagerInViewFilter, this interceptor is set
 * up in a Spring application context and can thus take advantage of bean wiring.
 * It derives from JdoAccessor to inherit common JDO configuration properties.
 *
 * @author Juergen Hoeller
 * @since 12.06.2004
 * @see OpenPersistenceManagerInViewFilter
 * @see org.springframework.orm.jdo.JdoInterceptor
 * @see org.springframework.orm.jdo.JdoTransactionManager
 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#getPersistenceManager
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public class OpenPersistenceManagerInViewInterceptor extends JdoAccessor implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
													 Object handler) throws DataAccessException {
		logger.debug("Opening JDO persistence manager in OpenPersistenceManagerInViewInterceptor");
		PersistenceManager pm = PersistenceManagerFactoryUtils.getPersistenceManager(getPersistenceManagerFactory(), true);
		TransactionSynchronizationManager.bindResource(getPersistenceManagerFactory(), new PersistenceManagerHolder(pm));
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response,
												 Object handler, ModelAndView modelAndView) {
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
															Object handler, Exception ex) throws DataAccessException {
		PersistenceManagerHolder pmHolder =
				(PersistenceManagerHolder) TransactionSynchronizationManager.unbindResource(getPersistenceManagerFactory());
		logger.debug("Closing JDO persistence manager in OpenPersistenceManagerInViewInterceptor");
		PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(pmHolder.getPersistenceManager(),
																																			getPersistenceManagerFactory());
	}

}
