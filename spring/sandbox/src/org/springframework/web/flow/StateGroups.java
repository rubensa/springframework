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
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class StateGroups implements Serializable {

	public static final String DEFAULT_GROUP_ID = "main";

	private Flow flow;

	private Set stateGroups = new LinkedHashSet(6);

	public StateGroups(Flow flow) {
		this.flow = flow;
	}

	public void add(AbstractState state) {
		add(DEFAULT_GROUP_ID, state);
	}

	public void add(String groupId, AbstractState state) {
		addAll(new AbstractState[] { state });
	}

	public void addAll(AbstractState[] states) {
		addAll(DEFAULT_GROUP_ID, states);
	}

	public void addAll(String groupId, AbstractState[] states) {
		StateGroup group = getGroup(groupId);
		if (group == null) {
			group = new StateGroup(flow, groupId, states);
			add(group);
		} else {
			group.addAll(states);
		}
	}
	
	public StateGroup getGroup(String groupId) {
		Iterator it = stateGroups.iterator();
		while (it.hasNext()) {
			StateGroup group = (StateGroup)it.next();
			if (group.getId().equals(groupId)) {
				return group;
			}
		}
		return null;
	}

	public boolean add(StateGroup stateGroup) {
		Assert.notNull(stateGroup, "The state group to add is required");
		return stateGroups.add(stateGroup);
	}

	public boolean isEmpty() {
		return stateGroups.isEmpty();
	}

	public StateGroup getLastGroup() {
		Iterator it = stateGroups.iterator();
		StateGroup lastGroup = null;
		while (it.hasNext()) {
			lastGroup = (StateGroup)it.next();
		}
		return lastGroup;
	}

	public Iterator iterator() {
		return stateGroups.iterator();
	}

	public Iterator statesIterator() {
		return new StatesIterator();
	}

	public class StatesIterator implements Iterator {
		private Iterator groupIterator = stateGroups.iterator();

		private Iterator statesIterator;

		public boolean hasNext() {
			return groupIterator.hasNext() || (statesIterator != null && statesIterator.hasNext());
		}

		public Object next() {
			if (statesIterator == null) {
				statesIterator = ((StateGroup)groupIterator.next()).iterator();
			}
			if (statesIterator.hasNext()) {
				return statesIterator.next();
			}
			else {
				statesIterator = ((StateGroup)groupIterator.next()).iterator();
				return next();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public boolean equals(Object o) {
		if (!(o instanceof StateGroups)) {
			return false;
		}
		StateGroups g = (StateGroups)o;
		return stateGroups.equals(g.stateGroups);
	}

	public int hashCode() {
		return stateGroups.hashCode();
	}

	public String toString() {
		return stateGroups.toString();
	}
}