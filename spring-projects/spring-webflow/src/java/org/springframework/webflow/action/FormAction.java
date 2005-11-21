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
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
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
 * Multi-action that implements common logic dealing with input forms.
 * <p>
 * Several action execution methods are provided:
 * <ul>
 * <li> {@link #exposeFormObject(RequestContext)} - Loads the backing form
 * object and exposes it and an empty errors instance in the model of the
 * executing flow in the correct scope. Any custom property editors for
 * formatting form object values will also be installed. This action method will
 * return success() if the form object was loaded successfully, error()
 * otherwise.
 * <li> {@link #setupForm(RequestContext)} - Prepares the backing form object
 * for display on a form. This method behaves exactly like exposeFormObject but
 * goes further by adding a capability to perform optional data binding on
 * setup. This action method will return the success() event if there are no
 * setup errors, otherwise it will return the error() event. </li>
 * <li> {@link #bindAndValidate(RequestContext)} - Binds all incoming event
 * parameters to the form object and validates the form object using a
 * registered validator. This action method will return the success() event if
 * there are no binding or validation errors, otherwise it will return the
 * error() event. </li>
 * <li> {@link #bind(RequestContext)} - Binds all incoming event parameters to
 * the form object. No additional validation is performed. This action method
 * will return the success() event if there are no binding errors, otherwise it
 * will return the error() event. </li>
 * <li> {@link #validate(RequestContext)} - Validates the form object using a
 * registered validator. No data binding is performed. This action method will
 * return the success() event if there are no validation errors, otherwise it
 * will return the error() event. </li>
 * <li> {@link #resetForm(RequestContext)} - Resets the form by reloading the
 * backing form object and reinstalling any custom property editors. Returns
 * success() on completion, error() if a form object load failure occurs. </li>
 * </ul>
 * <p>
 * Since this is a multi-action, a subclass could add any number of additional
 * action execution methods, e.g. "setupReferenceData(RequestContext)", or
 * "processSubmit(RequestContext)".
 * <p>
 * Using this action, it becomes very easy to implement form preparation and
 * submission logic in your flow. One way to do this follows:
 * <ol>
 * <li> Create a view state to display the form. In an entry action of that
 * state, invoke {@link #setupForm(RequestContext) setupForm} to prepare the new
 * form for display. </li>
 * <li> On submit, execute a state transition action that performs a
 * bindAndValidate. This will invoke
 * {@link #bindAndValidate(RequestContext) bindAndValidate} to bind incoming
 * event parameters to the form object and validate the form object.
 * <li>If there are binding or validation errors, the transition will not be
 * allowed and the view state will automatically be re-entered.
 * <li> If binding and validation is successful, go to an action state called
 * "processSubmit" (or any other appropriate name). This will invoke an action
 * method called "processSubmit" you must provide on a subclass to process form
 * submission, e.g. interacting with the business logic. </li>
 * <li> If business processing is ok, continue to a view state to display the
 * success view. </li>
 * </ol>
 * <p>
 * Here is an example implementation of such a compact form flow:
 * 
 * <pre>
 *     &lt;view-state id=&quot;displayCriteria&quot; view=&quot;searchCriteria&quot;&gt;
 *         &lt;entry&gt;
 *             &lt;action bean=&quot;searchFormAction&quot; method=&quot;setupForm&quot;/&gt;
 *         &lt;/entry&gt;
 *         &lt;transition on=&quot;search&quot; to=&quot;executeSearch&quot;&gt;
 *             &lt;action bean=&quot;searchFormAction&quot; method=&quot;bindAndValidate&quot;/&gt;
 *         &lt;/transition&gt;
 *     &lt;/view-state&gt;
 *      
 *     &lt;action-state id=&quot;executeSearch&quot;&gt;
 *         &lt;action bean=&quot;searchFormAction&quot;/&gt;
 *         &lt;transition on=&quot;success&quot; to=&quot;displayResults&quot;/&gt;
 *     &lt;/action-state&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * When you need additional flexibility, consider splitting the view state above
 * acting as a single logical <i>form state</i> into multiple states. For
 * example, you could have one action state handle form setup, a view state
 * trigger form display, another action state handle data binding and
 * validation, and another process form submission. This would be a bit more
 * verbose but would also give you more control over how you respond to specific
 * results of fine-grained actions that occur within the flow.
 * <p>
 * <b>Subclassing hooks:</b>
 * <ul>
 * <li>An important hook method provided by this class is
 * {@link #initBinder(RequestContext, DataBinder) initBinder}. This is called
 * after a new data binder is created by any of the action execution methods. It
 * allows you to install any custom property editors required to format
 * richly-typed form object property values.
 * <li>Another important hook is
 * {@link #loadFormObject(RequestContext) loadFormObject}. You may override
 * this to customize where the backing form object comes from (e.g instantiated
 * directly in memory or loaded from a database).
 * </ul>
 * <p>
 * Note that this action does not provide a <i>referenceData()</i> hook method
 * similar to that of Spring MVC's <code>SimpleFormController</code>. If you
 * need to expose reference data to populate form drop downs for example, you
 * should create a custom action method in your FormAction subclass that does
 * just that, and invoke it as either a chained action as part of a form setup
 * state, or as a fine grained state definition itself.
 * <p>
 * For example, you might create this method in your subclass:
 * 
 * <pre>
 * public Event setupReferenceData(RequestContext context) throws Exception {
 *     Scope requestScope = context.getRequestScope();
 *     requestScope.setAttribute(&quot;refData&quot;, referenceDataDao.getSupportingFormData());
 *     return success();
 * }
 * </pre>
 * 
 * <p>
 * <b>FormAction configurable properties</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>formObjectName</td>
 * <td>"formObject"</td>
 * <td>The name of the form object. The form object will be set in the
 * configured scope using this name. </td>
 * </tr>
 * <tr>
 * <td>formObjectClass</td>
 * <td>null</td>
 * <td>The form object class for this action. An instance of this class will
 * get populated and validated. </td>
 * </tr>
 * <tr>
 * <td>formObjectScope</td>
 * <td>{@link org.springframework.webflow.ScopeType#FLOW flow}</td>
 * <td>The scope in which the form object will be put. If put in flow scope the
 * object will be cached and reused over the life of the flow, preserving
 * previous values. Request scope will cause a new fresh form object instance to
 * be created each execution.</td>
 * </tr>
 * <tr>
 * <td>errorsScope</td>
 * <td>{@link org.springframework.webflow.ScopeType#REQUEST request}</td>
 * <td>The scope in which the form object errors instance will be put. If put
 * in flow scope the errors will be cached and reused over the life of the flow.
 * Request scope will cause a new errors instance to be created each execution.</td>
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
 * <td>The validator for this action. The validator must support the specified
 * form object class. </td>
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
 * <td>Indicates if the validator should be invoked after binding. </td>
 * </tr>
 * <tr>
 * <td>requireValidatorMethod</td>
 * <td>false</td>
 * <td>Indicates if the validator should be invoked ONLY if the
 * "validatorMethod" property is set before this action is executed. </td>
 * </tr>
 * <tr>
 * <td>messageCodesResolver</td>
 * <td>null</td>
 * <td>Set the strategy to use for resolving errors into message codes. </td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.webflow.action.FormActionMethods
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FormAction extends MultiAction implements InitializingBean, FormActionMethods {

	/**
	 * Optional property that identifies the method that should be invoked on
	 * the configured validator instance, to support piecemeal wizard page
	 * validation.
	 */
	public static final String VALIDATOR_METHOD_PROPERTY = "validatorMethod";

	/**
	 * The name the form object should be exposed under.
	 */
	private String formObjectName = "formObject";

	/**
	 * The type of form object -- typically a instantiable class.
	 */
	private Class formObjectClass;

	/**
	 * The scope in which the form object should be exposed.
	 */
	private ScopeType formObjectScope = ScopeType.FLOW;

	/**
	 * The scope in which the form object errors holder should be exposed.
	 */
	private ScopeType formErrorsScope = ScopeType.REQUEST;

	/**
	 * A centralized service for property editor registration, for type
	 * conversion during form object data binding.
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
	private DispatchMethodInvoker validateMethodInvoker = new DispatchMethodInvoker();

	/**
	 * Determines if validation should only be invoked if the "validatorMethod"
	 * property is set before this action is executed. Useful for piecemeal
	 * validation as part of a wizard.
	 */
	private boolean requireValidatorMethod;

	/**
	 * Creates a initially unconfigured FormAction instance relying on default
	 * property values.
	 */
	public FormAction() {

	}

	/**
	 * Creates a new form action that manages instance(s) of the specified form
	 * object class.
	 * @param formObjectClass the class of the form object (must be
	 * instantiable)
	 */
	public FormAction(Class formObjectClass) {
		setFormObjectClass(formObjectClass);
	}

	/**
	 * Return the name of the form object in the configured scope.
	 */
	public String getFormObjectName() {
		return this.formObjectName;
	}

	/**
	 * Set the name of the form object in the configured scope. The form object
	 * will be included in the configured scope under this name.
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
		if (formObjectName == null && formObjectClass != null) {
			formObjectName = ClassUtils.getShortNameAsProperty(formObjectClass);
		}
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
	public ScopeType getFormErrorsScope() {
		return formErrorsScope;
	}

	/**
	 * Set the scope in which the Errors object will be placed. Can be either
	 * flow scope ore request scope. Defaults to request scope.
	 */
	public void setFormErrorsScope(ScopeType errorsScope) {
		this.formErrorsScope = errorsScope;
	}

	/**
	 * Convenience setter that performs a string to ScopeType conversion for
	 * you.
	 * @param encodedScopeType the encoded scope type string
	 * @throws InvalidFormatException the encoded value was invalid
	 */
	public void setFormErrorsScopeAsString(String encodedScopeType) throws InvalidFormatException {
		this.formErrorsScope = (ScopeType)new LabeledEnumFormatter().parseValue(encodedScopeType, ScopeType.class);
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
	public void setBindOnSetupForm(boolean bindOnSetupForm) {
		this.bindOnSetupForm = bindOnSetupForm;
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
	 * Return if the validator should get aplied only if the validatorMethod
	 * property is set.
	 */
	public boolean isRequireValidatorMethod() {
		return requireValidatorMethod;
	}

	/**
	 * Set if the validator should only be applied if the validator method
	 * property is set.
	 */
	public void setRequireValidatorMethod(boolean requireValidatorMethod) {
		this.requireValidatorMethod = requireValidatorMethod;
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
			// initialize method dispatcher
			getValidateMethodInvoker().setTarget(getValidator());
			getValidateMethodInvoker().setParameterTypes(
					new Class[] { getFormObjectClass() == null ? Object.class : getFormObjectClass(), Errors.class });
		}
	}

	// action execute methods (as defined by the FormOperations interface)

	public Event exposeFormObject(RequestContext context) throws Exception {
		try {
			Object formObject = getFormObject(context);
			setFormObject(context, formObject);
			ensureFormErrorsExposed(context, formObject);
			return success();
		}
		catch (FormObjectRetrievalFailureException e) {
			return error(e);
		}
	}

	public Event setupForm(RequestContext context) throws Exception {
		Object formObject = null;
		try {
			formObject = getFormObject(context);
		}
		catch (FormObjectRetrievalFailureException e) {
			// handle retrieval exceptions only, other exceptions propagate
			return error(e);
		}
		setFormObject(context, formObject);
		if (setupBindingEnabled(context)) {
			DataBinder binder = createBinder(context, formObject);
			doBind(context, binder);
			setFormErrors(context, binder.getErrors());
			return binder.getErrors().hasErrors() ? error() : success();
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Setup form object binding was disabled for this request");
			}
			ensureFormErrorsExposed(context, formObject);
			return success();
		}
	}

	public Event bindAndValidate(RequestContext context) throws Exception {
		Object formObject = null;
		try {
			formObject = getFormObject(context);
		}
		catch (FormObjectRetrievalFailureException e) {
			// handle retrieval exceptions only, other exceptions propagate
			return error(e);
		}
		setFormObject(context, formObject);
		DataBinder binder = createBinder(context, formObject);
		doBind(context, binder);
		setFormErrors(context, binder.getErrors());
		if (getValidator() != null && isValidateOnBinding() && validationEnabled(context)) {
			doValidate(context, binder);
		}
		else {
			if (logger.isDebugEnabled()) {
				if (validator == null) {
					logger.debug("No validator is configured: no validation will occur");
				}
				else {
					logger.debug("Validation was disabled for this request");
				}
			}
		}
		return binder.getErrors().hasErrors() ? error() : success();
	}

	public Event bind(RequestContext context) throws Exception {
		Object formObject = null;
		try {
			formObject = getFormObject(context);
		}
		catch (FormObjectRetrievalFailureException e) {
			// handle retrieval exceptions only, other exceptions propagate
			return error(e);
		}
		setFormObject(context, formObject);
		DataBinder binder = createBinder(context, formObject);
		doBind(context, binder);
		setFormErrors(context, binder.getErrors());
		return binder.getErrors().hasErrors() ? error() : success();
	}

	public Event validate(RequestContext context) throws Exception {
		Object formObject = null;
		try {
			formObject = getFormObject(context);
		}
		catch (FormObjectRetrievalFailureException e) {
			// handle retrieval exceptions only, other exceptions propagate
			return error(e);
		}
		setFormObject(context, formObject);
		DataBinder binder = createBinder(context, formObject);
		doValidate(context, binder);
		setFormErrors(context, binder.getErrors());
		return binder.getErrors().hasErrors() ? error() : success();
	}

	/**
	 * Resets the form by clearing out the form object in the specified scope
	 * and reloading it by calling loadFormObject.
	 * @param context the request context
	 * @return "success" if the reset action completed successfully, "error"
	 * otherwise
	 * @throws Exception if an exception occured
	 */
	public Event resetForm(RequestContext context) throws Exception {
		try {
			Object formObject = loadFormObject(context);
			setFormObject(context, formObject);
			setFormErrors(context, createFormErrors(context, formObject));
			return success();
		}
		catch (FormObjectRetrievalFailureException e) {
			// handle retrieval exceptions only, other exceptions propagate
			return error(e);
		}
	}

	// internal helpers

	/**
	 * Create a new binder instance for the given form object and request
	 * context. Can be overridden to plug in custom DataBinder subclasses.
	 * <p>
	 * Default implementation creates a standard WebDataBinder, and invokes
	 * initBinder. Note that initBinder will not be invoked if you override this
	 * method!
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
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
	 * Bind parameters of the last event in given context to the form object
	 * using given binder.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @param binder the data binder to use
	 */
	protected void doBind(RequestContext context, DataBinder binder) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Binding allowed parameters in event: " + context.getLastEvent()
					+ " to form object with name: '" + binder.getObjectName() + "', prebind-toString: "
					+ binder.getTarget());
			if (binder.getAllowedFields() != null && binder.getAllowedFields().length > 0) {
				logger.debug("(Allowed event parameters are: " + binder.getAllowedFields() + ")");
			}
			else {
				logger.debug("(Any event parameter is allowed)");
			}
		}
		binder.bind(new MutablePropertyValues(context.getLastEvent().getParameters()));
		if (logger.isDebugEnabled()) {
			logger.debug("Binding completed for form object with name: '" + binder.getObjectName()
					+ "', postbind-toString: " + binder.getTarget());
			logger.debug("There are [" + binder.getErrors().getErrorCount() + "] errors, details: "
					+ binder.getErrors().getAllErrors());
		}
	}

	/**
	 * Validate given form object using a registered validator. If a
	 * "validatorMethod" action property is specified for the currently
	 * executing action, the identified validator method will be invoked. When
	 * no such property is found, the defualt <code>validate()</code> method
	 * is invoked.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @param binder the data binder to use
	 */
	protected void doValidate(RequestContext context, DataBinder binder) throws Exception {
		Assert.notNull(validator,
				"The validator must not be null when attempting validation, but it is: programmer error");
		String validatorMethod = (String)context.getProperties().getAttribute(VALIDATOR_METHOD_PROPERTY);
		if (StringUtils.hasText(validatorMethod)) {
			invokeValidatorMethod(validatorMethod, binder.getTarget(), binder.getErrors());
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking validator: " + validator);
			}
			getValidator().validate(binder.getTarget(), binder.getErrors());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Validation completed for form object with name: '" + binder.getObjectName() + "'");
			logger.debug("There are [" + binder.getErrors().getErrorCount() + "] errors, details: "
					+ binder.getErrors().getAllErrors());
		}
	}

	/**
	 * Invoke specified validator method on the validator registered with this
	 * action.
	 * @param validatorMethod the name of the validator method to invoke
	 * @param formObject the form object
	 * @param errors possible binding errors
	 */
	private void invokeValidatorMethod(String validatorMethod, Object formObject, Errors errors) throws Exception {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Invoking piecemeal validator method: '" + validatorMethod + "' on form object: "
							+ formObject);
		}
		getValidateMethodInvoker().invoke(validatorMethod, new Object[] { formObject, errors });
	}

	/**
	 * Convenience method that returns the form object for this form action. If
	 * not found in the configured scope, a new form object will be created or
	 * loaded by a call to {@link #loadFormObject(RequestContext)}.
	 * @param context the flow request context
	 * @return the form object
	 */
	protected Object getFormObject(RequestContext context) throws Exception {
		Object formObject = getFormObjectAccessor(context).getFormObject(getFormObjectName(), getFormObjectClass(),
				getFormObjectScope());
		if (formObject == null) {
			formObject = loadFormObject(context);
		}
		return formObject;
	}

	/**
	 * Put given form object in the configured scope of given context.
	 */
	private void setFormObject(RequestContext context, Object formObject) {
		getFormObjectAccessor(context).setFormObject(formObject, getFormObjectName(), getFormObjectScope());
	}

	/**
	 * Convenience method that returns the form object errors for this form
	 * action. If not found in the configured scope, a new form object errors
	 * will be created.
	 * @param context the flow request context
	 * @return the form errors
	 */
	protected Errors getFormErrors(RequestContext context) throws Exception {
		return ensureFormErrorsExposed(context, getFormObject(context));
	}

	/**
	 * Expose an empty errors collection in the model of the currently executing
	 * flow if neccessary.
	 * @param context the flow execution request context
	 * @param errors the errors
	 */
	private Errors ensureFormErrorsExposed(RequestContext context, Object formObject) {
		Errors errors = getFormObjectAccessor(context).getFormErrors(getFormObjectName(), getFormErrorsScope());
		if (errors instanceof BindException) {
			// make sure the existing form errors are consistent with the form
			// object
			BindException be = (BindException)errors;
			if (be.getTarget() != formObject) {
				if (logger.isInfoEnabled()) {
					logger
							.info("Inconsistency detected: the Errors instance in '"
									+ getFormErrorsScope()
									+ "' does NOT wrap the current form object: "
									+ formObject
									+ " of class: "
									+ formObject.getClass()
									+ "; instead this Errors instance unexpectedly wraps the target object: "
									+ be.getTarget()
									+ " of class: "
									+ be.getTarget().getClass()
									+ ". [Taking corrective action: overwriting the existing Errors instance with an empty one for the current form object]");
				}
				// fall through below
				errors = null;
			}
		}
		if (errors == null) {
			errors = createFormErrors(context, formObject);
			setFormErrors(context, errors);
		}
		return errors;
	}

	/**
	 * Return an empty errors collection with property editors registered.
	 * @param context the flow execution request context
	 * @param formObject the object
	 * @return the new errors instance
	 */
	private Errors createFormErrors(RequestContext context, Object formObject) {
		return createBinder(context, formObject).getErrors();
	}

	/**
	 * Put given errors instance in the configured scope of given context.
	 */
	private void setFormErrors(RequestContext context, Errors errors) {
		getFormObjectAccessor(context).setFormErrors(errors, getFormErrorsScope());
	}

	// subclassing hook methods

	/**
	 * Returns a dispatcher to invoke validation methods. Subclasses could
	 * override this to return a custom dispatcher.
	 */
	protected DispatchMethodInvoker getValidateMethodInvoker() {
		return validateMethodInvoker;
	}

	/**
	 * Factory method that returns a new form object accessor for accessing form
	 * objects in the provided request context.
	 * @param context the flow request context
	 * @return the accessor
	 */
	protected FormObjectAccessor getFormObjectAccessor(RequestContext context) {
		return new FormObjectAccessor(context);
	}

	/**
	 * Load the backing form object that should be updated from incoming event
	 * parameters and validated. By default, will attempt to instantiate a new
	 * form object instance transiently in memory if not already present in the
	 * configured scope by calling {@link #createFormObject(RequestContext)}.
	 * <p>
	 * Subclasses should override if they need to load the form object from a
	 * specific location or resource such as a database or filesystem.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return the form object
	 * @throws FormObjectRetrievalFailureException the form object could not be
	 * loaded
	 */
	protected Object loadFormObject(RequestContext context) throws FormObjectRetrievalFailureException, Exception {
		return createFormObject(context);
	}

	/**
	 * Create a new form object by instantiating the configured form object
	 * class.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return the form object
	 * @throws Exception the form object could not be created
	 */
	protected Object createFormObject(RequestContext context) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new form object '" + getFormObjectName() + "'");
		}
		if (this.formObjectClass == null) {
			throw new IllegalStateException("Cannot create form object without formObjectClass being set -- "
					+ "either set formObjectClass or override this method");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new form object of class [" + this.formObjectClass.getName() + "]");
		}
		return this.formObjectClass.newInstance();
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
	 * data in "flow scope" or "request scope"
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
	 * Returns true if event parameters should be bound to the form object
	 * during the {@link #setupForm(RequestContext)} action. The default
	 * implementation just calls {@link #isBindOnSetupForm()}.
	 */
	protected boolean setupBindingEnabled(RequestContext context) {
		return isBindOnSetupForm();
	}

	/**
	 * Return whether validation should be performed given the state of the flow
	 * request context.
	 * <p>
	 * Default implementation always returns true, unless the
	 * "requireValidatorMethod" property is set to true, then this
	 * implementation returns true only if the "validatorMethod" property is
	 * set. Can be overridden in subclasses to test validation, for example, if
	 * a special event parameter is set.
	 * @param context the request context, for accessing and setting data in
	 * "flow scope" or "request scope"
	 * @return whether or not validation is enabled
	 */
	protected boolean validationEnabled(RequestContext context) {
		if (requireValidatorMethod) {
			return context.getProperties().containsAttribute(VALIDATOR_METHOD_PROPERTY);
		}
		else {
			return true;
		}
	}
}