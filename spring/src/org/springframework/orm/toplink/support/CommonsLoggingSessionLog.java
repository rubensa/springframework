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

package org.springframework.orm.toplink.support;

import oracle.toplink.logging.AbstractSessionLog;
import oracle.toplink.logging.SessionLogEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TopLink 10.1.3 SessionLog implementation that logs through Commons Logging.
 *
 * <p>The namespace used is "oracle.toplink.xxx", with the latter part being
 * the TopLink log category ("sql"/"transaction"/etc). In case of no category
 * given, "session" will be used as default. This allows for fine-grained
 * filtering of log messages, for example through Log4J configuration.
 *
 * <p>Maps TopLink's SEVERE level to CL ERROR, TopLink's WARNING to CL WARN,
 * TopLink's INFO to CL INFO, TopLink's CONFIG/FINE/FINER to CL DEBUG,
 * and TopLink's FINEST to CL TRACE. This results in common CL log behavior:
 * INFO logging only at startup; operation logging at DEBUG level. Debug logging
 * can be further filtered according to categories: for example, activate Log4J
 * DEBUG logging for category "oracle.toplink.sql" to see the generated SQL.
 *
 * <p><b>Note:</b> This implementation will only work on TopLink 10.1.3 or higher,
 * as it is built against TopLink's new SessionLog facilities in the
 * <code>oracle.toplink.logging</code> package, supporting log categories.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see CommonsLoggingSessionLog904
 * @see oracle.toplink.logging.JavaLog
 * @see org.springframework.orm.toplink.LocalSessionFactoryBean#setSessionLog
 */
public class CommonsLoggingSessionLog extends AbstractSessionLog {

	public static final String NAMESPACE_PREFIX = "oracle.toplink.";

	public static final String DEFAULT_NAMESPACE = "session";

	public static final String DEFAULT_SEPARATOR = "--";


	private String separator = DEFAULT_SEPARATOR;


	/**
	 * Specify the separator between TopLink's supplemental details
	 * (session, connection) and the log message itself. Default is "--".
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * Return the separator between TopLink's supplemental details
	 * (session, connection) and the log message itself. Default is "--".
	 */
	public String getSeparator() {
		return separator;
	}


	public void log(SessionLogEntry entry) {
		Log logger = LogFactory.getLog(getCategory(entry));
		switch (entry.getLevel()) {
			case SEVERE:
				if (logger.isErrorEnabled()) {
					logger.error(getMessageString(entry), entry.getException());
				}
				break;
			case WARNING:
				if (logger.isWarnEnabled()) {
					logger.warn(getMessageString(entry), entry.getException());
				}
				break;
			case INFO:
				if (logger.isInfoEnabled()) {
					logger.info(getMessageString(entry), entry.getException());
				}
				break;
			case CONFIG:
				if (logger.isDebugEnabled()) {
					logger.debug(getMessageString(entry), entry.getException());
				}
			case FINE:
				if (logger.isDebugEnabled()) {
					logger.debug(getMessageString(entry), entry.getException());
				}
				break;
			case FINER:
				if (logger.isDebugEnabled()) {
					logger.debug(getMessageString(entry), entry.getException());
				}
				break;
			case FINEST:
				if (logger.isTraceEnabled()) {
					logger.trace(getMessageString(entry), entry.getException());
				}
				break;
		}
	}

	/**
	 * Determine the log category for the given log entry.
	 * <p>If the entry carries a name space value, it will be appended
	 * to the "oracle.toplink." prefix; else, "oracle.toplink.session"
	 * will be used.
	 */
	protected String getCategory(SessionLogEntry entry) {
		if (entry.getNameSpace() != null) {
			return NAMESPACE_PREFIX + entry.getNameSpace();
		}
		return NAMESPACE_PREFIX + DEFAULT_NAMESPACE;
	}

	/**
	 * Build the message String for the given log entry, including the
	 * supplemental details (session, connection) and the formatted message.
	 * @see #getSessionString(oracle.toplink.sessions.Session)
	 * @see #getConnectionString(oracle.toplink.internal.databaseaccess.Accessor)
	 * @see #formatMessage(oracle.toplink.logging.SessionLogEntry)
	 * @see #getSeparator()
	 */
	protected String getMessageString(SessionLogEntry entry) {
		StringBuffer buf = new StringBuffer();
		if (entry.getSession() != null) {
			buf.append(getSessionString(entry.getSession()));
			buf.append(getSeparator());
		}
		if (entry.getConnection() != null) {
			buf.append(getConnectionString(entry.getConnection()));
			buf.append(getSeparator());
		}
		buf.append(formatMessage(entry));
		return buf.toString();
	}


	/**
	 * Throws an UnsupportedOperationException: This method is not
	 * called any longer as of TopLink 10.1.3, but abstract in
	 * 10.1.3 Preview 3 (which we want to be able to compile against).
	 * <p>Do not remove this method for the time being, even if the
	 * superclass method is no longer abstract in <code>toplink-api.jar</code>!
	 */
	public void log(oracle.toplink.sessions.SessionLogEntry entry) {
		throw new UnsupportedOperationException("oracle.toplink.sessions.SessionLogEntry not supported");
	}

}
