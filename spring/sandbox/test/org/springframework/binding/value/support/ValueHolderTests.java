/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.binding.value.support;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.binding.value.ValueModel;
import org.springframework.core.closure.Closure;

/**
 * 
 * @author HP
 */
public class ValueHolderTests extends TestCase {

	public void testRefreshableValueHolder() {
		final List list = new ArrayList();
		list.add("1");
		list.add("2");
		list.add("3");
		Closure schemaAccessor = new Closure() {
			public Object call(Object o) {
				System.out.println("Adding");
				list.add(new Integer(list.size() + 1));
				return list;
			}
		};
		ValueModel itemsValueModel = new RefreshableValueHolder(schemaAccessor, true);
		assertEquals(new Integer(4), ((List)itemsValueModel.getValue()).get(3));
	}
}