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

import java.io.FileInputStream;
import java.util.Properties;

import org.springframework.beandoc.util.BeanDocUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;


/**
 * SpringLoader is a simple helper class for use by multiple client types.  The
 * single static method configures a <code>BeanFactory</code> from a supplied 
 * <code>SpringLoaderCommand</code> file and returns it to the caller.
 * 
 * @author Darren Davison
 * @since 1.0
 */
class SpringLoader {
    
    private static final String PROP_INPUT_FILES = "input.files";
    
    private static final String PROP_OUTPUT_DIR = "output.dir";
    
    private static final String PROP_TITLE = "html.title";
    
    private static final String DEFAULT_BEANDOC_XML = "/org/springframework/beandoc/client/beandoc.xml";
    
    private static final String SYSTEM_PROPS_PREFIX = "springbeandoc.";
        

    /**
     * Loads and configures (post-processes) a BeanFactory from an internal
     * definition file.
     * 
     * @param command the configuration needed to bootstrap the context for beandoc
     * @return the BeanFactory post-processed by the properties supplied
     */
    static BeanFactory getBeanFactory(SpringLoaderCommand command) throws Exception {
        
        Properties beandocProps = new Properties();    
        
        // evaluate system props first as they are lowest in the hierarchy
        beandocProps.putAll(BeanDocUtils.filterByPrefix(
                System.getProperties(), SYSTEM_PROPS_PREFIX));
        
        // load user props if specified, overriding any values from the System
        if (command.getBeandocPropsLocation() != null) {
            Properties userProps = new Properties();
            userProps.load(new FileInputStream(command.getBeandocPropsLocation()));
            beandocProps.putAll(BeanDocUtils.filterByPrefix(
                    userProps, command.getBeandocPropsPrefix()));
        }
        
        // finally, override with command line (or equiv) I/O attributes
        if (command.getInputFiles() != null)
            beandocProps.put(PROP_INPUT_FILES, command.getInputFiles());
        if (command.getOutputDir() != null)
            beandocProps.put(PROP_OUTPUT_DIR, command.getOutputDir());
        if (command.getTitle() != null)
            beandocProps.put(PROP_TITLE, command.getTitle());
            
        String beandocXml = (command.getBeandocContextLocation() == null) ? 
            DEFAULT_BEANDOC_XML : command.getBeandocContextLocation();
	    ClassPathResource res = new ClassPathResource(beandocXml);
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
