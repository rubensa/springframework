package org.springframework.workflow.jbpm;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.Connection;

/**
 * @author Rob Harrop
 */
public class LocalJbpmSessionFactoryBean implements FactoryBean, InitializingBean {

    private JbpmSessionFactory sessionFactory;

    private SessionFactory hibernateSessionFactory;

    private Configuration hibernateConfiguration;

    public void setHibernateSessionFactory(SessionFactory hibernateSessionFactory) {
        this.hibernateSessionFactory = hibernateSessionFactory;
    }

    public void setHibernateConfiguration(Configuration hibernateConfiguration) {
        this.hibernateConfiguration = hibernateConfiguration;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.hibernateConfiguration == null) {
            throw new FatalBeanException("Property [hibernateConfiguration] of [" + LocalJbpmSessionFactoryBean.class + "] is required.");
        }

        if (this.hibernateSessionFactory == null) {
            throw new FatalBeanException("Property [hibernateSessionFactory] of [" + LocalJbpmSessionFactoryBean.class + "] is required.");
        }

        this.sessionFactory = new SpringJbpmSessionFactory(this.hibernateConfiguration, this.hibernateSessionFactory);
    }

    public Object getObject() throws Exception {
        return this.sessionFactory;
    }

    public Class getObjectType() {
        return (this.sessionFactory == null) ? JbpmSessionFactory.class : this.sessionFactory.getClass();
    }

    public boolean isSingleton() {
        return true;
    }

    private static final class SpringJbpmSessionFactory extends JbpmSessionFactory {
        private SessionFactory sessionFactory;

        public SpringJbpmSessionFactory(Configuration configuration, SessionFactory sessionFactory) {
            super(configuration, sessionFactory);
            this.sessionFactory = sessionFactory;
        }

        public JbpmSession openJbpmSession(Connection jdbcConnection) {
            if (jdbcConnection != null) {
                throw new UnsupportedOperationException("Cannot start a new Hibernate Session using supplied JDBC connection");
            }

            Session session = SessionFactoryUtils.getSession(this.sessionFactory, true);
            return new SpringJbpmSession(this, session, this.sessionFactory);
        }
    }

    private static final class SpringJbpmSession extends JbpmSession {

        private SessionFactory sessionFactory;

        public SpringJbpmSession(JbpmSessionFactory jbpmSessionFactory, Session session, SessionFactory sessionFactory) {
            super(jbpmSessionFactory, session);
            this.sessionFactory = sessionFactory;
        }

        public void beginTransaction() {
            if(isSpringManagedTransaction()) {
                return;
            }

            super.beginTransaction();
        }


        public void commitTransaction() {
            if(isSpringManagedTransaction()) {
                return;
            }

            super.commitTransaction();
        }

        public void rollbackTransaction() {
            if(isSpringManagedTransaction()) {
                return;
            }

            super.rollbackTransaction();
        }

        // todo: thoroughly test these
        // todo: consider the effect of deferred close of these methods

        public void commitTransactionAndClose() {
            if(isSpringManagedTransaction()) {
                return;
            }

            super.commitTransactionAndClose();
        }

        public void rollbackTransactionAndClose() {
            if(isSpringManagedTransaction()) {
                return;
            }

            super.rollbackTransactionAndClose();
        }

        public void close() {
            if(isSpringManagedTransaction()) {
                return;
            }

            super.close();
        }

        private boolean isSpringManagedTransaction() {
            return SessionFactoryUtils.isSessionTransactional(getSession(), this.sessionFactory);
        }
    }
}
