package org.springframework.workflow.jbpm;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.jbpm.db.JbpmSession;

/**
 * @author Rob Harrop
 */
public class JbpmSessionHolder extends ResourceHolderSupport {

    private JbpmSession jbpmSession;

    public JbpmSessionHolder(JbpmSession jbpmSession) {
        this.jbpmSession = jbpmSession;
    }

    public JbpmSession getJbpmSession() {
        return this.jbpmSession;
    }

    public void clear() {
        this.jbpmSession = null;
    }
}
