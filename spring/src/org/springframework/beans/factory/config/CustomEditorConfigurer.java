/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * BeanFactoryPostProcessor implementation that allows for convenient
 * registration of custom property editors.
 *
 * <p>As of Spring 2.0, the recommended usage is with custom PropertyEditorRegistrar
 * implementations that in turn register desired editors on a given registry.
 * Each PropertyEditorRegistrar can register any number of custom editors.
 *
 * <pre class="code">&lt;bean id="customEditorConfigurer" class="org.springframework.beans.factory.config.CustomEditorConfigurer"&gt;
 *   &lt;property name="propertyEditorRegistrars"&gt;
 *     &lt;list&gt;
 *       &lt;bean class="mypackage.MyCustomDateEditorRegistrar"/&gt;
 *       &lt;bean class="mypackage.MyObjectEditorRegistrar"/&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * Alternative configuration example with custom editor instances,
 * assuming inner beans for PropertyEditor instances:
 *
 * <pre class="code">&lt;bean id="customEditorConfigurer" class="org.springframework.beans.factory.config.CustomEditorConfigurer"&gt;
 *   &lt;property name="customEditors"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="java.util.Date"&gt;
 *         &lt;bean class="mypackage.MyCustomDateEditor"/&gt;
 *       &lt;/entry&gt;
 *       &lt;entry key="mypackage.MyObject"&gt;
 *         &lt;bean id="myEditor" class="mypackage.MyObjectEditor"&gt;
 *           &lt;property name="myParam"&gt;&lt;value&gt;myValue&lt;/value&gt;&lt;/property&gt;
 *         &lt;/bean&gt;
 *       &lt;/entry&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * Also supports "java.lang.String[]"-style array class names and primitive
 * class names (e.g. "boolean"). Delegates to ClassUtils for actual class name
 * resolution.
 *
 * <p><b>NOTE:</b> Custom property editors registered with this configurer do
 * <i>not</i> apply to data binding. Custom editors for data binding need to
 * be registered on the DataBinder: Use a common base class or delegate to a
 * common PropertyEditorRegistrar code to reuse editor registration there.
 * 
 * @author Juergen Hoeller
 * @since 27.02.2004
 * @see java.beans.PropertyEditor
 * @see org.springframework.beans.PropertyEditorRegistrar
 * @see ConfigurableBeanFactory#addPropertyEditorRegistrar
 * @see ConfigurableBeanFactory#registerCustomEditor
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 * @see org.springframework.web.servlet.mvc.BaseCommandController#setPropertyEditorRegistrars
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 */
public class CustomEditorConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private PropertyEditorRegistrar[] propertyEditorRegistrars;

	private Map customEditors;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return order;
	}

	/**
	 * Specify the PropertyEditorRegistrars to apply to beans defined
	 * within the current application context.
	 * <p>This allows for sharing PropertyEditorRegistrars with DataBinders etc.
	 * Furthermore, it avoids the need for synchronization on custom editors:
	 * A PropertyEditorRegistrar will always create fresh editor instances for
	 * each bean creation attempt.
	 * @see ConfigurableListableBeanFactory#addPropertyEditorRegistrar
	 */
	public void setPropertyEditorRegistrars(PropertyEditorRegistrar[] propertyEditorRegistrars) {
		this.propertyEditorRegistrars = propertyEditorRegistrars;
	}

	/**
	 * Specify the custom editors to register via a Map, using the class name
	 * of the required type as key and the PropertyEditor instance as value.
	 * @see ConfigurableListableBeanFactory#registerCustomEditor
	 */
	public void setCustomEditors(Map customEditors) {
		this.customEditors = customEditors;
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.propertyEditorRegistrars != null) {
			for (int i = 0; i < this.propertyEditorRegistrars.length; i++) {
				beanFactory.addPropertyEditorRegistrar(this.propertyEditorRegistrars[i]);
			}
		}

		if (this.customEditors != null) {
			for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Class requiredType = null;
				if (key instanceof Class) {
					requiredType = (Class) key;
				}
				else if (key instanceof String) {
					String className = (String) key;
					try {
						requiredType = ClassUtils.forName(className, this.beanClassLoader);
					}
					catch (ClassNotFoundException ex) {
						throw new BeanInitializationException(
								"Could not load required type [" + className + "] for custom editor", ex);
					}
				}
				else {
					throw new BeanInitializationException(
							"Invalid key [" + key + "] for custom editor - needs to be Class or String");
				}
				Object value = this.customEditors.get(key);
				if (!(value instanceof PropertyEditor)) {
					throw new BeanInitializationException("Mapped value [" + value + "] for custom editor key [" +
							key + "] is not of required type [" + PropertyEditor.class.getName() + "]");
				}
				beanFactory.registerCustomEditor(requiredType, (PropertyEditor) value);
			}
		}
	}

}
