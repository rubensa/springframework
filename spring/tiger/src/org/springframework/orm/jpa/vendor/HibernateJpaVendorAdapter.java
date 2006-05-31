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

package org.springframework.orm.jpa.vendor;

import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Environment;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.ejb.HibernatePersistence;

import org.springframework.orm.jpa.JpaDialect;

/**
 * Hibernate-specific JpaVendorAdapter implementation.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 */
public class HibernateJpaVendorAdapter extends AbstractJpaVendorAdapter {

	private final JpaDialect jpaDialect = new HibernateJpaDialect();


	public Class getPersistenceProviderClass() {
		return HibernatePersistence.class;
	}

	public Map getJpaPropertyMap() {
		Properties jpaProperties = new Properties();

		if (getDatabasePlatform() != null) {
			jpaProperties.setProperty(Environment.DIALECT, getDatabasePlatform());
		}
		else if (getDatabase() != null) {
			Class databaseDialectClass = determineDatabaseDialectClass(getDatabase());
			if (databaseDialectClass != null) {
				jpaProperties.setProperty(Environment.DIALECT, databaseDialectClass.getName());
			}
		}

		if (isGenerateDdl()) {
			jpaProperties.setProperty(Environment.HBM2DDL_AUTO, "create");
		}
		if (isShowSql()) {
			jpaProperties.setProperty(Environment.SHOW_SQL, "true");
		}

		return jpaProperties;
	}

	protected Class determineDatabaseDialectClass(Database database) {
		switch (database) {
			case DB2: return DB2Dialect.class;
			case HSQL: return HSQLDialect.class;
			case INFORMIX: return InformixDialect.class;
			case MYSQL: return MySQLDialect.class;
			case ORACLE: return Oracle9Dialect.class;
			case POSTGRESQL: return PostgreSQLDialect.class;
			case SQL_SERVER: return SQLServerDialect.class;
			case SYBASE: return SybaseDialect.class;
			default: return null;
		}
	}

	public Class getEntityManagerInterface() {
		return HibernateEntityManager.class;
	}

	public JpaDialect getJpaDialect() {
		return jpaDialect;
	}

}
