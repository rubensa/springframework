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
 */

package org.springframework.orm.jpa.spi;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;

import org.springframework.aop.support.AopUtils;
import org.springframework.orm.jpa.domain.Person;
import org.springframework.test.ExpectedException;
import org.springframework.test.NotTransactional;

/**
 * @author Rod Johnson
 *
 */
public class ContainerManagedExtendedEntityManagerFactoryBeanIntegrationTests extends AbstractContainerEntityManagerFactoryBeanIntegrationTests {
	
	public void testEntityManagerProxyIsProxy() {
		EntityManager em = createContainerManagedEntityManager();
		assertTrue(AopUtils.isAopProxy(em));
		Query q = em.createQuery("select p from Person as p");
		List<Person> people = q.getResultList();
		assertTrue(people.isEmpty());
		
		assertTrue("Should be open to start with", em.isOpen());
		try {
			em.close();
			fail("Close should not work on container managed EM");
		}
		catch (IllegalStateException ex) {
			// Ok
		}
		assertTrue(em.isOpen());
	}
	
	@NotTransactional
	public void testContainerManagedEntityManagerProxyImplementsSpringInterface() {
		EntityManager applicationManagedEm = createContainerManagedEntityManager();
		ContainerEntityManagerFactoryBeanIntegrationTests.verifyImplementsPortableEntityManagerPlus(applicationManagedEm);
	}
	
	// This would be legal, at least if not actually _starting_ a tx
	@ExpectedException(IllegalStateException.class)
	public void testEntityManagerProxyRejectsProgrammaticTxManagement() {
		createContainerManagedEntityManager().getTransaction();
	}
	
	
	/*
	 * See comments in spec on EntityManager.joinTransaction().
	 * We take the view that this is a valid no op.
	 */
	public void testContainerEntityManagerProxyAllowsJoinTransactionInTransaction() {
		createContainerManagedEntityManager().joinTransaction();
	}
	
	@NotTransactional
	@ExpectedException(TransactionRequiredException.class)
	public void testContainerEntityManagerProxyRejectsJoinTransactionWithoutTransaction() {
		createContainerManagedEntityManager().joinTransaction();
	}
	
	public void testInstantiateAndSave() {
		EntityManager em = createContainerManagedEntityManager();
		doInstantiateAndSave(em);
	}
	
	public void doInstantiateAndSave(EntityManager em) {
		assertEquals("Should be no people from previous transactions",
				0, countRowsInTable("person"));
		Person p = new Person();
		
		p.setFirstName("Tony");
		p.setLastName("Blair");
		em.persist(p);
		
		em.flush();
		assertEquals("1 row must have been inserted",
				1, countRowsInTable("person"));
	}
	
	public void testReuseInNewTransaction() {
		EntityManager em = createContainerManagedEntityManager();
		doInstantiateAndSave(em);
		endTransaction();
		
		//assertFalse(em.getTransaction().isActive());
		
		startNewTransaction();
		// Call any method: should cause automatic tx invocation
		assertFalse(em.contains(new Person()));
		//assertTrue(em.getTransaction().isActive());
		
		doInstantiateAndSave(em);
		setComplete();
		endTransaction();	// Should rollback
		assertEquals("Tx must have committed back",
				1, countRowsInTable("person"));
		
		// Now clean up the database
		deleteFromTables(new String[] { "person" });
	}
	
	public void testRollbackOccurs() {
		EntityManager em = createContainerManagedEntityManager();
		doInstantiateAndSave(em);
		endTransaction();	// Should rollback
		assertEquals("Tx must have been rolled back",
				0, countRowsInTable("person"));
	}
	
	public void testCommitOccurs() {
		EntityManager em = createContainerManagedEntityManager();
		doInstantiateAndSave(em);
		setComplete();
		endTransaction();	// Should rollback
		assertEquals("Tx must have committed back",
				1, countRowsInTable("person"));
		
		// Now clean up the database
		deleteFromTables(new String[] { "person" });
	}
	
	
	// This displays incorrect behaviour in TopLink,
	// which throws some exceptions that are not subclasses of PersistenceException
//	public void testEntityManagerProxyException() {
//		EntityManager em = entityManagerFactory.createEntityManager();
//		try {
//			em.createQuery("select p from Person p where p.o=0").getResultList();
//			fail("Semantic nonsense should be rejected");
//		}
//		catch (PersistenceException ex) {
//			
//		}
//	}

}
