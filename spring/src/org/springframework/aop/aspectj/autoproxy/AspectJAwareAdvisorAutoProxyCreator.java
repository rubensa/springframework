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

package org.springframework.aop.aspectj.autoproxy;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aspectj.util.PartialOrder;
import org.aspectj.util.PartialOrder.PartialComparable;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJProxyUtils;
import org.springframework.aop.aspectj.ProxyCreationContext;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}
 * subclass that exposes AspectJ's invocation context and understands AspectJ's rules
 * for advice precedence when multiple pieces of advice come from the same aspect.
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 */
public class AspectJAwareAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {

	private static final Comparator DEFAULT_PRECEDENCE_COMPARATOR = new AspectJPrecedenceComparator();

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		try {
			ProxyCreationContext.notifyProxyCreationStart(beanName, !beanFactory.containsBean(beanName));
			return super.postProcessAfterInitialization(bean, beanName);
		} finally {
			ProxyCreationContext.notifyProxyCreationComplete();
		}
	}

	/**
	 * Sort the rest by AspectJ precedence. If two pieces of advice have
	 * come from the same aspect they will have the same order.
	 * Advice from the same aspect is then further ordered according to the
	 * following rules:
	 * <ul>
	 * <li>if either of the pair is after advice, then the advice declared
	 * last gets highest precedence (runs last)</li>
	 * <li>otherwise the advice declared first gets highest precedence (runs first)</li>
	 * </ul>
	 * <p><b>Important:</b> Advisors are sorted in precedence order, from highest
	 * precedence to lowest. "On the way in" to a join point, the highest precedence
	 * advisor should run first. "On the way out" of a join point, the highest precedence
	 * advisor should run last.
	 */
	protected List sortAdvisors(List advisors) {
		// build list for sorting
		List partiallyComparableAdvisors = new LinkedList();
		for (Iterator it = advisors.iterator(); it.hasNext();) {
			Advisor element = (Advisor) it.next();
			PartiallyComparableAdvisorHolder advisor =
					new PartiallyComparableAdvisorHolder(element, DEFAULT_PRECEDENCE_COMPARATOR);
			partiallyComparableAdvisors.add(advisor);
		}		
		
		// sort it
		List sorted = PartialOrder.sort(partiallyComparableAdvisors);
		if (sorted == null) {
			// TODO: work much harder to give a better error message here.
			throw new IllegalArgumentException("Advice precedence circularity error");
		}
		
		// extract results again
		List result = new LinkedList();
		for (Iterator it = sorted.iterator(); it.hasNext();) {
			PartiallyComparableAdvisorHolder pcAdvisor = (PartiallyComparableAdvisorHolder) it.next();
			result.add(pcAdvisor.getAdvisor());
		}
		
		return result;
	}

	/**
	 * Adds an {@link ExposeInvocationInterceptor} to the beginning of the advice chain.
	 * These additional advices are needed when using AspectJ expression pointcuts
	 * and when using AspectJ-style advice.
	 */
	protected void extendAdvisors(List candidateAdvisors) {
		AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(candidateAdvisors);
	}


	/**
	 * Implements AspectJ PartialComparable interface for defining partial orderings.
	 */
	private static class PartiallyComparableAdvisorHolder implements PartialComparable {

		private final Advisor advisor;

		private final Comparator comparator;

		public PartiallyComparableAdvisorHolder(Advisor advisor, Comparator comparator) {
			this.advisor = advisor;
			this.comparator = comparator;
		}

		public int compareTo(Object obj) {
			Advisor otherAdvisor = ((PartiallyComparableAdvisorHolder) obj).advisor;
			return this.comparator.compare(this.advisor, otherAdvisor);
		}

		public int fallbackCompareTo(Object obj) {
			return 0;
		}

		public Advisor getAdvisor() {
			return this.advisor;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			Advice advice = this.advisor.getAdvice();
			sb.append(ClassUtils.getShortName(advice.getClass()));
			sb.append(": ");
			if (this.advisor instanceof Ordered) {
				sb.append("order " + ((Ordered) this.advisor).getOrder() + ", ");
			}
			if (advice instanceof AbstractAspectJAdvice) {
				AbstractAspectJAdvice ajAdvice = (AbstractAspectJAdvice) advice;
				sb.append(ajAdvice.getAspectName());
				sb.append(", declaration order ");
				sb.append(ajAdvice.getDeclarationOrder());
			}
			return sb.toString();
		}
	}

}
