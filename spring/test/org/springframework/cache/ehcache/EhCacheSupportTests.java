/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.cache.ehcache;

import junit.framework.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 27.09.2004
 */
public class EhCacheSupportTests extends TestCase {

	public EhCacheSupportTests(String name) {
        super(name);
    }

    public void testLoadingBlankCacheManager() throws Exception {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = (CacheManager) cacheManagerFb.getObject();
			assertTrue("Loaded CacheManager with no caches", cm.getCacheNames().length == 0);
			Cache myCache1 = cm.getCache("myCache1");
			assertTrue("No myCache1 defined", myCache1 == null);
		}
		finally {
			cacheManagerFb.destroy();
		}
	}

	public void testLoadingCacheManagerFromConfigFile() throws Exception {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.setConfigLocation(new ClassPathResource("testEhcache.xml", getClass()));
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = (CacheManager) cacheManagerFb.getObject();
			assertTrue("Correct number of caches loaded", cm.getCacheNames().length == 1);
			Cache myCache1 = cm.getCache("myCache1");
			assertFalse("myCache1 is not eternal", myCache1.isEternal());
			assertTrue("myCache1.maxElements == 300", myCache1.getMaxElementsInMemory() == 300);
		}
		finally {
			cacheManagerFb.destroy();
		}
	}
	
	public void testEhCacheFactoryBeanWithDefaultCacheManager()
	throws Exception {
		doTestEhCacheFactoryBean(false);
	}
	
	public void testEhCacheFactory() 
	throws Exception {
		doTestEhCacheFactoryBean(true);
	}

	public void doTestEhCacheFactoryBean(boolean useCacheManagerFb) throws Exception {
		Cache cache = null;
		
		EhCacheManagerFactoryBean cacheManagerFb = null;		

		try {
			EhCacheFactoryBean cacheFb = new EhCacheFactoryBean();			
			
			if (useCacheManagerFb) {
				cacheManagerFb = new EhCacheManagerFactoryBean();
				cacheManagerFb.setConfigLocation(new ClassPathResource("testEhcache.xml", getClass()));
				cacheManagerFb.afterPropertiesSet();
				cacheFb.setCacheManager((CacheManager) cacheManagerFb.getObject());
			}		
			
			cacheFb.setCacheName("myCache1");
			cacheFb.afterPropertiesSet();
			cache = (Cache) cacheFb.getObject();
			assertEquals("myCache1", cache.getName());
			assertFalse("myCache1 is not eternal", cache.isEternal());
			assertEquals("myCach1.maxElements", 10000, cache.getMaxElementsInMemory());

			// Cache region is not defined. Should create one with default properties.
			cacheFb = new EhCacheFactoryBean();
			if (useCacheManagerFb) {
				cacheFb.setCacheManager((CacheManager) cacheManagerFb.getObject());
			}
			cacheFb.setCacheName("undefinedCache");
			cacheFb.afterPropertiesSet();
			cache = (Cache) cacheFb.getObject();
			assertEquals("undefinedCache", cache.getName());
			assertTrue("default maxElements is correct", cache.getMaxElementsInMemory() == 10000);
			assertTrue("default overflowToDisk is correct", cache.isOverflowToDisk());
			assertFalse("default eternal is correct", cache.isEternal());
			assertTrue("default timeToLive is correct", cache.getTimeToLiveSeconds() == 120);
			assertTrue("default timeToIdle is correct", cache.getTimeToIdleSeconds() == 120);
			assertTrue("default diskPersistent is correct", !cache.isDiskPersistent());
			assertTrue("default diskExpiryThreadIntervalSeconds is correct", cache.getDiskExpiryThreadIntervalSeconds() == 120);

			// overriding the default properties
			cacheFb = new EhCacheFactoryBean();
			if (useCacheManagerFb) {
				cacheFb.setCacheManager((CacheManager) cacheManagerFb.getObject());
			}
			cacheFb.setBeanName("undefinedCache2");
			cacheFb.setMaxElementsInMemory(5);
			cacheFb.setOverflowToDisk(false);
			cacheFb.setEternal(true);
			cacheFb.setTimeToLive(8);
			cacheFb.setTimeToIdle(7);
			cacheFb.setDiskPersistent(true);
			cacheFb.setDiskExpiryThreadIntervalSeconds(10);
			cacheFb.afterPropertiesSet();
			cache = (Cache) cacheFb.getObject();
			assertEquals("undefinedCache2", cache.getName());
			assertTrue("overridden maxElements is correct", cache.getMaxElementsInMemory() == 5);
			assertFalse("overridden overflowToDisk is correct", cache.isOverflowToDisk());
			assertTrue("overridden eternal is correct", cache.isEternal());
			assertTrue("default timeToLive is correct", cache.getTimeToLiveSeconds() == 8);
			assertTrue("default timeToIdle is correct", cache.getTimeToIdleSeconds() == 7);
			assertTrue("overridden diskPersistent is correct", cache.isDiskPersistent());
			assertTrue("overridden diskExpiryThreadIntervalSeconds is correct", cache.getDiskExpiryThreadIntervalSeconds() == 10);
		}
		finally {
			if (useCacheManagerFb) {
				cacheManagerFb.destroy();
			}
		}
	}
	
	public void testFactoryBeans() 
	throws Exception {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		
		assertEquals(CacheManager.class, cacheManagerFb.getObjectType());
		
		cacheManagerFb.afterPropertiesSet();
		
		assertEquals(CacheManager.getInstance().getClass(), cacheManagerFb.getObjectType());		
		assertTrue("Singleton property", cacheManagerFb.isSingleton());
		
		cacheManagerFb.destroy();
		
		EhCacheFactoryBean cacheFb = new EhCacheFactoryBean();
		cacheFb.setCacheName("myCache");
		
		assertEquals(Cache.class, cacheFb.getObjectType());	
		
		cacheFb.afterPropertiesSet();
		
		assertEquals(CacheManager.getInstance().getCache("myCache").getClass(), cacheFb.getObjectType());		
		assertTrue("Singleton property", cacheFb.isSingleton());
		
	}

    // Suite method is needed for the tests to succeed on JRockit
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new EhCacheSupportTests("testLoadingBlankCacheManager"));
        suite.addTest(new EhCacheSupportTests("testEhCacheFactoryBeanWithDefaultCacheManager"));
        suite.addTest(new EhCacheSupportTests("testEhCacheFactory"));
        suite.addTest(new EhCacheSupportTests("testFactoryBeans"));
        return suite;
    }

}
