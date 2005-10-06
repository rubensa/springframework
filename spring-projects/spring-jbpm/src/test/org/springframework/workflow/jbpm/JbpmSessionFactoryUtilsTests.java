package org.springframework.workflow.jbpm;

import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Rob Harrop
 */
public class JbpmSessionFactoryUtilsTests extends AbstractDependencyInjectionSpringContextTests {

    private JbpmSessionFactory jbpmSessionFactory;

    private PlatformTransactionManager transactionManager;

    public void setJbpmSessionFactory(JbpmSessionFactory jbpmSessionFactory) {
        this.jbpmSessionFactory = jbpmSessionFactory;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void testGetSessionOutsideTransaction() {
        JbpmSession firstJbpmSession = JbpmSessionFactoryUtils.getSession(this.jbpmSessionFactory);
        assertNotNull(firstJbpmSession);

        JbpmSession secondJbpmSession = JbpmSessionFactoryUtils.getSession(this.jbpmSessionFactory);
        assertNotNull(secondJbpmSession);

				assertNotSame(firstJbpmSession, secondJbpmSession);
    }

    public void testGetSessionInsideTransaction() {

        final Map sessionMap = new HashMap();

        TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
        transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                JbpmSession firstSession = JbpmSessionFactoryUtils.getSession(jbpmSessionFactory);
                JbpmSession secondSession = JbpmSessionFactoryUtils.getSession(jbpmSessionFactory);
                sessionMap.put("first", firstSession);
                sessionMap.put("second", secondSession);

                // try to close inside a transaction
                JbpmSessionFactoryUtils.releaseSession(firstSession, jbpmSessionFactory);
                assertTrue("Should not close session inside a transaction.", firstSession.getSession().isOpen());
                return null;
            }
        });

        JbpmSession first = (JbpmSession)sessionMap.get("first");
        JbpmSession second = (JbpmSession)sessionMap.get("second");

        assertSame("Should've got the same session twice inside the transaction", first, second);
        assertFalse("Session not closed in synchronization", first.getSession().isOpen());
    }

    public void testReleaseOutsideTransaction() {
        JbpmSession jbpmSession = JbpmSessionFactoryUtils.getSession(this.jbpmSessionFactory);
        JbpmSessionFactoryUtils.releaseSession(jbpmSession, this.jbpmSessionFactory);
        assertNull("Session should be closed", jbpmSession.getSession());
    }


    protected String[] getConfigLocations() {
        return new String[]{"org/springframework/workflow/jbpm/applicationContext.xml"};
    }


}
