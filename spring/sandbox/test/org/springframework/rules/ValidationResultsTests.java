/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules;

import java.util.Locale;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.rules.factory.Constraints;
import org.springframework.rules.reporting.ValidationResults;
import org.springframework.rules.reporting.ValidationResultsCollector;

/**
 * @author Keith Donald
 */
public class ValidationResultsTests extends TestCase {
    static ClassPathXmlApplicationContext ac;
    static RulesSource rulesSource;
    static Rules rules;
    private static final Constraints constraints = Constraints.instance();

    static {
        ac =
            new ClassPathXmlApplicationContext("org/springframework/rules/rules-context.xml");
        rulesSource = (RulesSource)ac.getBean("rulesSource");
        rules = rulesSource.getRules(Person.class);
    }

    public void testValidationResultsCollector() {
        Person p = new Person();
        ValidationResultsCollector c = new ValidationResultsCollector(p);
        ValidationResults r =
            c.collectResults(rulesSource.getRules(Person.class));
        assertEquals(2, r.getViolatedCount());
    }

    public void testValidationResultsCollectorCollectAllErrors() {
        Person p = new Person();
        ValidationResultsCollector c = new ValidationResultsCollector(p);
        c.setCollectAllErrors(true);
        ValidationResults r =
            c.collectResults(rulesSource.getRules(Person.class));
        assertEquals(2, r.getViolatedCount());
    }

    public void testNestedValidationResultsPropertyConstraint() {
        Person p = new Person();

        Rules rules = Rules.createRules(Person.class);
        UnaryPredicate constraint =
            constraints.or(
                constraints.all(
                    "firstName",
                    new UnaryPredicate[] {
                        constraints.required(),
                        constraints.minLength(2)}),
                constraints.not(
                    constraints.eqProperty("firstName", "lastName")));
        rules.add(constraint);
        ValidationResultsCollector c = new ValidationResultsCollector(p);
        c.setCollectAllErrors(true);
        ValidationResults r = c.collectResults(rules);
        assertEquals(3, r.getViolatedCount());
        String message =
            r.getResults("firstName").buildMessage(ac, Locale.getDefault());
        System.out.println(message);
        assertEquals(
            "First Name must *not* equal Last Name or must have text and must be at least 2 characters",
            message);

    }
}