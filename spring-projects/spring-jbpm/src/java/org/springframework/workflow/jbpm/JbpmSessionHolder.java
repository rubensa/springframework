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

package org.springframework.workflow.jbpm;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.jbpm.db.JbpmSession;

/**
 * @author Rob Harrop
 */
public class JbpmSessionHolder extends ResourceHolderSupport {

    private JbpmSession jbpmSession;

    public JbpmSessionHolder(JbpmSession jbpmSession) {
        this.jbpmSession = jbpmSession;
    }

    public JbpmSession getJbpmSession() {
        return this.jbpmSession;
    }

    public void clear() {
        this.jbpmSession = null;
    }
}
