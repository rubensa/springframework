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
package org.springframework.binding.convert.support;

import org.springframework.beans.BeanUtils;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.ConversionServiceAware;
import org.springframework.binding.expression.Expression;


/**
 * Base class for converters that use other converters to convert things, thus
 * they are conversion-service aware.
 * 
 * @author Keith Donald
 */
public abstract class ConversionServiceAwareConverter extends AbstractConverter implements ConversionServiceAware {

	protected static final String CLASS_PREFIX = "class:";

	/**
	 * The conversion service this converter is aware of.
	 */
	private ConversionService conversionService;

	public ConversionServiceAwareConverter() {
		
	}

	public ConversionServiceAwareConverter(ConversionService conversionService) {
		setConversionService(conversionService);
	}

	public ConversionService getConversionService() {
		if (conversionService == null) {
			throw new IllegalStateException("Conversion service not yet set: set it first before calling this method");
		}
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
	
	/**
	 * Returns a conversion executor capable of converting string objects to the 
	 * specified target class.
	 * @param targetClass the target class
	 * @return the conversion executor
	 */
	protected ConversionExecutor fromStringTo(Class targetClass) {
		return getConversionService().getConversionExecutor(String.class, targetClass);
	}
	
	/**
	 * Returns a conversion executor capable of converting string objects to the 
	 * target class aliased by the provided alias.
	 * @param targetAlias the target class alias, e.g "long" or "float"
	 * @return the conversion executor, or <code>null</code> if no suitable converter exists for alias
	 */
	protected ConversionExecutor fromStringToAliased(String targetAlias) {
		return getConversionService().getConversionExecutorByTargetAlias(String.class, targetAlias);
	}
		
	/**
	 * Returns a conversion executor capable of converting objects from one 
	 * class to another.
	 * @param sourceClass the source class to convert from
	 * @param targetClass the target class to convert to
	 * @return the conversion executor
	 */
	protected ConversionExecutor converterFor(Class sourceClass, Class targetClass) {
		return getConversionService().getConversionExecutor(sourceClass, targetClass);
	}

	/**
	 * Helper that parses given encoded class (which may start with "class:") and
	 * instantiates the identified class using the default constructor.
	 * @param encodedClass the encoded class reference, starting with "class:"
	 * @return an instantiated objected of the identified class
	 * @throws ConversionException when the class cannot be found or cannot be instantiated
	 */
	protected Object newInstance(String encodedClass) throws ConversionException {
		Class clazz = (Class)fromStringTo(Class.class).execute(encodedClass);
		return BeanUtils.instantiateClass(clazz);
	}
	
	/**
	 * Helper that parsers the given expression string into an expression, using the 
	 * installed String->Expression converter.
	 * @param expressionString the expression string to parse
	 * @return the parsed, evaluatable expression
	 */
	protected Expression parseExpression(String expressionString) {
		return (Expression)converterFor(String.class, Expression.class).execute(expressionString);
	}
}