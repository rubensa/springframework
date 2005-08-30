package org.springframework.workflow.jbpm;

import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * @author Rob Harrop
 */
public abstract class JbpmSessionFactoryUtils {

    public static JbpmSession getSession(JbpmSessionFactory sessionFactory) {
        Assert.notNull(sessionFactory, "No JbpmSessionFactory specified");

        JbpmSessionHolder jbpmSessionHolder = (JbpmSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

        if (jbpmSessionHolder != null && jbpmSessionHolder.getJbpmSession() != null) {
            return jbpmSessionHolder.getJbpmSession();
        }

        JbpmSession jbpmSession = sessionFactory.openJbpmSession();
        jbpmSessionHolder = new JbpmSessionHolder(jbpmSession);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new SpringJbpmSessionSynchronization(jbpmSessionHolder));
            TransactionSynchronizationManager.bindResource(sessionFactory, jbpmSessionHolder);
        }

        return jbpmSession;
    }

    public static void releaseSession(JbpmSession jbpmSession, JbpmSessionFactory jbpmSessionFactory) {
        if(!isTransactional(jbpmSession, jbpmSessionFactory)) {
           doClose(jbpmSession);
        }

        // todo: need to handle Hibernate sessions that are registered for deferred close
    }

    private static void doClose(JbpmSession jbpmSession) {
        jbpmSession.close();
    }

    private static boolean isTransactional(JbpmSession jbpmSession, JbpmSessionFactory jbpmSessionFactory) {
        JbpmSessionHolder jbpmSessionHolder = (JbpmSessionHolder)TransactionSynchronizationManager.getResource(jbpmSessionFactory);
        return (jbpmSessionHolder != null && jbpmSessionHolder.getJbpmSession() == jbpmSession);
    }

    private static class SpringJbpmSessionSynchronization implements TransactionSynchronization {
        private JbpmSessionHolder jbpmSessionHolder;

        public SpringJbpmSessionSynchronization(JbpmSessionHolder jbpmSessionHolder) {
            this.jbpmSessionHolder = jbpmSessionHolder;
        }

        public void suspend() {

        }

        public void resume() {

        }

        public void beforeCommit(boolean readOnly) {

        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int status) {
            JbpmSession jbpmSession = this.jbpmSessionHolder.getJbpmSession();
            jbpmSession.getSession().close();
            this.jbpmSessionHolder.clear();
        }
    }
}
