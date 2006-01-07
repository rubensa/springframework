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
package org.springframework.webflow.test;

import org.springframework.webflow.registry.ExternalizedFlowRegistrar;
import org.springframework.webflow.registry.XmlFlowRegistrar;

/**
 * Base class for flow integration tests that verify a XML flow definition
 * executes as expected.
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class SearchFlowExecutionTests extends AbstractXmlFlowExecutionTests {
 * 
 * 	   protected ExternalizedFlowDefinition getFlowDefinition() {
 * 	       File flowDir = new File(&quot;src/webapp/WEB-INF&quot;);
 * 	       Resource resource = return new FileSystemResource(new File(flowDir, &quot;search.xml&quot;));
 * 		   return new ExternalizedFlowDefinition("search", resource);
 * 	   }
 * 
 *     protected String[] getConfigLocations() {
 *         return new String[] { &quot;classpath:example/applicationContext.xml };
 *     }
 *     
 *     public void testStartFlow() {
 * 	       startFlow();
 * 		   assertCurrentStateEquals(&quot;displaySearchCriteria&quot;);
 * 	   }
 * 
 * 	   public void testDisplayCriteriaSubmitSuccess() {
 * 	       startFlow();
 * 		   Map parameters = new HashMap();
 * 		   parameters.put(&quot;firstName&quot;, &quot;Keith&quot;);
 * 		   parameters.put(&quot;lastName&quot;, &quot;Donald&quot;);
 * 		   ViewSelection view = signalEvent(&quot;search&quot;, parameters);
 * 		   assertCurrentStateEquals(&quot;displaySearchResults&quot;);
 * 		   assertModelAttributeCollectionSize(1, &quot;results&quot;, view);
 * 	   }
 * }
 * </pre>
 * 
 * @author Keith Donald
 */
public abstract class AbstractXmlFlowExecutionTests extends AbstractExternalizedFlowExecutionTests {
	protected ExternalizedFlowRegistrar createFlowRegistrar() {
		return new XmlFlowRegistrar();
	}
}