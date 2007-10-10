
package org.springframework.samples.petclinic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for Clinic tests. Allows subclasses to specify context locations.
 * <p>
 * This class extends {@link AbstractTransactionalDataSourceSpringContextTests},
 * one of the valuable test superclasses provided in the
 * org.springframework.test package. This represents best practice for
 * integration tests with Spring.
 * <p>
 * The AbstractTransactionalDataSourceSpringContextTests superclass provides the
 * following services:
 * <li>Injects test dependencies, meaning that we don't need to perform
 * application context lookups. See the setClinic() method. Injection uses
 * autowiring by type.
 * <li>Executes each test method in its own transaction, which is automatically
 * rolled back by default. This means that even if tests insert or otherwise
 * change database state, there is no need for a teardown or cleanup script.
 * <li>Provides useful inherited protected fields, such as a JdbcTemplate that
 * can be used to verify database state after test operations, or verify the
 * results of queries performed by application code. An ApplicationContext is
 * also inherited, and can be used for explicit lookup if necessary.
 * <p>
 * {@link AbstractTransactionalDataSourceSpringContextTests} and related classes
 * are shipped in <code>spring-mock.jar</code>.
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(locations = { "applicationContext-dataSourceCommon.xml" })
public abstract class AbstractClinicTests {

	@Autowired
	protected Clinic clinic;

	/**
	 * The SimpleJdbcTemplate that this base class manages, available to
	 * subclasses.
	 */
	protected SimpleJdbcTemplate simpleJdbcTemplate;


	/**
	 * Set the DataSource, typically provided via Dependency Injection.
	 *
	 * @param dataSource The DataSource to inject.
	 */
	@Autowired
	public void setDataSource(final DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Test
	public void testGetVets() {
		Collection<Vet> vets = this.clinic.getVets();

		// Use the inherited JdbcTemplate (from
		// AbstractTransactionalDataSourceSpringContextTests)
		// to verify the results of the query
		assertEquals("JDBC query must show the same number of vets",
				this.simpleJdbcTemplate.queryForInt("SELECT COUNT(0) FROM VETS"), vets.size());
		Vet v1 = EntityUtils.getById(vets, Vet.class, 2);
		assertEquals("Leary", v1.getLastName());
		assertEquals(1, v1.getNrOfSpecialties());
		assertEquals("radiology", (v1.getSpecialties().get(0)).getName());
		Vet v2 = EntityUtils.getById(vets, Vet.class, 3);
		assertEquals("Douglas", v2.getLastName());
		assertEquals(2, v2.getNrOfSpecialties());
		assertEquals("dentistry", (v2.getSpecialties().get(0)).getName());
		assertEquals("surgery", (v2.getSpecialties().get(1)).getName());
	}

	@Test
	public void testGetPetTypes() {
		Collection<PetType> petTypes = this.clinic.getPetTypes();
		assertEquals("JDBC query must show the same number of pet typess",
				this.simpleJdbcTemplate.queryForInt("SELECT COUNT(0) FROM TYPES"), petTypes.size());
		PetType t1 = EntityUtils.getById(petTypes, PetType.class, 1);
		assertEquals("cat", t1.getName());
		PetType t4 = EntityUtils.getById(petTypes, PetType.class, 4);
		assertEquals("snake", t4.getName());
	}

	@Test
	public void testFindOwners() {
		Collection<Owner> owners = this.clinic.findOwners("Davis");
		assertEquals(2, owners.size());
		owners = this.clinic.findOwners("Daviss");
		assertEquals(0, owners.size());
	}

	@Test
	public void testLoadOwner() {
		Owner o1 = this.clinic.loadOwner(1);
		assertTrue(o1.getLastName().startsWith("Franklin"));
		Owner o10 = this.clinic.loadOwner(10);
		assertEquals("Carlos", o10.getFirstName());

		// XXX: Add programmatic support for ending transactions with the
		// TestContext Framework.

		// Check lazy loading, by ending the transaction
		// endTransaction();

		// Now Owners are "disconnected" from the data store.
		// We might need to touch this collection if we switched to lazy loading
		// in mapping files, but this test would pick this up.
		o1.getPets();
	}

	@Test
	public void testInsertOwner() {
		Collection<Owner> owners = this.clinic.findOwners("Schultz");
		int found = owners.size();
		Owner owner = new Owner();
		owner.setLastName("Schultz");
		this.clinic.storeOwner(owner);
		// assertTrue(!owner.isNew()); -- NOT TRUE FOR TOPLINK (before commit)
		owners = this.clinic.findOwners("Schultz");
		assertEquals("Verifying number of owners after inserting a new one.", found + 1, owners.size());
	}

	@Test
	public void testUpdateOwner() throws Exception {
		Owner o1 = this.clinic.loadOwner(1);
		String old = o1.getLastName();
		o1.setLastName(old + "X");
		this.clinic.storeOwner(o1);
		o1 = this.clinic.loadOwner(1);
		assertEquals(old + "X", o1.getLastName());
	}

	@Test
	public void testLoadPet() {
		Collection<PetType> types = this.clinic.getPetTypes();
		Pet p7 = this.clinic.loadPet(7);
		assertTrue(p7.getName().startsWith("Samantha"));
		assertEquals(EntityUtils.getById(types, PetType.class, 1).getId(), p7.getType().getId());
		assertEquals("Jean", p7.getOwner().getFirstName());
		Pet p6 = this.clinic.loadPet(6);
		assertEquals("George", p6.getName());
		assertEquals(EntityUtils.getById(types, PetType.class, 4).getId(), p6.getType().getId());
		assertEquals("Peter", p6.getOwner().getFirstName());
	}

	@Test
	public void testInsertPet() {
		Owner o6 = this.clinic.loadOwner(6);
		int found = o6.getPets().size();
		Pet pet = new Pet();
		pet.setName("bowser");
		Collection<PetType> types = this.clinic.getPetTypes();
		pet.setType(EntityUtils.getById(types, PetType.class, 2));
		pet.setBirthDate(new Date());
		o6.addPet(pet);
		assertEquals(found + 1, o6.getPets().size());
		// both storePet and storeOwner are necessary to cover all ORM tools
		this.clinic.storePet(pet);
		this.clinic.storeOwner(o6);
		// assertTrue(!pet.isNew()); -- NOT TRUE FOR TOPLINK (before commit)
		o6 = this.clinic.loadOwner(6);
		assertEquals(found + 1, o6.getPets().size());
	}

	@Test
	public void testUpdatePet() throws Exception {
		Pet p7 = this.clinic.loadPet(7);
		String old = p7.getName();
		p7.setName(old + "X");
		this.clinic.storePet(p7);
		p7 = this.clinic.loadPet(7);
		assertEquals(old + "X", p7.getName());
	}

	@Test
	public void testInsertVisit() {
		Pet p7 = this.clinic.loadPet(7);
		int found = p7.getVisits().size();
		Visit visit = new Visit();
		p7.addVisit(visit);
		visit.setDescription("test");
		// both storeVisit and storePet are necessary to cover all ORM tools
		this.clinic.storeVisit(visit);
		this.clinic.storePet(p7);
		// assertTrue(!visit.isNew()); -- NOT TRUE FOR TOPLINK (before commit)
		p7 = this.clinic.loadPet(7);
		assertEquals(found + 1, p7.getVisits().size());
	}

}
