package org.springframework.workflow.jbpm;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * @author Rob Harrop
 */
public class LocalJbpmSessionFactoryBeanTests extends AbstractTransactionalDataSourceSpringContextTests {

    private JbpmSessionFactory jbpmSessionFactory;

    private SessionFactory hibernateSessionFactory;

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
        Session springSuppliedHibernateSession = SessionFactoryUtils.getSession(this.hibernateSessionFactory, false);

        assertSame("JbpmSession not using Spring-supplied Hibernate session", hibernateSession, springSuppliedHibernateSession);
    }

    protected String[] getConfigLocations() {
        return new String[]{"org/springframework/workflow/jbpm/applicationContext.xml"};
    }
}
