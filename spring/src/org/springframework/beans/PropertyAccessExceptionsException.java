package org.springframework.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.springframework.util.StringUtils;

/**
 * Combined exception, composed of individual binding exceptions.
 * An object of this class is created at the beginning of the binding
 * process, and errors added to it as necessary.
 *
 * <p>The binding process continues when it encounters application-level
 * exceptions, applying those changes that can be applied and storing
 * rejected changes in an object of this class.
 *
 * @author Rod Johnson
 * @since 18 April 2001
 * @version $Id$
 */
public class PropertyAccessExceptionsException extends BeansException {

	/** List of ErrorCodedPropertyVetoException objects */
	private List exceptions = new ArrayList();

	/** BeanWrapper wrapping the target object for binding */
	private BeanWrapper beanWrapper;

	/**
	 * Create new empty PropertyAccessExceptionsException.
	 * We'll add errors to it as we attempt to bind properties.
	 */
	PropertyAccessExceptionsException(BeanWrapper beanWrapper) {
		super("");
		this.beanWrapper = beanWrapper;
	}

	/**
	 * Return the BeanWrapper that generated this exception.
	 */
	public BeanWrapper getBeanWrapper() {
		return beanWrapper;
	}

	/**
	 * Return the object we're binding to.
	 */
	public Object getBindObject() {
		return this.beanWrapper.getWrappedInstance();
	}

	/**
	 * If this returns 0, no errors were encountered during binding.
	 */
	public int getExceptionCount() {
		return this.exceptions.size();
	}

	/**
	 * Return an array of the exceptions stored in this object.
	 * Will return the empty array (not null) if there were no errors.
	 */
	public PropertyAccessException[] getPropertyAccessExceptions() {
		return (PropertyAccessException[]) this.exceptions.toArray(new PropertyAccessException[this.exceptions.size()]);
	}

	/**
	 * Return the exception for this field, or null if there isn't one.
	 */
	public PropertyAccessException getPropertyAccessException(String propertyName) {
		for (Iterator it = this.exceptions.iterator(); it.hasNext();) {
			PropertyAccessException pae = (PropertyAccessException) it.next();
			if (propertyName.equals(pae.getPropertyChangeEvent().getPropertyName())) {
				return pae;
			}
		}
		return null;
	}

	/* package */
	void addPropertyAccessException(PropertyAccessException ex) {
		this.exceptions.add(ex);
	}

	public void printStackTrace(PrintStream ps) {
		ps.println(this);
		for (Iterator it = this.exceptions.iterator(); it.hasNext();) {
			PropertyAccessException pae = (PropertyAccessException) it.next();
			pae.printStackTrace(ps);
		}
	}

	public void printStackTrace(PrintWriter pw) {
		pw.println(this);
		for (Iterator it = this.exceptions.iterator(); it.hasNext();) {
			PropertyAccessException pae = (PropertyAccessException) it.next();
			pae.printStackTrace(pw);
		}
	}

	public String toString() {
		return "PropertyAccessExceptionsException (" + getExceptionCount() + " errors)";
	}

}
