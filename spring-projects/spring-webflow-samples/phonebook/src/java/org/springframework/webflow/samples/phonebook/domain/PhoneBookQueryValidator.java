/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.samples.phonebook.domain;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PhoneBookQueryValidator implements Validator {

	public boolean supports(Class clazz) {
		return clazz.equals(PhoneBookQuery.class);
	}

	public void validate(Object obj, Errors errors) {
		PhoneBookQuery query = (PhoneBookQuery)obj;
		if ((query.getFirstName() == null || query.getFirstName().length() == 0)
				&& (query.getLastName() == null || query.getLastName().length() == 0)) {
			errors.reject("noCriteria", "Please provide some query criteria!");
		}
	}
}