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
package org.springframework.orm.jpa;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple servlet to test instrumentation support inside web containers.
 * 
 * @author Costin Leau
 * 
 */
public class LwtTestServlet extends HttpServlet {

	private static final Log log = LogFactory.getLog(LwtTestServlet.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		log.info("LWT servlet initialized");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Servlet#destroy()
	 */
	public void destroy() {
		log.info("LWT servlet destroyed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		doPost(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		log.info("LWT servlet called");
		
		//System.setProperty("org.springframework.orm.jpa.provider", "hibernate");
		
		TestSuite suite = new TestSuite(TomcatContainerManagedEntityManagerIntegrationTsts.class);
		TestResult result = new TestResult();
		suite.run(result);
		log.info("suite" + suite);

		int count = 0;
		for (Enumeration enu = result.errors(); enu.hasMoreElements();) {
			log.error(enu.nextElement());
			count ++;
		}
		log.error("*** LWT errors: " + count);
		log.info("failures");
		count = 0;
		for (Enumeration enu = result.failures(); enu.hasMoreElements();) {
			log.error(enu.nextElement());
			count++;
		}
		log.error("*** LWT failures: " + count);
	}
	

}
