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

package org.springframework.web.portlet.mvc;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.HandlerAdapter;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.Controller;

/**
 * Adapter to use the Controller workflow interface with the generic DispatcherPortlet.
 *
 * <p>This is an SPI class, not used directly by application code.
 *
 * @author Rod Johnson
 * @version $Revision$
 * @see org.springframework.web.portlet.DispatcherPortlet
 * @see Controller
 */
public class SimpleControllerHandlerAdapter implements HandlerAdapter {
	
	public boolean supports(Object handler) {
		return (handler instanceof Controller);
	}
	
	public ModelAndView handle(RenderRequest request, RenderResponse response, Object handler)
			throws Exception {
		return ((Controller) handler).handleRequest(request, response);
	}

    public void handle(ActionRequest request, ActionResponse response, Object handler) throws Exception {
        ((Controller) handler).handleRequest(request, response);
    }

}
