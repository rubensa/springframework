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
 * SpringLoader
 * 
 * @author Darren Davison
 * @since 1.0
 */
class SpringLoaderCommand {

    private String inputFiles;
    private String outputDir;
    private String beandocPropsLocation;
    
    /**
     * @param inputFiles one or more resource resolveable Strings for input locations
     * @param outputDir a writeable location for beandoc output
     * @param beandocPropsLocation the absolute path to the beandoc.properties file.  Can be null if
     *      mandatory properties are specified as parmeters to this method or as System properties 
     *      using a "springbeandoc." qualifier (ie <code>springbeandoc.input.files=...</code> in 
     *      place of <code>input.files=...</code>)
     */
    public SpringLoaderCommand(String inputFiles, String outputDir, String beandocPropsLocation) {
        this.inputFiles = inputFiles;
        this.outputDir = outputDir;
        this.beandocPropsLocation = beandocPropsLocation;
    }
    
    public String getBeandocPropsLocation() {
        return beandocPropsLocation;
    }    
    
    public String getInputFiles() {
        return inputFiles;
    }
    
    public String getOutputDir() {
        return outputDir;
    }
}
