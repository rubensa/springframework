/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util.visitor;

/**
 * Super 'tag' interface to be implemented by objects that act as visitors.
 * <p>
 * This is a tag interface and as a result does not define any public
 * methods.  It is here to provide some degree of type safety and description
 * to Vistable dispatchers and for dispatch by reflection.
 * @author  Keith Donald
 * @see Visitable
 * @see ReflectiveVisitorSupport
 */
public interface Visitor {
}
