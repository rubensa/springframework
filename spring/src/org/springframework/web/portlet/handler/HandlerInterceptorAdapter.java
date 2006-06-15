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

package org.springframework.web.portlet.handler;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletException;

import org.springframework.web.portlet.HandlerInterceptor;
import org.springframework.web.portlet.ModelAndView;

/**
 * Abstract adapter class for the HandlerInterceptor interface,
 * for simplified implementation of pre-only/post-only interceptors.
 *
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @since 2.0
 */
public abstract class HandlerInterceptorAdapter implements HandlerInterceptor {

	/**
	 * This implementation delegates to <code>preHandle</code>.
	 * @see #preHandle
	 */
	public boolean preHandleAction(ActionRequest request, ActionResponse response, Object handler) throws Exception {
		return preHandle(request, response, handler);
	}

	/**
	 * This implementation delegates to <code>afterCompletion</code>.
	 * @see #afterCompletion
	 */
	public void afterActionCompletion(
			ActionRequest request, ActionResponse response, Object handler, Exception ex) throws Exception {

		afterCompletion(request, response, handler, ex);
	}


	/**
	 * This implementation delegates to <code>preHandle</code>.
	 * @see #preHandle
	 */
	public boolean preHandleRender(RenderRequest request, RenderResponse response, Object handler) throws Exception {
		return preHandle(request, response, handler);
	}

	/**
	 * This implementation is empty.
	 */
	public void postHandleRender(
			RenderRequest request, RenderResponse response, Object handler, ModelAndView modelAndView) throws Exception {
	}

	/**
	 * This implementation delegates to <code>afterCompletion</code>.
	 * @see #afterCompletion
	 */
	public void afterRenderCompletion(
			RenderRequest request, RenderResponse response, Object handler, Exception ex) throws Exception {

		afterCompletion(request, response, handler, ex);
	}


	/**
	 * Default callback that both <code>preHandleRender</code>
	 * and <code>preHandleAction</code> delegate to.
	 * <p>This implementation always returns <code>true</code>.
	 * @see #preHandleRender
	 * @see #preHandleAction
	 */
	protected boolean preHandle(PortletRequest request, PortletResponse response, Object handler)
			throws Exception {

		return true;
	}

	/**
	 * Default callback that both <code>preHandleRender</code>
	 * and <code>preHandleAction</code> delegate to.
	 * <p>This implementation is empty.
	 * @see #afterRenderCompletion
	 * @see #afterActionCompletion
	 */
	protected void afterCompletion(
			PortletRequest request, PortletResponse response, Object handler, Exception ex) throws Exception {

	}

}
