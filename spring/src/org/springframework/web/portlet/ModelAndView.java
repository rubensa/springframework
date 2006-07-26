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

package org.springframework.web.portlet;

import java.util.Map;

import org.springframework.ui.ModelMap;

/**
 * Holder for both Model and View in the web MVC framework.
 * Note that these are entirely distinct. This class merely holds
 * both to make it possible for a controller to return both model
 * and view in a single return value.
 *
 * <p>Represents a model and view returned by a handler, to be resolved
 * by a DispatcherPortlet. The view can take the form of a String
 * view name which will need to be resolved by a ViewResolver object;
 * alternatively a view object can be specified directly. The model
 * is a Map, allowing the use of multiple objects keyed by name.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.portlet.DispatcherPortlet
 * @see org.springframework.web.servlet.ViewResolver
 * @see org.springframework.web.portlet.HandlerAdapter
 * @see org.springframework.web.portlet.mvc.Controller
 */
public class ModelAndView {

	/** View instance or view name String */
	private Object view;

	/** Model Map */
	private ModelMap model;

	/**
	 * Indicates whether or not this instance has been cleared with a call to {@link #clear()}.
	 */
	private boolean cleared;


	/**
	 * Default constructor for bean-style usage: populating bean
	 * properties instead of passing in constructor arguments.
	 * @see #setView(Object)
	 * @see #setViewName(String)
	 */
	public ModelAndView() {
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * Can also be used in conjunction with <code>addObject</code>.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherPortlet's ViewResolver
	 * @see #addObject
	 */
	public ModelAndView(String viewName) {
		this.view = viewName;
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * Can also be used in conjunction with <code>addObject</code>.
	 * @param view View object to render (usually a Servlet MVC View object)
	 * @see #addObject
	 */
	public ModelAndView(Object view) {
		this.view = view;
	}

	/**
	 * Create a new ModelAndView given a view name and a model.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherPortlet's ViewResolver
	 * @param model Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be <code>null</code>, but the
	 * model Map may be <code>null</code> if there is no model data.
	 */
	public ModelAndView(String viewName, Map model) {
		this.view = viewName;
		if (model != null) {
			getModelMap().addAllObjects(model);
		}
	}

	/**
	 * Create a new ModelAndView given a View object and a model.
	 * @param view View object to render (usually a Servlet MVC View object)
	 * @param model Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be <code>null</code>, but the
	 * model Map may be <code>null</code> if there is no model data.
	 */
	public ModelAndView(Object view, Map model) {
		this.view = view;
		if (model != null) {
			getModelMap().addAllObjects(model);
		}
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherPortlet's ViewResolver
	 * @param modelName name of the single entry in the model
	 * @param modelObject the single model object
	 */
	public ModelAndView(String viewName, String modelName, Object modelObject) {
		this.view = viewName;
		addObject(modelName, modelObject);
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param view View object to render (usually a Servlet MVC View object)
	 * @param modelName name of the single entry in the model
	 * @param modelObject the single model object
	 */
	public ModelAndView(Object view, String modelName, Object modelObject) {
		this.view = view;
		addObject(modelName, modelObject);
	}


	/**
	 * Set a view name for this ModelAndView, to be resolved by the
	 * DispatcherPortlet via a ViewResolver. Will override any
	 * pre-existing view name or View.
	 */
	public void setViewName(String viewName) {
		this.view = viewName;
	}

	/**
	 * Return the view name to be resolved by the DispatcherPortlet
	 * via a ViewResolver, or <code>null</code> if we are using a view object.
	 */
	public String getViewName() {
		return (this.view instanceof String ? (String) this.view : null);
	}

	/**
	 * Set a View object for this ModelAndView. Will override any
	 * pre-existing view name or View.
	 * <p>The given View object will usually be a Servlet MVC View object.
	 * This is nevertheless typed as Object to avoid a Servlet API dependency
	 * in the Portlet ModelAndView class.
	 */
	public void setView(Object view) {
		this.view = view;
	}

	/**
	 * Return the View object, or <code>null</code> if we are using a view name
	 * to be resolved by the DispatcherPortlet via a ViewResolver.
	 */
	public Object getView() {
		return (!(this.view instanceof String) ? this.view : null);
	}

	/**
	 * Indicate whether or not this <code>ModelAndView</code> has a view, either
	 * as a view name or as a direct view instance.
	 */
	public boolean hasView() {
		return (this.view != null);
	}

	/**
	 * Return whether we use a view reference, i.e. <code>true</code>
	 * if the view has been specified via a name to be resolved by the
	 * DispatcherPortlet via a ViewResolver.
	 */
	public boolean isReference() {
		return (this.view instanceof String);
	}

	/**
	 * Return the model map. May return null.
	 * Called by DispatcherPortlet for evaluation of the model.
	 */
	protected Map getModelInternal() {
		return this.model;
	}

	/**
	 * Return the underlying <code>ModelMap</code> instance (never <code>null</code>).
	 */
	public ModelMap getModelMap() {
		if (this.model == null) {
			this.model = new ModelMap();
		}
		return this.model;
	}

	/**
	 * Return the model map. Never returns <code>null</code>.
	 * To be called by application code for modifying the model.
	 */
	public Map getModel() {
		return getModelMap();
	}


	/**
	 * Add an object to the model using parameter name generation.
	 * @param modelObject the object to add to the model (never <code>null</code>)
	 * @see ModelMap#addObject(Object)
	 */
	public ModelAndView addObject(Object modelObject) {
		getModelMap().addObject(modelObject);
		return this;
	}

	/**
	 * Add an object to the model.
	 * @param modelName name of the object to add to the model
	 * @param modelObject object to add to the model (never <code>null</code>)
	 * @return this ModelAndView, convenient to allow usages like
	 * return modelAndView.addObject("foo", bar);
	 */
	public ModelAndView addObject(String modelName, Object modelObject) {
		getModelMap().addObject(modelName, modelObject);
		return this;
	}

	/**
	 * Add all entries contained in the provided map to the model.
	 * @param modelMap a map of modelName -> modelObject pairs
	 * @return this ModelAndView, convenient to allow usages like
	 * return modelAndView.addAllObjects(myModelMap);
	 */
	public ModelAndView addAllObjects(Map modelMap) {
		getModelMap().addAllObjects(modelMap);
		return this;
	}


	/**
	 * Clear the state of this ModelAndView object.
	 * The object will be empty afterwards.
	 * <p>Can be used to suppress rendering of a given ModelAndView object
	 * in the <code>postHandleRender</code> method of a HandlerInterceptor.
	 * @see #isEmpty()
	 * @see HandlerInterceptor#postHandleRender
	 */
	public void clear() {
		this.view = null;
		this.model = null;
		this.cleared = true;
	}

	/**
	 * Return whether this ModelAndView object is empty
	 * i.e. whether it does not hold any view and does not contain a model.
	 */
	public boolean isEmpty() {
		return (this.view == null && this.model == null);
	}

	/**
	 * Return whether this ModelAndView object is empty as a result of a call to {@link #clear}
	 * i.e. whether it does not hold any view and does not contain a model.
	 * Returns <code>false</code> if any additional state was added to the instance
	 * <strong>after</strong> the call to {@link #clear}.
	 * @see #clear()
	 */
	public boolean wasCleared() {
		return (this.cleared && isEmpty());
	}


	/**
	 * Return diagnostic information about this model and view.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ModelAndView: ");
		if (isReference()) {
			buf.append("reference to view with name '").append(this.view).append("'");
		}
		else {
			buf.append("materialized View is [").append(this.view).append(']');
		}
		buf.append("; model is ").append(this.model);
		return buf.toString();
	}

}
