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

package org.springframework.beandoc.client;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.springframework.beandoc.ContextProcessor;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class AntTask extends Task {

    private static final String BEANDOC_PREFIX = "beandoc.";
    
    private Properties beandocProps = new Properties();

    public void execute() throws BuildException {      
        Hashtable antProps = getProject().getProperties();
        for (Iterator i = antProps.keySet().iterator(); i.hasNext();) {
            String nextKey = (String) i.next();
            if (nextKey.startsWith(BEANDOC_PREFIX))
                beandocProps.put(((String) nextKey).substring(BEANDOC_PREFIX.length()), antProps.get(nextKey));
                
        }
        
		BeanFactory factory = SpringLoader.getBeanFactory(beandocProps);	
		
		try {
		    ContextProcessor cp = (ContextProcessor) factory.getBean("processor");
		    cp.process();
		} catch (Exception e) {
		    throw new BuildException(e);
		}
    }
}
