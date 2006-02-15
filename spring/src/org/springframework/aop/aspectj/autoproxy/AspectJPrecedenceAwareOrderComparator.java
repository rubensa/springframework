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
 *
 * Created on 15-Feb-2006 by Adrian Colyer
 */
package org.springframework.aop.aspectj.autoproxy;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJAopUtils;
import org.springframework.aop.aspectj.AspectJPointcutAdvisor;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;

/**
 * <p>
 * Orders AspectJ advice/advisors by precedence (<i>not</i> invocation order).
 * </p>
 * <p>
 * Given two pieces of advice, <code>a</code> and <code>b</code>:
 * </p>
 * <ul>
 *   <li>if <code>a</code> and <code>b</code> are defined in different
 *   aspects, then the advice in the aspect with the lowest order
 *   value has the highest precedence</li>
 *   <li>if <code>a</code> and <code>b</code> are defined in the same
 *   aspect, then if one of <code>a</code> or <code>b</code> is a form of
 *   after advice, then the advice declared last in the aspect has the
 *   highest precedence. If neither <code>a</code> nor <code>b</code> is a
 *   form of after advice, then the advice declared first in the aspect has
 *   the highest precedence.</li>
 * </ul>
 * 
 * 
 * <p>Important :- note that unlike a normal comparator a return of 0 means
 * we don't care about the ordering, not that the two elements must be sorted
 * identically. Used with AspectJ PartialOrder class.
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
class AspectJPrecedenceAwareOrderComparator extends OrderComparator {
	
	private static final int HIGHER_PRECEDENCE = -1;
	private static final int SAME_PRECEDENCE = 0;
	private static final int LOWER_PRECEDENCE = 1;
	private static final int NOT_COMPARABLE = 0;

	public int compare(Object o1, Object o2) {
		if (! (isAdvisor(o1) && isAdvisor(o2))) {
			throw new IllegalArgumentException(
					"AspectJPrecedenceAwareOrderComparator can only compare the order of Advisors," +
					"but was passed a " + ((o1 != null) ? o1.getClass().getName() : "null object") +
					"and a " + ((o1 != null) ? o1.getClass().getName() : "null object") + ".");
		}
		
		int ret = NOT_COMPARABLE;
		
		Advisor advisor1 = (Advisor) o1;
		Advisor advisor2 = (Advisor) o2;

		boolean oneOrOtherIsAfterAdvice = (AspectJAopUtils.isAfterAdvice(advisor1) || AspectJAopUtils.isAfterAdvice(advisor2));
		boolean oneOrOtherIsBeforeAdvice = (AspectJAopUtils.isBeforeAdvice(advisor1) || AspectJAopUtils.isBeforeAdvice(advisor2));
		if (oneOrOtherIsAfterAdvice && oneOrOtherIsBeforeAdvice) {
			ret = NOT_COMPARABLE;
		}
		else {
			ret = getAdvisorPrecedence(advisor1, advisor2);
		}

		return ret;
	}

	/**
	 * @param advisor1
	 * @return
	 */
	private String getName(Advisor advisor) {
		StringBuffer sb = new StringBuffer();
		Advice advice = advisor.getAdvice();
		sb.append(advice.getClass().getSimpleName());
		sb.append(":");
		if (advisor instanceof Ordered) {
			sb.append("order " + ((Ordered)advisor).getOrder() + ", ");
		}
		if (advice instanceof AbstractAspectJAdvice) {
			AbstractAspectJAdvice ajAdvice = (AbstractAspectJAdvice) advice;
			sb.append(ajAdvice.getAspectName());
			sb.append(", declaration order= ");
			sb.append(ajAdvice.getOrder());
		}
		return sb.toString();
	}

	private boolean isAdvisor(Object obj) {
		return ((obj != null) && (obj instanceof Advisor));
	}
	
	private int getAdvisorPrecedence(Advisor advisor1, Advisor advisor2) {
		int advisorPrecedence = super.compare(advisor1,advisor2);
		
		if (haveSamePrecedence(advisorPrecedence) && declaredInSameAspect(advisor1,advisor2)) {
			advisorPrecedence = comparePrecedenceWithinAspect((AspectJPointcutAdvisor)advisor1,(AspectJPointcutAdvisor)advisor2);
		}
		
		return advisorPrecedence;
	}
	
	
	private int comparePrecedenceWithinAspect(AspectJPointcutAdvisor advisor1, AspectJPointcutAdvisor advisor2) {
		AbstractAspectJAdvice advice1 = (AbstractAspectJAdvice) advisor1.getAdvice();
		AbstractAspectJAdvice advice2 = (AbstractAspectJAdvice) advisor2.getAdvice();

		boolean oneOrOtherIsAfterAdvice = (AspectJAopUtils.isAfterAdvice(advisor1) || AspectJAopUtils.isAfterAdvice(advisor2));
		int adviceDeclarationOrderDelta = advice1.getOrder() - advice2.getOrder();
		
		if (oneOrOtherIsAfterAdvice) {
			// the advice declared last has higher precedence
			if (adviceDeclarationOrderDelta < 0) {
				// advice1 was declared before advice2
				// so advice1 has lower precedence
				return LOWER_PRECEDENCE;
			} 
			else if (adviceDeclarationOrderDelta == 0) {
				return SAME_PRECEDENCE;
			}
			else {
				return HIGHER_PRECEDENCE;
			}
		}
		else {
			// the advice declared first has higher precedence
			if (adviceDeclarationOrderDelta < 0) {
				// advice1 was declared before advice2
				// so advice1 has higher precedence
				return HIGHER_PRECEDENCE;
			}
			else if (adviceDeclarationOrderDelta == 0) {
				return SAME_PRECEDENCE;
			}
			else {
				return LOWER_PRECEDENCE;
			}
		}
	}

	private boolean declaredInSameAspect(Advisor advisor1, Advisor advisor2) {
		if (! ((advisor1.getAdvice() instanceof AbstractAspectJAdvice) &&
			   (advisor2.getAdvice() instanceof AbstractAspectJAdvice))) {
			return false;
		}
		else {
			AbstractAspectJAdvice advice1 = (AbstractAspectJAdvice) advisor1.getAdvice();
			AbstractAspectJAdvice advice2 = (AbstractAspectJAdvice) advisor2.getAdvice();
			
			return advice1.getAspectName().equals(advice2.getAspectName());
		}
	}

	private boolean haveSamePrecedence(int advisorPrecedence) {
		return advisorPrecedence == SAME_PRECEDENCE;
	}
	
}