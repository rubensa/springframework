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
 
package org.springframework.jms;

/**
 * Simple bean with properties.
 * @author Mark Pollack
 */
public class SimpleTestBean  {

    private double age;
    private String name;
    
        

    /**
     * @return
     */
    public double getAge() {
        return age;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param d
     */
    public void setAge(double d) {
        age = d;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

}
