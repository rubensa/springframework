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

package org.springframework.web.servlet.mvc.multiaction;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Implementation of MethodNameResolver which supports several strategies for
 * mapping parameter values to the names of methods to invoke.</p>
 * 
 * <p>The simplest strategy looks for a specific named parameter, whose value is
 * considered the name of the method to invoke. The name of the parameter may be
 * specified as a JavaBean property, if the default <code>action</code> is not
 * acceptable.</p>
 * 
 * <p>The second strategy uses the very existence of a request parameter (i.e.
 * a request parameter with a certain name is found) as an indication that a 
 * method with the same name should be dispatched to. In this case, the actual
 * request parameter value is ignored</p>
 * 
 * <p>The second resolution strategy is prmarilly expected to be used with web
 * pages containing multiple submit buttons. The 'name' attribute of each
 * button should be set to the mapped method name, while the 'value' attribute
 * is normally displayed as the button label by the browser, and will be
 * ignored by the resolver.</p>
 * 
 * <p>For use with either strategy, the name of a default handler method to use
 * when there is no match, can be specified as JavaBean properties.</p> 
 * 
 * <p>For both resolution strategies, the method name is of course coming from
 * some sort of view code, (such as a JSP page). While this may be acceptable,
 * it is sometimes desireable to treat this only as a 'logical' method name,
 * with a further mapping to a 'real' method name. As such, an optional 'logical'
 * mapping may be specified for this purpose.<p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @see #setParamName
 * @see #setDefaultMethodName
 * @see #setParamNameList(String[])
 */
public class ParameterMethodNameResolver implements MethodNameResolver {

	public static final String DEFAULT_PARAM_NAME = "action";

	private String paramName = DEFAULT_PARAM_NAME;

	private String defaultMethodName;
	
	private String[] paramNameList = {};
	
	private Properties logicalMappings = new Properties();

	/**
	 * Set the parameter name we're looking for.
	 * Default is "action".
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * Set the name of the default handler method that should be
	 * used when no parameter was found in the request
	 */
	public void setDefaultMethodName(String defaultMethodName) {
		this.defaultMethodName = defaultMethodName;
	}
	
	/**
	 * <p>A list (as a String array) of parameter names, where the very existence of
	 * each parameter in the list (with value ignored) means that a method of the
	 * same name should be resolved. This target method name may then be optionally
	 * further mapped via the {@link #logicalMappings} property, in which case it
	 * can be considered a logical name only.</p>
	 * 
	 * <p>Note that the value of any parameter matching the {@link #paramName} property
	 * will always be applied first if such a parameter exists, in which case this list
	 * will not be consulted.</p>
	 * 
	 * @param an array of mappings
	 */
	public void setParamNameList(String[] mappings) {
		this.paramNameList = mappings;
	}
	
	/**
	 * <p>Specifies a set of optional logical method name mappings. For both resolution
	 * strategies, the method name initially comes in from the view layer. If that needs
	 * to be treated as a 'logical' method name, and mapped to a 'real' method name, then
	 * a name/value pair for that purpose should be added to this Properties instance.
	 * Any method name not found in this mapping will be considered to already be the
	 * real method name.</p>
	 * 
	 * <p>Note that in the case of no match, where the {@link #defaultMethodName} property
	 * is used if available, that method name is considered to already be the real method
	 * name, and is not run through the logical mapping.</p>
	 * 
	 * @param logicalMappings a Properties object mapping logical method names to real method
	 * names. 
	 */
	public void setLogicalMappings(Properties logicalMappings) {
		this.logicalMappings = logicalMappings;
	}

	public String getHandlerMethodName(HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
		String methodName = request.getParameter(this.paramName);
		
		if (methodName == null) {
			for (int i = 0; i < paramNameList.length; ++i) {
				String candidate = paramNameList[i];
				if (request.getParameter(candidate) != null) {
					methodName = candidate;
					break;
				}
			}
		}
		
		if (methodName != null) {
			methodName = logicalMappings.getProperty(methodName, methodName);
		}
		else {
			methodName = this.defaultMethodName;
		}
		
		if (methodName == null) {
			throw new NoSuchRequestHandlingMethodException(request);
		}
		return methodName;
	}
}
