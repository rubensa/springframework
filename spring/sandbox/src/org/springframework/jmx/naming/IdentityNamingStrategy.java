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
package org.springframework.jmx.naming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.ObjectNameManager;
import org.springframework.jmx.exceptions.ObjectNamingException;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author Rob Harrop
 */
public class IdentityNamingStrategy implements ObjectNamingStrategy {

    public ObjectName getObjectName(Object managedResource, String key) {
        String objectName = null;
        try {
            StringBuffer sb = new StringBuffer(256);
            
            sb.append(managedResource.getClass().getPackage().getName());
            sb.append(":");
            sb.append("class=");
            sb.append(ClassUtils.getShortName(managedResource.getClass()));
            sb.append(",hashCode=");
            sb.append(ObjectUtils.getIdentityHexString(managedResource));
            
            objectName =  sb.toString();
            return ObjectNameManager.getInstance(objectName);
        } catch (MalformedObjectNameException ex) {
            throw new ObjectNamingException("Invalid ObjectName: " + objectName, ex);
        }
    }
}