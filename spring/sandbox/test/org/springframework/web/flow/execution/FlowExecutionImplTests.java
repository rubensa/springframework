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
package org.springframework.web.flow.execution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.config.FlowFactoryBean;
import org.springframework.web.flow.config.XmlFlowBuilder;
import org.springframework.web.flow.config.XmlFlowBuilderTests;
import org.springframework.web.flow.execution.impl.FlowExecutionImpl;

/**
 * Test case for FlowExecutionStack.
 * 
 * @see org.springframework.web.flow.execution.impl.FlowExecutionImpl
 * 
 * @author Erwin Vervaet
 */
public class FlowExecutionImplTests extends TestCase {

	private FlowLocator flowLocator;

	private FlowExecutionImpl flowExecution;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow.xml", XmlFlowBuilderTests.class));
		builder.setFlowServiceLocator(new XmlFlowBuilderTests.TestFlowServiceLocator());
		final Flow flow = new FlowFactoryBean(builder).getFlow();
		flowLocator = new FlowLocator() {
			public Flow getFlow(String flowDefinitionId) throws ServiceLookupException {
				if (flow.getId().equals(flowDefinitionId)) {
					return flow;
				}
				throw new ServiceLookupException(Flow.class, flowDefinitionId, null);
			}
		};
		flowExecution = new FlowExecutionImpl(flow);
	}

	protected void runFlowExecutionRehydrationTest() throws Exception {
		// serialize the flowExecution
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(flowExecution);
		oout.flush();

		// deserialize the flowExecution
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream oin = new ObjectInputStream(bin);
		FlowExecutionImpl restoredFlowExecution = (FlowExecutionImpl)oin.readObject();

		assertNotNull(restoredFlowExecution);

		// rehydrate the flow execution
		restoredFlowExecution.rehydrate(flowLocator, flowExecution.getListeners().toArray(), flowExecution.getTransactionSynchronizer());

		assertEquals(flowExecution.isActive(), restoredFlowExecution.isActive());
		if (flowExecution.isActive()) {
			assertTrue(entriesCollectionsAreEqual(
					flowExecution.getActiveSession().getScope().getAttributeMap().entrySet(),
					restoredFlowExecution.getActiveSession().getScope().getAttributeMap().entrySet()));
			assertEquals(flowExecution.getActiveSession().getCurrentState().getId(), restoredFlowExecution.getCurrentState().getId());
			assertEquals(flowExecution.getActiveSession().getFlow().getId(), restoredFlowExecution.getActiveSession().getFlow().getId());
			assertSame(flowExecution.getRootFlow(), restoredFlowExecution.getRootFlow());
		}
		assertEquals(flowExecution.getLastEventId(), restoredFlowExecution.getLastEventId());
		assertEquals(flowExecution.getLastRequestTimestamp(), restoredFlowExecution.getLastRequestTimestamp());
		assertEquals(flowExecution.getListeners().size(), restoredFlowExecution.getListeners().size());
	}

	public void testRehydrate() throws Exception {
		// setup some input data
		Map inputData = new HashMap(1);
		inputData.put("name", "value");
		// start the flow execution
		flowExecution.start(new Event(this, "start", inputData));
		runFlowExecutionRehydrationTest();
	}

	public void testRehydrateNotStarted() throws Exception {
		// don't start the flow execution
		runFlowExecutionRehydrationTest();
	}

	/**
	 * Helper to test if 2 collections of Map.Entry objects contain the same
	 * values.
	 */
	private boolean entriesCollectionsAreEqual(Collection collection1, Collection collection2) {
		if (collection1.size() != collection2.size()) {
			return false;
		}
		for (Iterator it1 = collection1.iterator(), it2 = collection2.iterator(); it1.hasNext() && it2.hasNext();) {
			Map.Entry entry1 = (Map.Entry)it1.next();
			Map.Entry entry2 = (Map.Entry)it2.next();
			if (!entry1.equals(entry2)) {
				return false;
			}
		}
		return true;
	}
}