/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.test.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.support.GenericXmlContextLoader;

/**
 * ContextConfiguration defines class-level metadata which can be used to
 * instruct client code with regard to how to load and configure an
 * {@link org.springframework.context.ApplicationContext ApplicationContext}.
 * Although the annotated class will generally be an integration or unit test,
 * the use of ContextConfiguration is not necessarily limited to testing
 * scenarios.
 *
 * @author Sam Brannen
 * @since 2.5
 * @see ContextLoader
 * @see org.springframework.context.ApplicationContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface ContextConfiguration {

	/**
	 * <p>
	 * The resource locations to use for loading the
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}.
	 * </p>
	 */
	String[] locations() default {};

	/**
	 * <p>
	 * Whether or not {@link #locations() resource locations} from superclasses
	 * should be <em>inherited</em>.
	 * </p>
	 * <p>
	 * The default value is <code>false</code>, which means that resource
	 * locations defined in the annotated class will override those defined by a
	 * superclass. If this value is set to <code>true</code>, however, the
	 * resource locations for the annotated class will be appended to the list
	 * of resource locations defined by a superclass. Thus, subclasses have the
	 * option of <em>extending</em> the list of resource locations. In the
	 * following example, the
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}
	 * for <code>ExtendedTest</code> would be loaded from
	 * &quot;base-context.xml&quot; and &quot;extended-context.xml&quot;. In
	 * addition, beans defined in &quot;base-context.xml&quot; may be overridden
	 * in &quot;extended-context.xml&quot;.
	 * </p>
	 *
	 * <pre class="code">
	 * {@link ContextConfiguration @ContextConfiguration}(locations={&quot;base-context.xml&quot;})
	 * public class BaseTest {
	 *     // ...
	 * }
	 * {@link ContextConfiguration @ContextConfiguration}(locations={&quot;extended-context.xml&quot;}, inheritLocations=true)
	 * public class ExtendedTest extends BaseTest {
	 *     // ...
	 * }
	 * </pre>
	 */
	boolean inheritLocations() default false;

	/**
	 * <p>
	 * The {@link ContextLoader} type to use for loading the
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}.
	 * </p>
	 *
	 * @see GenericXmlContextLoader
	 */
	Class<? extends ContextLoader> loader() default GenericXmlContextLoader.class;

}
