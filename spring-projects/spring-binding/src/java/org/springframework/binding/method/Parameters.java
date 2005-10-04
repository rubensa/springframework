package org.springframework.binding.method;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.core.style.ToStringCreator;

/**
 * An ordered list of method parameters.
 * 
 * @author Keith
 */
public class Parameters implements Serializable {

	/**
	 * Canonical instance for an empty parameters list.
	 */
	public static final Parameters NONE = new Parameters(0);

	/**
	 * The list.
	 */
	private List parameters;

	/**
	 * Create a parameter list of the default size (3 elements).
	 */
	public Parameters() {
		this(3);
	}

	/**
	 * Create an parameter list with the specified size.
	 * @param size the size
	 */
	public Parameters(int size) {
		this.parameters = new ArrayList(size);
	}

	/**
	 * Create an parameter list with one parameter.
	 * @param parameter the single parameter
	 */
	public Parameters(Parameter parameter) {
		this.parameters = new ArrayList(1);
		add(parameter);
	}

	/**
	 * Create an parameter list from the parameter array.
	 * @param parameters the parameters
	 */
	public Parameters(Parameter[] parameters) {
		this.parameters = new ArrayList(parameters.length);
		addAll(parameters);
	}

	/**
	 * Add a new parameter to this list.
	 * @param parameter the parameter
	 */
	public boolean add(Parameter parameter) {
		return this.parameters.add(parameter);
	}

	/**
	 * Add new parameters to this list.
	 * @param parameters the parameters
	 * @return the parameters
	 */
	public boolean addAll(Parameter[] parameters) {
		return this.parameters.addAll(Arrays.asList(parameters));
	}

	/**
	 * Return a parameter iterator.
	 * @return the iterator
	 */
	public Iterator iterator() {
		return parameters.iterator();
	}

	/**
	 * Get an array for each parameter type.
	 * @return the types
	 */
	public Class[] getTypesArray() {
		int i = 0;
		Class[] types = new Class[parameters.size()];
		for (Iterator it = parameters.iterator(); it.hasNext();) {
			Parameter arg = (Parameter)it.next();
			types[i] = arg.getType();
			i++;
		}
		return types;
	}

	/**
	 * Returns the number of parameters in this list.
	 * @return the size
	 */
	public int size() {
		return parameters.size();
	}

	/**
	 * Return the parameter at the provided index.
	 * @param index
	 * @return
	 */
	public Parameter getParameter(int index) {
		return (Parameter)parameters.get(index);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Parameters)) {
			return false;
		}
		Parameters other = (Parameters)obj;
		return parameters.equals(other.parameters);
	}

	public int hashCode() {
		return parameters.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("parameters", parameters).toString();
	}
}