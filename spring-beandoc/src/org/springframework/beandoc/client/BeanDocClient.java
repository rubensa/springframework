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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beandoc.ContextProcessor;
import org.springframework.beandoc.output.Decorator;
import org.springframework.beandoc.output.Transformer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

/**
 * Command line client for the beandoc tool.  Loads and runs a
 * {@link org.springframework.beandoc.ContextProcessor}
 * with a default configuration.  Accepts various command line
 * arguments which must contain values for input resources and an output directory and
 * can optionally contain other properties to further configure the default behaviour.
 * <p>
 * For total control and power consider supplying a context file location as this offers
 * the ability to select which {@link Decorator}s and
 * {@link Transformer}s are used in generating beandoc output.  Alternatively, use
 * the <code>DefaultContextProcessor</code> directly in code.
 *
 * @author Darren Davison
 * @author Michael Schuerig, <michael@schuerig.de>
 * @since 1.0
 */
public class BeanDocClient {
    
    // State constants for options parser
    private static final int OPTION = 0;
    
    private static final int OUTPUT_DIR = 1;
    
    private static final int PROPS = 2;
    
    private static final int PREFIX = 3;
    
    private static final int CONTEXT = 4;
    
    private static final int INPUT_FILE = 5;
    
    private static final int TITLE = 6;
    
    
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
     * <tr><td>compiler.dotExe</td><td>/usr/bin/dot</td></tr>
     * <tr><td>processor.validateFiles</td><td>set to 'true' or 'false' to have the XML
     * parser validate input files during parsing.  True by default.</td></tr>
     * <tr><td>graphs.outputType</td><td>default is png (strongly recommended) but
     * can be switched to gif, jpg or svg as desired</td></tr>
     * </table>
     *
     * @param args command line parameters using GNU style options..
     * 		<ul>
     * 		<li><b>--output [dir]</b> -  output directory</li>
     * 		<li><b>--properties [file]</b> - the location of a properties file
     * 		containing all mandatory and optional configuration properties for the
     * 		beandoc tool.</li>
     * 		<li><b>--prefix [string]</b> - the string that BeanDocClient should expect
     * 		to find prefixing all beandoc properties.</li>
     * 		<li><b>--context [file]</b> - a custom context file describing how to
     * 		configure BeanDoc's dependencies.  Power users only.</li>
     * 		<li><b>--help</b> - print usage to stdout and quit.</li>
     *      <li><b> [file1] [file2] [fileN]</b> -  input files</li>
     *      </ul>
     */
    public static void main(String[] args) {
        
        List inputFiles = new ArrayList();
        String output = null;
        String props = null;
        String prefix = null;
        String context = null;
        String title = null;
        
        int state = OPTION;
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            
            if (state == INPUT_FILE) {
                inputFiles.add(arg);
            } else {
                switch (state) {
                    case OPTION:
                        if ("--help".equals(arg)) {
                            usage();
                            System.exit(0);
                        } else if ("--output".equals(arg)) {
                            state = OUTPUT_DIR;
                        } else if ("--properties".equals(arg)) {
                            state = PROPS;
                        } else if ("--prefix".equals(arg)) {
                            state = PREFIX;
                        } else if ("--context".equals(arg)) {
                            state = CONTEXT;
                        } else if ("--title".equals(arg)) {
                            state = TITLE;
                        } else if ("--".equals(arg)) {
                            state = INPUT_FILE;
                        } else {
                            inputFiles.add(arg);
                        }
                        break;
                    case OUTPUT_DIR:
                        output = arg;
                        state = OPTION;
                        break;
                    case PROPS:
                        props = arg;
                        state = OPTION;
                        break;
                    case PREFIX:
                        prefix = arg;
                        state = OPTION;
                        break;
                    case CONTEXT:
                        context = arg;
                        state = OPTION;
                        break;
                    case TITLE:
                        title = arg;
                        state = OPTION;
                        break;
                }
            }
        }
        
        if ( ! ( (!inputFiles.isEmpty() && StringUtils.hasText(output)) ||
                StringUtils.hasText(props) ) ) {
            usage();
            System.exit(1);
        }
        
        // MS: this is a hack; further down the String is again
        // split into a List.
        String inputs = null;
        if (!inputFiles.isEmpty())
            inputs = StringUtils.collectionToCommaDelimitedString(inputFiles);
        
        try {
            BeanFactory factory =
                    SpringLoader.getBeanFactory(new SpringLoaderCommand(inputs, output, title, props, prefix, context));
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
        .append("java " + BeanDocClient.class.getName())
        .append(" [--output output directory]")
        .append(" [--properties beandoc.properties]")
        .append(" [--context beandoc context]")
        .append(" [--title documentation title to use]")
        .append(" [--prefix properties prefix]")
        .append(" [--] [input file...]")
        .append("\n\n")
        .append("Either a properties file, or BOTH input file(s) and output directory, or all three arguments must be specified.");
        
        System.out.println(usg.toString());
    }
}
