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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Mock object based tests for transaction aspects.
 * True unit test in that it tests how the transaction aspect uses
 * the PlatformTransactionManager helper, rather than indirectly
 * testing the helper implementation.
 *
 * <�>This is a superclass to allow testing both the AOP Alliance MethodInterceptor
 * and the AspectJ aspect.
 *
 * @author Rod Johnson
 * @since 16.03.2003
 */
public abstract class AbstractTransactionAspectTests extends TestCase {
	
	protected Method exceptionalMethod;
	
	protected Method getNameMethod;
	
	protected Method setNameMethod;
	
	public AbstractTransactionAspectTests() {
		try {
			// Cache the methods we'll be testing
			exceptionalMethod = ITestBean.class.getMethod("exceptional", new Class[] { Throwable.class });
			getNameMethod = ITestBean.class.getMethod("getName", (Class[]) null);
			setNameMethod = ITestBean.class.getMethod("setName", new Class[] { String.class} );
		}
		catch (NoSuchMethodException ex) {
			throw new RuntimeException("Shouldn't happen", ex);
		}
	}
	
	/**
	 * Subclasses must implement this to create an advised object based on the
	 * given target. In the case of AspectJ, the  advised object will already
	 * have been created, as there's no distinction between target and proxy.
	 * In the case of Spring's own AOP framework, a proxy must be created
	 * using a suitably configured transaction interceptor
	 * @param target target if there's a distinct target. If not (AspectJ),
	 * return target.
	 * @return transactional advised object
	 */
	protected abstract Object advised(
			Object target, PlatformTransactionManager ptm, TransactionAttributeSource tas) throws Exception;

	public void testNoTransaction() throws Exception {
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();

		// expect no calls
		ptxControl.replay();

		TestBean tb = new TestBean();
		TransactionAttributeSource tas = new MapTransactionAttributeSource();

		// All the methods in this class use the advised() template method
		// to obtain a transaction object, configured with the given PlatformTransactionManager
		// and transaction attribute source
		ITestBean itb = (ITestBean) advised(tb, ptm, tas);

		checkTransactionStatus(false);
		itb.getName();
		checkTransactionStatus(false);

		ptxControl.verify();
	}

