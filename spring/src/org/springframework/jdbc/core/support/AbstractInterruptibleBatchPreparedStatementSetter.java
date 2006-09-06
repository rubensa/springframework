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

package org.springframework.jdbc.core.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;

/**
 * Abstract implementation of the InterruptibleBatchPreparedStatementSetter
 * interface, combining the check for available values and setting of those
 * into a single callback method (<code>setValuesIfAvailable</code>).
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setValuesIfAvailable
 */
public abstract class AbstractInterruptibleBatchPreparedStatementSetter
		implements InterruptibleBatchPreparedStatementSetter {

	private boolean exhausted;


	/**
	 * This implementation calls <code>setValuesAndCheck</code>
	 * and sets this instance's exhaustion flag accordingly.
	 */
	public final void setValues(PreparedStatement ps, int i) throws SQLException {
		this.exhausted = !setValuesIfAvailable(ps, i);
	}

	/**
	 * This implementation return this instance's current exhaustion flag.
	 */
	public final boolean isBatchExhausted(int i) {
		return this.exhausted;
	}

	/**
	 * This implementation returns <code>Integer.MAX_VALUE</code>.
	 * Can be overridden in subclasses to lower the maximum batch size.
	 */
	public int getBatchSize() {
		return Integer.MAX_VALUE;
	}


	/**
	 * Check for available values and set them on the given PreparedStatement.
	 * If no values are available anymore, return <code>false</code>.
	 * @param ps PreparedStatement we'll invoke setter methods on
	 * @param i index of the statement we're issuing in the batch, starting from 0
	 * @return whether there were values to apply (that is, whether the applied
	 * parameters should be added to the batch and this method should be called
	 * for a further iteration)
	 * @throws SQLException if thrown by JDBC API methods
	 */
	protected abstract boolean setValuesIfAvailable(PreparedStatement ps, int i) throws SQLException;

}
