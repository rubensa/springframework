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

package org.springframework.web.portlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.OrderComparator;
import org.springframework.web.portlet.support.PortletController;
import org.springframework.web.portlet.support.PortletModeNameViewController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewRendererServlet;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Concrete front controller for use within the portlet MVC framework.
 * Dispatches to registered controllers for processing portlet requests.
 *
 * <p>This class and the MVC approach it delivers is based on the discussion in Chapter 12 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 *
 * <p>This portlet is very flexible: It can be used with just about any workflow,
 * with the installation of the appropriate adapter classes. It offers the
 * following functionality that distinguishes it from other MVC frameworks:
 *
 * <ul>
 * <li>It is based around a JavaBeans configuration mechanism.
 *
 * <li>It can use any ControllerMapping implementation - whether standard, or provided
 * as part of an application - to control the routing of requests to controller objects.
 * Additional ControllerMapping objects can be added through defining beans in the
 * servlet's application context that implement the ControllerMapping interface in this
 * package. ControllerMappings can be given any bean name (they are tested by type).
 *
 * <li>It can use any ControllerAdapter. Default is SimpleControllerControllerAdapter;
 * additional ControllerAdapter objects can be added through the application context.
 * Like ControllerMappings, ControllerAdapters can be given any bean name (tested by type).
 *
 * <li>Its view resolution strategy can be specified via a ViewResolver implementation.
 * Standard implementations support mapping URLs to bean names, and explicit mappings.
 * The ViewResolver bean name is "viewResolver"; default is InternalResourceViewResolver.
 * TODO: URL to bean viewresolver doesn't makes sense for portlets
 *
 * <li>Its theme resolution strategy is determined by a ThemeResolver implementation.
 * Implementations for a fixed theme and for cookie and session storage are included.
 * The ThemeResolver bean name is "themeResolver"; default is FixedThemeResolver.
 * </ul>
 * TODO: does theme make sense?
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @version $Id$
 * @see ControllerMapping
 * @see ControllerAdapter
 * @see ViewResolver
 * @see org.springframework.web.portlet.context.PortletApplicationContext
 * @see org.springframework.web.portlet.context.FrameworkPortlet
 */
public class DispatcherPortlet extends FrameworkPortlet {

	/**
	 * Well-known name for the ViewResolver object in the bean factory for this namespace.
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

	/**
	 * Request attribute to hold current portlet application context.
	 * Otherwise only the global portlet app context is obtainable by tags etc.
	 */
	public static final String PORTLET_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherPortlet.class.getName() + ".CONTEXT";
	
	/** List of PortletControllerMappings used by this portlet */
	private List controllerMappings;
	
	/** List of PortletControllers used by this portlet */
	private List portletControllers;
	
	/** List of ControllerExceptionResolvers used by this portlet */
	private List portletControllerExceptionResolvers;	

	/** ViewResolver used by this portlet */
	private ViewResolver viewResolver;
	

	/**
	 * Overridden method, invoked after any bean properties have been set and the
	 * PortletApplicationContext and BeanFactory for this namespace is available.
	 * <p>Loads ControllerMapping and ControllerAdapter objects, and configures a
	 * ViewResolver.
	 */
	protected void initFrameworkPortlet() throws PortletException, BeansException {
	    initPortletControllerMappings();
	    initPortletControllers();
	    initPortletControllerExceptionResolvers();
		initViewResolver();
	}

