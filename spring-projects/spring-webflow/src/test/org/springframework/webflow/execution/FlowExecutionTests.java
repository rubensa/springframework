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
package org.springframework.webflow.execution;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.StateTests.ExecutionCounterAction;
import org.springframework.webflow.StateTests.InputOutputMapper;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.builder.AbstractFlowBuilder;
import org.springframework.webflow.builder.FlowArtifactParameters;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilderException;
import org.springframework.webflow.builder.XmlFlowBuilder;
import org.springframework.webflow.builder.XmlFlowBuilderTests;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;
import org.springframework.webflow.support.ApplicationViewSelection;
import org.springframework.webflow.support.EventIdTransitionCriteria;
import org.springframework.webflow.support.SimpleViewSelector;
import org.springframework.webflow.support.StaticTargetStateResolver;
import org.springframework.webflow.test.MockExternalContext;

/**
 * General flow execution tests.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionTests extends TestCase {

	public void testFlowExecutionListener() {
		Flow subFlow = new Flow("mySubFlow");
		ViewState state1 = new ViewState(subFlow, "subFlowViewState");
		state1.setViewSelector(view("mySubFlowViewName"));
		state1.addTransition(new Transition(on("submit"), to("finish")));
		new EndState(subFlow, "finish");

		Flow flow = new Flow("myFlow");

		ActionState actionState = new ActionState(flow, "actionState");
		actionState.addAction(new ExecutionCounterAction());
		actionState.addTransition(new Transition(on("success"), to("viewState")));

		ViewState viewState = new ViewState(flow, "viewState");
		viewState.setViewSelector(view("myView"));
		viewState.addTransition(new Transition(on("submit"), to("subFlowState")));

		SubflowState subflowState = new SubflowState(flow, "subFlowState", subFlow);
		subflowState.setAttributeMapper(new InputOutputMapper());
		subflowState.addTransition(new Transition(on("finish"), to("finish")));

		new EndState(flow, "finish");

		MockFlowExecutionListener flowExecutionListener = new MockFlowExecutionListener();
		FlowExecutionImpl flowExecution = new FlowExecutionImpl(flow,
				new FlowExecutionListener[] { flowExecutionListener });
		flowExecution.start(new MockExternalContext());
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(0, flowExecutionListener.getFlowNestingLevel());
		assertEquals(2, flowExecutionListener.getTransitionCount());
		flowExecution.signalEvent("submit", new MockExternalContext());
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(1, flowExecutionListener.getFlowNestingLevel());
		assertEquals(4, flowExecutionListener.getTransitionCount());
		flowExecution.signalEvent("submit", new MockExternalContext());
		assertTrue(!flowExecutionListener.isExecuting());
		assertEquals(0, flowExecutionListener.getFlowNestingLevel());
		assertEquals(6, flowExecutionListener.getTransitionCount());
	}

	public void testLoopInFlow() throws Exception {
		AbstractFlowBuilder builder = new AbstractFlowBuilder() {
			public void buildStates() throws FlowBuilderException {
				addViewState("viewState", "viewName", new Transition[] { transition(on(submit()), to("viewState")),
						transition(on(finish()), to("endState")) });
				addEndState("endState");
			}
		};
		new FlowAssembler("flow", builder).assembleFlow();
		Flow flow = builder.getResult();
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ApplicationViewSelection view = (ApplicationViewSelection)flowExecution.start(new MockExternalContext());
		assertNotNull(view);
		assertEquals("viewName", view.getViewName());
		for (int i = 0; i < 10; i++) {
			view = (ApplicationViewSelection)flowExecution.signalEvent("submit", new MockExternalContext());
			assertEquals("viewName", view.getViewName());
		}
		assertTrue(flowExecution.isActive());
		flowExecution.signalEvent("finish", new MockExternalContext());
		assertFalse(flowExecution.isActive());
	}

	public void testLoopInFlowWithSubFlow() throws Exception {
		AbstractFlowBuilder childBuilder = new AbstractFlowBuilder() {
			public void buildStates() throws FlowBuilderException {
				addActionState("doOtherStuff", new AbstractAction() {
					private int executionCount = 0;

					protected Event doExecute(RequestContext context) throws Exception {
						executionCount++;
						if (executionCount < 2) {
							return success();
						}
						return error();
					}
				},
						new Transition[] { transition(on(success()), to(finish())),
								transition(on(error()), to("stopTest")) });
				addEndState(finish());
				addEndState("stopTest");
			}
		};
		new FlowAssembler("flow", childBuilder).assembleFlow();
		final Flow childFlow = childBuilder.getResult();
		AbstractFlowBuilder parentBuilder = new AbstractFlowBuilder() {
			public void buildStates() throws FlowBuilderException {
				addActionState("doStuff", new AbstractAction() {
					protected Event doExecute(RequestContext context) throws Exception {
						return success();
					}
				}, transition(on(success()), to("startSubFlow")));
				addSubflowState("startSubFlow", childFlow, new Transition[] {
						transition(on(finish()), to("startSubFlow")), transition(on("stopTest"), to("stopTest")) });
				addEndState("stopTest");
			}
		};
		new FlowAssembler("parentFlow", parentBuilder).assembleFlow();
		Flow parentFlow = parentBuilder.getResult();

		FlowExecution flowExecution = new FlowExecutionImpl(parentFlow);
		flowExecution.start(new MockExternalContext());
		assertFalse(flowExecution.isActive());
	}

	public void testExtensiveFlowNavigationScenario1() {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow1.xml", XmlFlowBuilderTests.class),
				new XmlFlowBuilderTests.TestFlowArtifactFactory());
		FlowAssembler assembler = new FlowAssembler("testFlow1", builder);
		assembler.assembleFlow();
		FlowExecution execution = new FlowExecutionImpl(builder.getResult());
		MockExternalContext context = new MockExternalContext();
		execution.start(context);
		assertEquals("viewState1", execution.getActiveSession().getState().getId());
		assertNotNull(execution.getActiveSession().getScope().get("items"));
		execution.signalEvent("event1", context);
		assertTrue(!execution.isActive());
	}

	public void testExtensiveFlowNavigationScenario2() {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow1.xml", XmlFlowBuilderTests.class),
				new XmlFlowBuilderTests.TestFlowArtifactFactory());
		AttributeMap attributes = new AttributeMap();
		attributes.put("scenario2", Boolean.TRUE);
		FlowAssembler assembler = new FlowAssembler(new FlowArtifactParameters("testFlow1", attributes), builder);
		assembler.assembleFlow();
		FlowExecution execution = new FlowExecutionImpl(builder.getResult());
		MockExternalContext context = new MockExternalContext();
		execution.start(context);
		assertEquals("viewState2", execution.getActiveSession().getState().getId());
		assertNotNull(execution.getActiveSession().getScope().get("items"));
		execution.signalEvent("event2", context);
		assertTrue(!execution.isActive());
	}

	public static TransitionCriteria on(String event) {
		return new EventIdTransitionCriteria(event);
	}

	public static TargetStateResolver to(String stateId) {
		return new StaticTargetStateResolver(stateId);
	}

	public static ViewSelector view(String viewName) {
		return new SimpleViewSelector(viewName);
	}
}