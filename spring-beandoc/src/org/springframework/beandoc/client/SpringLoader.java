/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beandoc.client;

import java.util.Properties;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;


/**
 * SpringLoader is a simple helper class for use by multiple client types.  The
 * single static method configures a <code>BeanFactory</code> from a supplied 
 * <code>Properties</code> file and returns it to the caller.
 * 
 * @author Darren Davison
 * @since 1.0
 */
class SpringLoader {
    
	private static final String BEANDOC_XML = "/org/springframework/beandoc/client/beandoc.xml";

    /**
     * Loads and configures (post-processes) a BeanFactory from an inernal
     * definition file.
     * 
     * @param beandocProps
     * @return the BeanFactory post-processed by the properties supplied
     */
    static BeanFactory getBeanFactory(Properties beandocProps) {
	    ClassPathResource res = new ClassPathResource(BEANDOC_XML);
	    XmlBeanFactory factory = new XmlBeanFactory(res);
	    PropertyPlaceholderConfigurer cfgPlacehoder = new PropertyPlaceholderConfigurer();
	    cfgPlacehoder.setProperties(beandocProps);
	    cfgPlacehoder.postProcessBeanFactory(factory);
	    
	    PropertyOverrideConfigurer cfgOverride = new PropertyOverrideConfigurer();        
	    // following prop requires >= 1.1.4 spring-core.jar
	    cfgOverride.setIgnoreInvalidKeys(true);
	    cfgOverride.setProperties(beandocProps);
	    cfgOverride.postProcessBeanFactory(factory);
	    
	    return factory;	    
    }
}
