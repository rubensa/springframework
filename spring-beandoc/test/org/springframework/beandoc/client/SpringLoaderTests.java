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
import java.io.FileWriter;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;



/**
 * SimpleDecoratorTests
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class SpringLoaderTests extends TestCase {
    
    public void testGoodLoaderNullProps() {
        SpringLoaderCommand c = new SpringLoaderCommand("classpath:org/springframework/beandoc/beandoc.xml", System.getProperty("java.io.tmpdir"), "Test", null);
        assertNotNull(c.toString());
        try {
            BeanFactory bf = SpringLoader.getBeanFactory(c);
        } catch (Exception e) {
            fail();
        }
    }
    
    public void testGoodLoaderWithProps() {
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        File props = new File(tmp, "beandoc.junit.properties");
        
        SpringLoaderCommand c = new SpringLoaderCommand(null, System.getProperty("java.io.tmpdir"), "Test", props.getAbsolutePath());
        try {
            if (!props.isFile())
                props.createNewFile();
            FileWriter fw = new FileWriter(props);
            fw.write("input.files=classpath:org/springframework/beandoc/beandoc.xml");
            fw.close();
            BeanFactory bf = SpringLoader.getBeanFactory(c);
        } catch (Exception e) {
            fail();
        }
    }
    
    public void testNulls() {
        SpringLoaderCommand c = new SpringLoaderCommand(null, null, null, null);
        try {
            BeanFactory bf = SpringLoader.getBeanFactory(c);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof BeanDefinitionStoreException);
        }
    }
}
