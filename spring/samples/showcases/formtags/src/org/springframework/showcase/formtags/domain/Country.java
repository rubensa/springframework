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

package org.springframework.showcase.formtags.domain;

/**
 * Models a country.
 *
 * @author Rob Harrop
 */
public class Country implements Comparable {


    private String code;
    private String name;


    /**
     * Creates a new instance of this {@link Country} class.
     */
    public Country() {
    }

    /**
     * Creates a new instance of this {@link Country} class.
     *
     * @param code the country code
     * @param name the name of the country
     */
    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public int compareTo(Object o) {
        return this.code.compareTo(((Country) o).code);
    }

}
