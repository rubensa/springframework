/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.oxm;

import java.io.IOException;

import javax.xml.transform.Source;

/**
 * Defines the contract for Object XML Mapping unmarshallers. Implementations of this interface can deserialize a given
 * XML Stream to an Object graph.
 * 
 * @author Arjen Poutsma
 */
public interface Unmarshaller {

    /**
     * Unmarshals the given provided <code>javax.xml.transform.Source</code> into an object graph.
     * 
     * @param source the source to marshal from
     * @return the object graph
     * @throws XmlMappingException if the given source cannot be mapped to an object 
     * @throws IOException if an I/O Exception occurs
     */
    Object unmarshal(Source source) throws XmlMappingException, IOException;
}
