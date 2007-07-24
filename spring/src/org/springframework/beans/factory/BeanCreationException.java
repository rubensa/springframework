/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.FatalBeanException;
import org.springframework.core.NestedRuntimeException;

/**
 * Exception thrown when a BeanFactory encounters an error when
 * attempting to create a bean from a bean definition.
 *
 * @author Juergen Hoeller
 */
public class BeanCreationException extends FatalBeanException {

	private String beanName;

	private String resourceDescription;

	private List relatedCauses;


	/**
	 * Create a new BeanCreationException.
	 * @param msg the detail message
	 */
	public BeanCreationException(String msg) {
		super(msg);
	}

	/**
	 * Create a new BeanCreationException.
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Create a new BeanCreationException.
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 */
	public BeanCreationException(String beanName, String msg) {
		this(beanName, msg, (Throwable) null);
	}

	/**
	 * Create a new BeanCreationException.
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanCreationException(String beanName, String msg, Throwable cause) {
		super("Error creating bean with name '" + beanName + "': " + msg, cause);
		this.beanName = beanName;
	}

	/**
	 * Create a new BeanCreationException.
	 * @param resourceDescription description of the resource
	 * that the bean definition came from
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 */
	public BeanCreationException(String resourceDescription, String beanName, String msg) {
		this(resourceDescription, beanName, msg, null);
	}

	/**
	 * Create a new BeanCreationException.
	 * @param resourceDescription description of the resource
	 * that the bean definition came from
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanCreationException(String resourceDescription, String beanName, String msg, Throwable cause) {
		super("Error creating bean with name '" + beanName + "'" +
				(resourceDescription != null ? " defined in " + resourceDescription : "") +
				": " + msg, cause);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
	}


	/**
	 * Return the name of the bean requested, if any.
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return the description of the resource that the bean
	 * definition came from, if any.
	 */
	public String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * Add a related cause to this bean creation exception,
	 * not being a direct cause of the failure but having occured
	 * earlier in the creation of the same bean instance.
	 * @param ex the related cause to add
	 */
	public void addRelatedCause(Throwable ex) {
		if (this.relatedCauses == null) {
			this.relatedCauses = new LinkedList();
		}
		this.relatedCauses.add(ex);
	}

	/**
	 * Return the related causes, if any.
	 * @return the array of related causes, or <code>null</code> if none
	 */
	public Throwable[] getRelatedCauses() {
		if (this.relatedCauses == null) {
			return null;
		}
		return (Throwable[]) this.relatedCauses.toArray(new Throwable[this.relatedCauses.size()]);
	}


	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		if (this.relatedCauses != null) {
			for (Iterator it = this.relatedCauses.iterator(); it.hasNext();) {
				Throwable relatedCause = (Throwable) it.next();
				sb.append("\nRelated cause: ");
				sb.append(relatedCause);
			}
		}
		return sb.toString();
	}

	public void printStackTrace(PrintStream ps) {
		synchronized (ps) {
			super.printStackTrace(ps);
			if (this.relatedCauses != null) {
				for (Iterator it = this.relatedCauses.iterator(); it.hasNext();) {
					Throwable relatedCause = (Throwable) it.next();
					ps.println("Related cause:");
					relatedCause.printStackTrace(ps);
				}
			}
		}
	}

	public void printStackTrace(PrintWriter pw) {
		synchronized (pw) {
			super.printStackTrace(pw);
			if (this.relatedCauses != null) {
				for (Iterator it = this.relatedCauses.iterator(); it.hasNext();) {
					Throwable relatedCause = (Throwable) it.next();
					pw.println("Related cause:");
					relatedCause.printStackTrace(pw);
				}
			}
		}
	}

	public boolean contains(Class exClass) {
		if (super.contains(exClass)) {
			return true;
		}
		if (this.relatedCauses != null) {
			for (Iterator it = this.relatedCauses.iterator(); it.hasNext();) {
				Throwable relatedCause = (Throwable) it.next();
				if (relatedCause instanceof NestedRuntimeException &&
						((NestedRuntimeException) relatedCause).contains(exClass)) {
					return true;
				}
			}
		}
		return false;
	}

}
