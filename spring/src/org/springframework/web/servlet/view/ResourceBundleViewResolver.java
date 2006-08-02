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

package org.springframework.web.servlet.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.View;

/**
 * {@link org.springframework.web.servlet.ViewResolver} implementation 
 * that uses bean definitions in a {@link ResourceBundle}, specified by
 * the bundle basename.
 * 
 * <p>The bundle is typically defined in a properties file, located in
 * the class path. The default bundle basename is "views".
 *
 * <p>This <code>ViewResolver</code> supports localized view definitions,
 * using the default support of {@link java.util.PropertyResourceBundle}.
 * For example, the basename "views" will be resolved as class path resources
 * "views_de_AT.properties", "views_de.properties", "views.properties" -
 * for a given Locale "de_AT".
 *
 * <p>Note: this <code>ViewResolver</code> implements the {@link Ordered}
 * interface to allow for flexible participation in <code>ViewResolver</code>
 * chaining. For example, some special views could be defined via this
 * <code>ViewResolver</code> (giving it 0 as "order" value), while all
 * remaining views could be resolved by a {@link UrlBasedViewResolver}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.ResourceBundle#getBundle
 * @see java.util.PropertyResourceBundle
 * @see UrlBasedViewResolver
 */
public class ResourceBundleViewResolver extends AbstractCachingViewResolver implements Ordered, DisposableBean {

	/** The default basename if no other basename is supplied. */
	public final static String DEFAULT_BASENAME = "views";


	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private String[] basenames = new String[] {DEFAULT_BASENAME};

	private ClassLoader bundleClassLoader = Thread.currentThread().getContextClassLoader();

	private String defaultParentView;

	/* Locale -> BeanFactory */
	private final Map localeCache = new HashMap();

	/* List of ResourceBundle -> BeanFactory */
	private final Map bundleCache = new HashMap();


	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	/**
	 * Set the basename, as defined in the {@link java.util.ResourceBundle}
	 * documentation.
	 * <p><code>ResourceBundle</code> supports different suffixes. For example,
	 * a base name of "views" might map to <code>ResourceBundle</code> files
	 * "views", "views_en_au" and "views_de".
	 * <p>The default is "views".
	 * @param basename the <code>ResourceBundle</code> basename
	 * @see #setBasenames
	 * @see java.util.ResourceBundle
	 */
	public void setBasename(String basename) {
		setBasenames(new String[] {basename});
	}

	/**
	 * Set multiple <code>ResourceBundle</code> basenames.
	 * @param basenames multiple <code>ResourceBundle</code> basenames
	 * @see #setBasename
	 */
	public void setBasenames(String[] basenames) {
		this.basenames = basenames;
	}

	/**
	 * Set the {@link ClassLoader} to load resource bundles with.
	 * Default is the thread context <code>ClassLoader</code>.
	 * @param classLoader the <code>ClassLoader</code> to load resource bundles with
	 */
	public void setBundleClassLoader(ClassLoader classLoader) {
		this.bundleClassLoader = classLoader;
	}

	/**
	 * Return the {@link ClassLoader} to load resource bundles with.
	 * <p>Default is the specified bundle <code>ClassLoader</code>,
	 * usually the thread context <code>ClassLoader</code>.
	 * @return the <code>ClassLoader</code> to load resource bundles with
	 */
	protected ClassLoader getBundleClassLoader() {
		return bundleClassLoader;
	}

	/**
	 * Set the default parent for views defined in the <code>ResourceBundle</code>.
	 * <p>This avoids repeated "yyy1.(parent)=xxx", "yyy2.(parent)=xxx" definitions
	 * in the bundle, especially if all defined views share the same parent.
	 * <p>The parent will typically define the view class and common attributes.
	 * Concrete views might simply consist of an URL definition then:
	 * a la "yyy1.url=/my.jsp", "yyy2.url=/your.jsp".
	 * <p>View definitions that define their own parent or carry their own
	 * class can still override this. Strictly speaking, the rule that a
	 * default parent setting does not apply to a bean definition that
	 * carries a class is there for backwards compatiblity reasons.
	 * It still matches the typical use case.
	 * @param defaultParentView the default parent view
	 */
	public void setDefaultParentView(String defaultParentView) {
		this.defaultParentView = defaultParentView;
	}


	protected View loadView(String viewName, Locale locale) throws Exception {
		BeanFactory factory = initFactory(locale);
		try {
			return (View) factory.getBean(viewName, View.class);
		}
		catch (NoSuchBeanDefinitionException ex) {
			// to allow for ViewResolver chaining
			return null;
		}
	}

	/**
	 * Initialize the {@link BeanFactory} from the <code>ResourceBundle</code>,
	 * for the given {@link Locale locale}.
	 * <p>Synchronized because of access by parallel threads.
	 * @param locale the target <code>Locale</code>
	 */
	protected synchronized BeanFactory initFactory(Locale locale) throws Exception {
		// Try to find cached factory for Locale:
		// Have we already encountered that Locale before?
		if (isCache()) {
			BeanFactory cachedFactory = (BeanFactory) this.localeCache.get(locale);
			if (cachedFactory != null) {
				return cachedFactory;
			}
		}

		// Build list of ResourceBundle references for Locale.
		List bundles = new LinkedList();
		for (int i = 0; i < this.basenames.length; i++) {
			ResourceBundle bundle = getBundle(this.basenames[i], locale);
			bundles.add(bundle);
		}

		// Try to find cached factory for ResourceBundle list:
		// even if Locale was different, same bundles might have been found.
		if (isCache()) {
			BeanFactory cachedFactory = (BeanFactory) this.bundleCache.get(bundles);
			if (cachedFactory != null) {
				this.localeCache.put(locale, cachedFactory);
				return cachedFactory;
			}
		}

		// Create child ApplicationContext for views.
		GenericWebApplicationContext factory = new GenericWebApplicationContext();
		factory.setParent(getApplicationContext());
		factory.setServletContext(getServletContext());

		// Load bean definitions from resource bundle.
		PropertiesBeanDefinitionReader reader = new PropertiesBeanDefinitionReader(factory);
		reader.setDefaultParentBean(this.defaultParentView);
		for (int i = 0; i < bundles.size(); i++) {
			ResourceBundle bundle = (ResourceBundle) bundles.get(i);
			reader.registerBeanDefinitions(bundle);
		}

		factory.refresh();

		// Cache factory for both Locale and ResourceBundle list.
		if (isCache()) {
			this.localeCache.put(locale, factory);
			this.bundleCache.put(bundles, factory);
		}

		return factory;
	}

	/**
	 * Obtain the resource bundle for the given basename and {@link Locale}.
	 * @param basename the basename to look for
	 * @param locale the <code>Locale</code> to look for
	 * @return the corresponding <code>ResourceBundle</code>
	 * @throws MissingResourceException if no matching bundle could be found
	 * @see java.util.ResourceBundle#getBundle(String, java.util.Locale, ClassLoader)
	 */
	protected ResourceBundle getBundle(String basename, Locale locale) throws MissingResourceException {
		return ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
	}


	/**
	 * Close the bundle bean factories on context shutdown.
	 */
	public void destroy() throws BeansException {
		for (Iterator it = this.bundleCache.values().iterator(); it.hasNext();) {
			ConfigurableApplicationContext factory = (ConfigurableApplicationContext) it.next();
			factory.close();
		}
		this.localeCache.clear();
		this.bundleCache.clear();
	}

}
