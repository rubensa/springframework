/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules.reporting;

import java.util.Iterator;

import org.springframework.rules.Constraint;
import org.springframework.rules.constraint.And;
import org.springframework.rules.constraint.CompoundBeanPropertyExpression;
import org.springframework.rules.constraint.Or;
import org.springframework.rules.constraint.bean.BeanPropertiesConstraint;
import org.springframework.rules.constraint.bean.ParameterizedBeanPropertyConstraint;
import org.springframework.util.Assert;
import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;

/**
 * @author Keith Donald
 */
public class SummingVisitor implements Visitor {
    private ReflectiveVisitorSupport visitorSupport = new ReflectiveVisitorSupport();
    private int sum;
    private Constraint constraint;

    public SummingVisitor(Constraint constraint) {
        Assert.notNull(constraint);
        this.constraint = constraint;
    }

    public int sum() {
        visitorSupport.invokeVisit(this, constraint);
        return sum;
    }

    void visit(CompoundBeanPropertyExpression rule) {
        visitorSupport.invokeVisit(this, rule.getPredicate());
    }

    void visit(BeanPropertiesConstraint e) {
        sum++;
    }

    void visit(ParameterizedBeanPropertyConstraint e) {
        sum++;
    }

    void visit(And and) {
        Iterator it = and.iterator();
        while (it.hasNext()) {
            Constraint p = (Constraint)it.next();
            visitorSupport.invokeVisit(this, p);
        }
    }

    void visit(Or or) {
        Iterator it = or.iterator();
        while (it.hasNext()) {
            Constraint p = (Constraint)it.next();
            visitorSupport.invokeVisit(this, p);
        }
    }

    void visit(Constraint constraint) {
        sum++;
    }

}