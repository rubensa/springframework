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
package org.springframework.samples.phonebook.web.flow.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.samples.phonebook.domain.Person;
import org.springframework.samples.phonebook.domain.PhoneBook;
import org.springframework.samples.phonebook.domain.UserId;
import org.springframework.web.flow.MutableFlowModel;
import org.springframework.web.flow.action.AbstractAction;

public class GetPersonAction extends AbstractAction {

	private PhoneBook phoneBook;

	public void setPhoneBook(PhoneBook phoneBook) {
		this.phoneBook = phoneBook;
	}
	
	protected String doExecuteAction(HttpServletRequest request,
			HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		UserId userId = (UserId)model.getAttribute("id");
		Person person = phoneBook.getPerson(userId);
		if (person != null) {
			model.setAttribute("person", person);
			return success();
		}
		else {
			return error();
		}
	}
}