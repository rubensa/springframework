package org.springframework.context;

import java.util.Locale;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.AbstractListableBeanFactoryTests;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.LifecycleBean;

/**
 * @author Rod Johnson
 * @version $RevisionId$
 */
public abstract class AbstractApplicationContextTests extends AbstractListableBeanFactoryTests {

	/** Must be supplied as XML */
	public static final String TEST_NAMESPACE = "testNamespace";

	protected ApplicationContext applicationContext;

	/** Subclass must register this */
	protected TestListener listener = new TestListener();

	protected TestListener parentListener = new TestListener();

	protected void setUp() throws Exception {
		this.applicationContext = createContext();
	}

	protected BeanFactory getBeanFactory() {
		return applicationContext;
	}

	/**
	 * Must register a TestListener.
	 * Must register standard beans.
	 * Parent must register rod with name Roderick
	 * and father with name Albert.
	 */
	protected abstract ApplicationContext createContext() throws Exception;

	public void testContextAwareSingletonWasCalledBack() throws Exception {
		ACATest aca = (ACATest) applicationContext.getBean("aca");
		assertTrue("has had context set", aca.getApplicationContext() == applicationContext);
		Object aca2 = applicationContext.getBean("aca");
		assertTrue("Same instance", aca == aca2);
		assertTrue("Says is singleton", applicationContext.isSingleton("aca"));
	}

	public void testContextAwarePrototypeWasCalledBack() throws Exception {
		ACATest aca = (ACATest) applicationContext.getBean("aca-prototype");
		assertTrue("has had context set", aca.getApplicationContext() == applicationContext);
		Object aca2 = applicationContext.getBean("aca-prototype");
		assertTrue("NOT Same instance", aca != aca2);
		assertTrue("Says is prototype", !applicationContext.isSingleton("aca-prototype"));
	}

	public void testParentNonNull() {
		assertTrue("parent isn't null", applicationContext.getParent() != null);
	}

	public void testGrandparentNull() {
		assertTrue("grandparent is null", applicationContext.getParent().getParent() == null);
	}

	public void testOverrideWorked() throws Exception {
		TestBean rod = (TestBean) applicationContext.getParent().getBean("rod");
		assertTrue("Parent's name differs", rod.getName().equals("Roderick"));
	}

	public void testGrandparentDefinitionFound() throws Exception {
		TestBean dad = (TestBean) applicationContext.getBean("father");
		assertTrue("Dad has correct name", dad.getName().equals("Albert"));
	}

	public void testGrandparentTypedDefinitionFound() throws Exception {
		TestBean dad = (TestBean) applicationContext.getBean("father", TestBean.class);
		assertTrue("Dad has correct name", dad.getName().equals("Albert"));
	}

	public void testCloseTriggersDestroy() {
		LifecycleBean lb = (LifecycleBean) applicationContext.getBean("lifecycle");
		assertTrue("Not destroyed", !lb.isDestroyed());
		applicationContext.close();
		if (applicationContext.getParent() != null) {
			applicationContext.getParent().close();
		}
		assertTrue("Destroyed", lb.isDestroyed());
		applicationContext.close();
		if (applicationContext.getParent() != null) {
			applicationContext.getParent().close();
		}
		assertTrue("Destroyed", lb.isDestroyed());
	}

	public void testContextOptions() throws Exception {
		assertTrue("contextOptions cannot be null", applicationContext.getOptions() != null);
	}

	public void testMessageSource() throws NoSuchMessageException {
		assertEquals(applicationContext.getMessage("code1", null, Locale.getDefault()), "message1");
		assertEquals(applicationContext.getMessage("code2", null, Locale.getDefault()), "message2");

		try {
			applicationContext.getMessage("code0", null, Locale.getDefault());
			fail("looking for code0 should throw a NoSuchMessageException");
		} catch (NoSuchMessageException ex) {
			// that's how it should be
		}
	}

	public void testEvents() throws Exception {
		listener.zeroCounter();
		parentListener.zeroCounter();
		assertTrue("0 events before publication", listener.getEventCount() == 0);
		assertTrue("0 parent events before publication", parentListener.getEventCount() == 0);
		this.applicationContext.publishEvent(new MyEvent(this));
		assertTrue("1 events after publication, not " + listener.getEventCount(), listener.getEventCount() == 1);
		assertTrue("1 parent events after publication", parentListener.getEventCount() == 1);
	}

	public void testBeanAutomaticallyHearsEvents() throws Exception {
		//String[] listenerNames = ((ListableBeanFactory) applicationContext).getBeanDefinitionNames(ApplicationListener.class);
		//assertTrue("listeners include beanThatListens", Arrays.asList(listenerNames).contains("beanThatListens"));
		BeanThatListens b = (BeanThatListens) applicationContext.getBean("beanThatListens");
		b.zero();
		assertTrue("0 events before publication", b.getEventCount() == 0);
		this.applicationContext.publishEvent(new MyEvent(this));
		assertTrue("1 events after publication, not " + b.getEventCount(), b.getEventCount() == 1);
	}


	public class MyEvent extends ApplicationEvent {

		public MyEvent(Object source) {
			super(source);
		}
	}

}
