package org.springframework.workflow.jbpm;

import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;

import java.util.List;

/**
 * @author Rob Harrop
 */
public interface JbpmOperations {

    List findProcessInstances();

    ProcessInstance findProcessInstance(Long processInstanceId);

    Long saveProcessInstance(ProcessInstance processInstance);

    void signal(ProcessInstance processInstance);

    void signal(ProcessInstance processInstance, String transitionId);

    void signal(ProcessInstance processInstance, Transition transition);
}
