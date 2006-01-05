/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.bind;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Parameter extraction methods, for an approach distinct from data binding,
 * in which parameters of specific types are required.
 *
 * <p>This approach is very useful for simple submissions, where binding
 * request parameters to a command object would be overkill.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Keith Donald
 * @deprecated since Spring 2.0: use ServletRequestUtils instead
 */
public abstract class RequestUtils {

	/**
	 * Throw a ServletException if the given HTTP request method should be rejected.
	 * @param request request to check
	 * @param method method (such as "GET") which should be rejected
	 * @throws ServletException if the given HTTP request is rejected
	 */
	public static void rejectRequestMethod(HttpServletRequest request, String method) throws ServletException {
		if (request.getMethod().equals(method)) {
			throw new ServletException("This resource does not support request method '" + method + "'");
		}
	}


	/**
	 * Get an Integer parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Integer value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Integer getIntParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		if (request.getParameter(name) == null) {
			return null;
		}
		return new Integer(getRequiredIntParameter(request, name));
	}

	/**
	 * Get an int parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static int getIntParameter(HttpServletRequest request, String name, int defaultVal) {
		try {
			return getRequiredIntParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of int parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static int[] getIntParameters(HttpServletRequest request, String name) {
		try {
			return getRequiredIntParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new int[0];
		}
	}

	/**
	 * Get an int parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static int getRequiredIntParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredIntParameter(request, name);
	}

	/**
	 * Get an array of int parameters, throwing an exception if not found or one is not a number..
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static int[] getRequiredIntParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredIntParameters(request, name);
	}


	/**
	 * Get a Long parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Long value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Long getLongParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		if (request.getParameter(name) == null) {
			return null;
		}
		return new Long(getRequiredLongParameter(request, name));
	}

	/**
	 * Get a long parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static long getLongParameter(HttpServletRequest request, String name, long defaultVal) {
		try {
			return getRequiredLongParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of long parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static long[] getLongParameters(HttpServletRequest request, String name) {
		try {
			return getRequiredLongParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new long[0];
		}
	}

	/**
	 * Get a long parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static long getRequiredLongParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredLongParameter(request, name);
	}

	/**
	 * Get an array of long parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static long[] getRequiredLongParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredLongParameters(request, name);
	}


	/**
	 * Get a Float parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Float value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Float getFloatParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		if (request.getParameter(name) == null) {
			return null;
		}
		return new Float(getRequiredFloatParameter(request, name));
	}

	/**
	 * Get a float parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static float getFloatParameter(HttpServletRequest request, String name, float defaultVal) {
		try {
			return getRequiredFloatParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of float parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static float[] getFloatParameters(HttpServletRequest request, String name) {
		try {
			return getRequiredFloatParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new float[0];
		}
	}

	/**
	 * Get a float parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static float getRequiredFloatParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredFloatParameter(request, name);
	}

	/**
	 * Get an array of float parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static float[] getRequiredFloatParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredFloatParameters(request, name);
	}


	/**
	 * Get a Double parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Double value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Double getDoubleParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		if (request.getParameter(name) == null) {
			return null;
		}
		return new Double(getRequiredDoubleParameter(request, name));
	}

	/**
	 * Get a double parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static double getDoubleParameter(HttpServletRequest request, String name, double defaultVal) {
		try {
			return getRequiredDoubleParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of double parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static double[] getDoubleParameters(HttpServletRequest request, String name) {
		try {
			return getRequiredDoubleParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new double[0];
		}
	}

	/**
	 * Get a double parameter, throwing an exception if it isn't found or isn't a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static double getRequiredDoubleParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredDoubleParameter(request, name);
	}

	/**
	 * Get an array of double parameters, throwing an exception if not found or one is not a number.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static double[] getRequiredDoubleParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredDoubleParameters(request, name);
	}


	/**
	 * Get a Boolean parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value isn't a boolean.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the Boolean value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static Boolean getBooleanParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		if (request.getParameter(name) == null) {
			return null;
		}
		return (getRequiredBooleanParameter(request, name) ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * Get a boolean parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value as default to enable checks of whether it was supplied.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultVal) {
		try {
			return getRequiredBooleanParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of boolean parameters, return an empty array if not found.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static boolean[] getBooleanParameters(HttpServletRequest request, String name) {
		try {
			return getRequiredBooleanParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new boolean[0];
		}
	}

	/**
	 * Get a boolean parameter, throwing an exception if it isn't found
	 * or isn't a boolean.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static boolean getRequiredBooleanParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredBooleanParameter(request, name);
	}

	/**
	 * Get an array of boolean parameters, throwing an exception if not found
	 * or one isn't a boolean.
	 * <p>Accepts "true", "on", "yes" (any case) and "1" as values for true;
	 * treats every other non-empty value as false (i.e. parses leniently).
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static boolean[] getRequiredBooleanParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredBooleanParameters(request, name);
	}


	/**
	 * Get a String parameter, or <code>null</code> if not present.
	 * Throws an exception if it the parameter value is empty.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @return the String value, or <code>null</code> if not present
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String getStringParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		if (request.getParameter(name) == null) {
			return null;
		}
		return getRequiredStringParameter(request, name);
	}

	/**
	 * Get a String parameter, with a fallback value. Never throws an exception.
	 * Can pass a distinguished value to default to enable checks of whether it was supplied.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @param defaultVal the default value to use as fallback
	 */
	public static String getStringParameter(HttpServletRequest request, String name, String defaultVal) {
		try {
			return getRequiredStringParameter(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return defaultVal;
		}
	}

	/**
	 * Get an array of String parameters, return an empty array if not found.
	 * @param request current HTTP request
	 * @param name the name of the parameter with multiple possible values
	 */
	public static String[] getStringParameters(HttpServletRequest request, String name) {
		try {
			return getRequiredStringParameters(request, name);
		}
		catch (ServletRequestBindingException ex) {
			return new String[0];
		}
	}

	/**
	 * Get a String parameter, throwing an exception if it isn't found or is empty.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String getRequiredStringParameter(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredStringParameter(request, name);
	}

	/**
	 * Get an array of String parameters, throwing an exception if not found or one is empty.
	 * @param request current HTTP request
	 * @param name the name of the parameter
	 * @throws ServletRequestBindingException a subclass of ServletException,
	 * so it doesn't need to be caught
	 */
	public static String[] getRequiredStringParameters(HttpServletRequest request, String name)
			throws ServletRequestBindingException {

		return ServletRequestUtils.getRequiredStringParameters(request, name);
	}

}
