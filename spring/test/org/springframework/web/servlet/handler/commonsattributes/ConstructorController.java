/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.handler.commonsattributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.ITestBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * 
 * @author Rod Johnson
 * @version $Id$
 * 
 * @@org.springframework.web.servlet.handler.commonsattributes.PathMap("/constructor.cgi")
 */
public class ConstructorController extends AbstractController {
	
	public ITestBean testBean;

	public ConstructorController(ITestBean testBean) {
		this.testBean = testBean;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
		return new ModelAndView("test");
	}

}
