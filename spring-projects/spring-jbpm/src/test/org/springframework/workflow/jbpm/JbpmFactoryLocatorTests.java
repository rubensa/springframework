/**
 * Created on Jan 24, 2006
 *
 * $Id$
 * $Revision$
 */
package org.springframework.workflow.jbpm;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author Costin Leau
 *
 */
public class JbpmFactoryLocatorTests extends AbstractDependencyInjectionSpringContextTests {

	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/workflow/jbpm/locatorContext.xml" };
	}

	public void testJbpmFactoryLocator() {
		BeanFactoryLocator locator1 = (BeanFactoryLocator) applicationContext.getBean("instance1");
		BeanFactoryLocator locator2 = (BeanFactoryLocator) applicationContext.getBean("instance2");

		// verify the static map
		BeanFactory factory1 = locator1.useBeanFactory("instance1").getFactory();
		BeanFactory factory2 = locator1.useBeanFactory("instance2").getFactory();
		BeanFactory factory3 = locator2.useBeanFactory("instance2").getFactory();
		// get the alias from different factories
		BeanFactory alias1 = locator1.useBeanFactory("alias1").getFactory();
		BeanFactory alias2 = locator2.useBeanFactory("alias2").getFactory();
		
		assertSame(factory1, factory2);
		assertSame(factory1, factory3);
		// verify it's the same bean factory as the application context
		assertSame(factory1, applicationContext.getBeanFactory());
		
		// verify aliases
		assertSame(alias1, alias2);
		assertSame(factory1, alias1);
	}
	
	public void testFactoryLocatorDefault()
	{
		BeanFactoryLocator locator1 = (BeanFactoryLocator) applicationContext.getBean("instance1");
		try {
			locator1.useBeanFactory(null);
			fail("there are more then one bean factories registered - should have thrown exception");
		}
		catch (IllegalArgumentException e) {
			// it's okay
		}
		
	}
	
	public void testFactoryLocatorOverride()
	{
		JbpmFactoryLocator locator = new JbpmFactoryLocator();
		// apply the correct order
		locator.setBeanName("instance1");
		try {
			locator.setBeanFactory(applicationContext);
			fail("should have received exception");
		}
		catch (IllegalArgumentException e) {
			// it's okay
		}
	}
	
	public void testBeanFactoryLocatorContract()
	{
		BeanFactoryLocator locator1 = (BeanFactoryLocator) applicationContext.getBean("instance1");
		BeanFactoryReference factory1 = locator1.useBeanFactory("instance1");
		assertNotNull(factory1.getFactory());
		factory1.release();
		try {
			factory1.getFactory();
			fail("should have received exception");
		}
		catch (IllegalArgumentException e) {
			// it's okay
		}
		factory1.release();
	}
}
