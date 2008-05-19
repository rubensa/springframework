/*
 * Copyright 2002-2008 the original author or authors.
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
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceProvider;

import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.jpa.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.config.TargetDatabase;

import org.springframework.orm.jpa.JpaDialect;

/**
 * {@link org.springframework.orm.jpa.JpaVendorAdapter} implementation for
 * Eclipse Persistence Services (EclipseLink). Developed and tested against
 * EclipseLink 1.0 M4.
 *
 * <p>Exposes EclipseLink's persistence provider and EntityManager extension interface,
 * and supports {@link AbstractJpaVendorAdapter}'s common configuration settings.
 *
 * <p>This class is very analogous to {@link TopLinkJpaVendorAdapter}, since
 * EclipseLink is effectively the next generation of the TopLink product.
 * Thanks to Mike Keith for the original EclipseLink support prototype!
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see org.eclipse.persistence.jpa.PersistenceProvider
 * @see org.eclipse.persistence.jpa.JpaEntityManager
 */
public class EclipseLinkJpaVendorAdapter extends AbstractJpaVendorAdapter {

	private final PersistenceProvider persistenceProvider = new org.eclipse.persistence.jpa.PersistenceProvider();

	private final JpaDialect jpaDialect = new EclipseLinkJpaDialect();


	public PersistenceProvider getPersistenceProvider() {
		return this.persistenceProvider;
	}

	public String getPersistenceProviderRootPackage() {
		return "org.eclipse.persistence";
	}

	public Map getJpaPropertyMap() {
		Properties jpaProperties = new Properties();

		if (getDatabasePlatform() != null) {
			jpaProperties.setProperty(PersistenceUnitProperties.TARGET_DATABASE, getDatabasePlatform());
		}
		else if (getDatabase() != null) {
			String targetDatabase = determineTargetDatabaseName(getDatabase());
			if (targetDatabase != null) {
				jpaProperties.setProperty(PersistenceUnitProperties.TARGET_DATABASE, targetDatabase);
			}
		}

		if (isGenerateDdl()) {
			jpaProperties.setProperty(PersistenceUnitProperties.DDL_GENERATION,
					PersistenceUnitProperties.CREATE_ONLY);
			jpaProperties.setProperty(PersistenceUnitProperties.DDL_GENERATION_MODE,
					PersistenceUnitProperties.DDL_DATABASE_GENERATION);
		}
		if (isShowSql()) {
			jpaProperties.setProperty(PersistenceUnitProperties.LOGGING_LEVEL, Level.FINE.toString());
		}

		return jpaProperties;
	}

	/**
	 * Determine the EclipseLink target database name for the given database.
	 * @param database the specified database
	 * @return the EclipseLink target database name, or <code>null<code> if none found
	 */
	protected String determineTargetDatabaseName(Database database) {
		switch (database) {
			case DB2: return TargetDatabase.DB2;
			case DERBY: return TargetDatabase.Derby;
			case HSQL: return TargetDatabase.HSQL;
			case INFORMIX: return TargetDatabase.Informix;
			case MYSQL: return TargetDatabase.MySQL4;
			case ORACLE: return TargetDatabase.Oracle;
			case POSTGRESQL: return TargetDatabase.PostgreSQL;
			case SQL_SERVER: return TargetDatabase.SQLServer;
			case SYBASE: return TargetDatabase.Sybase;
			default: return null;
		}
	}

	public JpaDialect getJpaDialect() {
		return this.jpaDialect;
	}

	public Class<? extends EntityManager> getEntityManagerInterface() {
		return JpaEntityManager.class;
	}

}
