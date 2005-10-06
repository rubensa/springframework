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
