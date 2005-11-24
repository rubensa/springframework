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
package org.springframework.webflow.samples.itemlist;

import java.util.ArrayList;
import java.util.List;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

public class AddItemAction extends AbstractAction {

	protected Event doExecute(RequestContext context) throws Exception {
		// check to ensure the incoming request is within the active transaction
		// note that we're also ending the transaction using end==true
		if (!context.inTransaction(true)) {
			// the transaction was not valid so cannot continue normal
			// processing
			return result("txError");
		}
		List list = (List)context.getFlowScope().getOrCreateAttribute("list", ArrayList.class);
		String data = (String)context.getExternalContext().getRequestParameterMap().get("data");
		if (data != null && data.length() > 0) {
			list.add(data);
		}
		// add a bit of artificial think time
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
		}
		return success();
	}
}