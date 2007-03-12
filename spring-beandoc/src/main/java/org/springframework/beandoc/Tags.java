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

package org.springframework.beandoc;

/**
 * Specifies common constants such as tag and attribute names from both Spring's DTD and
 * beandoc's decorator model.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class Tags {

    public static final String TAGNAME_IMPORT = "import";

    public static final String TAGNAME_BEANS = "beans";

    public static final String TAGNAME_BEAN = "bean";

    public static final String TAGNAME_IDREF = "idref";

    public static final String TAGNAME_REF = "ref";
    
    public static final String TAGNAME_LOOKUP = "lookup-method";

    public static final String TAGNAME_REPLACE = "replaced-method";

    public static final String TAGNAME_DESCRIPTION = "description";

    public static final String TAGNAME_PROPERTY = "property";

    public static final String ATTRIBUTE_PARENT = "parent";

    public static final String ATTRIBUTE_NAME = "name";
    
    public static final String ATTRIBUTE_ID = "id";
    
    public static final String ATTRIBUTE_CLASSNAME = "class";

    public static final String ATTRIBUTE_RESOURCE = "resource";

    public static final String ATTRIBUTE_BD_FILENAME = "beandocFileName";

    public static final String ATTRIBUTE_BD_PATHRELATIVE = "beandocPathRelative";

    public static final String ATTRIBUTE_REF_BEAN = "bean";

    public static final String ATTRIBUTE_REF_LOCAL = "local";

    public static final String ATTRIBUTE_REF_REPLACER = "replacer";
    
    public static final String ATTRIBUTE_PROXY_FOR = "beandocProxyFor";

}
