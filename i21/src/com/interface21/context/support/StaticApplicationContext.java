package com.interface21.context.support;

import java.util.HashMap;

import com.interface21.beans.BeansException;
import com.interface21.beans.PropertyValues;
import com.interface21.beans.factory.BeanFactory;
import com.interface21.beans.factory.ListableBeanFactory;
import com.interface21.beans.factory.support.ListableBeanFactoryImpl;
import com.interface21.beans.factory.support.RootBeanDefinition;
import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextException;
import java.util.Locale;

/**
 * ApplicationContext to allow concrete registration
 * of Java objects in code, rather than from external configuration sources.
 * Especially useful for testing.
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class StaticApplicationContext extends AbstractApplicationContext {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	ListableBeanFactoryImpl defaultBeanFactory;

	/** Namespace --> name */
	private HashMap beanFactoryHash = new HashMap();


	//---------------------------------------------------------------------
	// Constructor
	//---------------------------------------------------------------------
	public StaticApplicationContext() throws BeansException, ApplicationContextException {
		this(null);
	}

	public StaticApplicationContext(ApplicationContext parent) throws BeansException, ApplicationContextException {
		super(parent);

		// create bean factory with parent
		defaultBeanFactory = new ListableBeanFactoryImpl(parent);

		// Register the message source bean
		defaultBeanFactory.registerBeanDefinition(MESSAGE_SOURCE_BEAN_NAME,
			new RootBeanDefinition(StaticMessageSource.class, null, true));

		//refresh();
	}


	/**
	 * Must invoke when finished
	 */
	public void rebuild() throws ApplicationContextException {
		refresh();
	}

	/**
	 * Return the BeanFactory for this namespace
	 */
	protected BeanFactory loadBeanFactory(String namespace) throws ApplicationContextException {
		BeanFactory bf = (BeanFactory) beanFactoryHash.get(namespace);
		if (bf == null)
			// No one's created it yet
			throw new ApplicationContextException("Unknown namespace '" + namespace + "'");
		return bf;
	}

	//---------------------------------------------------------------------
	// Implementation of abstract methods
	//---------------------------------------------------------------------
	/**
	 *  Do nothing: we rely on callers to update our public methods
	 * @see AbstractApplicationContext#refreshBeanFactory()
	 */
	protected void refreshBeanFactory() throws ApplicationContextException {
	}

	/**
	 * @see AbstractApplicationContext#getBeanFactory()
	 */
	protected ListableBeanFactory getBeanFactory() {
		return defaultBeanFactory;
	}


	//---------------------------------------------------------------------
	// Public methods
	//---------------------------------------------------------------------
	/**
	 * Register a bean with the default bean factory
	 */
	public void registerSingleton(String name, Class clazz, PropertyValues pvs) throws BeansException {
		defaultBeanFactory.registerBeanDefinition(name,
			new RootBeanDefinition(clazz, pvs, true));
	}

	public void registerPrototype(String name, Class clazz, PropertyValues pvs) throws BeansException {
		defaultBeanFactory.registerBeanDefinition(name,
			new RootBeanDefinition(clazz, pvs, false));
	}

        /**
         * Associate the given message with the given code.
         * @param code lookup code
         * @param locale locale message should be found within
         * @param message message associated with this lookup code
         */
        public void addMessage(String code, Locale locale, String defaultMessage) {
		//messageSource.addMessage(code,message)
		throw new UnsupportedOperationException("Add message not yet implemeneted");
	}


}

