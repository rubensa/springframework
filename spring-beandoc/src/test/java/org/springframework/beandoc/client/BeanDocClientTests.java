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
import java.io.IOException;

import junit.framework.TestCase;



/**
 * BeanDocClientTests
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class BeanDocClientTests extends TestCase {

    static final String TMP = System.getProperty("java.io.tmpdir");
    
    String[] args1 = {
        "--output", TMP,
        "--prefix", "anything.",
        "--context", "org/springframework/beandoc/client/dummyContext.xml",
        "--title", "BeanDocTest",
        "classpath:org/springframework/beandoc/context1.xml"
    };
    
    String[] args2 = {
        "--output", TMP,
        "--prefix", "anything.",
        "--properties", TMP + "/beandoc.properties",
        "--context", "org/springframework/beandoc/client/dummyContext.xml",
        "--title", "BeanDocTest",
        "classpath:org/springframework/beandoc/context1.xml"
    };
    
    public void testMain() throws IOException {        
        BeanDocClient.main(args1);     
        File tmp = new File(TMP);
        File p = new File(tmp, "beandoc.properties");
        p.createNewFile();
        BeanDocClient.main(args2);
    }
    
}
