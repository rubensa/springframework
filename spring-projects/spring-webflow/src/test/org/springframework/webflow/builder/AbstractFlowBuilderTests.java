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
package org.springframework.webflow.builder;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewState;

/**
 * Test Java based flow builder logic (subclasses of AbstractFlowBuilder).
 * 
 * @see org.springframework.webflow.builder.AbstractFlowBuilder
 * 
 * @author Keith Donald
 * @author Rod Johnson
 * @author Colin Sampaleanu
 */
public class AbstractFlowBuilderTests extends TestCase {

	private String PERSONS_LIST = "person.List";

	private static String PERSON_DETAILS = "person.Detail";

	public void testDependencyLookup() {
		TestMasterFlowBuilderLookupById master = new TestMasterFlowBuilderLookupById();
		master.setFlowArtifactFactory(new FlowArtifactFactoryAdapter() {
			public Flow getSubflow(String id) throws FlowArtifactLookupException {
				if (id.equals(PERSON_DETAILS)) {
					BaseFlowBuilder builder = new TestDetailFlowBuilderLookupById();
					builder.setFlowArtifactFactory(this);
					FlowAssembler assembler = new FlowAssembler(PERSON_DETAILS, builder);
					assembler.assembleFlow();
					return builder.getResult();
				}
				else {
					throw new FlowArtifactLookupException(Flow.class, id);
				}
			}

			public Action getAction(String actionId) throws FlowArtifactLookupException {
				return new NoOpAction();
			}

			public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
				if (id.equals("id.attributeMapper")) {
					return new PersonIdMapper();
				}
				else {
					throw new FlowArtifactLookupException(FlowAttributeMapper.class, id);
				}
			}
		});

		FlowAssembler assembler = new FlowAssembler(PERSONS_LIST, master);
		assembler.assembleFlow();
		Flow flow = master.getResult();

		assertEquals("person.List", flow.getId());
		assertTrue(flow.getStateCount() == 4);
		assertTrue(flow.containsState("getPersonList"));
		assertTrue(flow.getState("getPersonList") instanceof ActionState);
		assertTrue(flow.containsState("viewPersonList"));
		assertTrue(flow.getState("viewPersonList") instanceof ViewState);
		assertTrue(flow.containsState("person.Detail"));
		assertTrue(flow.getState("person.Detail") instanceof SubflowState);
		assertTrue(flow.containsState("finish"));
		assertTrue(flow.getState("finish") instanceof EndState);
	}

	public void testNoArtifactFactorySet() {
		TestMasterFlowBuilderLookupById master = new TestMasterFlowBuilderLookupById();
		try {
			FlowAssembler assembler = new FlowAssembler(PERSONS_LIST, master);
			assembler.assembleFlow();
			fail("Should have failed, artifact lookup not supported");
		}
		catch (FlowArtifactLookupException e) {
			// expected
		}
	}

	public class TestMasterFlowBuilderLookupById extends AbstractFlowBuilder {
		public void buildStates() {
			addActionState("getPersonList", action("noOpAction"), on(success(), "viewPersonList"));
			addViewState("viewPersonList", "person.list.view", on(submit(), "person.Detail"));
			addSubflowState(PERSON_DETAILS, flow("person.Detail"), attributeMapper("id.attributeMapper"),
					onAnyEvent("getPersonList"));
			addEndState("finish");
		}
	}

	public class TestMasterFlowBuilderDependencyInjection extends AbstractFlowBuilder {
		private NoOpAction noOpAction;

		private Flow subFlow;

		private PersonIdMapper personIdMapper;

		public void setNoOpAction(NoOpAction noOpAction) {
			this.noOpAction = noOpAction;
		}

		public void setPersonIdMapper(PersonIdMapper personIdMapper) {
			this.personIdMapper = personIdMapper;
		}

		public void setSubFlow(Flow subFlow) {
			this.subFlow = subFlow;
		}

		public void buildStates() {
			addActionState("getPersonList", noOpAction, on(success(), "viewPersonList"));
			addViewState("viewPersonList", "person.list.view", on(submit(), "person.Detail"));
			addSubflowState(PERSON_DETAILS, subFlow, personIdMapper, onAnyEvent("getPersonList"));
			addEndState("finish");
		}
	}

	public static class PersonIdMapper implements FlowAttributeMapper {
		public Map createSubflowInput(RequestContext context) {
			Map inputMap = new HashMap(1);
			inputMap.put("personId", context.getFlowScope().getAttribute("personId"));
			return inputMap;
		}

		public void mapSubflowOutput(RequestContext context) {
		}
	}

	public static class TestDetailFlowBuilderLookupById extends AbstractFlowBuilder {
		public void buildStates() {
			addActionState("getDetails", action("noOpAction"), on(success(), "viewDetails"));
			addViewState("viewDetails", "person.Detail.view", on(submit(), "bindAndValidateDetails"));
			addActionState("bindAndValidateDetails", action("noOpAction"), new Transition[] {
					on(error(), "viewDetails"), on(success(), "finish") });
			addEndState("finish");
		}
	}

	public static class TestDetailFlowBuilderDependencyInjection extends AbstractFlowBuilder {

		private NoOpAction noOpAction;

		public void setNoOpAction(NoOpAction noOpAction) {
			this.noOpAction = noOpAction;
		}

		public void buildStates() {
			addActionState("getDetails", noOpAction, on(success(), "viewDetails"));
			addViewState("viewDetails", "person.Detail.view", on(submit(), "bindAndValidateDetails"));
			addActionState("bindAndValidateDetails", noOpAction, new Transition[] { on(error(), "viewDetails"),
					on(success(), "finish") });
			addEndState("finish");
		}
	};

	/**
	 * Action bean stub that does nothing, just returns a "success" result.
	 */
	public static final class NoOpAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			return new Event(this, "success");
		}
	}
}