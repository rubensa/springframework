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

package org.springframework.beandoc.output;

import org.springframework.beandoc.BeanDocException;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class InvalidTransformerException extends BeanDocException {  

    private static final long serialVersionUID = 3256446923383911734L;
    
    /**
     * @param msg
     * @param ex
     */
    public InvalidTransformerException(String msg, Exception ex) {
        super(msg, ex);
    }
    
}
