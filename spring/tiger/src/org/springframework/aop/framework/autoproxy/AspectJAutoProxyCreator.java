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

package org.springframework.aop.framework.autoproxy;

import java.util.LinkedList;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.aspectj.AtAspectJAdvisorFactory;
import org.springframework.aop.support.aspectj.ReflectiveAtAspectJAdvisorFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * DefaultAdvisorAutoProxyCreator subclasses that processes all AspectJ annotation classes
 * in the current application context, as well as Spring Advisors. 
 * <br>
 * Any AspectJ annotated classes will automatically be recognized, and their advice
 * applied if Spring AOP's proxy-based model is capable of applying it.
 * This covers method execution joinpoints.
 * <br>
 * Processing of Spring Advisors follows the rules established in DefaultAdvisorAutoProxyCreator.
 * 
 * @author Rod Johnson
 * @since 1.3
 * @see org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
 * @see org.springframework.aop.support.aspectj.AtAspectJAdvisorFactory
 */
public class AspectJAutoProxyCreator extends DefaultAdvisorAutoProxyCreator {
	
	private AtAspectJAdvisorFactory aspectJAdvisorFactory = new ReflectiveAtAspectJAdvisorFactory();

	public void setAspectJAdvisorFactory(AtAspectJAdvisorFactory aspectJAdvisorFactory) {
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// TODO if scoped != singleton and singleton or reverse
//		if (!getBeanFactory().isSingleton(beanName)) {
//			throw new IllegalArgumentException("Inconsistent aspect scope for bean " + beanName);
//		}
		return super.postProcessBeforeInitialization(bean, beanName);
	}


	@Override
	protected List findCandidateAdvisors() {
		
		List<Advisor> advisors = new LinkedList<Advisor>();
		
		// Add all the Spring advisors found according to superclass rules
		advisors.addAll(super.findCandidateAdvisors());
		
		if (!(getBeanFactory() instanceof BeanDefinitionRegistry)) {
			throw new IllegalStateException(
					"Cannot use " + getClass().getName() + " without a BeanDefinitionRegistry");
		}
		BeanDefinitionRegistry owningFactory = (BeanDefinitionRegistry) getBeanFactory();

		// Look for AspectJ annotated aspect classes
		// Create a Spring Advisor for each advice method
		for (String beanName : owningFactory.getBeanDefinitionNames()) {
			//if (getBeanFactory().isSingleton(beanName)) {
				// We must be careful not to instantiate beans eagerly as in this
				// case they would be cached by the Spring container but would not
				// have bean weaved
				// TODO pull out into helper, findBeansWithAnnotation
				BeanDefinition bd = owningFactory.getBeanDefinition(beanName);
				if (!(bd instanceof RootBeanDefinition)) {
					continue;
				}
				RootBeanDefinition rbd = (RootBeanDefinition) bd;
				if (rbd.getBeanClass() == null) {
					continue;
				}

				if (aspectJAdvisorFactory.isAspect(rbd.getBeanClass())) {
					logger.debug("Found aspect bean '" + beanName + "'");
					Object beanInstance = getBeanFactory().getBean(beanName);
					List<Advisor> classAdvisors = this.aspectJAdvisorFactory.getAdvisors(beanInstance);
					logger.debug("Found " + classAdvisors.size() + " AspectJ advice methods");
					advisors.addAll(classAdvisors);
				}
			//}
		}
		
		// Need this to expose target and invocation context data to
		// AspectJ expression pointcuts
		if (!advisors.isEmpty()) {
			advisors.add(new DefaultPointcutAdvisor(ExposeInvocationInterceptor.INSTANCE));
		}
		
		return advisors;
	}

}
