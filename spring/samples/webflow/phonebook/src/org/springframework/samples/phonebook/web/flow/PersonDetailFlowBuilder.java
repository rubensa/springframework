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
package org.springframework.samples.phonebook.web.flow;

import org.springframework.samples.phonebook.web.flow.action.GetPersonAction;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;

/**
 * Java-based flow builder that builds the person details flow.
 * 
 * This encapsulates the page flow of viewing a person's details and their
 * collegues in a reusable, self-contained module.
 * 
 * @author Keith Donald
 */
public class PersonDetailFlowBuilder extends AbstractFlowBuilder {

	private static final String PERSON = "person";

	protected String flowId() {
		return "person.Detail";
	}

	public void buildStates() throws FlowBuilderException {
		// get the person given a userid as input
		addGetState(PERSON, executeAction(GetPersonAction.class));

		// view the person
		String setCollegueId = qualify(set("collegueId"));
		addViewState(PERSON, new Transition[] { onBackFinish(), onSelect(setCollegueId) });

		// set the selected collegue (chosen from the person's collegue list)
		addActionState(setCollegueId, onSuccess("collegue.Detail"));

		// spawn subflow to view selected collegue details
		addSubFlowState("collegue.Detail", PersonDetailFlowBuilder.class, useModelMapper("collegueId"),
				new Transition[] { onFinishGet(PERSON), onErrorEnd() });

		// end
		addFinishEndState();

		// end error
		addErrorEndState();
	}
}