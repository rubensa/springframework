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

package org.springframework.orm.jpa;

import javax.persistence.EntityManager;


/**
 * Common API for portable value adds supported by all vendors
 * <p>
 * Many of these features may make their way into future version of
 * the JPA API. In that case we will implement these methods to
 * use the standard method, and deprecate our own method.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public interface PortableEntityManagerPlusOperations {
	
	EntityManager getNativeEntityManager();
	
	void evict(Object o);
	
	// all methods
	
	// EMFInfo?
	

}
