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

import org.springframework.beandoc.Configuration;

/**
 * Test boot strapper to be able to play with various config options
 * 
 * @author Darren Davison
 */
public class TestLoader extends BeanDocLoader {
    
        
    /* (non-Javadoc)
     * @see org.springframework.beandoc.client.DefaultBootStrapper#postProcessConfiguration(org.springframework.beandoc.Configuration)
     */
    protected void postProcessConfiguration(Configuration beanCfg) {
        
        // title
        beanCfg.setTitle("JPetStore application context (beandoc demo)");
        
        // no validation (cannot resolve DTD!)
        beanCfg.setValidateFiles(false);
        
        //beanCfg.setGraphXSize(21.5f);
        //beanCfg.setGraphYSize(11.5f);
        
        // ignore some standalone 'meta' beans
        beanCfg.addIgnoreBeans("*Configurer");
        beanCfg.addIgnoreBeans("*Resolver");
        
        // colour the remoting stuff
        beanCfg.addBeanColours("/OrderService*", "#ffcccc");
        
        //beanCfg.setRemoveDotFiles(false);
        
    }
    
    public static void main(String[] args) {
    
        if (args.length < 2) {
			BeanDocLoader.usage();
            System.exit(99);
        }
    
        String[] inputFiles = new String[args.length - 1];
        String outputDir = args[args.length - 1];

        for (int f = 0; f < args.length -1; f++)
            inputFiles[f] = args[f];
        
        BeanDocLoader boot = new TestLoader();
        boot.configureAndRun(inputFiles, outputDir);        
    }

}
