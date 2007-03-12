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

package org.springframework.beandoc.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;



public class LocalizedDateFactoryBeanTests extends TestCase {
    
    DateFormat testDf;
    LocalizedDateFactoryBean fb = new LocalizedDateFactoryBean();
    Locale[] locales = {
        Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN
    };
    
    public void testFormatting() throws Exception { 
        for (int i = 0; i < locales.length; i++) {
            fb.setLocale(locales[i]);
            testDf = new SimpleDateFormat("MMMM", locales[i]);
            String currMth = testDf.format(new Date());
            assertTrue(((String) fb.getObject()).indexOf(currMth) > -1);
        }
    }
}
