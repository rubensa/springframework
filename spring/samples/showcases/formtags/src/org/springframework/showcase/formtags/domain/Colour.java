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

import org.springframework.core.enums.ShortCodedLabeledEnum;

/**
 * Simple enumeration for common colors.
 *
 * @author Rob Harrop
 */
public class Colour extends ShortCodedLabeledEnum {


    public static final Colour RED = new Colour(0, "RED");
    public static final Colour GREEN = new Colour(1, "GREEN");
    public static final Colour BLUE = new Colour(2, "BLUE");


    private Colour(int code, String label) {
        super(code, label);
    }

}
