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

package org.springframework.mock.web.portlet;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;

/**
 * Mock implementation of the PortalContext interface.
 *
 * @author John A. Lewis
 * @author Juergen Hoeller
 * @since 2.0
 */
public class MockPortalContext implements PortalContext {

	private final Properties properties = new Properties();

	private final Vector portletModes;

	private final Vector windowStates;


	/**
	 * Create a new MockPortalContext
	 * with default PortletModes (VIEW, EDIT, HELP)
	 * and default WindowStates (NORMAL, MAXIMIZED, MINIMIZED).
	 * @see javax.portlet.PortletMode
	 * @see javax.portlet.WindowState
	 */
	public MockPortalContext() {
		this.portletModes = new Vector(3);
		this.portletModes.add(PortletMode.VIEW);
		this.portletModes.add(PortletMode.EDIT);
		this.portletModes.add(PortletMode.HELP);

		this.windowStates = new Vector(3);
		this.windowStates.add(WindowState.NORMAL);
		this.windowStates.add(WindowState.MAXIMIZED);
		this.windowStates.add(WindowState.MINIMIZED);
	}

	/**
	 * Create a new MockPortalContext with the given PortletModes and WindowStates.
	 * @param supportedPortletModes the List of supported PortletMode instances
	 * @param supportedWindowStates the List of supported WindowState instances
	 * @see javax.portlet.PortletMode
	 * @see javax.portlet.WindowState
	 */
	public MockPortalContext(List supportedPortletModes, List supportedWindowStates) {
		this.portletModes = new Vector(supportedPortletModes);
		this.windowStates = new Vector(supportedWindowStates);
	}


	public String getPortalInfo() {
		return "MockPortal/1.0";
	}

	public String getProperty(String name) {
		return this.properties.getProperty(name);
	}

	public Enumeration getPropertyNames() {
		return this.properties.propertyNames();
	}

	public Enumeration getSupportedPortletModes() {
		return this.portletModes.elements();
	}

	public Enumeration getSupportedWindowStates() {
		return this.windowStates.elements();
	}

}
