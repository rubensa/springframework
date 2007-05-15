/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core.simple;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.AbstractRowMapperTests;
import org.springframework.jdbc.core.test.Person;

import java.sql.SQLException;
import java.util.List;

/**
 * Mock object based tests for ParameterizedBeanPropertyRowMapper.
 *
 * @author trisberg
 */
public class ParameterizedBeanPropertyRowMapperTests extends AbstractRowMapperTests {

	private SimpleJdbcTemplate simpleJdbcTemplate;

	protected void setUp() throws SQLException {
		super.setUp();
		simpleJdbcTemplate = new SimpleJdbcTemplate(jdbcTemplate);
	}

	public void testOverridingClassDefinedForMapping() {
		ParameterizedBeanPropertyRowMapper<Person> mapper =
				new ParameterizedBeanPropertyRowMapper<Person>(Person.class);
		try {
			((ParameterizedBeanPropertyRowMapper)mapper).setMappedClass(Long.class);
			fail("Setting new class should have thrown InvalidDataAccessApiUsageException");
		}
		catch (InvalidDataAccessApiUsageException ex) {}
		try {
			mapper.setMappedClass(Person.class);
		}
		catch (InvalidDataAccessApiUsageException ex) {
			fail("Setting same class should not have thrown InvalidDataAccessApiUsageException");
		}
	}

	public void testStaticQueryWithRowMapper() throws SQLException {

		List<Person> result = simpleJdbcTemplate.query("select name, age, birth_date, balance from people",
				new ParameterizedBeanPropertyRowMapper<Person>(Person.class));

		assertEquals(1, result.size());

		Person bean = result.get(0);

		verify(bean);

	}

}
