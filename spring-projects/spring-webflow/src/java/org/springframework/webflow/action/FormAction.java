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
package org.springframework.webflow.action;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.binding.format.support.LabeledEnumFormatter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.PropertyEditorRegistrar;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.util.DispatchMethodInvoker;

/**
 * Multi-action that implements logic dealing with input forms: form setup and
 * bind & validate. Two action execution methods are provided:
 * <ul>
 * <li> {@link #setupForm(RequestContext)} - Prepares a form object for display
 * in a new form. This will initialize the binder so that all custom property
 * editors are available for use in the new form. This action method will return
 * (signal) the success() event if there are no setup errors, otherwise it will
 * return the error() event. </li>
 * <li> {@link #bindAndValidate(RequestContext)} - Bind all incoming event
 * parameters to the form object and validate the form object using a
 * registered validator. This action method will return (signal) the success()
 * event if there are no binding or validation errors, otherwise it will return
 * the error() event. </li>
 * </ul>
 * Since this is a multi-action, a subclass could add any number of additional
 * action execution methods, e.g. an "onSubmit()".
 * <p>
 * Using this action, it becomes very easy to implement form preparation and
 * submission logic in your flow:
 * <ol>
 * <li> Start of with an action state called "setupForm". This will invoke
 * {@link #setupForm(RequestContext) setupForm} to prepare the new form for
 * display. </li>
 * <li> Now show the form using a view state. </li>
 * <li> Go to an action state called "bindAndValidate" when the form is
 * submitted. This will invoke
 * {@link #bindAndValidate(RequestContext) bindAndValidate} to bind incoming
 * event data to the form object and validate the form object. If there are
 * binding or validation errors, go back to the previous view state to redisplay
 * the form with error messages. </li>
 * <li> If binding and validation was successful, go to an action state called
 * "onSubmit" (or any other appropriate name). This will invoke an action method
 * called "onSubmit" you must provide on a subclass to process form submission,
 * e.g. interacting with the business logic. </li>
 * <li> If business processing is ok, contine to a view state to display the
 * success view. </li>
 * </ol>
 * <p>
 * The most important hook method provided by this class is the method
 * {@link #initBinder(RequestContext, DataBinder) initBinder}. This will be
 * called after a new data binder is created by both
 * {@link #setupForm(RequestContext) setupForm} and
 * {@link #bindAndValidate(RequestContext) bindAndValidate}. It allows you to
 * register any custom property editors required by the form and form object.
 * <p>
 * Note that this action does not provide a <i>referenceData()</i> hook method
 * similar to that of the <code>SimpleFormController</code>. If you need to
 * set up reference data you should create a separate state in your flow to do
 * just that and make sure you pass through that state before showing the form
 * view. Note that you can add the method that handles this reference data
 * setup logic to a subclass of this class since this is a multi-action! Typically
 * you would define an action execute method like
 * <pre>
 *    public Event setupReferenceData(RequestContext context) throws Exception
 * </pre>
 * in that case.
 * <p>
 * <b>Exposed configuration properties</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>formObjectName</td>
 * <td>"formObject"</td>
 * <td>The name of the form object. The form object will be
 * set in the configured scope using this name. </td>
 * </tr>
 * <tr>
 * <td>formObjectClass</td>
 * <td>null</td>
 * <td>The form object class for this action. An instance of this class will
 * get populated and validated. </td>
 * </tr>
 * <tr>
 * <td>formObjectScope</td>
 * <td>{@link org.springframework.webflow.ScopeType#REQUEST request}</td>
 * <td>The scope in which the form object will be put. If put in flow scope the
 * object will be cached and reused over the life of the flow, preserving previous
 * values.  Request scope will cause a new fresh form object instance to be created
 * each execution.</td>
 * </tr>
 * <tr>
 * <td>errorsScope</td>
 * <td>{@link org.springframework.webflow.ScopeType#REQUEST request}</td>
 * <td>The scope in which the form object errors instance will be put.
 * If put in flow scope the errors will be cached and reused over the life
 * of the flow.  Request scope will cause a new errors instance to be created
 * each execution.</td>
 * </tr>
 * <tr>
 * <td>propertyEditorRegistrar</td>
 * <td>null</td>
 * <td>The strategy used to register custom property editors with the data
 * binder. This is an alternative to overriding the
 * {@link #initBinder(RequestContext, DataBinder) initBinder} hook method. </td>
 * </tr>
 * <tr>
 * <td>validator</td>
 * <td>null</td>
 * <td>The validator for this action. The validator must support the
 * specified form object class. </td>
 * </tr>
 * <tr>
 * <td>bindOnSetupForm</td>
 * <td>false</td>
 * <td>Set if request parameters should be bound to the form object during the
 * {@link #setupForm(RequestContext) setupForm} action. </td>
 * </tr>
 * <tr>
 * <td>validateOnBinding</td>
 * <td>true</td>
 * <td>Indicates if the validator should get applied when binding. </td>
 * </tr>
 * <tr>
 * <td>messageCodesResolver</td>
 * <td>null</td>
 * <td>Set the strategy to use for resolving errors into message codes. </td>
 * </tr>
 * </table>
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FormAction extends MultiAction implements InitializingBean {

	/**
	 * Optional property that identifies the method that should be invoked on the 
	 * configured validator instance, to support piecemeal wizard page validation.
	 */
	public static final String VALIDATOR_METHOD_PROPERTY = "validatorMethod";
	
	/**
	 * The name the form object should be exposed under.
	 */
	private String formObjectName = FormObjectAccessor.FORM_OBJECT_ALIAS;

	/**
	 * The type of form object - typically a instantiable class. 
	 */
	private Class formObjectClass;

	/**
	 * The scope in which the form object should be exposed. 
	 */
	private ScopeType formObjectScope = ScopeType.REQUEST;

	/**
	 * The scope in which the form object errors holder should be exposed.
	 */
	private ScopeType errorsScope = ScopeType.REQUEST;

	/**
	 * A centralized service for property editor registration, for type conversion during
	 * form object data binding. 
	 */
	private PropertyEditorRegistrar propertyEditorRegistrar;

	/**
	 * A validator for the form's form object.
	 */
	private Validator validator;

	/**
	 * Should binding from event parameters happen on form setup?
	 */
	private boolean bindOnSetupForm = false;

	/**
	 * Should validation happen after data binding?
	 */
	private boolean validateOnBinding = true;

	/**
	 * Strategy for resolving error message codes.
	 */
	private MessageCodesResolver messageCodesResolver;

	/**
	 * A cache for dispatched action execute methods.
	 */
	private DispatchMethodInvoker validateMethodDispatcher = new DispatchMethodInvoker();

	/**
	 * Return the name of the form object in the flow scope.
	 */
	public String getFormObjectName() {
		return this.formObjectName;
	}

	/**
	 * Set the name of the form object in the flow scope. The form object object
	 * will be included in the flow scope under this name.
	 */
	public void setFormObjectName(String formObjectName) {
		this.formObjectName = formObjectName;
	}

	/**
	 * Return the form object class for this action.
	 */
	public Class getFormObjectClass() {
		return this.formObjectClass;
	}

	/**
	 * Set the form object class for this action. An instance of this class will
	 * get populated and validated.
	 */
	public void setFormObjectClass(Class formObjectClass) {
		this.formObjectClass = formObjectClass;
		this.validateMethodDispatcher.setParameterTypes(new Class[] { this.formObjectClass, Errors.class });
	}

	/**
	 * Get the scope in which the form object will be placed. Can be either flow
	 * scope or request scope. Defaults to request scope.
	 */
	public ScopeType getFormObjectScope() {
		return this.formObjectScope;
	}

	/**
	 * Set the scope in which the form object will be placed. Can be either flow
	 * scope or request scope.
	 */
	public void setFormObjectScope(ScopeType scopeType) {
		this.formObjectScope = scopeType;
	}

	/**
	 * Convenience setter that performs a string to ScopeType conversion for
	 * you.
	 * @param encodedScopeType the encoded scope type string
	 * @throws InvalidFormatException the encoded value was invalid
	 */
	public void setFormObjectScopeAsString(String encodedScopeType) throws InvalidFormatException {
		this.formObjectScope = (ScopeType)new LabeledEnumFormatter().parseValue(encodedScopeType, ScopeType.class);
	}

	/**
	 * Get the scope in which the Errors object will be placed. Can be either
	 * flow scope ore request scope. Defaults to request scope.
	 */
	public ScopeType getErrorsScope() {
		return errorsScope;
	}

	/**
	 * Set the scope in which the Errors object will be placed. Can be either
	 * flow scope ore request scope. Defaults to request scope.
	 */
	public void setErrorsScope(ScopeType errorsScope) {
		this.errorsScope = errorsScope;
	}

	/**
	 * Convenience setter that performs a string to ScopeType conversion for
	 * you.
	 * @param encodedScopeType the encoded scope type string
	 * @throws InvalidFormatException the encoded value was invalid
	 */
	public void setErrorsScopeAsString(String encodedScopeType) throws InvalidFormatException {
		this.errorsScope = (ScopeType)new LabeledEnumFormatter().parseValue(encodedScopeType, ScopeType.class);
	}

	/**
	 * Get the property editor registration strategy for this action's data
	 * binders.
	 */
	public PropertyEditorRegistrar getPropertyEditorRegistrar() {
		return propertyEditorRegistrar;
	}

	/**
	 * Set a property editor registration strategy for this action's data
	 * binders. This is an alternative to overriding the initBinder() method.
	 */
	public void setPropertyEditorRegistrar(PropertyEditorRegistrar propertyEditorRegistrar) {
		this.propertyEditorRegistrar = propertyEditorRegistrar;
	}

	/**
	 * Returns the validator for this action.
	 */
	public Validator getValidator() {
		return validator;
	}

	/**
	 * Set the validator for this action. The validator must support the
	 * specified form object class.
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
		this.validateMethodDispatcher.setTarget(validator);
	}

	/**
	 * Returns if event parameters should be bound to the form object during the
	 * {@link #setupForm(RequestContext)} action.
	 * @return bind on setup form
	 */
	public boolean isBindOnSetupForm() {
		return this.bindOnSetupForm;
	}
	
	/**
	 * Set if event parameters should be bound to the form object during the
	 * {@link #setupForm(RequestContext)} action.
	 */
	public void setBindOnSetupForm(boolean bindOnNewForm) {
		this.bindOnSetupForm = bindOnNewForm;
	}

	/**
	 * Return if the validator should get applied when binding. Defaults to
	 * true.
	 */
	public boolean isValidateOnBinding() {
		return validateOnBinding;
	}

	/**
	 * Set if the validator should get applied when binding.
	 */
	public void setValidateOnBinding(boolean validateOnBinding) {
		this.validateOnBinding = validateOnBinding;
	}

	/**
	 * Return the strategy to use for resolving errors into message codes.
	 */
	public MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver;
	}

	/**
	 * Set the strategy to use for resolving errors into message codes. Applies
	 * the given strategy to all data binders used by this action.
	 * <p>
	 * Default is null, i.e. using the default strategy of the data binder.
	 * @see #createBinder(RequestContext, Object)
	 * @see org.springframework.validation.DataBinder#setMessageCodesResolver(org.springframework.validation.MessageCodesResolver)
	 */
	public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
		this.messageCodesResolver = messageCodesResolver;
	}

	protected void initAction() {
		if (getValidator() != null) {
			if (getFormObjectClass() != null && !getValidator().supports(getFormObjectClass())) {
				throw new IllegalArgumentException("Validator [" + getValidator()
						+ "] does not support form object class [" + getFormObjectClass() + "]");
			}
		}
	}

	// action execute methods

	/**
	 * Prepares a form object for display in a new form. This will initialize
	 * the binder so that all custom property editors are available for use in
	 * the new form.
	 * <p>
	 * If the setupBindingEnabled method returns true a bind and validate step will be
	 * executed to pre-populate the new form with incoming event parameters.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @return success() when binding and validation is successful, error()
	 *         otherwise
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	public Event setupForm(RequestContext context) throws Exception {
		Object formObject = getFormObject(context);
		if (formObject == null) {
			formObject = createFormObject(context);
			exposeFormObject(context, formObject);
			exposeEmptyErrors(context, formObject);
		}
		if (setupBindingEnabled(context)) {
			return doBindAndValidate(context, formObject);
		}
		else {
			return success();
		}
	}

	/**
	 * Bind all incoming request parameters to the form object and validate the
	 * form object using a registered validator.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @return "success" when binding and validation is successful, "error"
	 *         otherwise
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	public Event bindAndValidate(RequestContext context) throws Exception {
		return doBindAndValidate(context, loadFormObject(context));
	}

	/**
	 * Internal helper method to do bind and validate logic.
	 */
	private Event doBindAndValidate(RequestContext context, Object formObject) throws Exception {
		DataBinder binder = createBinder(context, formObject);
		Event result = bindAndValidateInternal(context, binder);
		exposeFormObject(context, formObject);
		exposeErrors(context, binder.getErrors());
		return result != null ? result : calculateResult(context, formObject, binder.getErrors());
	}
	
	/**
	 * Load the backing form object that should be updated from incoming event
	 * parameters and validated. Throws an exception if the object could not be
	 * loaded.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @return the form object
	 * @throws FormObjectRetrievalFailureException the form object could not be
	 *         loaded
	 * @throws IllegalStateException the form object loaded was
	 *         <code>null</code>
	 */
	protected Object loadRequiredFormObject(RequestContext context) throws FormObjectRetrievalFailureException,
			IllegalStateException {
		// get the form object
		Object formObject = loadFormObject(context);
		Assert.state(formObject != null, "The loaded form object cannot be null");
		return formObject;
	}

	/**
	 * Load the backing form object that should be updated from incoming event
	 * parameters and validated. By default, will attempt to instantiate a new
	 * form object instance transiently in memory if not already present in the
	 * configured scope.
	 * <p>
	 * Subclasses should override if they need to load the form object from a
	 * specific location or resource such as a database or filesystem.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @return the form object
	 * @throws FormObjectRetrievalFailureException the form object could not be
	 *         loaded
	 */
	protected Object loadFormObject(RequestContext context) throws FormObjectRetrievalFailureException {
		Object formObject = getFormObject(context);
		if (formObject != null) {
			return formObject;
		}
		else {
			return createFormObject(context);
		}
	}
	
	/**
	 * Convenience method that returns the form object for this form action.
	 * @param context the flow request context
	 * @return the form object, or <code>null</code> if not found
	 */
	protected Object getFormObject(RequestContext context) {
		return getFormObjectAccessor(context).getFormObject(getFormObjectName(), getFormObjectClass(), getFormObjectScope());
	}
	
	/**
	 * Factory method that returns a new form object accessor for accessing form objects 
	 * in the provided request context.
	 * @param context the flow request context
	 * @return the accessor
	 */
	protected FormObjectAccessor getFormObjectAccessor(RequestContext context) {
		return new FormObjectAccessor(context);
	}
	
	/**
	 * Create a new form object instance of the configured class.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @return the new form object instance
	 * @throws FormObjectRetrievalFailureException if the form object class could not be
	 *         instantiated or if the form object class or its constructor is not accessible
	 */
	protected Object createFormObject(RequestContext context) throws FormObjectRetrievalFailureException {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new form object '" + getFormObjectName() + "'");
		}
		try {
			if (this.formObjectClass == null) {
				throw new IllegalStateException("Cannot create form object without formObjectClass being set -- "
						+ "either set formObjectClass, override loadFormObject or createFormObject");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Creating new form object of class [" + this.formObjectClass.getName() + "]");
			}
			return this.formObjectClass.newInstance();
		}
		catch (InstantiationException e) {
			throw new FormObjectRetrievalFailureException(getFormObjectClass(), getFormObjectName(),
					"Unable to instantiate form object", e);
		}
		catch (IllegalAccessException e) {
			throw new FormObjectRetrievalFailureException(getFormObjectClass(), getFormObjectName(),
					"Unable to access form object class constructor", e);
		}
	}

	/**
	 * Create a new binder instance for the given form object and request
	 * context. Can be overridden to plug in custom DataBinder subclasses.
	 * <p>
	 * Default implementation creates a standard WebDataBinder, and invokes
	 * initBinder. Note that initBinder will not be invoked if you override this
	 * method!
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @param formObject the form object to bind onto
	 * @return the new binder instance
	 * @see #initBinder(RequestContext, DataBinder)
	 */
	protected DataBinder createBinder(RequestContext context, Object formObject) {
		DataBinder binder = new WebDataBinder(formObject, getFormObjectName());
		if (this.messageCodesResolver != null) {
			binder.setMessageCodesResolver(this.messageCodesResolver);
		}
		initBinder(context, binder);
		return binder;
	}

	/**
	 * Bind the parameters of the last event in given request context to the
	 * given form object using given data binder.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @param binder the binder to use for binding
	 * @return the action result outcome
	 */
	protected Event bindAndValidateInternal(RequestContext context, DataBinder binder) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Binding allowed matching event parameters for event " + context.getLastEvent() + " to object '" + binder.getObjectName()
					+ "', details='" + binder.getTarget() + "'");
		}
		binder.bind(new MutablePropertyValues(context.getLastEvent().getParameters()));
		onBind(context, binder.getTarget(), binder.getErrors());
		if (logger.isDebugEnabled()) {
			logger.debug("Binding completed for object '" + binder.getObjectName() + "', details='"
					+ binder.getTarget() + "'");
		}
		if (getValidator() != null && isValidateOnBinding() && validationEnabled(context)) {
			validate(context, binder.getTarget(), binder.getErrors());
		}
		else {
			if (logger.isDebugEnabled()) {
				if (validator == null) {
					logger.debug("No validator is configured; no additional validation will occur");
				}
				else {
					logger.debug("Validation was disabled for this request; details: validateOnBinding=" +
							isValidateOnBinding() + ", validationEnabled=" + validationEnabled(context));
				}
			}
		}
		return onBindAndValidate(context, binder.getTarget(), binder.getErrors());
	}

	/**
	 * Validate given form object using a registered validator. If a "validatorMethod"
	 * action property is specified for the currently executing action state action,
	 * the identified validator method will be invoked. When no such property is found,
	 * the defualt <code>validate()</code> method is invoked.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @param formObject the form object
	 * @param errors possible binding errors
	 */
	protected void validate(RequestContext context, Object formObject, Errors errors) throws Exception {
		String validatorMethod = (String)context.getProperties().getAttribute(VALIDATOR_METHOD_PROPERTY);
		if (StringUtils.hasText(validatorMethod)) {
			invokeValidatorMethod(validatorMethod, formObject, errors);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking validator: " + validator);
			}
			getValidator().validate(formObject, errors);
		}
	}

	/**
	 * Invoke specified validator method on the validator registered with this
	 * action.
	 * @param validatorMethod the name of the validator method to invoke
	 * @param formObject the form object
	 * @param errors possible binding errors
	 */
	protected void invokeValidatorMethod(String validatorMethod, Object formObject, Errors errors) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking piecemeal validator method '" + validatorMethod + "' on form object: " + formObject);
		}
		this.validateMethodDispatcher.dispatch(validatorMethod, new Object[] { formObject, errors });
	}

	/**
	 * Expose the form object in the model of the currently executing flow.
	 * @param context the flow execution request context
	 * @param formObject the form object
	 */
	protected void exposeFormObject(RequestContext context, Object formObject) {
		getFormObjectAccessor(context).exposeFormObject(formObject, getFormObjectName(), getFormObjectScope());
	}

	/**
	 * Expose the errors collection in the model of the currently executing flow.
	 * @param context the flow execution request context
	 * @param errors the errors
	 */
	protected void exposeErrors(RequestContext context, Errors errors) {
		getFormObjectAccessor(context).exposeErrors(errors, getErrorsScope());
	}

	/**
	 * Expose an empty errors collection in the model of the currently executing flow.
	 * @param context the flow execution request context
	 * @param formObject the object
	 */
	protected void exposeEmptyErrors(RequestContext context, Object formObject) {
		getFormObjectAccessor(context).exposeEmptyErrors(formObject, getFormObjectName(), getErrorsScope());
	}

	/**
	 * Get the default action result for this action; this implementation
	 * returns error() if the binder has errors, success() otherwise. Subclasses
	 * may overrride.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @param formObject the form object
	 * @param errors possible binding errors
	 * @return success() when there are no binding errors, error() otherwise
	 */
	protected Event calculateResult(RequestContext context, Object formObject, Errors errors) {
		return errors.hasErrors() ? error() : success();
	}

	// subclassing hook methods

	/**
	 * Returns true if event parameters should be bound to the form object during
	 * the {@link #setupForm(RequestContext)} action. The defautl implementation just
	 * calls {@link #isBindOnSetupForm()}.
	 */
	protected boolean setupBindingEnabled(RequestContext context) {
		return isBindOnSetupForm();
	}

	/**
	 * Return whether validation should be performed given the state of the flow request
	 * context.
	 * <p>
	 * Default implementation always returns true. Can be overridden
	 * in subclasses to test validation, for example, if a special
	 * event parameter is set.
	 * @param context the request context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @return whether or not validation is enabled
	 */
	protected boolean validationEnabled(RequestContext context) {
		return true;
	}

	/**
	 * Callback for custom post-processing in terms of binding. Called on each
	 * submit, after standard binding but before validation.
	 * <p>
	 * Default implementation is empty.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @param formObject the form object
	 * @param errors validation errors holder, allowing for additional custom
	 *        registration of binding errors
	 */
	protected void onBind(RequestContext context, Object formObject, Errors errors) {
	}

	/**
	 * Callback for custom post-processing in terms of binding and validation.
	 * Called on each submit, after standard binding and validation, but before
	 * error evaluation. Subclasses may optionally return an action result to
	 * supercede the default result event, which will be success() or error()
	 * depending on whether or not there are binding errors.
	 * <p>
	 * Default implementation will call onBindAndValidateSuccess() if given
	 * errors instance does not have errors. Otherwise [null] will be returned,
	 * which indicates that the default action result calculated by the
	 * getDefaultActionResult() method will be used.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @param formObject the form object
	 * @param errors validation errors holder, allowing for additional custom
	 *        registration of binding errors
	 * @return the action result
	 */
	protected Event onBindAndValidate(RequestContext context, Object formObject, Errors errors) {
		if (!errors.hasErrors()) {
			return onBindAndValidateSuccess(context, formObject, errors);
		}
		return null;
	}

	/**
	 * Hook called when binding and validation completed successfully;
	 * subclasses may optionally return an action result to supercede the
	 * default result event, which will be success().
	 * <p>
	 * Default implementation just returns null.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @param formObject the form object
	 * @param errors validation errors holder, allowing for additional custom
	 *        registration of binding errors
	 * @return the action result
	 */
	protected Event onBindAndValidateSuccess(RequestContext context, Object formObject, Errors errors) {
		return null;
	}

	/**
	 * Initialize the given binder instance, for example with custom editors.
	 * Called by createBinder().
	 * <p>
	 * This method allows you to register custom editors for certain fields of
	 * your form object. For instance, you will be able to transform Date
	 * objects into a String pattern and back, in order to allow your JavaBeans
	 * to have Date properties and still be able to set and display them in an
	 * HTML interface.
	 * <p>
	 * Default implementation will simply call registerCustomEditors on any
	 * propertyEditorRegistrar object that has been set for the action.
	 * <p>
	 * The request context may be used to feed reference data to any property
	 * editors, although it may be better (in the interest of not bloating the
	 * session, to have the editors get this from somewhere else).
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @param binder new binder instance
	 * @see #createBinder(RequestContext, Object)
	 */
	protected void initBinder(RequestContext context, DataBinder binder) {
		if (propertyEditorRegistrar != null) {
			propertyEditorRegistrar.registerCustomEditors(binder);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No property editor registrar set, no custom editors to register");
			}
		}
	}
	
	/**
	 * Resets the form by clearing out the formObject in the specified scope and
	 * reloading it by calling loadFormObject.
	 * @param context the request context
	 * @return success if the reset action completed successfully
	 * @throws Exception if an exception occured
	 */
	public Event resetForm(RequestContext context) throws Exception {
		removeFormObject(context);
		Object formObject = loadFormObject(context);
		exposeFormObject(context, formObject);
		exposeEmptyErrors(context, formObject);
		return success();
	}
	
	/**
	 * Removes the form object and errors collection from the specified scopes.
	 * @param context the context
	 */
	protected void removeFormObject(RequestContext context) {
		getFormObjectAccessor(context).exposeFormObject(null, getFormObjectName(), getFormObjectScope());
		getFormObjectAccessor(context).exposeErrors(null, getErrorsScope());
	}
}