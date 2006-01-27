package org.springframework.workflow.jbpm;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * @author Rob Harrop
 */
public class LocalJbpmSessionFactoryBeanTests extends AbstractTransactionalDataSourceSpringContextTests {

	private JbpmSessionFactory jbpmSessionFactory;

	private SessionFactory hibernateSessionFactory;

	private ProcessDefinition processDefinition;

	/**
	 * @param processDefinition The processDefinition to set.
	 */
	public void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}

	public void setJbpmSessionFactory(JbpmSessionFactory jbpmSessionFactory) {
		this.jbpmSessionFactory = jbpmSessionFactory;
	}

	public void setHibernateSessionFactory(SessionFactory hibernateSessionFactory) {
		this.hibernateSessionFactory = hibernateSessionFactory;
	}

	public void testGetSession() {
		JbpmSession session = this.jbpmSessionFactory.openJbpmSession();
		assertNotNull("JbpmSession should not be null", session);

		Session hibernateSession = session.getSession();
		Session springSuppliedHibernateSession = SessionFactoryUtils.getSession(this.hibernateSessionFactory,
				false);

		assertSame("JbpmSession not using Spring-supplied Hibernate session", hibernateSession,
				springSuppliedHibernateSession);

		hibernateSession = null;
		session.close();

	}

	public void testJbpmCloseSession() throws Exception {
		JbpmSession session = this.jbpmSessionFactory.openJbpmSession();

		Session springSession = SessionFactoryUtils.getSession(hibernateSessionFactory, false);
		// session is opened
		assertTrue(session.getSession().isOpen());
		// same session returned by current jbpm session
		assertSame(session.getSession(), JbpmSession.getCurrentJbpmSession().getSession());
		// same session as the one thread-bounded by Spring
		assertSame(springSession, session.getSession());

		// close jbpm session
		session.close();

		// jbpm session nullified
		assertNull(session.getSession());
		// current jbpm session (jBPM code) is null
		assertNull(JbpmSession.getCurrentJbpmSession());

		// thread-bound session not null and opened
		assertTrue(springSession.isOpen());
	}

	public void testSpringHandler() throws Exception {
		Action action = processDefinition.getAction("myAction");
		ActionHandler delegate = (ActionHandler) action.getActionDelegation().getInstance();

		// create the context and pass it on to the action
		ProcessInstance instance = processDefinition.createProcessInstance();
		// we have to use transient variables or otherwise HB will get in the way
		instance.getContextInstance().setTransientVariable(DummyActionHandler.TEST_LABEL,
				applicationContext.getBean("jbpmAction"));
		Token token = instance.getRootToken();

		delegate.execute(new ExecutionContext(token));
	}

	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/workflow/jbpm/applicationContext.xml" };
	}
}
