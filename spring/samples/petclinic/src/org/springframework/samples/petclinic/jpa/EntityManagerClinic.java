
package org.springframework.samples.petclinic.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of the Clinic interface using EntityManager.
 * <p>
 * The mappings are defined in "orm.xml" located in the META-INF directory.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 22.4.2006
 */
@Transactional
@Repository
public class EntityManagerClinic implements Clinic {

	@PersistenceContext
	private EntityManager em;


	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<Vet> getVets() throws DataAccessException {
		return this.em.createQuery("SELECT vet FROM Vet vet ORDER BY vet.lastName, vet.firstName").getResultList();
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<PetType> getPetTypes() throws DataAccessException {
		return this.em.createQuery("SELECT ptype FROM PetType ptype ORDER BY ptype.name").getResultList();
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<Owner> findOwners(String lastName) throws DataAccessException {
		Query query = this.em.createQuery("SELECT owner FROM Owner owner WHERE owner.lastName LIKE :lastName");
		query.setParameter("lastName", lastName + "%");
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public Owner loadOwner(int id) throws DataAccessException {
		return this.em.find(Owner.class, id);
	}

	@Transactional(readOnly = true)
	public Pet loadPet(int id) throws DataAccessException {
		return this.em.find(Pet.class, id);
	}

	public void storeOwner(Owner owner) throws DataAccessException {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		this.em.merge(owner);
	}

	public void storePet(Pet pet) throws DataAccessException {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		this.em.merge(pet);
	}

	public void storeVisit(Visit visit) throws DataAccessException {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		this.em.merge(visit);
	}

}
