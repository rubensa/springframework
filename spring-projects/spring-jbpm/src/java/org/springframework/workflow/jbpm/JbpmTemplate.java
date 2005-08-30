package org.springframework.workflow.jbpm;

import org.hibernate.HibernateException;
import org.jbpm.db.JbpmSession;
import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;

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
        return (Long)execute(new JbpmCallback() {
            public Object doInJbpm(JbpmSession session) throws Exception {
                session.getGraphSession().saveProcessInstance(processInstance);
                return new Long(processInstance.getId());
            }
        });
    }

    public ProcessInstance findProcessInstance(final Long processInstanceId) {
        return (ProcessInstance)execute(new JbpmCallback(){
            public Object doInJbpm(JbpmSession session) throws Exception {
                return session.getGraphSession().loadProcessInstance(processInstanceId.longValue());
            }
        });
    }

    public List findProcessInstances() {
        return (List)execute(new JbpmCallback(){
            public Object doInJbpm(JbpmSession session) throws Exception {
                return session.getGraphSession().findProcessInstances(processDefinition.getId());
            }
        });
    }

    public void signal(final ProcessInstance processInstance) {
        execute(new JbpmCallback() {
            public Object doInJbpm(JbpmSession session) throws Exception {
                processInstance.signal();
                return null;
            }
        });
    }

    public void signal(final ProcessInstance processInstance, final String transitionId) {
        execute(new JbpmCallback(){
            public Object doInJbpm(JbpmSession session) throws Exception {
                processInstance.signal(transitionId);
                return null;
            }
        });
    }

    public void signal(final ProcessInstance processInstance, final Transition transition) {
        execute(new JbpmCallback(){
            public Object doInJbpm(JbpmSession session) throws Exception {
                processInstance.signal(transition);
                return null;
            }
        });
        throw new UnsupportedOperationException();
    }

    public Object execute(JbpmCallback callback) {
        JbpmSession jbpmSession = getSession();
        try {
            return callback.doInJbpm(jbpmSession);
        } catch (Exception ex) {
            throw convertJbpmException(ex);
        }
        finally {
            releaseSession(jbpmSession);
        }
    }

    private RuntimeException convertJbpmException(Exception ex) {
        // try to decode and translate HibernateExceptions
        if (ex instanceof HibernateException) {
            return SessionFactoryUtils.convertHibernateAccessException((HibernateException) ex);
        }

        if (ex.getCause() instanceof HibernateException) {
            // todo: going to loose a message here - perhaps create a NestedDataAccessException or similar
            return SessionFactoryUtils.convertHibernateAccessException((HibernateException) ex.getCause());
        }

        // todo: classify into something like UncategorizedWorkflowException
        return new RuntimeException(ex);
    }

    protected void releaseSession(JbpmSession jbpmSession) {
        JbpmSessionFactoryUtils.releaseSession(jbpmSession, this.jbpmSessionFactory);
    }

    protected JbpmSession getSession() {
        return JbpmSessionFactoryUtils.getSession(this.jbpmSessionFactory);
    }
}
