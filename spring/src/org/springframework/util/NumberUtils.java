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

package org.springframework.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Miscellaneous utility methods for number conversion and parsing.
 * Mainly for internal use within the framework; consider Jakarta's
 * Commons Lang for a more comprehensive suite of string utilities.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1.2
 */
public abstract class NumberUtils {

	/**
	 * Convert the given number into an instance of the given target class.
	 * @param number the number to convert
	 * @param targetClass the target class to convert to
	 * @return the converted number
	 * @throws IllegalArgumentException if the target class is not supported
	 * (i.e. not a standard Number subclass as included in the JDK)
	 * @see java.lang.Byte
	 * @see java.lang.Short
	 * @see java.lang.Integer
	 * @see java.lang.Long
	 * @see java.math.BigInteger
	 * @see java.lang.Float
	 * @see java.lang.Double
	 * @see java.math.BigDecimal
	 */
	public static Number convertNumberToTargetClass(Number number, Class targetClass)
			throws IllegalArgumentException {

		if (targetClass.isInstance(number)) {
			return number;
		}
		else if(targetClass.equals(Byte.class)) {
			return new Byte(number.byteValue());
		}
		else if (targetClass.equals(Short.class)) {
			return new Short(number.shortValue());
		}
		else if (targetClass.equals(Integer.class)) {
			return new Integer(number.intValue());
		}
		else if (targetClass.equals(Long.class)) {
			return new Long(number.longValue());
		}
		else if (targetClass.equals(Float.class)) {
			return new Float(number.floatValue());
		}
		else if (targetClass.equals(Double.class)) {
			return new Double(number.doubleValue());
		}
		else if (targetClass.equals(BigInteger.class)) {
			return BigInteger.valueOf(number.longValue());
		}
		else if (targetClass.equals(BigDecimal.class)) {
			// using BigDecimal(String) here, to avoid unpredictability of BigDecimal(double)
			// (see BigDecimal javadoc for details)
			return new BigDecimal(number.toString());
		}
		else {
			throw new IllegalArgumentException("Couldn't convert number [" + number +
					"] to target class [" + targetClass.getName() + "]");
		}
	}

	/**
	 * Parse the given text into a number instance of the given target class,
	 * using the corresponding default <code>decode</code> methods. Trims the
	 * input <code>String</code> before attempting to parse the number.
	 * Supports numbers in hex format (with leading 0x) and in octal format (with
	 * leading 0).
	 * @param text the text to convert
	 * @param targetClass the target class to parse into
	 * @return the parsed number
	 * @throws IllegalArgumentException if the target class is not supported
	 * (i.e. not a standard Number subclass as included in the JDK)
	 * @see java.lang.Byte#decode
	 * @see java.lang.Short#decode
	 * @see java.lang.Integer#decode
	 * @see java.lang.Long#decode
	 * @see #decodeBigInteger(String)
	 * @see java.lang.Float#valueOf
	 * @see java.lang.Double#valueOf
	 * @see java.math.BigDecimal#BigDecimal(String)
	 */
	public static Number parseNumber(String text, Class targetClass) {
		String trimmed = text.trim();

		if (targetClass.equals(Byte.class)) {
			return Byte.decode(trimmed);
		}
		else if (targetClass.equals(Short.class)) {
			return Short.decode(trimmed);
		}
		else if (targetClass.equals(Integer.class)) {
			return Integer.decode(trimmed);
		}
		else if (targetClass.equals(Long.class)) {
			return Long.decode(trimmed);
		}
		else if (targetClass.equals(BigInteger.class)) {
			return decodeBigInteger(trimmed);
		}
		else if (targetClass.equals(Float.class)) {
			return Float.valueOf(trimmed);
		}
		else if (targetClass.equals(Double.class)) {
			return Double.valueOf(trimmed);
		}
		else if (targetClass.equals(BigDecimal.class)) {
			return new BigDecimal(trimmed);
		}
		else {
			throw new IllegalArgumentException(
					"Cannot convert [" + trimmed + "] to target class [" + targetClass.getName() + "]");
		}
	}

	/**
	 * Parse the given text into a number instance of the given target class,
	 * using the given NumberFormat. Trims the input <code>String</code>
	 * before attempting to parse the number.
	 * @param text the text to convert
	 * @param targetClass the target class to parse into
	 * @param numberFormat the NumberFormat to use for parsing
	 * @return the parsed number
	 * @throws IllegalArgumentException if the target class is not supported
	 * (i.e. not a standard Number subclass as included in the JDK)
	 * @see java.text.NumberFormat#parse
	 * @see #convertNumberToTargetClass
	 */
	public static Number parseNumber(String text, Class targetClass, NumberFormat numberFormat) {
		try {
			Number number = numberFormat.parse(text.trim());
			return convertNumberToTargetClass(number, targetClass);
		}
		catch (ParseException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	/**
	 * Decode a {@link java.math.BigInteger} from a {@link String} value.
	 * Supports decimal, hex and octal notation.
	 * @see BigInteger#BigInteger(String, int)
	 */
	private static BigInteger decodeBigInteger(String value) {
		int radix = 10;
		int index = 0;
		boolean negative = false;

		// Handle minus sign, if present.
		if (value.startsWith("-")) {
			negative = true;
			index++;
		}

		// Handle radix specifier, if present.
		if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
			index += 2;
			radix = 16;
		}
		else if (value.startsWith("#", index)) {
			index++;
			radix = 16;
		}
		else if (value.startsWith("0", index) && value.length() > 1 + index) {
			index++;
			radix = 8;
		}

		BigInteger result = new BigInteger(value.substring(index), radix);
		return (negative ? result.negate() : result);
	}

}
