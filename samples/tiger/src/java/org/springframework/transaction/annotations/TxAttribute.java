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

package org.springframework.transaction.annotations;

import java.lang.annotation.*;

/**
 * JDK 1.5+ Annotation for describing transaction attributes on a method or class.
 * This annotation type is generally directly comparable to Spring's 
 * {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute}
 * class, and in fact {@link AnnotationsTransactionAttributeSource} will directly
 * convert the data to the latter class, so that Spring's transaction support code
 * does not have to know about Annotations. If no rules are relevant to the exception,
 * it will be treated like DefaultTransactionAttribute (rolling back on
 * runtime exceptions).
 * 
 * @see @link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 * @see @link org.springframework.transaction.interceptor.DefaultTransactionAttribute
 * 
 * @author Colin Sampaleanu
 */
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface TxAttribute {
	
	/**
	 * The transaction propagation type 
	 */
	PropagationType propagationType() default PropagationType.REQUIRED;
	
    /**
     * The transaction isolation level
     */
	IsolationLevel isolationLevel() default IsolationLevel.DEFAULT;
	
	/**
	 * True if the transaction is read-only 
	 */
	boolean readOnly() default false;
	
	/**
	 * <p>0 or more exception Classes, which must be a subclas of Throwable, indicating
	 * which exception types should cause a transaction rollback. This is the preferred
	 * way to construct a rollback rule, matching the exception class and subclasses.</p>
	 * 
	 * Similar to {@link
	 * org.springframework.transaction.interceptor.RollbackRuleAttribute.RollbackRuleAttribute(Class clazz)}
	 */
	Class[] rollbackFor() default {};
	
	/**
	 * <p>0 or more exception names (for exceptions which must be a subclass of Throwable)
	 * which indicate exception types which should cause a transaction rollback.</p>
	 * 
	 * <p>This can be a substring, with no wildcard support at present.
	 * A value of "ServletException" would match ServletException and
	 * subclasses, for example.
	 * <p><b>NB: </b>Consider carefully how specific the pattern is, and whether
	 * to include package information (which isn't mandatory). For example,
	 * "Exception" will match nearly anything, and will probably hide other rules.
	 * "java.lang.Exception" would be correct if "Exception" was meant to define
	 * a rule for all checked exceptions. With more unusual Exception
	 * names such as "BaseBusinessException" there's no need to use a FQN.</p>
	 * 
	 * Similar to {@link
	 * org.springframework.transaction.interceptor.RollbackRuleAttribute.RollbackRuleAttribute(String exceptionName)}
	 */
    String[] rollbackForClassname() default {};
    
    /**
	 * <p>0 or more exception Classes, which must be a subclas of Throwable, indicating
	 * which exception types should not cause a transaction rollback. This is the preferred
	 * way to construct a rollback rule, matching the exception class and subclasses.</p>
	 * 
	 * Similar to {@link
	 * org.springframework.transaction.interceptor.NoRollbackRuleAttribute.NoRollbackRuleAttribute(Class clazz)}
	 */
	Class[] noRollbackFor() default {};
	
	/**
	 * <p>0 or more exception names (for exceptions which must be a subclass of
	 * Throwable) which indicate exception types which should not cause a
	 * transaction rollback.</p>
	 * 
	 * <p>See description of {@link #rollbackForClassname()} for more info on how
	 * the specified names are treated.</p>
	 * 
	 * Similar to {@link
	 * org.springframework.transaction.interceptor.NoRollbackRuleAttribute.NoRollbackRuleAttribute(String exceptionName)}
	 */
	String[] noRollbackForClassname() default {};
}