	/**
	 * Initialize the PortletControllerMappings used by this class.
	 * If no PortletController beans are defined in the BeanFactory
	 * for this namespace, we default to PortletModeViewNameMapping.
	 */
	private void initPortletControllerMappings() throws BeansException {
		// find all PortletControllerMappings in the ApplicationContext
		Map matchingBeans = getPortletApplicationContext().getBeansOfType(PortletControllerMapping.class, true, false);
		this.controllerMappings = new ArrayList(matchingBeans.values());
		// Ensure we have at least one PortletController, by registering
		// a default PortletController if no other mappings are found.
		if (this.controllerMappings.isEmpty()) {
			// TODO map view, edit, help 
		    // PortletModeControllerMapping controllerMapping = new PortletModeControllerMapping();
		    
		    
		    // TODO i we need this
			// hm.setApplicationContext(getPortletApplicationContext());
			//this.controllerMappings.add(controllerMapping);
			logger.info("No PortletControllerMappings found for portlet '" + getPortletName() + "': using default");
		}
		else {
			// we keep PortletControllerMappings in sorted order
			Collections.sort(this.controllerMappings, new OrderComparator());
		}
	}

	
	/**
	 * Initialize the PortletControllers used by this class.
	 * If no PortletController beans are defined in the BeanFactory
	 * for this namespace, we default to PortletModeViewNameController.
	 */
	private void initPortletControllers() throws BeansException {
		// find all PortletControllers in the ApplicationContext
		Map matchingBeans = getPortletApplicationContext().getBeansOfType(PortletController.class, true, false);
		this.portletControllers = new ArrayList(matchingBeans.values());
		// Ensure we have at least one PortletController, by registering
		// a default PortletController if no other adapters are found.
		if (this.portletControllers.isEmpty()) {
			this.portletControllers.add(new PortletModeNameViewController());
			//this.portletControllers.add(new ThrowawayControllerPortletController());
			logger.info("No PortletControllers found for portlet '" + getPortletName() + "': using defaults");
		}
		else {
			// we keep PortletControllers in sorted order
			Collections.sort(this.portletControllers, new OrderComparator());
		}
	}
	
	/**
	 * Initialize the PortletControllerExceptionResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to no exception resolver.
	 */
	private void initPortletControllerExceptionResolvers() throws BeansException {
		// find all PortletControllerExceptionResolvers in the ApplicationContext
		Map matchingBeans = getPortletApplicationContext().getBeansOfType(PortletControllerExceptionResolver.class, true, false);
		this.portletControllerExceptionResolvers = new ArrayList(matchingBeans.values());
		// we keep PortletControllerExceptionResolvers in sorted order
		Collections.sort(this.portletControllerExceptionResolvers, new OrderComparator());
	}
	
	
	/**
	 * Initialize the ViewResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to InternalResourceViewResolver.
	 */
	private void initViewResolver() throws BeansException {
		try {
			this.viewResolver = (ViewResolver) getPortletApplicationContext().getBean(VIEW_RESOLVER_BEAN_NAME);
			logger.info("Loaded view resolver [" + viewResolver + "]");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default
			InternalResourceViewResolver vr = new InternalResourceViewResolver();
			vr.setApplicationContext(getPortletApplicationContext());
			this.viewResolver = vr;
			logger.info("Unable to locate view resolver with name '" + VIEW_RESOLVER_BEAN_NAME +
									"': using default [" + this.viewResolver + "]");
		}
	}
	
	private void initControllerMappings() throws BeansException {
	
	
	}

	/**
	 * Return the controller for this request.
	 * Try all controller mappings in order.
	 * @return the controller, or null if no controller could be found
	 */
	private PortletControllerExecutionChain getPortletController(PortletRequest request) throws Exception {
		Iterator itr = this.controllerMappings.iterator();
		while (itr.hasNext()) {
			PortletControllerMapping cm = (PortletControllerMapping) itr.next();
			logger.debug("Testing controller map [" + cm  + "] in DispatcherPortlet with name '" + getPortletName() + "'");
			PortletControllerExecutionChain controller = cm.getPortletController(request);
			if (controller != null)
				return controller;
		}
		return null;
	}

