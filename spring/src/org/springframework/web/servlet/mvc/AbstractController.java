/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.mvc;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>Convenient superclass for controller implementations, using the Template
 * Method design pattern.</p>
 * <p>As stated in the {@link org.springframework.web.servlet.mvc.Controller Controller}
 * interface, a lot of functionality is already provided by certain abstract
 * base controllers. The AbstractController is one of the most important
 * abstract base controller providing basic features such as the generation
 * of caching headers and the enabling or disabling of
 * supported methods (GET/POST).</p>
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="Controller.html#workflow">and that defined by interface</a>):</b><br>
 * <ol>
 *  <li>{@link #handleRequest(HttpServletRequest,HttpServletResponse) handleRequest()}
 *      will be called by the DispatcherServlet</li>
 *  <li>Inspection of supported methods (ServletException if request method
 *      is not support)</li>
 *  <li>If session is required, try to get it (ServletException if not found)</li>
 *  <li>Set caching headers if needed according to cacheSeconds propery</li>
 *  <li>Call abstract method {@link #handleRequestInternal(HttpServletRequest,HttpServletResponse) handleRequestInternal()},
 *      which should be implemented by extending classes to provide actual
 *      functionality to return {@link org.springframework.web.servlet.ModelAndView ModelAndView} objects.</li>
 * </ol>
 * </p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="Controller.html#config">and those defined by interface</a>):</b><br>
 * <table border="1">
 *  <tr>
 *      <td><b>name</b></th>
 *      <td><b>default</b></td>
 *      <td><b>description</b></td>
 *  </tr>
 *  <tr>
 *      <td>supportedMethods</td>
 *      <td>GET,POST</td>
 *      <td>comma-separated (CSV) list of methods supported by this controller,
 *          such as GET, POST and PUT</td>
 *  </tr>
 *  <tr>
 *      <td>requiresSession</td>
 *      <td>false</td>
 *      <td>whether a session should be required for requests to be able to
 *          be handled by this controller. This ensures, derived controller
 *          can - without fear of Nullpointers - call request.getSession() to
 *          retrieve a session. If no session can be found while processing
 *          the request, a ServletException will be thrown</td>
 *  </tr>
 *  <tr>
 *      <td>cacheSeconds</td>
 *      <td>-1</td>
 *      <td>indicates the amount of seconds to include in the cache header
 *          for the response following on this request. 0 (zero) will include
 *          headers for no caching at all, -1 (the default) will not generate
 *          <i>any headers</i> and any positive number will generate headers
 *          that state the amount indicated as seconds to cache the content</td>
 *  </tr>
 * </table>
 *
 * @author Rod Johnson
 */
public abstract class AbstractController extends WebContentGenerator implements Controller {

	public static final String METHOD_GET = "GET";

	public static final String METHOD_POST = "POST";

	/**
	 * Set of supported methods (GET/POST etc.). GET and POST by default.
	 */
	private Set	supportedMethods;
	
	private boolean requireSession = false;

	private boolean synchronizeOnSession = false;

	private int cacheSeconds = -1;
	
	/**
	 * Create a new Controller supporting GET and POST methods.
	 */
	public AbstractController() {
		this.supportedMethods = new HashSet();
		this.supportedMethods.add(METHOD_GET);
		this.supportedMethods.add(METHOD_POST);
	}
	
	/**
	 * Set supported methods as CSV.
	 * The String[] property editor will get the type right.
	 */
	public final void setSupportedMethods(String[] supportedMethodsArray) throws ApplicationContextException {
		if (supportedMethodsArray == null || supportedMethodsArray.length == 0)
			throw new ApplicationContextException("SupportedMethods must include some methods");
		this.supportedMethods.clear();
		for (int i = 0; i < supportedMethodsArray.length; i++) {
			this.supportedMethods.add(supportedMethodsArray[i]);
			logger.info("Supported request method '" + supportedMethodsArray[i] + "'");
		}
	}
	
	/**
	 * Set if a session should be required to handle requests.
	 */
	public final void setRequireSession(boolean requireSession) {
		this.requireSession = requireSession;
		logger.info("Requires session set to " + requireSession);
	}

	/**
	 * Set if controller execution should be synchronized on the session,
	 * to serialize parallel invocations from the same client.
	 * <p>More specifically, the execution of the handleRequestInternal
	 * method will get synchronized if this flag is true.
	 * @see #handleRequestInternal
	 */
	public void setSynchronizeOnSession(boolean synchronizeOnSession) {
		this.synchronizeOnSession = synchronizeOnSession;
	}

	/**
	 * If 0 disable caching, default is no caching header generation.
	 * Only if this is set to 0 (no cache) or a positive value (cache for this many
	 * seconds) will this class generate cache headers.
	 * They can be overwritten by subclasses anyway, before content is generated.
	 */
	public final void setCacheSeconds(int seconds) {
		this.cacheSeconds = seconds;
		logger.info("Cache seconds set to " + seconds);
	}
	
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		// Check whether we should support the request method
		String method = request.getMethod();
		if (!this.supportedMethods.contains(method)) {
			logger.info("Disallowed '" + method + "' request");
			throw new ServletException("This resource does not support request method '" + method + "'");
		}
		
		// Check whether session was required
		HttpSession session = request.getSession(false);
		if (this.requireSession && session == null) {
			throw new ServletException("This resource requires a pre-existing HttpSession: none was found");
		}

		// Do declarative cache control
		// Revalidate if the controller supports last-modified
		applyCacheSeconds(response, this.cacheSeconds, this instanceof LastModified);

		// If everything's OK, let subclass do its business
		if (this.synchronizeOnSession && session != null) {
			synchronized (session) {
				return handleRequestInternal(request, response);
			}
		}
		else {
			return handleRequestInternal(request, response);
		}
	}

	/**
	 * Template method. Subclasses must implement this.
	 * The contract is the same as for handleRequest.
	 * @see #handleRequest
	 */
	protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException;

}
