/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop.aspectj.annotation;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.Assert;

/**
 * Helper for retrieving @AspectJ beans from a BeanFactory and building
 * Spring Advisors based on them, for use with auto-proxying.
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AnnotationAwareAspectJAutoProxyCreator
 * @see org.springframework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper
 */
public class BeanFactoryAspectJAdvisorsBuilder {

	private static final Log logger = LogFactory.getLog(BeanFactoryAspectJAdvisorsBuilder.class);

	private final ListableBeanFactory beanFactory;

	private final AspectJAdvisorFactory advisorFactory;


	/**
	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {
		this(beanFactory, new ReflectiveAspectJAdvisorFactory());
	}

	/**
	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 * @param advisorFactory the AspectJAdvisorFactory to build each Advisor with
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		Assert.notNull(advisorFactory, "AspectJAdvisorFactory must not be null");
		this.beanFactory = beanFactory;
		this.advisorFactory = advisorFactory;
	}


	/**
	 * Look for AspectJ-annotated aspect beans in the current bean factory,
	 * and return to a list of Spring AOP Advisors representing them.
	 * <p>Creates a Spring Advisor for each AspectJ advice method.
	 * @return the list of {@link org.springframework.aop.Advisor} beans
	 * @see #isEligibleBean
	 */
	public List<Advisor> buildAspectJAdvisors() {
		List<Advisor> advisors = new LinkedList<Advisor>();
		String[] beanNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this.beanFactory, Object.class, true, false);

		for (String beanName : beanNames) {
			if (!isEligibleBean(beanName)) {
				continue;
			}

			// We must be careful not to instantiate beans eagerly as in this
			// case they would be cached by the Spring container but would not
			// have been weaved
			Class beanType = this.beanFactory.getType(beanName);
			if (beanType == null) {
				continue;
			}

			if (this.advisorFactory.isAspect(beanType)) {
				AspectMetadata amd = new AspectMetadata(beanType, beanName);
				MetadataAwareAspectInstanceFactory factory = null;
				if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
					factory = new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
				}
				else {
					// Per target or per this
					if (this.beanFactory.isSingleton(beanName)) {
						throw new IllegalArgumentException(
								"Bean with name '" + beanName + "' is a singleton, but aspect instantiation model is not singleton");
					}
					factory = new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
				}
				List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
				if (logger.isDebugEnabled()) {
					logger.debug("Found " + classAdvisors.size() +
							" AspectJ advice methods in bean with name '" + beanName + "'");
				}
				advisors.addAll(classAdvisors);
			}
		}
		return advisors;
	}

	/**
	 * Return whether the aspect bean with the given name is eligible.
	 * @param beanName the name of the aspect bean
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}
