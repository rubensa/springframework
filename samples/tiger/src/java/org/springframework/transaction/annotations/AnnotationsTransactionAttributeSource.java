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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.metadata.Attributes;
import org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * <p>Implementation of TransactionAttributeSource for working with transaction
 * metadata in JDK 1.5+ Annotations format.</p>
 * 
 * <p>This class expects the Spring Attributes implementation it reads from to 
 * supply JDK 1.5+ standard Annotations from the   
 * {@link org.springframework.transaction.annotations} package. (Typically the
 * Attributes implementation will be
 * {@link org.springframework.metadata.annotations.AnnotationsAttributes 
 * AnnotationsAttributes}.) These Annotations are then converted on the fly and
 * returned as Spring transaction 
 * {@link org.springframework.transaction.interceptor attributes}.
 *  
 * @author Colin Sampaleanu
 * @see org.springframework.metadata.Attributes
 * @see org.springframework.metadata.annotations 
 * @see org.springframework.transaction.annotations
 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource
 */
public class AnnotationsTransactionAttributeSource extends
			AbstractFallbackTransactionAttributeSource implements InitializingBean {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	/**
	 * Underlying Attributes implementation we're using
	 */
	private Attributes attributes;
    
    
    /**
     * Create a new AnnotationsTransactionAttributeSource.
     * @see #setAttributes
     */
    public AnnotationsTransactionAttributeSource() {
    }

	public AnnotationsTransactionAttributeSource(Attributes attributes) {
		this.attributes = attributes;
	}
    
    /**
     * Set the Attributes implementation to use.
     */
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public void afterPropertiesSet() {
        if (this.attributes == null) {
            throw new IllegalArgumentException("'attributes' is required");
        }
    }

	/**
	 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#findAllAttributes(java.lang.Class)
	 */
	protected Collection findAllAttributes(Class clazz) {
		return attributes.getAttributes(clazz);
	}

	/**
	 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#findAllAttributes(java.lang.reflect.Method)
	 */
	protected Collection findAllAttributes(Method m) {
		return attributes.getAttributes(m);
	}
	
	
	/**
	 * Return the transaction attribute, given this set of attributes
	 * attached to a method or class. Overrides method from parent class.
	 * This version actually converts JDK 5.0+ Annotations to the Spring
	 * classes.
	 * Return null if it's not transactional. 
	 * @param atts attributes attached to a method or class. May
	 * be null, in which case a null TransactionAttribute will be returned.
	 * @return TransactionAttribute configured transaction attribute, or null
	 * if none was found
	 */
	protected TransactionAttribute findTransactionAttribute(Collection atts) {
		
		if (atts == null)
			return null;

		// see if there is a transaction annotation
		for (Object att : atts) {
			if (att instanceof TxAttribute) {
				TxAttribute ruleBasedTx = (TxAttribute) att;
				
				RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
				rbta.setPropagationBehavior(ruleBasedTx.propagationType().value());
				rbta.setIsolationLevel(ruleBasedTx.isolationLevel().value());
				rbta.setReadOnly(ruleBasedTx.readOnly());
				
				ArrayList<RollbackRuleAttribute> rollBackRules = new ArrayList<RollbackRuleAttribute>();
				
				Class[] rbf = ruleBasedTx.rollbackFor();
				for (int i=0; i < rbf.length; ++i) {
					RollbackRuleAttribute rule = new RollbackRuleAttribute(rbf[i]);
					rollBackRules.add(rule);
				}
				
				String[] rbfc = ruleBasedTx.rollbackForClassname();
				for (int i=0; i < rbfc.length; ++i) {
					RollbackRuleAttribute rule = new RollbackRuleAttribute(rbfc[i]);
					rollBackRules.add(rule);
				}
				
				Class[] nrbf = ruleBasedTx.noRollbackFor();
				for (int i=0; i < nrbf.length; ++i) {
					NoRollbackRuleAttribute rule = new NoRollbackRuleAttribute(nrbf[i]);
					rollBackRules.add(rule);
				}
				
				String[] nrbfc = ruleBasedTx.noRollbackForClassname();
				for (int i=0; i < nrbfc.length; ++i) {
					NoRollbackRuleAttribute rule = new NoRollbackRuleAttribute(nrbfc[i]);
					rollBackRules.add(rule);
				}
				
				rbta.getRollbackRules().addAll(rollBackRules);

				return rbta;
			}
		}

		return null;
	}
	
}
