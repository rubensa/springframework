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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.springframework.beandoc.ContextProcessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;


/**
 * Ant task that sets up and runs the beandoc tool based on a properties file
 * containing all configuration options. It's not possible to allow all options
 * to be specified on the Ant task as setters (attributes) since beandoc is
 * extensible and any number of custom decorators or transformers could be added
 * to the core setup, all with their own properties.
 * <p>
 * This class is a very thin wrapper that accepts the mandatory beandoc options
 * as task attributes and boots the default context via a
 * <code>SpringLoader</code>.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class AntTask extends MatchingTask {

    protected final Log logger = LogFactory.getLog(getClass());
    
    private File beandocProps;
    
    private String beandocPropsPrefix;
    
    private File outputDir;
    
    private String inputFiles;
    
    private String title;
    
    private String beandocContext;
    
    private List fileSets = new ArrayList();

    /**
     * Load and run the default beandoc processor wrapping any <code>Exception</code> as
     * an Ant <code>BuildException</code>.
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {  	        
        ClassLoader sysClassLoader = Thread.currentThread().getContextClassLoader();
        
        try {   
            AntClassLoader newClassLoader = 
                new AntClassLoader(getClass().getClassLoader(), true);
            Thread.currentThread().setContextClassLoader(newClassLoader);
            
            String resolvedInputFiles = resolveInputFiles();        
		         
            BeanFactory factory = 
                SpringLoader.getBeanFactory(
                    new SpringLoaderCommand(
                        resolvedInputFiles, 
                        (outputDir != null ) ? outputDir.getAbsolutePath() : null, 
                        title,
                        (beandocProps != null ) ? beandocProps.getAbsolutePath() : null,
                        beandocPropsPrefix,
                        beandocContext
                    )
                );

            ContextProcessor cp = (ContextProcessor) factory.getBean("processor");
            cp.process();
            
		} catch (Exception e) {
		    throw new BuildException(e);
            
		} finally {
            // rollback of our classloader change
            Thread.currentThread().setContextClassLoader(sysClassLoader);
        }
    }
    
    /**
     * FileSets overrides inputFiles (which are deprecated)
     * 
     * @return a String of resolved input files that can be used by the
     * SpringLoaderCommand
     */
    private String resolveInputFiles() {
        if (fileSets.size() > 0) {
            List resolvedPaths = new ArrayList();
            
            for(Iterator it = fileSets.iterator(); it.hasNext(); ) {
                FileSet fs = (FileSet) it.next();
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                File base  = ds.getBasedir();
                String[] includedFiles = ds.getIncludedFiles();
                
                for (int i = 0; i < includedFiles.length; i++) {
                    File f = new File(base, includedFiles[i]);
                    resolvedPaths.add(f.getAbsolutePath());
                }
                
            }
            
            return StringUtils.collectionToCommaDelimitedString(resolvedPaths);
            
        } else if (inputFiles != null)
            return inputFiles;
        
        throw new BuildException("No valid input files have been specified");        
    }

    /**
     * Specify a set of files to be used as the target for the BeanDoc
     * task.
     * 
     * @param fileset a set of files to add as one or more input files
     */
    public void addFileset(FileSet fileset) {
        fileSets.add(fileset);
    }

    /**
     * Overrides any System or beandoc property to provide a direct value for
     * input files for the beandoc tool.
     * 
     * @param inputFiles a String representing one or more (comma separated or
     *        wildcarded) Resources that will be used as input files.
     * @deprecated use one or more nested FileSet's instead.
     */
    public void setInputFiles(String inputFiles) {
        this.inputFiles = inputFiles;
    }
    
    /**
     * Overrides any System or beandoc property to provide a direct value for
     * output directory for the beandoc tool.
     * 
     * @param outputDir a directory (which must be writeable for the current
     *        user) where beandoc will store its output.
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
    
    /**
     * Define a title to be used in the output documentation (principally the
     * HTML)
     * 
     * @param title the title you wish to see in HTML &lt;title&gt; tags and
     *        headers.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Specify the location of the required properties file used to configure
     * the beandoc tool. The task will fail if this isn't set to the location of
     * a valid properties file.
     * 
     * @param beandocProps the location of the Properties file to be used to
     *        configure the beandoc tool
     */
    public void setBeandocProps(File beandocProps) {
        this.beandocProps = beandocProps;
    }

    /**
     * Specify a prefix for relevant properties. Properties without the prefix
     * are ignored; from matching properties the prefix is removed.
     * 
     * @param beandocPropsPrefix prefix for relevant properties
     * @see #setBeandocProps(File)
     */
    public void setBeandocPropsPrefix(String beandocPropsPrefix) {
        this.beandocPropsPrefix = beandocPropsPrefix;
    }

    /**
     * Power users may wish to completely customise beandoc setup through a
     * different context definition file. Specify the location here.
     * 
     * @param context location of the custom context file to use
     */
    public void setBeandocContext(String context) {
        beandocContext = context;
    }

}
