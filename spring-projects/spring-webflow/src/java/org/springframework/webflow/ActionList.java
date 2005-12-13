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
package org.springframework.webflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.style.ToStringCreator;

/**
 * An ordered, typed list of actions mainly for use internally by flow artifacts
 * that can execute groups of actions.
 * 
 * @see Flow#getStartActionList()
 * @see State#getEntryActionList()
 * @see ActionState#getActionList()
 * @see TransitionableState#getExitActionList()
 * @see Flow#getEndActionList()
 * 
 * @author Keith Donald
 */
public class ActionList {

	/**
	 * The lists of actions.
	 */
	private List actions = new LinkedList();

	/**
	 * Add an action to this list.
	 * @param action the action to add
	 * @return true if this list's contents changed as a result of the add
	 * operation
	 */
	public boolean add(Action action) {
		return actions.add(action);
	}

	/**
	 * Add a collection of actions to this list.
	 * @param actions the actions to add
	 * @return true if this list's contents changed as a result of the add
	 * operation
	 */
	public boolean addAll(Action[] actions) {
		if (actions == null) {
			return false;
		}
		return this.actions.addAll(Arrays.asList(actions));
	}

	/**
	 * Tests if the action is in this list.
	 * @param action the action
	 * @return true if the action is contained in this list, false otherwise
	 */
	public boolean contains(Action action) {
		return actions.contains(action);
	}

	/**
	 * Remove the action instance from this list.
	 * @param action the action to add
	 * @return true if this list's contents changed as a result of the remove
	 * operation 
	 */
	public boolean remove(Action action) {
		return actions.remove(action);
	}

	/**
	 * Returns the size of this action list.
	 * @return the action list size.
	 */
	public int size() {
		return actions.size();
	}

	/**
	 * Returns an iterator over the list of actions in this list.
	 * @return the action list iterator
	 */
	public Iterator iterator() {
		return actions.iterator();
	}

	/**
	 * Returns the action in this list at the provided index.
	 * @param index the action index
	 * @return the action the action
	 */
	public Action get(int index) throws IndexOutOfBoundsException {
		return (Action)actions.get(index);
	}

	/**
	 * Returns the action in this list at the provided index, exposing it as an
	 * annotated action. This allows clients to access specific properties about
	 * a target action instance if they exist.
	 * @return the action, as an annotated action
	 */
	public AnnotatedAction getAnnotated(int index) throws IndexOutOfBoundsException {
		Action action = get(index);
		if (action instanceof AnnotatedAction) {
			return (AnnotatedAction)action;
		}
		else {
			return new AnnotatedAction(action);
		}
	}

	/**
	 * Convert this list to a typed action array.
	 * @return the action list, as a typed array
	 */
	public Action[] toArray() {
		return (Action[])actions.toArray(new Action[0]);
	}

	/**
	 * Convert this list to a <code>java.util.List</code>.
	 * @return the action list, as a java.util.List
	 */
	public List toList() {
		return Collections.unmodifiableList(actions);
	}

	/**
	 * Returns the list of actions in this list as a typed annotated action
	 * array. This is a convenience method allowing clients to access properties
	 * about an action if they exist.
	 * @return the annotated action list, as a typed array
	 */
	public AnnotatedAction[] toAnnotatedArray() {
		AnnotatedAction[] annotatedActions = new AnnotatedAction[actions.size()];
		for (int i = 0; i < size(); i++) {
			Action action = (Action)get(i);
			if (action instanceof AnnotatedAction) {
				annotatedActions[i] = (AnnotatedAction)action;
			}
			else {
				annotatedActions[i] = new AnnotatedAction(action);
			}
		}
		return annotatedActions;
	}

	/**
	 * Executes the actions contained within this action list. Simply executes
	 * over each action and calls execute. Action result events are ignored.
	 * @param context the action execution request context
	 */
	public void execute(RequestContext context) {
		Iterator it = actions.iterator();
		while (it.hasNext()) {
			Action action = (Action)it.next();
			new ActionExecutor(action).execute(context);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("actions", actions).toString();
	}
}