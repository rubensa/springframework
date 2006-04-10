/**
 * 
 */
package org.springframework.webflow;

import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.support.ApplicationViewSelector;
import org.springframework.webflow.support.ExternalRedirectSelector;
import org.springframework.webflow.support.DefaultTargetStateResolver;

public class SimpleFlow extends Flow {
	public SimpleFlow() {
		super("simpleFlow");

		ViewState state1 = new ViewState(this, "view");
		state1.setViewSelector(new ApplicationViewSelector(new StaticExpression("view")));
		state1.addTransition(new Transition(new DefaultTargetStateResolver("end")));

		EndState state2 = new EndState(this, "end");
		state2.setViewSelector(new ExternalRedirectSelector(new StaticExpression("confirm"), true));
	}
}