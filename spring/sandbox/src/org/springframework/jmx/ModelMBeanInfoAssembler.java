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
package org.springframework.jmx;

import javax.management.modelmbean.ModelMBeanInfo;

/**
 * Interface to be implemented by all classes that can 
 * create management interface metadata for a managed resource.
 * @author Rob Harrop
 */
public interface ModelMBeanInfoAssembler {

    /**
     * Creates the ModelMBeanInfo for the given managed resource.
     * @param bean The resource that is to be managed.
     * @return The ModelMBeanInfo metadata.
     */
    public ModelMBeanInfo getMBeanInfo(Object bean);
}