	/**
	 * Render the given ModelAndView. This is the last stage in handling a request.
	 * It may involve resolving the view by name.
	 * @throws Exception if there's a problem rendering the view
	 */
	private void render(ModelAndView mv, RenderRequest request, RenderResponse response)
	    throws Exception {
		View view = null;
		if (mv.isReference()) {
			// we need to resolve this view name
			view = this.viewResolver.resolveViewName(mv.getViewName(), request.getLocale());
		}
		else {
			// no need to lookup: the ModelAndView object contains the actual View object
			view = mv.getView();
		}
		if (view == null) {
			throw new PortletException("Error in ModelAndView object or View resolution encountered by portlet with name '" +
																 getPortletName() + "': View to render cannot be null with ModelAndView [" + mv + "]");
		}
		request.setAttribute(ViewRendererServlet.VIEW_ATTRIBUTE, view);
		request.setAttribute(ViewRendererServlet.MODEL_ATTRIBUTE, mv.getModel());
		request.setAttribute(ViewRendererServlet.DISPATCHER_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, getPortletContextAttributeName());
		PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher(getViewRendererServlet());
		prd.include(request, response);
	}


	/**
	 * Obtain and use the controller for this method.
	 * The controller will be obtained by applying the portlet's ControllerMappings in order.
	 * The ControllerAdapter will be obtained by querying the portlets's
	 * installed ControllerAdapters to find the first that supports the controller class.
	 * All ActionRequests are handled by this method.
	 * It's up to ControllerAdapters to decide which PortletModes are acceptable.
	 */
    protected void doActionService(ActionRequest request, ActionResponse response) throws Exception {
        logger.debug("DispatcherPortlet with name '" + getPortletName() + "' in context [" + request.getContextPath() 
                + "] received an Action request.");

        // make framework objects available for controllers
        request.setAttribute(PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, getPortletApplicationContext());

        ActionRequest processedRequest = request;
        PortletControllerExecutionChain mappedController = null;
        int interceptorIndex = -1;
        try {
                mappedController = getPortletController(processedRequest);
                if (mappedController == null || mappedController.getPortletController() == null) {
                    // if we didn't find a controller
                    //pageNotFoundLogger.warn("No controller mapping for ActionRequest in DispatcherPortlet with name '" + getPortletName() + "'"
                    //        + " in context [" + request.getContextPath() + "].");
                    // TODO how to indicate to portal we have a problem
                    // response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // apply preController methods of registered interceptors
                if (mappedController.getInterceptors() != null) {
                    for (int i = 0; i < mappedController.getInterceptors().length; i++) {
                        PortletControllerInterceptor interceptor = mappedController.getInterceptors()[i];
                        if (!interceptor.preController(processedRequest, response, mappedController.getPortletController())) {
                            triggerAfterCompletion(mappedController, interceptorIndex, processedRequest, response, null);
                            return;
                        }
                        interceptorIndex = i;
                    }
                }

                // actually invoke the controller
                PortletController pc = mappedController.getPortletController();
                pc.handleRequest(processedRequest, response);

            triggerAfterCompletion(mappedController, interceptorIndex, processedRequest, response, null);
        } catch (Exception ex) {
            triggerAfterCompletion(mappedController, interceptorIndex, processedRequest, response, ex);
        }
    }

	/**
	 * Obtain and use the controller for this method.
	 * The controller will be obtained by applying the portlet's ControllerMappings in order.
	 * The ControllerAdapter will be obtained by querying the portlets's
	 * installed ControllerAdapters to find the first that supports the controller class.
	 * All RenderRequests are handled by this method.
	 * It's up to ControllerAdapters to decide which PortletModes are acceptable.
	 */
    protected void doRenderService(RenderRequest request, RenderResponse response) throws Exception {
        logger.debug("DispatcherPortlet with name '" + getPortletName() + "' in context [" + request.getContextPath() 
                + "] received a Render request.");

        // make framework objects available for controllers
        request.setAttribute(PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, getPortletApplicationContext());

        RenderRequest processedRequest = request;
        PortletControllerExecutionChain mappedController = null;
        int interceptorIndex = -1;
        try {
            ModelAndView mv = null;
            try {
                mappedController = getPortletController(processedRequest);
                if (mappedController == null || mappedController.getPortletController() == null) {
                    // if we didn't find a controller
                    //pageNotFoundLogger.warn("No mapping for RenderRequest in DispatcherPortlet with name '" + getPortletName() + "'"
                    //        + " in context [" + request.getContextPath() + "].");
                    // TODO how to indicate to portal we have a problem
                    // response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // apply preHandle methods of registered interceptors
                if (mappedController.getInterceptors() != null) {
                    for (int i = 0; i < mappedController.getInterceptors().length; i++) {
                        PortletControllerInterceptor interceptor = mappedController.getInterceptors()[i];
                        if (!interceptor.preController(processedRequest, response, mappedController.getPortletController())) {
                            triggerAfterCompletion(mappedController, interceptorIndex, processedRequest, response, null);
                            return;
                        }
                        interceptorIndex = i;
                    }
                }

                // actually invoke the controller
                PortletController pc = mappedController.getPortletController();
                mv = pc.handleRequest(processedRequest, response);

                // apply postHandle methods of registered interceptors
                if (mappedController.getInterceptors() != null) {
                    for (int i = mappedController.getInterceptors().length - 1; i >= 0; i--) {
                        PortletControllerInterceptor interceptor = mappedController.getInterceptors()[i];
                        interceptor.postController(processedRequest, response, mappedController.getPortletController(), mv);
                    }
                }
            } catch (ModelAndViewDefiningException ex) {
                logger.debug("ModelAndViewDefiningException encountered", ex);
                mv = ex.getModelAndView();
            } catch (Exception ex) {
                ModelAndView exMv = null;
                PortletController pc = (mappedController != null ? mappedController.getPortletController() : null);
                for (Iterator it = this.portletControllerExceptionResolvers.iterator(); exMv == null && it.hasNext();) {
                    PortletControllerExceptionResolver resolver = (PortletControllerExceptionResolver) it.next();
                    exMv = resolver.resolveException(request, response, pc, ex);
                }
                if (exMv != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("PortletControllerExceptionResolver returned ModelAndView [" + exMv + "] for exception");
                    }
                    logger.warn("PortletController execution resulted in exception - forwarding to resolved error view", ex);
                    mv = exMv;
                } else {
                    throw ex;
                }
            }

            // did the controller return a view to render?
            if (mv != null) {
                logger.debug("Will render view in DispatcherPortlet with name '" + getPortletName() + "'");
                render(mv, processedRequest, response);
            } else {
                logger.debug("Null ModelAndView returned to DispatcherPortlet with name '" + getPortletName()
                        + "': assuming ControllerAdapter completed request handling");
            }

            triggerAfterCompletion(mappedController, interceptorIndex, processedRequest, response, null);
        } catch (Exception ex) {
            triggerAfterCompletion(mappedController, interceptorIndex, processedRequest, response, ex);
        }
    }

	/**
	 * Trigger afterCompletion callbacks on the mapped PortletControllerInterceptors.
	 * Will just invoke afterCompletion for all interceptors whose preController
	 * invocation has successfully completed and returned true.
	 * @param mappedController the mapped PortletControllerExecutionChain
	 * @param interceptorIndex index of last interceptor that successfully completed
	 * @param ex Exception thrown on handler execution, or null if none
	 * @see PortletControllerInterceptor#afterCompletion
	 */
	private void triggerAfterCompletion(PortletControllerExecutionChain mappedController, int interceptorIndex,
																					 PortletRequest request, PortletResponse response,
																					 Exception ex) throws Exception {
		// apply afterCompletion methods of registered interceptors
		Exception currEx = ex;
		if (mappedController != null) {
			if (mappedController.getInterceptors() != null) {
				for (int i = interceptorIndex; i >=0; i--) {
					PortletControllerInterceptor interceptor = mappedController.getInterceptors()[i];
					try {
						interceptor.afterCompletion(request, response, mappedController.getPortletController(), ex);
					}
					catch (Exception ex2) {
						if (currEx != null) {
							logger.error("Exception overridden by PortletControllerInterceptor.afterCompletion exception", currEx);
						}
						currEx = ex2;
					}
				}
			}
		}
		if (currEx != null) {
			throw currEx;
		}
	}

    
}
