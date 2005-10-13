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

package org.springframework.workflow.jbpm;

import java.util.List;

import org.hibernate.HibernateException;
import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * @author Rob Harrop
 */
public class JbpmTemplate implements JbpmOperations {

	private JbpmSessionFactory jbpmSessionFactory;

	private ProcessDefinition processDefinition;

	public JbpmTemplate() {
	}

	public JbpmTemplate(JbpmSessionFactory jbpmSessionFactory) {
		this.jbpmSessionFactory = jbpmSessionFactory;
	}

	public JbpmTemplate(JbpmSessionFactory jbpmSessionFactory, ProcessDefinition processDefinition) {
		this.jbpmSessionFactory = jbpmSessionFactory;
		this.processDefinition = processDefinition;
	}

	public void setJbpmSessionFactory(JbpmSessionFactory jbpmSessionFactory) {
		this.jbpmSessionFactory = jbpmSessionFactory;
	}

	public Long saveProcessInstance(final ProcessInstance processInstance) {
		return (Long) execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				session.getGraphSession().saveProcessInstance(processInstance);
				return new Long(processInstance.getId());
			}
		});
	}

	public ProcessInstance findProcessInstance(final Long processInstanceId) {
		return (ProcessInstance) execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				return session.getGraphSession().loadProcessInstance(processInstanceId.longValue());
			}
		});
	}

	public List findProcessInstances() {
		return (List) execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				return session.getGraphSession().findProcessInstances(processDefinition.getId());
			}
		});
	}

	public void signal(final ProcessInstance processInstance) {
		execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				processInstance.signal();
				return null;
			}
		});
	}

	public void signal(final ProcessInstance processInstance, final String transitionId) {
		execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				processInstance.signal(transitionId);
				return null;
			}
		});
	}

	public void signal(final ProcessInstance processInstance, final Transition transition) {
		execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				processInstance.signal(transition);
				return null;
			}
		});
		throw new UnsupportedOperationException();
	}

	public List findPooledTaskInstances(final String actorId) {
		return (List)execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				return session.getTaskMgmtSession().findPooledTaskInstances(actorId);
			}
		});
	}

	public List findPooledTaskInstances(final List actorIds) {
		return (List)execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				return session.getTaskMgmtSession().findPooledTaskInstances(actorIds);
			}
		});
	}

	public List findTaskInstances(final String actorId) {
		return (List)execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				return session.getTaskMgmtSession().findTaskInstances(actorId);
			}
		});
	}

	public List findTaskInstances(final String[] actorIds) {
		return (List)execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				return session.getTaskMgmtSession().findTaskInstances(actorIds);
			}
		});
	}

	public List findTaskInstances(final List actorIds) {
		return (List)execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				return session.getTaskMgmtSession().findTaskInstances(actorIds);
			}
		});
	}

	public List findTaskInstancesByToken(Token token) {
		return findTaskInstancesByToken(token.getId());
	}

	public List findTaskInstancesByToken(final long tokenId) {
		return (List)execute(new JbpmCallback() {

			public Object doInJbpm(JbpmSession session) {
				return session.getTaskMgmtSession().findTaskInstancesByToken(tokenId);
			}
		});
	}

	public Object execute(JbpmCallback callback) {
		JbpmSession jbpmSession = getSession();
		try {
			return callback.doInJbpm(jbpmSession);
		}
		catch (RuntimeException ex) {
			throw convertJbpmException(ex);
		}
		finally {
			releaseSession(jbpmSession);
		}
	}

	private RuntimeException convertJbpmException(RuntimeException ex) {
		// try to decode and translate HibernateExceptions
		if (ex instanceof HibernateException) {
			return SessionFactoryUtils.convertHibernateAccessException((HibernateException) ex);
		}

		if (ex.getCause() instanceof HibernateException) {
			DataAccessException rootCause = SessionFactoryUtils.convertHibernateAccessException((HibernateException) ex.getCause());
			return new NestedDataAccessException(ex.getMessage(), rootCause);
		}

		// cannot convert the exception in any meaningful way
		return ex;
	}

	protected void releaseSession(JbpmSession jbpmSession) {
		JbpmSessionFactoryUtils.releaseSession(jbpmSession, this.jbpmSessionFactory);
	}

	protected JbpmSession getSession() {
		return JbpmSessionFactoryUtils.getSession(this.jbpmSessionFactory);
	}
}
