package org.springframework.workflow.jbpm.definition;

import org.jbpm.db.JbpmSessionFactory;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.jpdl.par.ProcessArchiveDeployer;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * @author Rob Harrop
 */
public class ProcessDefinitionFactoryBean implements FactoryBean, InitializingBean {

    private JbpmSessionFactory jbpmSessionFactory;

    private ProcessDefinition processDefinition;

    private Resource definitionLocation;

    private boolean deployProcessDefinitionOnStartup;

    public void setDefinitionLocation(Resource definitionLocation) {
        this.definitionLocation = definitionLocation;
    }

    public void setDeployProcessDefinitionOnStartup(boolean deployProcessDefinitionOnStartup) {
        this.deployProcessDefinitionOnStartup = deployProcessDefinitionOnStartup;
    }

    public void setJbpmSessionFactory(JbpmSessionFactory jbpmSessionFactory) {
        this.jbpmSessionFactory = jbpmSessionFactory;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.definitionLocation == null) {
            throw new FatalBeanException("Property [definitionLocation] of class [" +
                    ProcessDefinitionFactoryBean.class.getName() + "] is required.");
        }

        if (this.deployProcessDefinitionOnStartup && (this.jbpmSessionFactory == null)) {
            throw new FatalBeanException("Property [jbpmSessionFactory] of class [" +
                    ProcessDefinitionFactoryBean.class +
                    "] is required when property [deployProcessDefinitionOnStartup] is true.");
        }

        InputStream inputStream = null;
        try {
            inputStream = this.definitionLocation.getInputStream();
            this.processDefinition = ProcessDefinition.parseXmlInputStream(inputStream);

            if (this.deployProcessDefinitionOnStartup) {
                ProcessArchiveDeployer.deployProcessDefinition(this.processDefinition, this.jbpmSessionFactory);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public Object getObject() throws Exception {
        return this.processDefinition;
    }

    public Class getObjectType() {
        return (processDefinition == null) ? ProcessDefinition.class : processDefinition.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}