	/**
	 * Check that a transaction is created and committed.
	 */
	public void testTransactionShouldSucceed() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = getNameMethod;
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		TransactionStatus status = transactionStatusForNewTransaction();
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// expect a transaction
		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status, 1);
		ptm.commit(status);
		ptxControl.setVoidCallable(1);
		ptxControl.replay();

		TestBean tb = new TestBean();
		
		ITestBean itb = (ITestBean) advised(tb, ptm, tas);

		checkTransactionStatus(false);
		// verification!?
		itb.getName();
		checkTransactionStatus(false);

		ptxControl.verify();
	}
	
	/**
	 * Check that a transaction is created and committed.
	 */
	public void testTransactionShouldSucceedWithNotNew() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = getNameMethod;
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		TransactionStatus status = new DefaultTransactionStatus(new Object(), false, false, false, false, null);
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// expect a transaction
		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status, 1);
		ptm.commit(status);
		ptxControl.setVoidCallable(1);
		ptxControl.replay();

		TestBean tb = new TestBean();

		ITestBean itb = (ITestBean) advised(tb, ptm, tas);

		checkTransactionStatus(false);
		// verification!?
		itb.getName();
		checkTransactionStatus(false);

		ptxControl.verify();
	}

	public void testEnclosingTransactionWithNonTransactionMethodOnAdvisedInside() throws Throwable {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = exceptionalMethod;
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		TransactionStatus status = transactionStatusForNewTransaction();
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// Expect a transaction
		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status, 1);
		ptm.commit(status);
		ptxControl.setVoidCallable(1);
		ptxControl.replay();
		
		final String spouseName = "innerName";

		TestBean outer = new TestBean() {
			public void exceptional(Throwable t) throws Throwable {
				TransactionInfo ti = TransactionAspectSupport.currentTransactionInfo();
				assertTrue(ti.hasTransaction());
				assertEquals(spouseName, getSpouse().getName());
			}
		};
		TestBean inner = new TestBean() {
			public String getName() {
				// Assert that we're in the inner proxy
				TransactionInfo ti = TransactionAspectSupport.currentTransactionInfo();
				assertFalse(ti.hasTransaction());
				return spouseName;
			}
		};
		
		ITestBean outerProxy = (ITestBean) advised(outer, ptm, tas);
		ITestBean innerProxy = (ITestBean) advised(inner, ptm, tas);
		outer.setSpouse(innerProxy);

		checkTransactionStatus(false);

		// Will invoke inner.getName, which is non-transactional
		outerProxy.exceptional(null);		
		
		checkTransactionStatus(false);

		ptxControl.verify();
	}
	
	public void testEnclosingTransactionWithNestedTransactionOnAdvisedInside() throws Throwable {
		final TransactionAttribute outerTxatt = new DefaultTransactionAttribute();
		final TransactionAttribute innerTxatt = new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_NESTED);

		Method outerMethod = exceptionalMethod;
		Method innerMethod = getNameMethod;
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(outerMethod, outerTxatt);
		tas.register(innerMethod, innerTxatt);

		TransactionStatus outerStatus = transactionStatusForNewTransaction();
		TransactionStatus innerStatus = transactionStatusForNewTransaction();
		
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// Expect a transaction
		ptm.getTransaction(outerTxatt);
		ptxControl.setReturnValue(outerStatus, 1);
		
		ptm.getTransaction(innerTxatt);
		ptxControl.setReturnValue(innerStatus, 1);
		
		ptm.commit(innerStatus);
		ptxControl.setVoidCallable(1);
		
		ptm.commit(outerStatus);
		ptxControl.setVoidCallable(1);
		ptxControl.replay();
		
		final String spouseName = "innerName";

		TestBean outer = new TestBean() {
			public void exceptional(Throwable t) throws Throwable {
				TransactionInfo ti = TransactionAspectSupport.currentTransactionInfo();
				assertTrue(ti.hasTransaction());
				assertEquals(outerTxatt, ti.getTransactionAttribute());
				assertEquals(spouseName, getSpouse().getName());
			}
		};
		TestBean inner = new TestBean() {
			public String getName() {
				// Assert that we're in the inner proxy
				TransactionInfo ti = TransactionAspectSupport.currentTransactionInfo();
				// Has nested transaction
				assertTrue(ti.hasTransaction());
				assertEquals(innerTxatt, ti.getTransactionAttribute());
				return spouseName;
			}
		};
		
		ITestBean outerProxy = (ITestBean) advised(outer, ptm, tas);
		ITestBean innerProxy = (ITestBean) advised(inner, ptm, tas);
		outer.setSpouse(innerProxy);

		checkTransactionStatus(false);

		// Will invoke inner.getName, which is non-transactional
		outerProxy.exceptional(null);		
		
		checkTransactionStatus(false);

		ptxControl.verify();
	}
	
	public void testRollbackOnCheckedException() throws Throwable {
		doTestRollbackOnException(new Exception(), true, false);
	}

	public void testNoRollbackOnCheckedException() throws Throwable {
		doTestRollbackOnException(new Exception(), false, false);
	}

	public void testRollbackOnUncheckedException() throws Throwable {
		doTestRollbackOnException(new RuntimeException(), true, false);
	}

	public void testNoRollbackOnUncheckedException() throws Throwable {
		doTestRollbackOnException(new RuntimeException(), false, false);
	}

	public void testRollbackOnCheckedExceptionWithRollbackException() throws Throwable {
		doTestRollbackOnException(new Exception(), true, true);
	}

	public void testNoRollbackOnCheckedExceptionWithRollbackException() throws Throwable {
		doTestRollbackOnException(new Exception(), false, true);
	}

	public void testRollbackOnUncheckedExceptionWithRollbackException() throws Throwable {
		doTestRollbackOnException(new RuntimeException(), true, true);
	}

	public void testNoRollbackOnUncheckedExceptionWithRollbackException() throws Throwable {
		doTestRollbackOnException(new RuntimeException(), false, true);
	}

	/**
	 * Check that the given exception thrown by the target can produce the
	 * desired behavior with the appropriate transaction attribute.
	 * @param ex exception to be thrown by the target
	 * @param shouldRollback whether this should cause a transaction rollback
	 */
	protected void doTestRollbackOnException(
			final Exception ex, final boolean shouldRollback, boolean rollbackException) throws Exception {

		TransactionAttribute txatt = new DefaultTransactionAttribute() {
			public boolean rollbackOn(Throwable t) {
				assertTrue(t == ex);
				return shouldRollback;
			}
		};

		Method m = exceptionalMethod;
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		TransactionStatus status = new DefaultTransactionStatus(null, false, false, false, false, null);
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// Gets additional call(s) from TransactionControl

		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status, 1);

		if (shouldRollback) {
			ptm.rollback(status);
		}
		else {
			ptm.commit(status);
		}
		TransactionSystemException tex = new TransactionSystemException("system exception");
		if (rollbackException) {
			ptxControl.setThrowable(tex, 1);
		}
		else {
			ptxControl.setVoidCallable(1);
		}
		ptxControl.replay();

		TestBean tb = new TestBean();		
		ITestBean itb = (ITestBean) advised(tb, ptm, tas);

		try {
			itb.exceptional(ex);
			fail("Should have thrown exception");
		}
		catch (Throwable t) {
			if (rollbackException) {
				assertEquals("Caught wrong exception", tex, t );
			}
			else {
				assertEquals("Caught wrong exception", ex, t);
			}
		}

		ptxControl.verify();
	}
	
	/**
	 * Test that TransactionControl.setRollbackOnly works
	 * @throws java.lang.Exception
	 */
	public void testProgrammaticRollback() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = getNameMethod;
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		TransactionStatus status = transactionStatusForNewTransaction();
		assertTrue(status.isNewTransaction());
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();

		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status, 1);
		ptm.commit(status);
		ptxControl.setVoidCallable(1);
		ptxControl.replay();

		final String name = "jenny";
		TestBean tb = new TestBean() {
			public String getName() {
				TransactionStatus txStatus = TransactionInterceptor.currentTransactionStatus();
				txStatus.setRollbackOnly();
				return name;
			}
		};
		
		ITestBean itb = (ITestBean) advised(tb, ptm, tas);

		// verification!?
		assertTrue(name.equals(itb.getName()));

		ptxControl.verify();
	}
	
	/**
	 * @return a TransactionStatus object configured for a new transaction
	 */
	private TransactionStatus transactionStatusForNewTransaction() {
		return new DefaultTransactionStatus(new Object(), true, false, false, false, null);
	}

	/**
	 * Simulate a transaction infrastructure failure.
	 * Shouldn't invoke target method.
	 */
	public void testCannotCreateTransaction() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = getNameMethod;
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// Expect a transaction
		ptm.getTransaction(txatt);
		CannotCreateTransactionException ex = new CannotCreateTransactionException("foobar", null);
		ptxControl.setThrowable(ex);
		ptxControl.replay();

		TestBean tb = new TestBean() {
			public String getName() {
				throw new UnsupportedOperationException(
						"Shouldn't have invoked target method when couldn't create transaction for transactional method");
			}
		};		
		ITestBean itb = (ITestBean) advised(tb, ptm, tas);

		try {
			itb.getName();
			fail("Shouldn't have invoked method");
		}
		catch (CannotCreateTransactionException thrown) {
			assertTrue(thrown == ex);
		}
		ptxControl.verify();
	}

	/**
	 * Simulate failure of the underlying transaction infrastructure to commit.
	 * Check that the target method was invoked, but that the transaction
	 * infrastructure exception was thrown to the client
	 */
	public void testCannotCommitTransaction() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = setNameMethod;
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);
		Method m2 = getNameMethod;
		// No attributes for m2

		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();

		TransactionStatus status = transactionStatusForNewTransaction();
		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status);
		UnexpectedRollbackException ex = new UnexpectedRollbackException("foobar", null);
		ptm.commit(status);
		ptxControl.setThrowable(ex);
		ptxControl.replay();

		TestBean tb = new TestBean();		
		ITestBean itb = (ITestBean) advised(tb, ptm, tas);

		String name = "new name";
		try {
			itb.setName(name);
			fail("Shouldn't have succeeded");
		}
		catch (UnexpectedRollbackException thrown) {
			assertTrue(thrown == ex);
		}

		// Should have invoked target and changed name
		assertTrue(itb.getName() == name);
		ptxControl.verify();
	}

	protected void checkTransactionStatus(boolean expected) {
		try {
			TransactionInterceptor.currentTransactionStatus();
			if (!expected) {
				fail("Should have thrown NoTransactionException");
			}
		}
		catch (NoTransactionException ex) {
			if (expected) {
				fail("Should have current TransactionStatus");
			}
		}
	}

}
