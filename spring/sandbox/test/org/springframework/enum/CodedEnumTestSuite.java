/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.enum;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.DefaultObjectStyler;

/**
 * %single sentence summary caption%.
 * 
 * %long description%.
 *
 * @author  keith
 */
public class CodedEnumTestSuite extends TestCase {
    static ClassPathXmlApplicationContext ac;

    static {
        ac =
            new ClassPathXmlApplicationContext("org/springframework/enum/enum-context.xml");
    }
    
    public void testEnumPropertyEditor() {
        CodedEnumEditor e = new CodedEnumEditor();
        e.setEnumResolver((CodedEnumResolver)ac.getBean("enumResolver"));
        e.setAsText("gender.m");
    }
    
    public void testEnumRetrieval() {
        CodedEnumResolver resolver = (CodedEnumResolver)ac.getBean("enumResolver");
        Map map = resolver.getEnumsAsMap("gender");
        System.out.println(DefaultObjectStyler.evaluate(map));
        map = resolver.getEnumsAsMap("gender");
        System.out.println(DefaultObjectStyler.evaluate(map));
        List list = resolver.getEnumsAsList("gender");
        System.out.println(DefaultObjectStyler.evaluate(list));
    }

}
