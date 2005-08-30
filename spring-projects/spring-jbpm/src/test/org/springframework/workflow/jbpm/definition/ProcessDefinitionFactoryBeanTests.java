package org.springframework.workflow.jbpm.definition;

import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.FatalBeanException;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.db.JbpmSessionFactory;

/**
 * @author Rob Harrop
 */
public class ProcessDefinitionFactoryBeanTests extends AbstractTransactionalDataSourceSpringContextTests {

    private static final ClassPathResource DEFINITION_LOCATION = new ClassPathResource("org/springframework/workflow/jbpm/simpleWorkflow.xml");

    private JbpmSessionFactory jbpmSessionFactory;

    public void setJbpmSessionFactory(JbpmSessionFactory jbpmSessionFactory) {
        this.jbpmSessionFactory = jbpmSessionFactory;
    }

    public void testWithoutDefinitionLocation() throws Exception {
        ProcessDefinitionFactoryBean factoryBean = new ProcessDefinitionFactoryBean();

        try {
            factoryBean.afterPropertiesSet();
            fail("Should not be able to call afterPropertiesSet without setting the definitionLocation");
        } catch (FatalBeanException e) {
            // success
        }
    }
    public void testLoadSimple() throws Exception {
        ProcessDefinitionFactoryBean factoryBean = new ProcessDefinitionFactoryBean();
        factoryBean.setDefinitionLocation(DEFINITION_LOCATION);
        factoryBean.afterPropertiesSet();

        ProcessDefinition processDefinition = (ProcessDefinition) factoryBean.getObject();
        assertNotNull("Process Definition should not be null", processDefinition);
        assertEquals("simple", processDefinition.getName());
    }

    public void testWithDeploy() throws Exception {
        ProcessDefinitionFactoryBean factoryBean = new ProcessDefinitionFactoryBean();
        factoryBean.setDefinitionLocation(DEFINITION_LOCATION);
        factoryBean.setDeployProcessDefinitionOnStartup(true);
        factoryBean.setJbpmSessionFactory(this.jbpmSessionFactory);
        factoryBean.afterPropertiesSet();

        ProcessDefinition processDefinition = (ProcessDefinition) factoryBean.getObject();
        assertNotNull("Process Definition should not be null", processDefinition);
        assertEquals("simple", processDefinition.getName());
        System.out.println(processDefinition.getVersion());
    }

    public void testWithDeployButNoSessionFactory() throws Exception {
       ProcessDefinitionFactoryBean factoryBean = new ProcessDefinitionFactoryBean();
        factoryBean.setDefinitionLocation(DEFINITION_LOCATION);
        factoryBean.setDeployProcessDefinitionOnStartup(true);
        try {
            factoryBean.afterPropertiesSet();
            fail("Should not be able to call afterPropertiesSet without a JbpmSessionFactory");
        } catch (FatalBeanException e) {
            // success
        }
    }

    protected String[] getConfigLocations() {
        return new String[]{"org/springframework/workflow/jbpm/applicationContext.xml"};
    }
}
