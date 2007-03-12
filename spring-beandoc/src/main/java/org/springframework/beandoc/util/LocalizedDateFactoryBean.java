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
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * Manages a <code>DateFormat</code> instance that can be customized with
 * a given <code>Locale</code>.  Each request of this factory returns a 
 * String containing the formatted Date/Time from the time of the request.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class LocalizedDateFactoryBean implements FactoryBean, InitializingBean {

    private DateFormat df;

    private Locale locale = Locale.getDefault();


    /**
     * Returns the formatted, current <code>Date</code> according to the supplied
     * or system default Locale.
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return df.format(new Date());
    }

    /**
     * Returns the <code>String</code> class.
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return String.class;
    }

    /**
     * A new instance of the managed object has the current date and time
     * formatted according to the supplied Locale (or system default locale
     * if none was specified).  Therefore each managed bean instance is
     * different and a prototype.  This method always returns <code>false</code>
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return false;
    }

    /**
     * Setup the <code>DateFormat</code> managed object based on the supplied
     * locale (or System default locale if none is supplied)
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
    }

    /**
     * Set the Locale to use for the managed <code>DateFormat</code>, overriding the system
     * default locale.
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
        try {
            afterPropertiesSet();
        } catch (Exception e) {
            // only if locale is invalid
        }
    }
}
