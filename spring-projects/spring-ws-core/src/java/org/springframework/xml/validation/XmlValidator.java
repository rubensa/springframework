/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.xml.validation;

import java.io.IOException;
import javax.xml.transform.Source;

import org.xml.sax.SAXParseException;

/**
 * Simple processor that validates a given <code>Source</code>. Can be created via the
 * <code>XmlValidatorFactory</code>.
 * <p/>
 * <code>XmlValidator</code> instances are designed to be thread safe.
 *
 * @author Arjen Poutsma
 * @see XmlValidatorFactory#createValidator(org.springframework.core.io.Resource, String)
 */
public interface XmlValidator {

    /**
     * Validates the given <code>Source</code>, and returns an array of <code>SAXParseException</code>s as result. The
     * array will be empty if no validation errors are found.
     *
     * @param source the input document
     * @return an array of <code>SAXParseException</code>s
     * @throws IOException            if the <code>source</code> cannot be read
     * @throws XmlValidationException if the <code>source</code> cannot be validated
     */
    SAXParseException[] validate(Source source) throws IOException;

}
