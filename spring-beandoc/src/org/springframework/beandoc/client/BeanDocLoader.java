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

import java.io.IOException;

import org.springframework.beandoc.BeanDocEngine;
import org.springframework.beandoc.BeanDocException;
import org.springframework.beandoc.Configuration;

/**
 * Can be used programatically, or from the command line via its <code>main</code>
 * method.  Loads, configures and runs a {@link BeanDocEngine} with a default
 * {@link Configuration}.  Can be subclassed in order to post-process the 
 * <code>Configuration</code> for added flexibility.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class BeanDocLoader {
    
    /**
     * Inputs are standard Spring <code>Resource</code>s and can point to 
     * individual files or classpatch entries with or without protocol indicators.  It
     * is also possible to specify a pattern for loading multiple resources. 
     * <p>
     * This class is handy for quick output documentation using default values for all
     * configurable aspects but lacks any flexibility.  Consider subclassing it and 
     * post-processing the {@link Configuration} object to tailor the output more
     * effectively.
     * 
     * @param inputFiles
     * @param outputDir
     */
    public void configureAndRun(String[] inputFiles, String outputDir) {
        
        try {
            Configuration beanCfg = null;     
    
            beanCfg =
                org.springframework.beandoc.Configuration.getDefaultConfiguration(
                    inputFiles,
                    outputDir);
    
            postProcessConfiguration(beanCfg);
    
            if (beanCfg.getDotExe() == null)
                System.out.println("Graphing output disabled - no dot executable location supplied");
        
            new BeanDocEngine(beanCfg).process();
    
        } catch (IOException e) {
            System.err.println("Problem resolving/reading input files or output directory: " + e.getMessage());
    
        } catch (BeanDocException bde) {
            System.err.println("Problem processing configuration: " + bde.getMessage());
        }
    }

    /**
     * Permit configuring further the default configuration for the beandoc tool
     * obtained with the <code>Configuration.getDefaultConfiguration()</code> method.
     * Default implementation does nothing.
     * 
     * @param beanCfg a default configuration that can be further modified prior
     *      to processing the beandoc.
     */
    protected void postProcessConfiguration(Configuration beanCfg) {
    }
    
	/**
	 * DefaultBootStrapper for the beandoc client.  Command line parameters should be
	 * in the form <code>inputResource-1 [inputResource-n] outputDir</code>.  Input
	 * resources are standard Spring <code>Resource</code>s and can point to 
	 * individual files or classpatch entries with or without protocol indicators.  It
	 * is also possible to specify a pattern for loading multiple resources.
	 * <p>
	 * Some examples of usage:
	 * <table>
	 * <tr><td><code>java org.springframework.beandoc.client.BeanDocLoader<br>
	 * &nbsp;&nbsp;/projects/myproject/applicationContext.xml /output/html/</code></td></tr>
	 * <tr><td><code>java ...BeanDocLoader classpath:/com/foo/bar/* /output/html/</code></td></tr>
	 * <tr><td><code>java ...BeanDocLoader file:/projects/myproject/applicationContext.xml 
	 * classpath:/com/foo/bar/anotherFile.xml /output/html/</code></td></tr>
	 * </table>
	 * 
	 * @param args must consist of one or more resolvable Resources to be used as
	 *      input files, followed by exactly one output directory which must exist
	 *      and be writeable for the current user.
	 */
	public static void main(String[] args) {

		if (args.length < 2) {
			usage();
			System.exit(99);
		}
    
		String[] inputFiles = new String[args.length - 1];
		String outputDir = args[args.length - 1];

		for (int f = 0; f < args.length -1; f++)
			inputFiles[f] = args[f];
        
		BeanDocLoader boot = new BeanDocLoader();
		boot.configureAndRun(inputFiles, outputDir);        
	}

	/**
	 * print usage to stdout
	 */
	public static void usage() {
		StringBuffer usg = new StringBuffer("Usage:\n\n")
			.append("java [-D").append(Configuration.DOTEXE_SYSTEM_PROPERTY)
			.append("=/path/to/dot/executable] org.springframework.beandoc.client.DefaultLoader ")
			.append("inputFile-1.xml [inputFile-2.xml.. inputFile-n.xml] outputDirectory\n\n")
			.append("Graphing output is only produced if the System property is specified pointing to the ")
			.append("platform specific location of the dot executable/binary file.  (See http://www.graphviz.org)");
        
		System.out.println(usg.toString());
	}
}
