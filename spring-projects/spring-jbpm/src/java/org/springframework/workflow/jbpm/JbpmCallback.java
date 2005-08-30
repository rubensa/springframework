package org.springframework.workflow.jbpm;

import org.jbpm.db.JbpmSession;

/**
 * @author Rob Harrop
 */
public interface JbpmCallback {

    Object doInJbpm(JbpmSession session) throws Exception;
}
