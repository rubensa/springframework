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

package org.springframework.beandoc;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;


/**
 * Implementation of the ResourceLoader interface that assumes Files as the default 
 * resource if no protocol is specified.
 * 
 * @author Darren Davison
 * @since 1.0
 */
class DefaultFileSystemResourceLoader extends DefaultResourceLoader {
    protected Resource getResourceByPath(String path) {
        return new FileSystemResource(path);
    }
}
