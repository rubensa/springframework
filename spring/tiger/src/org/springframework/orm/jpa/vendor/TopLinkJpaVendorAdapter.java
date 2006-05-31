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
import java.util.logging.Level;

import oracle.toplink.essentials.config.TargetDatabase;
import oracle.toplink.essentials.config.TopLinkProperties;
import oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider;

import org.springframework.orm.jpa.JpaDialect;

/**
 * TopLink-specific JpaVendorAdapter implementation.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class TopLinkJpaVendorAdapter extends AbstractJpaVendorAdapter {

	private final JpaDialect jpaDialect = new TopLinkJpaDialect();


	public Class getPersistenceProviderClass() {
		return EntityManagerFactoryProvider.class;
	}

	public Map getJpaPropertyMap() {
		Properties jpaProperties = new Properties();

		if (getDatabasePlatform() != null) {
			jpaProperties.setProperty(TopLinkProperties.TARGET_DATABASE, getDatabasePlatform());
		}
		else if (getDatabase() != null) {
			String targetDatabase = determineTargetDatabaseName(getDatabase());
			if (targetDatabase != null) {
				jpaProperties.setProperty(TopLinkProperties.TARGET_DATABASE, targetDatabase);
			}
		}

		if (isGenerateDdl()) {
			jpaProperties.setProperty(EntityManagerFactoryProvider.DDL_GENERATION,
					EntityManagerFactoryProvider.DROP_AND_CREATE);
			jpaProperties.setProperty(EntityManagerFactoryProvider.DDL_GENERATION_MODE,
					EntityManagerFactoryProvider.DDL_DATABASE_GENERATION);
		}
		if (isShowSql()) {
			jpaProperties.setProperty(TopLinkProperties.LOGGING_LEVEL, Level.FINE.toString());
		}

		return jpaProperties;
	}

	protected String determineTargetDatabaseName(Database database) {
		switch (database) {
			case DB2: return TargetDatabase.DB2;
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

	public Class getEntityManagerInterface() {
		return oracle.toplink.essentials.ejb.cmp3.EntityManager.class;
	}

	public JpaDialect getJpaDialect() {
		return jpaDialect;
	}

}
