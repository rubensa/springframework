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

import org.springframework.beandoc.ContextProcessor;
import org.springframework.beandoc.output.Decorator;
import org.springframework.beandoc.output.Transformer;
import org.springframework.beans.factory.BeanFactory;

/**
 * Command line client for the beandoc tool.  Loads and runs a 
 * {@link org.springframework.beandoc.ContextProcessor} 
 * with a default configuration.  Accepts a properties file as the sole command line
 * argument which must contain values for input resources and an output directory and
 * can optionally contain other properties to further configure the default behaviour.
 * <p>
 * For more control and power consider using the <code>DefaultContextProcessor</code>
 * directly in code as this offers the ability to select which {@link Decorator}s and 
 * {@link Transformer}s are used in generating beandoc output.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class BeanDocClient {
	
    /**
	 * Default boot strapper for the beandoc client.  Command line parameter should be
	 * the location of a properties file, either as a file pointer or as a classpath
	 * location.  Any default Spring Resource strategy should work.
	 * <p>
	 * The properties file <strong>must</strong> contain values shown below in bold and
	 * can optionally contain any or none of the others.
	 * <p>
	 * <table border="1">
	 * <tr><td><strong>input.files</strong></td><td>one or more (comma or space separated)
	 * resources that will be used as input files for the beandoc tool.  All input files are
	 * assumed to make a single application context or bean factory.<br><br>Possible values
	 * are:<br>classpath:/com/foo/bar/*.xml<br>file:///home/jdoe/file1.xml,file:///home/jdoe/file2.xml
	 * </td></tr>
	 * <tr><td><strong>output.dir</strong></td><td>the directory (which must be writable for the
	 * current user) that output will be written to.</td></tr>
	 * <tr><td>graphVizTransformer.dotExe</td><td>/usr/bin/dot</td></tr>
	 * <tr><td>processor.validateFiles</td><td>set to 'true' or 'false' to have the XML
	 * parser validate input files during parsing.  True by default.</td></tr>
	 * <tr><td>graphVizDecorator.graphOutputType</td><td>default is png (strongly recommended) but
	 * can be switched to gif, jpg or svg as desired</td></tr>
	 * </table>
	 * 
	 * @param args command line parameters.  
	 * 		<ul><li>If a single parameter is offered, it is taken 
	 * 		to be the location of a properties file containing all mandatory and optional
	 * 		configuration properties for the beandoc tool.</li>
	 * 		<li>If two parameters are supplied, they are the input files (comma separated) 
	 * 		and output directory respectively.M/li>
	 * 		<li>Three parameters specify input file(s), output directory and location of
	 * 		properties file</li></ul>
	 */
	public static void main(String[] args) {
	    
		String inputs = null;
		String output = null;
		String props = null;
	    	    
		switch (args.length) {
		    case 0:
			usage();
			System.exit(99);
			break;
		
			case 1:
		    props = args[0];
		    break;
		    
		    case 3:    
		    props = args[2];
		    // ok to fall through
		
			case 2:  		
		    inputs = args[0];
		    output = args[1];	
		    break;
		    
		}
    
		try {
            BeanFactory factory = 
                SpringLoader.getBeanFactory(new SpringLoaderCommand(inputs, output, props));
            ContextProcessor cp = (ContextProcessor) factory.getBean("processor");
            cp.process();
                    
        } catch (Exception e) {
            System.err.println("Unable to run beandoc tool; " + e.getMessage());
            System.exit(99);
        }
	}

	/**
	 * print usage to stdout
	 */
	private static void usage() {
		StringBuffer usg = new StringBuffer("Usage:\n")
			.append("java org.springframework.beandoc.client.BeanDocClient [input file(s)] [output directory] [beandoc.properties]\n\n")
			.append("Either a properties file, or BOTH input and output files, or all three arguments must\n")
			.append("be specified.");
        
		System.out.println(usg.toString());
	}
}
