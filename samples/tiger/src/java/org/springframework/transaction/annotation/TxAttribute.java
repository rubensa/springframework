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

package org.springframework.transaction.annotation;

import java.lang.annotation.*;

/**
 * TODO: document after interfaces and classes are stable
 *
 * @author Colin Sampaleanu
 */
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface TxAttribute {
	PropagationType propagationType() default PropagationType.REQUIRED;
	IsolationLevel isolationLevel() default IsolationLevel.DEFAULT;
	boolean readOnly() default false;
	Class[] rollbackFor() default {};
	String[] rollbackForClassname() default {};
	Class[] noRollbackFor() default {};
	String[] noRollbackForClassname() default {};
}
