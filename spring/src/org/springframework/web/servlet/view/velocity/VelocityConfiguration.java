/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.velocity;

import org.apache.velocity.app.VelocityEngine;

/**
 * Interface to be implemented by objects that configure
 * and manage a VelocityEngine.
 * @author Rod Johnson
 * @version $Id$
 */
public interface VelocityConfiguration {
	
	/**
	 * 
	 * @return the VelocityEngine for this web application context.
	 * May be unique to one servlet, or shared.
	 */
	VelocityEngine getVelocityEngine();

}
