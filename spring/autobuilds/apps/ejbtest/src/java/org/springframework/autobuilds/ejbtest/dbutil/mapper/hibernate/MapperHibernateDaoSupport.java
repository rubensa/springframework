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

package org.springframework.autobuilds.ejbtest.dbutil.mapper.hibernate;

import org.springframework.orm.hibernate.support.HibernateDaoSupport;

/**
 * Subclass of Spring's HibernateDaoSupport which defaults the defaultAllowCreate flag to 
 * false. It also has a constructor variant which allows an instance to be created based
 * on a passed in Mapper instance. InitializingBean's afterPropertiesSet must still be
 * called when using this constructor! 
 * 
 * @author colin sampaleanu
 */

public abstract class MapperHibernateDaoSupport extends HibernateDaoSupport {

  // constructor for bean style creation
  public MapperHibernateDaoSupport() {
    defaultTemplateAllowCreateValue = false;  
  }

  // construction off an existing mapper instance
  protected MapperHibernateDaoSupport(MapperImpl mapper) {
    setSessionFactory(mapper.getSessionFactory());
  }

}
