/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.freemarker;

import freemarker.template.Configuration;


/**
 * FreemarkerConfig
 * 
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id$
 */
public interface FreemarkerConfig {
	
	Configuration getConfiguration();
	
}
