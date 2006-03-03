/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory;

import java.util.Stack;

/**
 * Simple {@link Stack}-based structure for tracking the logical position during
 * a parsing process. {@link Entry entries} are added to the stack at each point
 * during the parse phase in a {@link org.springframework.beans.factory.support.BeanDefinitionReader}-specific
 * manner.
 * <p/>
 * Calling {@link #toString()} will render a tree-style view of the current logical
 * position in the parse phase. This representation is intended for use in
 * error messages.
 * 
 * @author Rob Harrop
 * @since 2.0
 */
public final class ParseState {

	/**
	 * Tab character used when rendering the tree-style representation.
	 */
	private static final char TAB = '\t';

	/**
	 * Internal {@link Stack} storage.
	 */
	private final Stack state;

	/**
	 * Creates a new <code>ParseState</code> with an empty {@link Stack}.
	 */
	public ParseState() {
		this.state = new Stack();
	}

	/**
	 * Creates a new <code>ParseState</code> whose {@link Stack} is a {@link Object#clone clone}
	 * of that of the passed in <code>ParseState</code>.
	 */
	private ParseState(ParseState other) {
		this.state = (Stack) other.state.clone();
	}

	/**
	 * Adds a new {@link Entry} to the {@link Stack}.
	 */
	public void push(Entry entry) {
		this.state.push(entry);
	}

	/**
	 * Removes an {@link Entry} from the {@link Stack}.
	 */
	public void pop() {
		this.state.pop();
	}

	/**
	 * Returns the {@link Entry} currently at the top of the {@link Stack} or
	 * <code>null</code> if the {@link Stack} is empty.
	 */
	public Entry peek() {
		return (Entry) (this.state.empty() ? null : this.state.peek());
	}

	/**
	 * Creates a new instance of {@link ParseState} which is an independent snapshot
	 * of this instance.
	 */
	public ParseState snapshot() {
		return new ParseState(this);
	}

	/**
	 * Returns a tree-style representation of the current <code>ParseState</code>.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int x = 0; x < this.state.size(); x++) {
			if (x > 0) {
				sb.append('\n');
				for (int y = 0; y < x; y++) {
					sb.append(TAB);
				}
				sb.append("-> ");
			}
			sb.append(this.state.get(x));
		}
		return sb.toString();
	}
}
