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

package org.springframework.beandoc.output;

import java.util.*;

import org.jdom.Element;
import org.springframework.util.StringUtils;

/**
 * Decorator implementation that adds JavaDoc href's to beans based on their classes.  The Decorator
 * knows about standard Java library class locations, and additional locations can be added based
 * on a simple pattern matching strategy for the class name.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class JavaDocDecorator extends SimpleDecorator {

    protected static final String ATTRIBUTE_JAVADOC = "beandocJavaDoc";

    private static String javaVersion = System.getProperty("java.specification.version");
    
    private SortedMap javaDocLocations = new TreeMap(
        new Comparator() {
            public int compare(Object path1, Object path2) {
                return ((String) path2).length() - ((String) path1).length();
            }                
        }
    );
    
    /**
     * Default constructor adds well know locations to the JavaDoc location Map
     * which can be used by a Transformer.
     */
    public JavaDocDecorator() {
        addJavaDocLocation(
            "java.",
            "http://java.sun.com/j2se/" + javaVersion + "/docs/api/");
        addJavaDocLocation(
            "javax.",
            "http://java.sun.com/j2se/" + javaVersion + "/docs/api/");
        addJavaDocLocation(
            "org.springframework.",
            "http://www.springframework.org/docs/api/");
        addJavaDocLocation(
            "org.springframework.samples.",
            null);
    }

    /**
     * @see org.springframework.beandoc.output.SimpleDecorator#decorateElement
     */
    protected void decorateElement(Element element) {
	    if (element.getName().equals("bean")) {
		    String idOrName = element.getAttributeValue(Tags.ATTRIBUTE_ID);
		    if (idOrName == null) idOrName = element.getAttributeValue(Tags.ATTRIBUTE_NAME);
		    String className = element.getAttributeValue(Tags.ATTRIBUTE_CLASSNAME);
		      
		    String javaDoc = null;
		    if (className != null) 
		        javaDoc = getJavaDocForClassName(className);
		      
		    if (javaDoc != null) element.setAttribute(ATTRIBUTE_JAVADOC, javaDoc);
	    }        
    }    

    /**
     * A <code>SortedMap</code> keyed by package prefixes that point to URI's (local, remote,
     * absolute or relative) of JavaDoc locations.  Used to link classnames to their javadoc
     * locations in the HTML output.  The preferred way to modify this list programmatically is 
     * through the {@link #addJavaDocLocation} convenience method.
     * 
     * @param map a <code>SortedMap</code> of javadoc locations keyed by package name prefixes.
     * @see #addJavaDocLocation
     */
    public void setJavaDocLocations(SortedMap map) {
        javaDocLocations = map;
    }

    /**
     * Add a JavaDoc location used in the output documents to link classnames to their
     * JavaDoc pages.  Specify a package prefix followed by a URL.  Classes whose
     * package names start with that prefix (using <code>String.startsWith()</code>)
     * will be linked to this location.
     * <p>
     * For example, adding a location where <pre>
     *    classPrefix = com.foo.bar.
     *    url = http://our.server.com/doc/api/
     * </pre> will cause a class in this package named <code>Example.class</code> to be 
     * linked with an HREF of;
     * <code>http://our.server.com/doc/api/com/foo/bar/Example.html</code>
     * <p>
     * 
     * Note that the package names are used as keys in a <code>SortedMap</code> which 
     * is sorted in reverse order by key length.  This means you can add package keys 
     * in any order you like, and class names will be evaluated against the longest 
     * package names first down to the shortest.  This enables you for example to
     * specify that <code>org.springframework.samples.</code> has a different document
     * location than <code>org.springframework.</code>
     *  
     * @param classPrefix the prefix, of arbitrary length, of the package and class name to 
     *      match against.  May not be null (will be ignored)
     * @param url the root of the API documentation
     */
    public void addJavaDocLocation(String classPrefix, String url) {
        if (classPrefix == null) return;
        javaDocLocations.put(classPrefix, url);
    }

    /**
     * A <code>SortedMap</code> keyed by package prefixes that point to URI's (local, remote,
     * absolute or relative) of JavaDoc locations.  Used to link classnames to their javadoc
     * locations in the HTML output.
     * <p>
     * The returned underlying <code>SortedMap</code> is modifiable and will, if modified, affect
     * subsequent calls to the <code>BeanDocEngine</code>'s <code>process()</code> method if
     * you are using the tool programmatically.  The preferred way to modify this list is 
     * through the {@link #addJavaDocLocation} convenience method.
     * 
     * @return a <code>SortedMap</code> of javadoc locations keyed by package name prefixes.
     * @see #addJavaDocLocation
     */
    public SortedMap getJavaDocLocations() {
        return javaDocLocations;
    }
    
    /**
     * Queries the internal Map of locations and returns the first
     * matching JavaDoc location that meets this className prefix.
     * 
     * @param className the package prefix pattern that a JavaDoc location is specified
     *      for.
     * @return javaDoc location (URL) or null if no location is
     *      specified for the given class name
     * @see #addJavaDocLocation
     */
    private String getJavaDocForClassName(String className) {
        for (Iterator i = javaDocLocations.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            if (className.startsWith(key)) {
                String class2url = StringUtils.replace(className, ".", "/") + ".html";
                String jdoc = (String) javaDocLocations.get(key);
                if (jdoc == null) return null;
                if (!jdoc.endsWith("/")) jdoc += "/";
                return jdoc + class2url;
            }
        }
        
        return null;
    }

}
