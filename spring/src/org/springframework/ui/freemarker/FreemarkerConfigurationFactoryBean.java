/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ui.freemarker;

import java.io.IOException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;

/**
 * Factory bean that creates a FreeMarker Configuration and provides it as
 * bean reference. This bean is intended for any kind of usage of FreeMarker
 * in application code, e.g. for generating email content. For web views,
 * FreemarkerConfigurer is used to set up a FreemarkerConfigurationFactory.
 *
 * <p>See base class FreemarkerConfigurationFactory for details.
 *
 * <p>Note: Spring's FreeMarker support requires FreeMarker 2.3 or higher.
 *
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id$
 */
public class FreemarkerConfigurationFactoryBean extends FreemarkerConfigurationFactory
		implements FactoryBean, InitializingBean, ResourceLoaderAware {

	private Configuration configuration;

	public void afterPropertiesSet() throws IOException, TemplateException {
		this.configuration = createConfiguration();
	}

	public Object getObject() {
		return this.configuration;
	}

	public Class getObjectType() {
		return Configuration.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
