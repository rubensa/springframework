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

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;



/**
 * SimpleDecoratorTests
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class AntTaskTests extends TestCase {

    private static final String TMP = System.getProperty("java.io.tmpdir");
    
    AntTask task;
    
    public void setUp() {
       task = new AntTask();
       task.setProject(new Project());
    }
    
    public void testFileSets() {
        FileSet fs = new FileSet();
        fs.setDir(new File(TMP + "/beandoc-test"));
        fs.setIncludes("**/*.xml");
        task.setOutputDir(new File(TMP));
        task.addFileset(fs);
        
        try {
            task.execute();
            fail();
            
        } catch (BuildException be) {
            // ok
        }
    }
    
    public void testExecute() {
        task.setInputFiles("classpath:org/springframework/beandoc/context1.xml");
        task.setOutputDir(new File(TMP));
        task.setTitle("BeandocTest");
        
        // provide a context with a processor that does nothing
        task.setBeandocContext("org/springframework/beandoc/client/dummyContext.xml");
        
        task.execute();
    }
    
    public void testNullProps() {
        task.setBeandocProps(null);
        task.setBeandocPropsPrefix("anything.");
        
        try {
            task.execute();
            fail();
            
        } catch (BuildException be) {
            // ok
        }
    }
}