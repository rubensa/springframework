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

package org.springframework.beandoc.util;

import org.springframework.beandoc.util.FileSystemResourceLoader;
import org.springframework.core.io.Resource;

import junit.framework.TestCase;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class FileSystemResourceLoaderTests extends TestCase { 
    
    FileSystemResourceLoader rl;
    
    public void setUp() {
        rl = new FileSystemResourceLoader();
    }
    
    public void testResourceLoading() {
        Resource res = rl.getResourceByPath(System.getProperty("user.dir"));
        assertTrue(res.exists());
        Resource res2 = rl.getResourceByPath("/bet/you/do/not/have/a/path/like/this.huh?");
        assertFalse(res2.exists());
    }
    
}
