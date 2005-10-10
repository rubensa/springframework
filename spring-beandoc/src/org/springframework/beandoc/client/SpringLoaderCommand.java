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

package org.springframework.beandoc.client;


/**
 * Composite class for configuration required to bootstrap a bean factory via
 * SpringLoader. Simply aggregates a few parameters to pass to the
 * <code>SpringLoader</code>'s static <code>getBeanFactory</code> method.
 * <p>
 * For ultimate flexibility you can specify a context definition file for
 * beandoc. It must contain a fully configured <code>ContextProcessor</code>
 * with an id of "processor".
 * 
 * @author Darren Davison
 * @since 1.0
 * @see SpringLoader
 */
class SpringLoaderCommand {

    private String inputFiles;
    
    private String outputDir;
    
    private String title;
    
    private String beandocPropsLocation;
    
    private String beandocPropsPrefix;
    
    private String beandocContextLocation;
    
    /**
     * @param inputFiles one or more resource resolveable Strings for input
     *        locations
     * @param outputDir a writeable location for beandoc output
     * @param beandocPropsLocation the absolute path to the beandoc.properties
     *        file. Can be null if mandatory properties are specified as
     *        parmeters to this method or as System properties using a
     *        "springbeandoc." prefix (ie
     *        <code>springbeandoc.input.files=...</code> in place of
     *        <code>input.files=...</code>)
     * @param beandocPropsPrefix the prefix to expect all beandoc properties to
     *        have. May be used to make beandoc more accessible to other client
     *        types, in particular, Maven.
     * @param private beandocContextLocation the location of a custom Context
     *        file that defines a ContextProcessor named "processor"
     */
    public SpringLoaderCommand(
        String inputFiles, 
        String outputDir, 
        String title,
        String beandocPropsLocation, 
        String beandocPropsPrefix, 
        String beandocContextLocation) 
    {        
        this.inputFiles = inputFiles;
        this.outputDir = outputDir;
        this.title = title;
        this.beandocPropsLocation = beandocPropsLocation;
        this.beandocPropsPrefix = beandocPropsPrefix;
        this.beandocContextLocation = beandocContextLocation;
    }
    
    /**
     * @param inputFiles one or more resource resolveable Strings for input
     *        locations
     * @param outputDir a writeable location for beandoc output
     * @param beandocPropsLocation the absolute path to the beandoc.properties
     *        file. Can be null if mandatory properties are specified as
     *        parmeters to this method or as System properties using a
     *        "springbeandoc." prefix (ie
     *        <code>springbeandoc.input.files=...</code> in place of
     *        <code>input.files=...</code>)
     */
    public SpringLoaderCommand(String inputFiles, String outputDir, String title, String beandocPropsLocation) {
        this(inputFiles, outputDir, title, beandocPropsLocation, null, null);
    }

    /**
     * @return location of the beandoc context file
     */
    public String getBeandocContextLocation() {
        return beandocContextLocation;
    }
    
    /**
     * @return location of the beandoc.properties file
     */
    public String getBeandocPropsLocation() {
        return beandocPropsLocation;
    }   

    /**
     * @return prefix used to select relevant properties.
     * @see #getBeandocPropsLocation()
     */
    public String getBeandocPropsPrefix() {
        return beandocPropsPrefix;
    } 
    
    /**
     * @return the input files identifier
     */
    public String getInputFiles() {
        return inputFiles;
    }
    
    /**
     * @return the location of the output directory
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * @return the title used in the documentation
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new StringBuffer("inputFiles: ").append(inputFiles)
        .append("; outputDir: ").append(outputDir)
        .append("; title: ").append(title)
        .append("; beandocPropsLocation: ").append(beandocPropsLocation)
        .append("; beandocPropsPrefix: ").append(beandocPropsPrefix)
        .append("; beandocContextLocation: ").append(beandocContextLocation)
        .toString();
    }

}
