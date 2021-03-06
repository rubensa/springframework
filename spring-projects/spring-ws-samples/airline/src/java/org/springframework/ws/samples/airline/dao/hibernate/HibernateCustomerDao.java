/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.ws.samples.airline.dao.hibernate;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.ws.samples.airline.dao.CustomerDao;
import org.springframework.ws.samples.airline.domain.Customer;


public class HibernateCustomerDao extends HibernateDaoSupport implements CustomerDao {

    public Customer getCustomer(long customerId) throws DataAccessException {
        return (Customer) getHibernateTemplate().get(Customer.class, new Long(customerId));
    }

    public void insertCustomer(Customer customer) throws DataAccessException {
        getHibernateTemplate().save(customer);
    }
    
    public List getCustomers(String name) throws DataAccessException {
        return getHibernateTemplate().findByNamedParam("from Customer c where c.name = :name", "name", name);
    }

}
